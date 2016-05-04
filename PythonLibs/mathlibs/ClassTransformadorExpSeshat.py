#!/usr/bin/env python
#encoding: latin1
class Transformador:
    """Transforma una expresio extreta de seshat en una expresio normal"""

    def __init__(self,expresio):
        """ Crea el transformador i guardo l'expresió obtinguda de seshat """
        self.exp = expresio

    def transformarExpresio(self):
        self.exp = self.exp.replace('/cdot','*')
        self.exp = self.exp.replace('\sqrt','!')
        self.exp = self.exp.replace('\sin','sin')
        self.exp = self.exp.replace(',','.')

        return self.exp

