#!/usr/bin/env python
#encoding: latin1
from SimpleCV import Image

#Creo la imatge a partir del bmp
imatge = Image('/Ccalc/PoinTransform/PoinTransform/bin/Debug/autotrace-0.31.1/render.bmp')
#La binaritzo, li trec totes les sombres i la natejo
imgBin = imatge.binarize(-1,255,19,7)
#I la inverteixo, la paso de fons negre i lletres blanques a fons blanc a lletres negres
imgInver = imgBin.invert()
#la guardo
imgInver.save('/Ccalc/PoinTransform/PoinTransform/bin/Debug/autotrace-0.31.1/render.bmp')

print 1; #Retorno aquest 1 a java perque sapiga que ha acabat
