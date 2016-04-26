package com.educem.eyecalc.androideyecalc;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.View;
import android.widget.Button;

public class ResultActivity extends Activity {
    //boto per anar a la primera activity i torna a fer la foto
    private Button scanAgain;
    //id de transacció, perque el servidor sepiga qui soc quan li demani el resultat.
    private int ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        //agafo la id que em servire per que el servidor sepiga qui soc.
        Intent in = getIntent();
        ID = in.getExtras().getInt("id");
        //Mostro una animacio (processant)
        //obro threads d'escolta al servidor
        //inicialitzo el boto per tornar a scanejar i li poso l'escoltador per quan el clickin
        scanAgain = (Button) findViewById(R.id.btScanAgain);
        scanAgain.setOnClickListener(new takenfotoClicker());
    }
    //classe a que sentra quan fas click a scanAgain
    public class takenfotoClicker implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            //creo un intent per tornar a la primera activity
            Intent intTofirstActivity = new Intent(ResultActivity.this,InitialActivity.class);
            //vaig a ala primera activity per torna a scanejar
            startActivity(intTofirstActivity);
            //tanco la activity
            ResultActivity.this.finish();
        }
    }
}
