#!/usr/bin/env python
#encoding: latin1
class Transformador:
    """Transforma una expresio extreta de seshat en una expresio normal"""

    def __init__(self,expresio):
        """ Crea el transformador i guardo l'expresió obtinguda de seshat """
        self.exp = expresio

    def transformarExpresio(self):
        self.exp = self.exp.replace('.','*')
        self.exp = self.exp.replace('- \cdots ','+')
        self.exp = self.exp.replace('\cdots - ','+') #mato tots els cdots sense sentit que sol retornar a vegades
        self.exp = self.exp.replace('\cdots ','') #mato tots els cdots sense sentit que sol retornar a vegades
        self.exp = self.exp.replace('\cdot','*')
        self.exp = self.exp.replace('\sqrt','!')
        self.exp = self.exp.replace('\div','/')
        self.exp = self.exp.replace('\log','?')
        self.exp = self.exp.replace('\sin','s')
        self.exp = self.exp.replace(',','.')
        self.exp = self.exp.replace('\cos','|')
        self.exp = self.exp.replace('\\tg','$')
        self.exp = self.exp.replace('\\frac','@')
        self.exp = self.exp.replace('\\pm','+')
        self.exp = self.exp.replace('\pm','+')
        self.exp = self.exp.replace('\pi','3.14159265358979323846264338')
        self.exp = self.exp.replace(' ', '')
        
        if "^{-" in (self.exp):
            self.exp = self.exp.replace('^{-','-')
            self.exp = self.exp.replace('}','')

        return self.exp

