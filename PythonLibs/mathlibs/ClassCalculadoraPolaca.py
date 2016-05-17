#!/usr/bin/env python
#encoding: latin1
import math
from ClassPila import Pila

class PolacCalc:

    def __init__(self,expresio):
        self.expresio=expresio;

    def calculadoraPolaca(self,elements):
        """ Dada una expresio d'elements que representen els components d'una
            expresio en notació polaca inversa, evalua aquesta expresio.
            Si l'expresio esta mal formada, llença un ValueError. """

        p = Pila()
        for element in elements:
           # print "DEBUG:", element
            # L'intentem convertir a numero
            try:
                nombre = float(element)
                p.apilar(nombre)
                #print "DEBUG: apila ", nombre
            # Si no el podem convertir en numero, hauria de ser un operador
            except ValueError:
                # Si no es un operador valid, llença un ValueError
                if element not in "+-*/ %!?s^|$@" or len(element) != 1:
                    raise ValueError("Operador invalid")
                # Si es un operador valid, intenta desapilar i operar
                try:
                    if element in "!?s|$":
                        a1 = p.desapilar()
                    else:
                        a1 = p.desapilar()
                       # print "DEBUG: desapila ",a1
                        a2 = p.desapilar()
                   # print "DEBUG: desapila ",a2
                # Si hi han problemes al desapilar
                except ValueError:
                    print "DEBUG: error pila falten operants"
                    raise ValueError("Falten operants")

                if element == "+":
                    resultat = a2 + a1
                elif element == "-":
                    resultat = a2 - a1
                elif element == "*":
                    resultat = a2 * a1
                elif element == "/":
                    resultat = a2 / a1
                elif element == " %":
                    resultat = a2 % a1
                elif element == "^":
                    resultat =pow(a2,a1)
                elif element == "!":
                    resultat = math.sqrt(a1)
                elif element == "?":
                    resultat = math.log(a1)
                elif element =="s":
                    resultat = math.sin(a1)
                elif element == '|':
                    resultat = math.cos(a1)
                elif element == '$':
                    resultat = math.tan(a1)
                elif element == "@":
                    resultat = a2 / a1
                p.apilar(resultat)
        # Al final el resultat té que ser l'unic de la pila
        res = p.desapilar()
        if p.esta_buida():
            return res
        else:
            print p.items
            print "DEBUG: error a la pila, sobren operants"
            raise ValueError("Sobren operants")

    def calcularExpresio(self):
        elements = self.expresio.split()
        return self.calculadoraPolaca(elements)
