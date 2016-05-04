#!/usr/bin/env python
#encoding: latin1
import sys
import os
from ClassTransformadorExpSeshat import Transformador;
from ClassNotacioPolaca import NotacioPolaca;
from ClassCalculadoraPolaca import PolacCalc;


#Creo un lector de fitxers inkml i li passo la ruta del fitxer que vui llegir
#ObjInkmlReader = InkmlReader('/Ccalc/ServidorCcalc/ServidorCcalc/seshat/out'+sys.argv[1]+'.inkml')

#Obting la operació del fitxer.
try:
    infile = open('/Ccalc/ServidorCcalc/ServidorCcalc/expresions/exp'+sys.argv[1]+'.txt','r')
    operacio = infile.readline().rstrip()
    ObjTransformador = Transformador(operacio)
    operacio = ObjTransformador.transformarExpresio()
    #operacio = ObjInkmlReader.llegirINKML()
    ObjNotacioPolaca = NotacioPolaca(operacio)

    #Passo la operació a notació polaca i la fico en un string expresioPolaca
    cuaSortida = ObjNotacioPolaca.pasarExpresioAPolaca()
    expresioPolaca = ""
    for ex in cuaSortida:
        expresioPolaca += ex + " "

    print expresioPolaca

    #Un cop ting l'string a notacio polaca "expresioPolaca", es hora de resoldre la operació
    #print expresioPolaca + '------->'
    ObjPolacCalc = PolacCalc(expresioPolaca);
    resultat = ObjPolacCalc.calcularExpresio();
    operacioResultat = str(operacio)+":"+str(resultat)
except ValueError:
    operacioResultat = "null:err"
    file = open('/Ccalc/ServidorCcalc/ServidorCcalc/fitxersSortida/temp'+sys.argv[1]+'.txt', 'w+')
    file.write(operacioResultat)
    file.close()
    os.rename('/Ccalc/ServidorCcalc/ServidorCcalc/fitxersSortida/temp'+sys.argv[1]+'.txt','/Ccalc/ServidorCcalc/ServidorCcalc/fitxersSortida/'+sys.argv[1]+'.txt')
    raise ValueError("No s'ha pogut realitzar la operació")

file = open('/Ccalc/ServidorCcalc/ServidorCcalc/fitxersSortida/temp'+sys.argv[1]+'.txt', 'w+')
file.write(operacioResultat)
file.close()
os.rename('/Ccalc/ServidorCcalc/ServidorCcalc/fitxersSortida/temp'+sys.argv[1]+'.txt','/Ccalc/ServidorCcalc/ServidorCcalc/fitxersSortida/'+sys.argv[1]+'.txt')





