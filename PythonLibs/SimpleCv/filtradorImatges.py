#!/usr/bin/env python
#encoding: latin1
import sys
from SimpleCV import Image

#Creo la imatge a partir del bmp
imatge = Image('/Ccalc/ServidorCcalc/ServidorCcalc/imatges/'+sys.argv[1])
#La binaritzo, li trec totes les sombres i la natejo
imgBin = imatge.binarize(-1,255,37,8)
#I la inverteixo, la paso de fons negre i lletres blanques a fons blanc a lletres negres
imgInver = imgBin.invert()
#la guardo
imgInver.save('/Ccalc/ServidorCcalc/ServidorCcalc/imatges/'+ sys.argv[1])

print 1; #Retorno aquest 1 a java perque sapiga que ha acabat
