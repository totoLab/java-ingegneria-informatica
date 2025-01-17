package esercitazione2.esercizio2_2;

import java.io.*;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class ChatNode {

    private final List<Socket> clients = Collections.synchronizedList(new ArrayList<Socket>()); // TODO: removing clients only if get EXIT command
    private final int port;
    public static final int DEFAULT_SERVER_PORT = 2222;
    private ServerSocket server;

    public ChatNode(int port) {
        if (1023 < port && port <= 65536) this.port = port;
        else throw new RuntimeException("Invalid port");
        try {
            server = new ServerSocket(this.port);
        } catch (IOException e) {
            printError("Couldn't create server on port " + port, e);
        }
        loop();
    }

    private void printInfo(String message) {
        System.out.println("INFO: " + message);
    }
    private void printError(String message, Exception e) {
        System.err.println(message + "\n JVM: " + e);
    }

    private synchronized boolean checkIfConnected(InetAddress addr) {
        for (Socket client : clients) {
            if (addr.equals(client.getInetAddress())) {
                return true;
            }
        }
        return false;
    }

    private List<String> getIpsFromFile(String path) {
        List<String> ret = null;
        File f = null;
        try {
            f = new File(path);
            BufferedReader bf = new BufferedReader(new FileReader(f));
            ret = new LinkedList<>(
                    bf.lines().filter(e -> !e.startsWith("#")).toList()
            );
        } catch (Exception e) {
            printError("Couldn't read file" + f, e);
        }
        return ret;
    }

    private void loop() {

        new Thread(() -> {
            printInfo("Now accepting connections...");
            try {
                while (true) {
                    Socket client = server.accept();
                    printInfo("Connected to " + client.getInetAddress());

                    if (!checkIfConnected(client.getInetAddress())) {
                        clients.add(client);
                        switchClient();
                    }
                }
            } catch (IOException e) {
                printError("Couldn't accept client", e);
            } finally {
                try {
                    server.close();
                } catch (IOException e) {
                    printError("Couldn't gracefully shut down server.", e);
                }
            }
        }).start();

        new Thread(() -> {
            List<String> peers = getIpsFromFile("ip.txt"); // file is in root of project
            if (peers.isEmpty()) return; // disabling connecting to peers if list is empty
            try {
                Iterator<String> it = peers.iterator();
                while (it.hasNext()) {
                    TimeUnit.SECONDS.sleep(3);
                    String host = it.next();
                    InetAddress addr = Inet4Address.getByName(host);

                    if (checkIfConnected(addr)) {
                        it.remove();
                    } else {
                        Socket anotherServer = new Socket(addr, DEFAULT_SERVER_PORT);
                        clients.add(anotherServer);
                        handleClient(anotherServer);
                    }
                    if (!peers.isEmpty() && !it.hasNext()) it = peers.iterator();
                }
            } catch (InterruptedException e) {
                printError("Couldn't sleep", e);
            } catch (IOException e) {
                printError("Couldn't accept client", e);
            }
        }).start();
    }

    private void switchClient() {
        new Thread(() -> {
            Socket currentSocket = null;
            long last = System.currentTimeMillis();
            long interval = 1000 * 10; // 10 seconds
            int index = 0;
            while (!clients.isEmpty()) {
                long now = System.currentTimeMillis();
                if (now - last > interval) {
                    if (!clients.isEmpty()) {
                        currentSocket = clients.get(index % clients.size());
                        printInfo("Switching to client " + currentSocket.getInetAddress());
                        handleClient(currentSocket);
                        index++;
                    }
                }
                last = now;
            }
        }).start();
    }

    private void handleClient(Socket socket) {

        // in
        new Thread(() -> {
            printInfo("Spawning input Thread for socket " + socket.getInetAddress());
            BufferedReader in = null;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                while (true) {
                    System.out.println(socket.getLocalAddress() + ": " + in.readLine());
                }
            } catch (IOException e) {
                printError("Couldn't receive stream correctly", e);
            } finally {
                if (in != null) {
                    try { in.close(); }
                    catch (IOException e) {
                        printError("Couldn't close BufferedReader for socket " + socket.getInetAddress(), e);
                    }
                }
            }
        }).start();

        // out
        new Thread(() -> {
            printInfo("Spawning output Thread for socket " + socket);
            PrintWriter out = null;
            Scanner user = null;
            try {
                out = new PrintWriter(socket.getOutputStream(), true);
                user = new Scanner(System.in);
                while (true) {
                    String message = user.nextLine();
                    out.println(message);
                }
            } catch (IOException e) {
                printError("Couldn't send stream correctly", e);
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }).start();
    }

    public static void main(String[] args) {
        new ChatNode(DEFAULT_SERVER_PORT);
    }

}

