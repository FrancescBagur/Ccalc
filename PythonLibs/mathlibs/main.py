#!/usr/bin/env python
#encoding: latin1
import sys
import os
from ClassTransformadorExpSeshat import Transformador
from ClassNotacioPolaca import NotacioPolaca
from ClassCalculadoraPolaca import PolacCalc
sys.path.insert(1,'/root/anaconda2/bin')
#print sys.path
from ClassCalculadoraEquacions import EcuationCalc


"""funcio que retorna l'expresio matematica en format normal """
def transformadorExpres(exp):
    exp = exp.replace("s","sin")
    exp = exp.replace("!","sqrt")
    exp = exp.replace("?","log")
    exp = exp.replace("|","cos")
    exp = exp.replace("$","tan")
    exp = exp.replace("@","frac")
    return exp

def netejarExpresio(exp):
    i = 0
    trobat = False
    cleanExp = ""
    while trobat == False:
        lletra = exp[i]
        if lletra == '=':
            trobat = True
        else:
            i += 1
            cleanExp += lletra
    cleanExp = cleanExp.replace('^{','**')
    cleanExp = cleanExp.replace('}','')
    return cleanExp

#Creo un lector de fitxers inkml i li passo la ruta del fitxer que vui llegir
#ObjInkmlReader = InkmlReader('/Ccalc/ServidorCcalc/ServidorCcalc/seshat/out'+sys.argv[1]+'.inkml')

#Obting la operació del fitxer.
operacioResultat = ""
try:
    infile = open('/Ccalc/ServidorCcalc/ServidorCcalc/expresions/exp'+sys.argv[1]+'.txt','r')
    operacio = infile.readline().rstrip()
    ObjTransformador = Transformador(operacio)
    operacio = ObjTransformador.transformarExpresio()
    #operacio = ObjInkmlReader.llegirINKML()
    if "x" in operacio or "y" in operacio or  "z" in operacio:
        operacio=netejarExpresio(operacio)
        ObjEcuationCalc = EcuationCalc(operacio)
        resultat = ObjEcuationCalc.calcularEquacio()
        print resultat
    else:
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
        operacioResultat = transformadorExpres(operacioResultat)
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





