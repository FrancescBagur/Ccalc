import string

precedencia={'+':1,'-':1,'*':2,'/':2,'^':3}
asociativo={'+':'i','-':'i','*':'i','/':'i','^':'d'}
operador='+-*/^'
papertura='([{'
pcierre=')]}'
sep=',;'
func=['sqrt','log','ln','sin','cos','tg','cotg']
expresion_infixa=''
stack=[]
cola_salida=[]
lista_tipo_token=[]

def cola(token):
    #escribe el token en la lista de salida
    cola_salida.append(token)

def push(token):
    #mete el token en el stack
    stack.insert(0,token)
    return
 
def pop():
    #saca el primer elemento del stack
    return stack.pop(0)

def vacia_stack():
    #al final vacia todo el stack en la cola
    while len(stack)>0:
        cola(pop())

def tipo_char(i):
    #comprueba el tipo del caracter encontrado en la lista
    #de la expresion de entrada, para agruparlos 
    if string.digits.find(i)!=-1:
        #es una cifra
        tipo='num'
    elif operador.find(i)!=-1:
        #es un operador
        tipo='op'
    elif papertura.find(i)!=-1 or pcierre.find(i)!=-1:
        #es un parentesis
        tipo='par'
    elif sep.find(i)!=-1:
        #es un separador de argumento de funcion
        tipo='sep'
    else:
        #es una letra
        tipo='char'
    return tipo

def infixa_a_tokens():
    lista_tokens=[] 
    token=''
    tipoa=tipo_char(expresion_infixa[0])

    for char in expresion_infixa:
        tipo=tipo_char(char)
        if tipo=='par' or tipo=='sep' or tipo=='op':
            #primero guardamos el numero, o var o funcion
            # que pudieramos estar acumulando
            if tipoa=='char' or tipoa=='num':
                lista_tokens.append(token)
                lista_tipo_token.append(tipoa)     
                token=''
           
            lista_tokens.append(char)
            lista_tipo_token.append(tipo)
            tipoa=tipo
        else:
            #es numero, o variable, o funcion
            #y si antes tambien lo era, concatenamos los caracteres
            token=token+char
            tipoa=tipo
 
    if tipoa=='num' or tipoa=='char':  
        lista_tokens.append(token) 
        lista_tipo_token.append(tipo) 
    return lista_tokens


# MAIN

expresion_infixa=raw_input("Introduzca funcion : ")
print expresion_infixa

#buscamos los tokens que hay en infixa, y los metemos en una lista
lista=infixa_a_tokens()
print lista

for i in range(len(lista)):
    tipo=lista_tipo_token[i]
    token=lista[i]

    if tipo=='num':
        #a la cola salida
        cola(token)
  
    elif tipo=='sep':
        #separador de parametros de funcion
        while stack[0]!='(':
            cola(pop())
  
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
        if len(stack)>0:
            while (len(stack)>0 and
                  ((operador.find(stack[0])!=-1 and
                  asociativo.get(token)=='i' and
                  precedencia.get(token)<=precedencia.get(stack[0])) or
                  (asociativo.get(token)=='d' and 
                  precedencia.get(token)<precedencia.get(stack[0])))):
                cola(pop())
        push(token)
vacia_stack()
print cola_salida