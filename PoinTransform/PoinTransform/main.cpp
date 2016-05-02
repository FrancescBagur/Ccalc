#include <iostream>
#include <cstdio>
#include <fstream>
#include <cstdlib>
#include <cstring>
#include <vector>
#include <string>
#include <boost/algorithm/string.hpp>
#include <sstream> 
#include <errno.h>

using namespace std;
using namespace boost;

// declaración de prototipo
int arrodonirFloat(float numf);
int existsFile(char* filename); 

int main (int argc, char *argv[]){
	vector< vector<int> > StrokesMatrix;
	vector <int> numInt;
	//Miro que m'hagin pasat un argument que es la id de la transaccio
	std::string id;
	if(argc!=2) {
		//Si no l'han passat faig el print i surto del programa.
		printf("Falta el parametre id\n"); 
		id="";
		//exit(1);
	}else{
		id=argv[1];
	}
	//Si l'han passat executo l'autotrace amb el parametre imatge que m'han passat i envio el resultat a un fitxer de sortida
	char comanda[50];
	char fitxerSortida[25];
	char fitxSort[25];
	sprintf(fitxerSortida,"%sfitxerSortida%s",argv[1],".txt");
	sprintf(fitxSort,"fitxerSortida%s%s",argv[1],".txt");
	if(existsFile(fitxSort)==1){
		//Si ja existeix un fitxer anterior, l'elimino per poder seguir amb el proces
		if(remove(fitxSort) != 0)
    		printf("Error a l'eliminar arxiu antic");
  		else
    		printf("S'ha eliminat un fitxer antic");
	}
	
	//Executo l'escript de l'autotrace
	sprintf(comanda,"script.autotrace %s > %s; mv -i %s %s;",argv[1],fitxerSortida,fitxerSortida,fitxSort);
	system(comanda);
	//Ara he d'esperar a que l'autotrace acabi, i ho faig mirant si ja existeix el fitxer de sortida amb 
	//el nom canviat per l'script
	bool seguir = false;
	while(!seguir){
		printf("Soc al bucle");
		if(existsFile(fitxSort)==1){
			//El fitxer ja existeix, autotrace ha acabat i per tant podem seguir
			seguir = true;
		}
	}
	printf("Autotrace ha acabat, pasem a anàlizi del fitxer.");
	//Un cop tenim la sortida de l'autotrace dins el fitxer, es hora d'analitzarlo.
	string linia;
	
	
  	ifstream fitxer(fitxSort);
  	if(fitxer.is_open()){
  		printf("S'ha obert el fitxer");
  		//Si he pogut obrir el fitxer el vaig analitzant linia a linia.
  		int i=0,j=0,t=0;
	    while (getline (fitxer,linia)){
	    	if(t>=23){
	    		if ((linia.find("K") == std::string::npos) && (linia.find("k") == std::string::npos) && (linia.find("U") == std::string::npos) && (linia.find("Trailer") == std::string::npos) && (linia.find("u") == std::string::npos) && (linia.find("EOF") == std::string::npos)){
	    			//Ignoro les linies que no son de punts.
	    			if(linia.find("S") == std::string::npos){
	    				//Si no hi ha una S significa que son punts
	    				vector <string> nombres;
	    				printf("linia:\n");
	    				printf(linia.c_str());
	    				printf("\n");
	    				split(nombres, linia, is_any_of("   "));
	    				for(int k=0; k< nombres.size(); k++){
	    					if(nombres[k] != "m" && nombres[k] != "l" && nombres[k] != "c"){
	    						printf(nombres[k].c_str());
	    						
	    						//si no es cap lletra es que es un numero, el passo a float per no perdre els desimals
	    						if(!nombres[k].compare("") == 0){
	    							printf("-");
		    						float numFloat = 0;
		    						sscanf(nombres[k].c_str(), "%f", &numFloat); //Passa la cadena a un float
		    						numInt.push_back(arrodonirFloat(numFloat)); //El passo a integer
		    						printf("%i->", (int)numFloat);
		    					}
	    					}
	    				}

	    				printf("\n");
	    			}else{
	    				//significa que comença una nova stroke
	    				StrokesMatrix.push_back(numInt);
	    				numInt.clear();
	    				i++;
	    			}
				}
	    	}	
	    	t++;
	    }
	    fitxer.close();
	    std::stringstream stream;  
		std::string strNum; 
 		std::string nomFitxerEntradaSeshat = "exp" + id + ".scgink";
	    ofstream fitxerEntrada (("seshat/SampleMathExps/"+nomFitxerEntradaSeshat).c_str());
  		if (fitxerEntrada.is_open()){
  			//fitxerEntrada << "SCG_INK\n";
  			fitxerEntrada << "[";
  			//stream << StrokesMatrix.size();
	    	//strNum = stream.str();
	    	stream.str("");
  			//fitxerEntrada << strNum + "\n";
	    	for(int z=0; z <StrokesMatrix.size(); z++){
	    		//stream << StrokesMatrix[z].size()/2;
	    		fitxerEntrada << "[";
	    		//strNum = stream.str();
	    		stream.str("");
	    		//fitxerEntrada << strNum + "\n";
	    		for(int w=0; w<StrokesMatrix[z].size(); w+=2){
	    			fitxerEntrada << "[";
	    			stream << StrokesMatrix[z][w];
	    			strNum = stream.str() + ",";
	    			stream.str("");
	    			stream << StrokesMatrix[z][w+1];
	    			strNum += stream.str();
	    			stream.str("");
	    			if(w<StrokesMatrix[z].size()-2)
	    				fitxerEntrada << strNum + "],";
	    			else
	    				fitxerEntrada << strNum + "]";
	    		}
	    		if (z < StrokesMatrix.size() - 1) 
	    			fitxerEntrada << "],";
	    		else
	    			fitxerEntrada << "]";
	    	}
	    	fitxerEntrada << "]";
   	 		fitxerEntrada.close();
   	 		char comandaSeshat[60];
   	 		char fitxer1[30];
			char fitxer2[30];
			sprintf(fitxer1,"%s%s",argv[1],"out.inkml");
			sprintf(fitxer2,"%s%s%s","out",argv[1],".inkml");
   	 		//Si existeix el fitxer de sortida antic de seshat el borrem
   	 		if(existsFile(fitxer1)==1){
			//Si ja existeix un fitxer anterior, l'elimino per poder seguir amb el proces
				if(remove(fitxer1) != 0)
		    		printf("Error a l'eliminar arxiu antic");
		  		else
		    		printf("S'ha eliminat un fitxer antic");
			}
   	 		printf("Genero el fitxer de sortida per enviar al servidor de seshat");
			sprintf(comandaSeshat,"cd seshat;mv -i %s %s;",argv[1],fitxer1,fitxer2);
			system(comandaSeshat);
  		}else cout << "Unable to open file";
    	
  	}else printf("Imposible obrir el fitxer");
}

int arrodonirFloat(float numf){
	int numAux = (int) numf;
	if(numf-numAux > 0.5)numAux++;
	return numAux;
}

int existsFile(char* filename) {
	FILE* f = NULL;
	f = fopen(filename,"r");
	if (f == NULL && errno == ENOENT) 
		return 0;
	else {
		fclose(f);
		return 1;
	}
}