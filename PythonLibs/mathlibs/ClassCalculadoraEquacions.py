#!/usr/bin/env python
#encoding: latin1
from sympy.solvers import solve
from sympy import Symbol
from sympy import sympify

#Una claculadora d'equacions
class EcuationCalc:

    def __init__(self,expresio):
        self.expresio=expresio;

    def calcularEquacio(self):
        if "x" in self.expresio:
            expr=sympify(self.expresio)
            for symbol in expr.atoms(Symbol):
                if str(symbol)=='x':
                    return solve(expr, symbol)
        elif "y" in self.expresio:
            expr=sympify(self.expresio)
            for symbol in expr.atoms(Symbol):
                if str(symbol)=='y':
                    return solve(expr, symbol)
        else:
            expr=sympify(self.expresio)
            for symbol in expr.atoms(Symbol):
                if str(symbol)=='z':
                    return solve(expr, symbol)

