<?php

	/*$ruta = "Imatges/" .basename($_POST['image']);
	if(move_uploaded_file($_POST['image'], $ruta))
	       chmod ("Imatges/".basename( $_POST['image']), 0644);*/
	   	
	   	/*$file=fopen("test.txt", 'wb');
	   	foreach ($_REQUEST as $key => $value) {
	   		fwrite($file, $key."------------");
	   		fwrite($file, $value."------------");
	   	}
		fclose($file);*/
	if(isset($_REQUEST)){
		//$decodedImage  = base64_decode($_REQUEST);
		
		header('Content-Type: bitmap; charset=utf-8');

		$file=fopen("test.bmp", 'wb');
		fwrite($file, $_REQUEST);
		fclose($file);

		//jpeg2wbmp(jpegname, wbmpname, dest_height, dest_width, threshold)
		

		if($img = imagecreatefromstring($_REQUEST)){
			//imagejpeg($img,"Imatges/imatge.jpg",100);
			imagewbmp($img,"Imatges/img.bmp",100);
		}
		
		
		
	}
?>