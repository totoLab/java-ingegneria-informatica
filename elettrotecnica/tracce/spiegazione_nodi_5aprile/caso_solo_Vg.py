import math
import sys
import numpy as np

def todo():
    print("Not yet implemented")
    sys.exit()

# ---------- support methods ----------
mode = "default"

def pr(v, r):
    global mode
    if mode == "n":
        return math.pow(v, 2) / r
    elif mode == "a":
        return math.pow(v, 2) * r
    else:
        print("Parameter not found. Enter a or n as parameter.")
        sys.exit()

def pg(I, V):
    return V * I

def solve(M, N):
    global mode

    for i in range(len(M)):
        for j in range(len(M[0])):
            resistenze = M[i][j]
            if mode == "n": # usa conduttanze
                M[i][j] = sum([1/R if R != 0 else 0 for R in resistenze])
            elif mode == "a": # usa resistenze
                M[i][j] = sum(resistenze)
            else:
                print("Parameter not found. Enter a or n as parameter.")
                sys.exit()

    level = det(M)
    result = []
    for j in range(len(M[0])):
        result.append(
            cramer(M, N, j) / level
        )
    return result

def det(M):
    array = np.array(M)
    return np.linalg.det(array)


def cramer(M, N, j):
    T = [row[:] for row in M]

    for i in range(len(T)):
        T[i][j] = N[i][0]

    return det(T)


# ---------- dati del problema ----------
Vg1, Vg2 = 1, 2
r1, r2, r3, r4 = 1, 1, 2, 2

# ---------- metodi ----------
def nodi():
    print("Metodo dei nodi:")

    # config
    global mode
    mode = "n"

    # potenziali noti
    eb = 0
    ec = Vg2
    # in questo caso possiamo scrivere l'intera relazione di ea a mano e usarla in ed o creare un sistema "fittizio" 2x2 da risolvere automaticamente (uso quest'ultimo perché è bello da vedere)
    # ea = (Vg2 * (1 / r2 + 1/ r3) + Vg1 * (1 / r3 + 1 / r4)) / (1 / r1 + 1 / r2 + 1 / r3 + 1 / r4)
    # ed = ea - Vg1

    # matrice di incidenza
    R = [
        [ [r1, r2], [r3, r4] ],
        [ [-1], [1] ]
    ]

    # vettore colonna termini noti
    N = [
        [ec / r2 + ec / r3],
        [-Vg1]
    ]

    # potenziali rimanenti
    ea, ed = solve(R, N)
    print(f"{ea=} {ed=}")

    # potenze
    Pr1 = pr(ea - eb, r1)
    Pr2 = pr(ea - ec, r2)
    Pr3 = pr(ec - ed, r3)
    Pr4 = pr(eb - ed, r4)
    Pr = Pr1 + Pr2 + Pr3 + Pr4
    print(f"{Pr1=} + {Pr2=} + {Pr3=} + {Pr4=} = {Pr=}")

    Ivg1 = (ea - eb) / r1 + (ea - ec) / r2
    Ivg2 = (ec - ed) / r3 + (ec - ea) / r2
    print(f"{Ivg1=} {Ivg2=}")

    Pvg1 = pg(Ivg1, Vg1)
    Pvg2 = pg(Ivg2, Vg2)
    Pg = Pvg1 + Pvg2
    print(f"{Pvg1=} + {Pvg2=} = {Pg=}")

    print(f"Error: {abs(Pr - Pg) / Pr :.2f}%")

def anelli():
    print("Metodo degli anelli:")
    todo()
    # config
    global mode
    mode = "a"

    # correnti note
    i1 = -Ig

    # matrice di incidenza
    M = [
        [ [r2], [0] ],
        [ [0], [r3, r4] ]
    ]
    # vettore colonna termini noti
    N = [
        [Vg + i1 * r2],
        [-Vg]
    ]

    # correnti rimanenti
    i2, i3 = solve(M, N)

    print(f"{i2=}, {i3=}")
    Pr1 = pr(i1, r1)
    Pr2 = pr(i1 - i2, r2)
    Pr3 = pr(i3, r3)
    Pr4 = pr(i3, r4)
    Pr = Pr1 + Pr2 + Pr3 + Pr4
    print(f"{Pr1=} + {Pr2=} + {Pr3=} + {Pr4=} = {Pr=}")

    Vig = i2 * r2 + - i1 * (r1 + r2)
    print(f"{Vig=}")

    Ivg = i2 - i3
    Pvg = pg(Ivg, Vg)
    Pig = pg(Ig, Vig)
    Pg = Pvg + Pig
    print(f"{Pig=} + {Pvg=} = {Pg=}")

    print(f"Error: {abs(Pr - Pg) / Pr :.2f}%")

def millman():
    print("Millman:")
    todo()
    # config
    # R in serie con Vg
    Rs = [r3, r4]

    # R in parallelo con Ig !!! IGNORARLE !!!
    Rp = []

    # R sul ramo singolo
    Rc = []

    # Vg
    V = [v1, v2]

    # Ig
    I = [i1, i2]

    assert(len(Rs) == len(V))
    
    correnti_fake = [ V[i]/Rs[i] for i in range(len(V)) ]
    reciproci_rs =  [ 1/Rs[i] for i in range(len(Rs)) ]
    reciproci_rc =  [ 1/Rc[i] for i in range(len(Rc)) ]

    totale_correnti    = sum(correnti_fake) + sum(I)
    totale_conduttanze = sum(reciproci_rs) + sum(reciproci_rc) 

    assert(totale_conduttanze != 0)
    
    VM = totale_correnti / totale_conduttanze
    print(f"{VM=}")

# ---------- entrypoint ----------
if __name__ == "__main__":
    args = sys.argv
    assert(len(args) > 1)
    index = args[1]
    assert(index.isnumeric())
    index = int(index)

    if index == 0:
        nodi()
    elif index == 1:
        anelli()
    elif index == 2:
        millman()
    else:
        print("No functions with that index.")
    
