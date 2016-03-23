from SimpleCV import Image

#Ee crea la instancia de la Imatge agafant la imatege

#la imatge
imatge = Image('/home/francesc/Development/CCalc/autotrace-0.31.1/render2.bmp')

#S'executa el binarize sense parametres(negre)'

imgBin = imatge.binarize()

#Se salva la imagen como resultado3.jpg

imgBin.save('/home/francesc/Development/CCalc/autotrace-0.31.1/renderfinal.bmp')