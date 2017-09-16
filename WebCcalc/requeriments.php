<html lang="ca">
	<head><title>Ccalc</title></head>
	<?php include_once("bootstrap/bootstrap.html"); ?> <!--Incloc les capçaleres de bootstrap-->
	<link rel="stylesheet" href="css/style.css">
	<body>
		<?php $pag = "requeriments"; 
		include_once("html/navBarIndex.html");?> <!-- Incloc la barra del navegador -->
		<div class="container">
			<div class="container2">
				<h1 class="title22">Requeriments d'usuari</h1>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/samsung.jpg" alt="java">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">Telèfon</h4>
				    Android 4.0.x - Ice Cream Sandwich.
				  </div>
				</div>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/internet.png" alt="java">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">Internet</h4>
				    &Eacute;s imprescindible una connexi&oacute; a internet.
				  </div>
				</div>
				<h1 class="title22">Requeriments del desenvolupador</h1>
				<h3 class="title22">Sistema Operatiu</h3>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/os.jpg" alt="java">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">SO</h4>
				    Un sistema operatiu que suporti Java 8, C++ i Python.
				  </div>
				</div>
				<h3 class="title22">Llenguatges de programaci&oacute;</h3>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/java.png" alt="java">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">Java</h4>
				    Hem utilitzat java per programar l'aplicaci&oacute; Android del dispositiu m&ograve;bil de forma nativa i per desenvolupar el servidor. La comunicaci&oacute; entre aquests dos s'ha fet mitjançant sockets java.
				  </div>
				</div>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/python.jpeg" alt="java">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">Python</h4>
				    Hem utilitzat python per programar les llibreries matem&agrave;tiques ja que aquest ens donava molt alt nivell i a m&eacute;s com esta basat amb c++, si en un futur el volem integrar dins el dispositiu m&ograve;bil, el Java NDK ens ho permet.
				  </div>
				</div>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/c++.png" alt="java">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">C++</h4>
				    C++ s'ha utilitzat per programar coses a més baix nivell, com per exemple un transformador de punts extrets de autotrace per passar-los al format del seshat. Aquest llenguatge també es pot integrar dintre dels dispositius m&ograve;bils Android gr&agrave;cies al java NDK.
				  </div>
				</div>
				<h3 class="title22">Llenguatges de programaci&oacute; Web</h3>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/html5.png" alt="java">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">Html 5</h4>
				    Per programar la pàgina web hem utilitzat Html 5.
				  </div>
				</div>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/css.svg" alt="java">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">Css 3</h4>
				    Per donar estils a la web hem utilitzat Css 3.
				  </div>
				</div>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/bootstrap.png" alt="java">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">Bootstrap</h4>
				    Per donar estils també hem utlilitzat Bootstrap que és un framework css per fer webs responsive.
				  </div>
				</div>
				<h3 class="title22">Llibreries</h3>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/seshat.jpg" alt="java">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">Seshat</h4>
				    Llibreria creada per el senyor Francisco &Aacute;lvaro com a treball del seu doctorat. Sense aquesta llibreria 
				    el nostre projecte no hagu&eacute;s sigut possible degut a l'elevat cost de desenvolupament que hagu&eacute;s tingut
				    el reconeixement d'expressions matem&agrave;tiques. </br>
				    <a>https://github.com/falvaro/seshat/blob/master/README.md</a>
				  </div>
				</div>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/autotrace.jpg" alt="java">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">Autotrace</h4>
				    Autotrace &eacute;s un software que serveix per vectoritzar firmes digitals. Nosaltres vam readaptar el software
				    aprofitant que aquest extreia les traçades i els punts de una imatge i el vam fer servir per obtenir les dades d'entrada
				    de seshat.
				  </div>
				</div>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/simplecv.png" alt="java">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">SimpleCv</h4>
				    Per filtrar possibles errors i defectes de les imatges procedents de la c&agrave;mera, utilitzem SimpleCv, que 
				    &eacute;s una llibreria Python que et dona m&eacute;s alt nivell per fer aquestes coses. Aix&iacute; evitem molts 
				    errors a Autotrace.
				  </div>
				</div>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/ucrop.png" alt="java">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">UCrop</h4>
				    UCrop &eacute;s una llibreria Android que et dona la interficie feta per a la edici&oacute; d'imatges. En 
				    aquest cas nosaltres l'hem aprofitada per a ajustar les operacions matem&agrave;tiques i així reduir els 
				    possibles errors.
				  </div>
				</div>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/sympy.png" alt="java">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">Sympy</h4>
				    Sympy son unes llibreries matem&agrave;tiques Python que hem utilitzat per a resoltre les equacions de primer
				    i segon grau, ja que aquestes llibreries et donen eines de molt alt nivell que et faciliten molt la feina.
				  </div>
				</div>
				<h1 class="title22">Tecnologies</h1>
				<h3 class="title22">IDE utilitzats per desenvolupar</h3>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/androidstudio.jpeg" alt="java">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">AndroidStudio</h4>
				    Per desenvolupar l'aplicaci&oacute; m&ograve;bil, hem utilitzat AndroidStudio, que &eacute;s la proporcionada per Android.
				  </div>
				</div>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/monodevelop.png" alt="monodevelop">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">Monodevelop</h4>
				    Per desenvolupar amb C++ em esollit Monodevelop, la vam escollir despr&eacute;s de fer una cerca per internet i llegir que era de les millors.
				  </div>
				</div>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/IJ.png" alt="idea">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">IDEA de Intel IJ</h4>
				    Per programar el servidor java hem utilitzat el ide de Intel IJ idea.
				  </div>
				</div>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/pycharm.png" alt="idea">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">Pycharm de Intel IJ</h4>
				    Per programar les llibreries matem&agrave;tiques amb python hem utilitzat el ide de Intel IJ Pycharm.
				  </div>
				</div>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/sublimetext.jpeg" alt="idea">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">Sublime Text</h4>
				    Tamb&eacute; per programar amb Python i per desenvolupar aquesta mateixa web, hem utilitzat sublime text.
				  </div>
				</div>
				<h3 class="title22">Altres tecnologies</h3>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/github.gif" alt="idea">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">GitHub</h4>
				    Per tenir copies de seguretat i treballar millor des de casa hem escollit utilitzar Github com a control de versions.
				  </div>
				</div>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/evernote.png" alt="idea">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">Evernote</h4>
				    Per repartir tasques, apuntar les hores invertides en el projecte i guardar informació rellevant que anem trobant per internet
				     i que sigui accessible a tothom utilitzem Evernote.
				  </div>
				</div>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/taiga.png" alt="idea">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">Taiga</h4>
				    Per desenvolupar el projecte, hem utilitzat una metodologia àgil anomenada scrum, que consisteix en anar repartint 
				    les tasques en sprints setmanals. Per portar un control de tot això hem utilitzat Taiga.
				  </div>
				</div>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/virtualbox.png" alt="idea">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">VirtualBox</h4>
				    Per emular màquines virtuals, hem utilitzat VirtualBox.
				  </div>
				</div>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/debian.jpg" alt="idea">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">Debian</h4>
				    Desde el nostre punt de vista el millor sistema operatiu per treballar és Linux i la seva millor
				    distribució de cara a servidors és Debian per això vam escollir treballar amb aquesta.
				  </div>
				</div>
				<div class="media">
				  <div class="media-left">
				      <img class="media-object imgwidth" src="imatges/elementary.svg" alt="idea">
				  </div>
				  <div class="media-body">
				    <h4 class="media-heading">Elementary OS</h4>
				    Com a sistema operatiu també s'ha utilitzat Elementary OS.
				  </div>
				</div>
			</div>
		</div>
		<?php include_once("html/footer.html");?> <!--Incloc el footer de la pàgina -->
	</body>
</html>