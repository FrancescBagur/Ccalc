<html lang="ca">
	<head><title>Ccalc</title></head>
	<?php include_once("bootstrap/bootstrap.html"); ?> <!--Incloc les capçaleres de bootstrap-->
	<link rel="stylesheet" href="css/style.css">
	<body>
		<?php $pag = "aplicacio";
		include_once("html/navBarIndex.html");?> <!-- Incloc la barra del navegador -->
		<div class="container">
			<div class="container3">
				<h2 class="title22">Pantalla inicial</h2>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/Ccalc1.png" alt="java">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">Initial Activity</h4>
				    A la pantalla inicial, hi ha dos botons que ens permetran escollir la manera d'inserir les operacions. </br>
				    El bot&oacute; Scan, ens portar&agrave; a l'Activity for Ucrop i el Write a la Drawing Activity. 
				  </div>
				</div>
				<h2 class="title22">Pantalles d'inserci&oacute; de dades</h2>
				<div class="row">
				  <div class="col-sm-6 col-md-5">
				      <div class="media">
						  <div class="media-left">
						      <img class="media-object imgwidth" src="imatges/Ccalc2.png" alt="java">
						  </div>
						  <div class="media-body">
						    <h4 class="media-heading">Activity for Ucrop</h4>
						    Abans d'arribar aqu&iacute; el m&ograve;bil t'obrir&agrave; la c&agrave;mera de fotos per fer una foto a 
						    una operaci&oacute;. Un cop feta, s'obrir&agrave; aquesta pantalla en la que es demana ajustar el rectangle 
						    a l'expressi&oacute;.
						  </div>
						</div>
				  </div>
				  <div class="col-sm-6 col-md-7">
				      	<div class="media">
						  <div class="media-left">
						      <img class="media-object imgwidth2" src="imatges/Ccacl3.png" alt="java">
						  </div>
						  <div class="media-body">
						    <h4 class="media-heading">Drawing Activity</h4>
						    Aquesta pantalla serveix per dibuixar  a m&agrave; alçada l'expressi&oacute; que es vol resoldre. Tens un bot&oacute;
						     ClearScreen que serveix per borrar si t'has equivocat, i un bot&oacute; Send per enviar un cop has acabat de dibuixar.
						  </div>
						</div>
				  	 </div>
				</div>
				<h2 class="title22">Pantalla de resultat</h2>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/Ccalc4.png" alt="java">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">Result Activity</h4>
				    &Eacute;s la pantalla on es mostr&agrave; el resultat obtingut de la petici&oacute; al servidor tant de Ucrop com de Drawing Activity.
				  </div>
				</div>
				<h2 class="title22">Diagrama Explicatiu</h2>
				<img class="imgwidth4" src="diagrames/diagramExplicatiu2.PNG" alt="java">
				<h2 class="title22">Diagrama de casos d'us</h2>
				<img class="imgwidth4" src="diagrames/useCase.PNG" alt="java">
				<h2 class="title22">Diagrama de classes de l'app mòbil</h2>
				<img class="imgwidth4" src="diagrames/clasesAndroid.PNG" alt="java">
				<h2 class="title22">Diagrama de classes del servidor</h2>
				<img class="imgwidth4" src="diagrames/ClassesServer.PNG" alt="java">
				<h2 class="title22">Diagrama de classes de les llibreries matemàtiques</h2>
				<img class="imgwidth4" src="diagrames/clasesMathLibs.PNG" alt="java">
				<h2 class="title22">Diagrama de desplegament</h2>
				<img class="imgwidth4" src="diagrames/Desplegament.PNG" alt="java">
			</div>
		</div>
		<?php include_once("html/footer.html");?> <!--Incloc el footer de la pàgina -->
	</body>
</html>