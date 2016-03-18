#!/usr/bin/env python
#encoding: latin1

class InkmlReader:
	"""Es un lector de fitxers inkml. """

	def __init__(self,ruta):
		""" Crea el lector i guarda la ruta del fitxer a llegir """
		self.ruta = ruta;

	def llegirINKML():
    #obro el fitxer en mode lectura 'r'
    infile = open(self.ruta, 'r');
    #Defineixo la variable operacio a la que anire concatenant les operacions que vagi trobant
    operacio = "";
    print('>>> Lectura del fichero linea a linea');
    for line in infile:
        if ' <annotation type="truth">' in line:
    	    operacio += line[27:-14];
	
    # tanco el fitxer
    infile.close();
    # retorno l'expresio obtinguda
    return operacio;