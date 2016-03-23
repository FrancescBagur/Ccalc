import string

class NotacioPolaca:

    def __init__(self, expresio):
        self.expresio = expresio;
        self.precedencia={'+':1,'-':1,'*':2,'/':2,'^':3}
        self.asociativo={'+':'i','-':'i','*':'i','/':'i','^':'d'}
        self.operador='+-*/^'
        self.papertura='([{'
        self.pcierre=')]}'
        self.sep=',;'
        self.func=['sqrt','log','ln','sin','cos','tg','cotg']
        self.expresion_infixa=''
        self.stack=[]
        self.cola_salida=[]
        self.lista_tipo_token=[]

    def cola(self, token):
        #escribe el token en la lista de salida
        self.cola_salida.append(token)

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
        #comprueba el tipo del caracter encontrado en la lista
        #de la expresion de entrada, para agruparlos 
        if string.digits.find(i)!=-1:
            #es una cifra
            tipo='num'
        elif self.operador.find(i)!=-1:
            #es un operador
            tipo='op'
        elif self.papertura.find(i)!=-1 or self.pcierre.find(i)!=-1:
            #es un parentesis
            tipo='par'
        elif self.sep.find(i)!=-1:
            #es un separador de argumento de funcion
            tipo='sep'
        else:
            #es una letra
            tipo='char'
        return tipo

    def infixa_a_tokens(self):
        lista_tokens=[] 
        token=''
        tipo=self.tipo_char(self.expresion_infixa[0])

        for char in self.expresion_infixa:
            tipo=self.tipo_char(char)
            if tipo=='par' or tipo=='sep' or tipo=='op':
                #primero guardamos el numero, o var o funcion
                # que pudieramos estar acumulando
                if tipoa=='char' or tipoa=='num':
                    lista_tokens.append(token)
                    self.lista_tipo_token.append(tipoa)     
                    token=''
               
                lista_tokens.append(char)
                self.lista_tipo_token.append(tipo)
                tipoa=tipo
            else:
                #es numero, o variable, o funcion
                #y si antes tambien lo era, concatenamos los caracteres
                token=token+char
                tipoa=tipo
     
        if tipoa=='num' or tipoa=='char':  
            lista_tokens.append(token) 
            self.lista_tipo_token.append(tipo) 
        return lista_tokens

    def pasarExpresioAPolaca(self):
        self.expresion_infixa=self.expresio
        print self.expresion_infixa

        #buscamos los tokens que hay en infixa, y los metemos en una lista
        lista=self.infixa_a_tokens()
        print lista

        for i in range(len(lista)):
            tipo=self.lista_tipo_token[i]
            token=lista[i]

            if tipo=='num':
                #a la cola salida
                self.cola(token)
          
            elif tipo=='sep':
                #separador de parametros de funcion
                while stack[0]!='(':
                    self.cola(pop())
          
            elif tipo=='par':
                #ver si es apertura parent. o cierre
                if papertura.find(token)!=-1:
                  #es apertura
                  push(token)
                else:
                    #es cierre
                    comp=papertura[pcierre.find(token)]
                    while stack[0]<>comp:
                        cola(pop())
                    pop()#saca el parentesis y no lo mete en la cola
                    if len(stack)>0:
                        if func.count(stack[0])!=0:
                           #metemos la funcion en la cola
                            cola(pop())

            elif tipo=='char':
                #ver si es una funcion
                if func.count(token)!=0:
                    #es una funcion
                    push(token)
                else:
                    #es una variable, la consideramos como un numero
                    cola(token)

            elif tipo=='op':
                if len(self.stack)>0:
                    while (len(self.stack)>0 and
                          ((self.operador.find(self.stack[0])!=-1 and
                          self.asociativo.get(token)=='i' and
                          self.precedencia.get(token)<=self.precedencia.get(stack[0])) or
                          (self.asociativo.get(token)=='d' and 
                          self.precedencia.get(token)<self.precedencia.get(stack[0])))):
                        self.cola(pop())
                self.push(token)
        self.vacia_stack()
        print self.cola_salida
        return self.cola_salida