package com.educem.eyecalc.androideyecalc;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;

public class ResultActivity extends Activity {
    private  int ID;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);
        //agafo la id que em servire per que el servidor sepiga qui soc.
        Intent in = getIntent();
        ID = in.getExtras().getInt("id");
        //Mostro una animacio (processant)
        //obro threads d'escolta al servidor
    }
}
