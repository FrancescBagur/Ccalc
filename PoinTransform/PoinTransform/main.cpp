#include <iostream>
#include <cstdio>
#include <fstream>
#include <cstdlib>
#include <cstring>
#include <vector>
#include <string>
#include <boost/algorithm/string.hpp>
#include <sstream> 

using namespace std;
using namespace boost;

// declaración de prototipo
int arrodonirFloat(float numf);

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
	/*const char * instruccio = "sh ../../../../autotrace-0.31.1/script.autotrace "; //Això es l'script a executar
	const char * f = file.c_str();//El fitxer que m'an passat
	char script[200];
	strcpy(script, instruccio); 
	if(f != ""){
		strcat(script, f); 
	}
	//Executo l'script!*/
  	
	/*char *my_args[4];
	strcpy( my_args[0], "autotrace" );
	strcpy( my_args[1], "-centerline" );
	strcpy( my_args[2], "render.bmp" );
	strcpy( my_args[3], NULL );*/
	char comanda[50];
	char fitxerSortida[25];
	sprintf(fitxerSortida, " > fitxerSortida%s%s",argv[1],".txt");
	sprintf(comanda,"sh script.autotrace %s %s",argv[1],fitxerSortida);
	system(comanda);
	//execv("autotrace-0.31.1/autotrace",my_args );
	//Un cop tenim la sortida de l'autotrace dins el fitxer, es hora d'analitzarlo.
	string linia;
	char fitxSort[25];
	sprintf(fitxSort,"fitxerSortida%s%s",argv[1],".txt");
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
	    
	   /* printf("%i\n",StrokesMatrix.size());
	    for(int z=0; z <StrokesMatrix.size(); z++){
	    	printf("%i\n",StrokesMatrix[z].size()/2);
	    	for(int w=0; w<StrokesMatrix[z].size(); w+=2){
	    		printf("%i %i\n", StrokesMatrix[z][w],StrokesMatrix[z][w+1]);
	    	}	
	    }
	    std::stringstream stream;  
		std::string strNum; 
 
	    ofstream fitxerEntrada ("exp3.scgink");
	    }*/
	    std::stringstream stream;  
		std::string strNum; 
 		std::string nomFitxerEntradaSeshat = "exp" + id + ".scgink";
	    ofstream fitxerEntrada (("/Ccalc/PoinTransform/PoinTransform/bin/Debug/seshat/SampleMathExps/"+nomFitxerEntradaSeshat).c_str());
  		if (fitxerEntrada.is_open()){
  			fitxerEntrada << "SCG_INK\n";
  			stream << StrokesMatrix.size();
	    	strNum = stream.str();
	    	stream.str("");
  			fitxerEntrada << strNum + "\n";
	    	for(int z=0; z <StrokesMatrix.size(); z++){
	    		stream << StrokesMatrix[z].size()/2;
	    		strNum = stream.str();
	    		stream.str("");
	    		fitxerEntrada << strNum + "\n";
	    		for(int w=0; w<StrokesMatrix[z].size(); w+=2){
	    			stream << StrokesMatrix[z][w];
	    			strNum = stream.str() + " ";
	    			stream.str("");
	    			stream << StrokesMatrix[z][w+1];
	    			strNum += stream.str();
	    			stream.str("");
	    			fitxerEntrada << strNum + "\n";
	    		}
	    	}
   	 		fitxerEntrada.close();
   	 		char comandaSeshat[60];
			sprintf(comandaSeshat,"cd seshat;script.seshat %s",argv[1]);
			system(comandaSeshat);
  		}else cout << "Unable to open file";
    	
  	}else printf("Imposible obrir el fitxer");
	
	return 0;
}

int arrodonirFloat(float numf){
	int numAux = (int) numf;
	if(numf-numAux > 0.5)numAux++;
	return numAux;
}