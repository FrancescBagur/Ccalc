from SimpleCV import Image

#Ee crea la instancia de la Imatge agafant la imatege

#la imatge
imatge = Image('/home/palmendr/Documentos/PerePersonal/ServidorCcalc/ServidorCcalc/CCalc/autotrace-0.31.1/render2.bmp')

#S'executa el binarize sense parametres(negre)'

imgBin = imatge.binarize()

#Se salva la imagen como resultado3.jpg

imgBin.save('/home/palmendr/Documentos/PerePersonal/CCalc/ServidorCcalc/ServidorCcalc/autotrace-0.31.1/renderfinal.bmp')