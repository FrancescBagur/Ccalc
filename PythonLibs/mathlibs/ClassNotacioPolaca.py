 #!/usr/bin/env python
#encoding: latin1
import string

"""Aquesta classe  ens passa una expresió a notació polaca inversa """
class NotacioPolaca:

    def __init__(self, expresio):
        self.expresio = expresio;
        self.precedencia={'+':1,'-':1,'*':2,'/':2,'^':3}
        self.associatiu={'+':'i','-':'i','*':'i','/':'i','^':'d'}
        self.operador='+-*/^'
        self.papertura='([{'
        self.pcierre=')]}'
        self.sep=',;'
        self.func=['sqrt','log','ln','sin','cos','tg','cotg']
        self.expresion_infixa=''
        self.stack=[]
        self.cuaSortida=[]
        self.lista_tipo_token=[]

    def cola(self, token):
        #escribe el token en la lista de salida
        self.cuaSortida.append(token)

    def push(self, token):
        #mete el token en el stack
        self.stack.insert(0,token)
        return

    def pop(self):
        #saca el primer elemento del stack
        return self.stack.pop(0)

    def vacia_stack(self):
        #al final vacia todo el stack en la cola
        while len(self.stack)>0:
            self.cola(self.pop())

    def tipo_char(self, i):
        #comprova el tipus de caracter trobat a  la llista
        #de la expresio d'entrada per agrupar-los
        if string.digits.find(i)!=-1:
            #es una xifra
            tipo='num'
        elif self.operador.find(i)!=-1:
            #es un operador
            tipo='op'
        elif self.papertura.find(i)!=-1 or self.pcierre.find(i)!=-1:
            #es un parentesis
            tipo='par'
        elif self.sep.find(i)!=-1:
            #es un separador de arguments de funció
            tipo='sep'
        else:
            #es una lletra
            tipo='char'
        return tipo

    def infixa_a_tokens(self):
        lista_tokens=[]
        token=''
        tipoa=self.tipo_char(self.expresion_infixa[0])

        for char in self.expresion_infixa:
            tipo=self.tipo_char(char)
            if tipo=='par' or tipo=='sep' or tipo=='op':
                #primer guerdem el numero, o var o funcio
                # que puguessim estar acumulant
                if tipoa=='char' or tipoa=='num':
                    lista_tokens.append(token)
                    self.lista_tipo_token.append(tipoa)
                    token=''

                lista_tokens.append(char)
                self.lista_tipo_token.append(tipo)
                tipoa=tipo
            else:
                #es numero, o variable, o funcio
                #i si antes també ho era, concatenarem els caracters
                token=token+char
                tipoa=tipo

        if tipoa=='num' or tipoa=='char':
            lista_tokens.append(token)
            self.lista_tipo_token.append(tipo)
        return lista_tokens

    def pasarExpresioAPolaca(self):
        self.expresion_infixa=self.expresio
        #print self.expresion_infixa

        #cercam els tockens que hi ha en l'expresio infixa i els ficam en una llista
        lista=self.infixa_a_tokens()
       # print lista

        for i in range(len(lista)):
            tipo=self.lista_tipo_token[i]
            token=lista[i]

            if tipo=='num':
                #a la cola salida
                self.cola(token)

            elif tipo=='sep':
                #separador de parametres de la funcio
                while self.stack[0]!='(':
                    self.cola(self.pop())

            elif tipo=='par':
                #veure si es una obertura o un tancament
                if self.papertura.find(token)!=-1:
                  #es obertura
                  self.push(token)
                else:
                    #es tancament
                    comp=self.papertura[self.pcierre.find(token)]
                    while self.stack[0]<>comp:
                        self.cola(self.pop())
                    self.pop()#treu el parèntesi i no el fica en cua
                    if len(self.stack)>0:
                        if self.func.count(self.stack[0])!=0:
                           #fiquem la funció en la cua
                            self.cola(self.pop())

            elif tipo=='char':
                #veure si es una funcio
                if self.func.count(token)!=0:
                    #es una funcio
                    self.push(token)
                else:
                    #es una variable, la consideram com un numero
                    self.cola(token)

            elif tipo=='op':
                if len(self.stack)>0:
                    while (len(self.stack)>0 and
                          ((self.operador.find(self.stack[0])!=-1 and
                          self.associatiu.get(token)=='i' and
                          self.precedencia.get(token)<=self.precedencia.get(self.stack[0])) or
                          (self.associatiu.get(token)=='d' and
                          self.precedencia.get(token)<self.precedencia.get(self.stack[0])))):
                        self.cola(self.pop())
                self.push(token)
        self.vacia_stack()
        #print self.cuaSortida
        return self.cuaSortida
