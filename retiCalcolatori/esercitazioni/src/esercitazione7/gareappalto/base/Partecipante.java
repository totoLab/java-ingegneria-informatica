package esercitazione7.gareappalto.base;

import utils.Logging;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static utils.Logging.print;

public class Partecipante {



    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {

            int finalI = i;
            new Thread(() -> {
                try {
                    Random rand = new Random();
                    MulticastSocket multicastSocket = new MulticastSocket(3000);
                    InetAddress group = InetAddress.getByName("230.0.0.1");
                    multicastSocket.joinGroup(group);
                    byte[] buf = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(buf, buf.length);
                    boolean ok = false;
                    Integer importoMassimo = -1;
                    while(!ok) {
                        multicastSocket.receive(packet);
                        String content = new String(packet.getData()).trim();
                        String[] parts = content.split("importoMassimo=");
                        if (parts.length == 2) {
                            String number = parts[1].replace("}", "").trim();
                            try {
                                importoMassimo = Integer.parseInt(number);
                                ok = true;
                            } catch (NumberFormatException e) {
                                System.out.println("Couldn't parse importo: " + number);
                            }
                        }
                    }
                    multicastSocket.leaveGroup(group);

                    // ---------- //

                    Socket server = new Socket(InetAddress.getLocalHost(), 4000);
                    ObjectOutputStream oos = new ObjectOutputStream(server.getOutputStream());

                    int importo = rand.nextInt(0, 5000);
                    Offerta offerta = new Offerta(finalI, importo);

                    print(Logging.Type.INFO,"Sending offer: " + offerta, null, Thread.currentThread(), null);
                    oos.writeObject(offerta);
                    oos.flush();

                    server.close();
                    TimeUnit.SECONDS.sleep(5);

                    // ----- //

                    multicastSocket.joinGroup(group);
                    buf = new byte[1024];
                    packet = new DatagramPacket(buf, buf.length);
                    multicastSocket.receive(packet);
                    String response = new String(packet.getData()).trim();
                    print(Logging.Type.INFO,response, null, Thread.currentThread(), null);

                    multicastSocket.leaveGroup(group);

                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }
    }
}