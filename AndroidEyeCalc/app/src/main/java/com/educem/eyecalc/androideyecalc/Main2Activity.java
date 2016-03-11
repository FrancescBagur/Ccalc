package com.educem.eyecalc.androideyecalc;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.media.Image;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class Main2Activity extends AppCompatActivity {

    Button tkfoto;   //boto que obra la camara
    ImageView takenfoto; //image view que mostra la foto feta
    private static final int CAM_REQUEST = 1313;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        //inicialitza i associa elements visuals amb els programatics
        tkfoto = (Button) findViewById(R.id.btTakePhoto);
        takenfoto = (ImageView) findViewById(R.id.imgTaken);
        //escoltador del boto tkfoto que crida a una clase
        tkfoto.setOnClickListener(new takenfotoClicker());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent Data) {
        super.onActivityResult(requestCode, resultCode, Data);

        if (requestCode == CAM_REQUEST) {
            Bitmap bmp = (Bitmap) Data.getExtras().get("data");

            //invertir la imatge
            Matrix m = new Matrix();
            m.preScale(1,-1);
            Bitmap bmpInvertit = Bitmap.createBitmap(bmp,0,0,bmp.getWidth(),bmp.getHeight(),m,false);
            bmpInvertit.setDensity(DisplayMetrics.DENSITY_DEFAULT);
            //mostra la imatge
            takenfoto.setImageBitmap(bmpInvertit);

        }
    }

    //classe a que sentra quan fas click a tkfoto
    class takenfotoClicker implements Button.OnClickListener {

        @Override
        public void onClick(View v) {
            //intent que obrira la camara i guardara la foto que es faci
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //envia l'intent amb la foto feta
            startActivityForResult(cameraIntent, CAM_REQUEST);
        }
    }
}