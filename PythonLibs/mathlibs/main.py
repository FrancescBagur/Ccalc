#!/usr/bin/env python
#encoding: latin1

from ClassInkmlReader import InkmlReader;
from ClassCalculadoraPolaca import PolacCalc;

def main():
    #Creo un lector de fitxers inkml i li passo la ruta del fitxer que vui llegir
    ObjInkmlReader = InkmlReader('../PoinTransform/PoinTransform/bin/Debug/seshat/out.inkml');
    #Obting la operació del fitxer.
    operacio = ObjInkmlReader.llegirINKML();

if __name__ == "__main__":
    main()

