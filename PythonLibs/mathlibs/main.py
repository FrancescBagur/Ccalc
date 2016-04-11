#!/usr/bin/env python
#encoding: latin1

from ClassInkmlReader import InkmlReader;
from ClassNotacioPolaca import NotacioPolaca;
from ClassCalculadoraPolaca import PolacCalc;

def main():

    #Creo un lector de fitxers inkml i li passo la ruta del fitxer que vui llegir
    ObjInkmlReader = InkmlReader('../../PoinTransform/PoinTransform/bin/Debug/seshat/out.inkml')

    #Obting la operació del fitxer.
    operacio = ObjInkmlReader.llegirINKML()
    ObjNotacioPolaca = NotacioPolaca(operacio)

    #Passo la operació a notació polaca i la fico en un string expresioPolaca
    cuaSortida = ObjNotacioPolaca.pasarExpresioAPolaca()
    expresioPolaca = ""
    for ex in cuaSortida:
        expresioPolaca += ex + " "

    #Un cop ting l'string a notacio polaca "expresioPolaca", es hora de resoldre la operació
    print expresioPolaca + '------->'
    ObjPolacCalc = PolacCalc(expresioPolaca);
    ObjPolacCalc.calcularExpresio();


if __name__ == "__main__":
    main()

