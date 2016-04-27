#!/usr/bin/env python
#encoding: latin1
import sys
from ClassInkmlReader import InkmlReader;
from ClassNotacioPolaca import NotacioPolaca;
from ClassCalculadoraPolaca import PolacCalc;


#Creo un lector de fitxers inkml i li passo la ruta del fitxer que vui llegir
ObjInkmlReader = InkmlReader('/Ccalc/ServidorCcalc/ServidorCcalc/seshat/out'+sys.argv[1]+'.inkml')

#Obting la operació del fitxer.
operacio = ObjInkmlReader.llegirINKML()
ObjNotacioPolaca = NotacioPolaca(operacio)

#Passo la operació a notació polaca i la fico en un string expresioPolaca
cuaSortida = ObjNotacioPolaca.pasarExpresioAPolaca()
expresioPolaca = ""
for ex in cuaSortida:
    expresioPolaca += ex + " "

#Un cop ting l'string a notacio polaca "expresioPolaca", es hora de resoldre la operació
#print expresioPolaca + '------->'
ObjPolacCalc = PolacCalc(expresioPolaca);
resultat = ObjPolacCalc.calcularExpresio();
operacioResultat = operacio+":"+resultat
file = open('/Ccalc/ServidorCcalc/ServidorCcalc/fitxersSortida/'+sys.argv[1]+'.txt', 'w+')
file.write(operacioResultat)
file.close()




