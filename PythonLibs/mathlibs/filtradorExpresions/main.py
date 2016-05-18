#!/usr/bin/env python
#encoding: latin1
import sys

def transformarExpresio(exp):
    exp = exp.replace('- \cdots ','+')
    exp = exp.replace('\cdots - ','+') #mato tots els cdots sense sentit que sol retornar a vegades
    exp = exp.replace('\cdots ','') #mato tots els cdots sense sentit que sol retornar a vegades
    exp = exp.replace(',','.')
    exp = exp.replace('\\pm','+')
    exp =exp.replace('\pm','+')
    if "^{-" in (exp):
        exp = exp.replace('^{-','-')
        exp = exp.replace('}','')

    return exp

print transformarExpresio(sys.argv[1])

