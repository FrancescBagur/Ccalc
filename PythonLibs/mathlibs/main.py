
def llegirINKML():
    #obro el fitxer en mode lectura 'r'
    infile = open('../PoinTransform/PoinTransform/bin/Debug/seshat/out.inkml', 'r');
    #Defineixo la variable operacio a la que anire concatenant les operacions que vagi trobant
    operacio = "";
    print('>>> Lectura del fichero linea a linea');
    for line in infile:
        if ' <annotation type="truth">' in line:
    	    operacio += line[27:-14];
	
    # tanco el fitxer
    print(operacio);
    infile.close();

def main():
    llegirINKML();

if __name__ == "__main__":
    main()

