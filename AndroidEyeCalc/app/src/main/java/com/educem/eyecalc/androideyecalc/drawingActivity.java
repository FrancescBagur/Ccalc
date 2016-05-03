package com.educem.eyecalc.androideyecalc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

public class drawingActivity extends Activity {
    private Button CLEAR;  //nateja la pantalla
    private Button SEND;  //envia les dades
    //variables per escriure per pantalla
    private DrawingView dv ;
    private Paint mPaint;
    private LinearLayout llPrinc;
    private int nStrokes=0;
    //tracades per enviar i esperar resposta!
    private String[] tracades = new String[500];
    private String[] strokes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);
        //linear layout gefe
        llPrinc = (LinearLayout) findViewById(R.id.llPrinc);
        //Buttons
        CLEAR = (Button) findViewById(R.id.btClear);
        SEND = (Button) findViewById(R.id.btSend);
        CLEAR.setOnClickListener(new listenClick());
        SEND.setOnClickListener(new listenClick());
        //prepara la pantalla per escriure amb el dit
        obrirCanvas();
    }
    //prepara la pantalla per escriure amb el dit
    private void obrirCanvas(){
        //per escriure per pantalla
        dv = new DrawingView(this);
        dv.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        llPrinc.addView(dv,0);
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);
    }
    //Comprobo si hi ha internet
    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }
    //fa feina quan es clicken els botons, o nateja la pantalla o envia les dades.
    public class listenClick implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btClear){
                //aqui es nateja la pantalla
                drawingActivity.this.recreate();
            }
            else {
                if(!isNetworkAvailable(getApplicationContext())){
                    SEND.setText("No connection, Reload");
                    drawingActivity.this.recreate();
                } else {
                    //aqui senvian les dades al servidor
                    //obra un activity de resultat enviatli els tracades realitzats
                    if(tracades[0]!=null) {
                        preparaDades();
                        Intent goRes = new Intent(drawingActivity.this, DrawResultActivity.class);
                        goRes.putExtra("strokes", strokes);
                        startActivity(goRes);
                        drawingActivity.this.finish();
                    }
                }
            }
        }
    }
    //prepara les dades(treu nulls de l'array) per enviarles a la activity on es mostrara el resultat
    private void preparaDades(){
        int i=0;
        while (tracades[i]!=null)i++;
        strokes = new String[i];
        for(i=0; i<strokes.length; i++) strokes[i] = tracades[i];
    }
    //clase per escriure a la pantalla amb el dit
    public class DrawingView extends View {

        public int width;
        public  int height;
        private Bitmap mBitmap;
        private Canvas mCanvas;
        private Path mPath;
        private Paint   mBitmapPaint;
        Context context;
        private Paint circlePaint;
        private Path circlePath;

        public DrawingView(Context c) {
            super(c);
            context=c;
            mPath = new Path();
            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
            circlePaint = new Paint();
            circlePath = new Path();
            circlePaint.setAntiAlias(true);
            circlePaint.setColor(Color.parseColor("#3a5795"));
            circlePaint.setStyle(Paint.Style.STROKE);
            circlePaint.setStrokeJoin(Paint.Join.MITER);
            circlePaint.setStrokeWidth(4f);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);

            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            canvas.drawBitmap( mBitmap, 0, 0, mBitmapPaint);
            canvas.drawPath( mPath,  mPaint);
            canvas.drawPath( circlePath,  circlePaint);
        }

        private float mX, mY;
        private static final float TOUCH_TOLERANCE = 4;

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
            //inici de stroke en x,y
            if (nStrokes < 500) {
                String puntIni = "[[" + Math.round(x) + "," + Math.round(y) + "]";
                tracades[nStrokes] = puntIni;
            }
        }

        private void touch_move(float x, float y) {
            float dx = Math.abs(x - mX);
            float dy = Math.abs(y - mY);
            if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
                mPath.quadTo(mX, mY, (x + mX)/2, (y + mY)/2);
                mX = x;
                mY = y;

                circlePath.reset();
                circlePath.addCircle(mX, mY, 30, Path.Direction.CW);
                //la stroke va aumentant en x,y
                if(nStrokes < 500) {
                    String puntMove = ",[" + Math.round(x) + "," + Math.round(y) + "]";
                    tracades[nStrokes] += puntMove;
                }
            }
        }

        private void touch_up() {
            mPath.lineTo(mX, mY);
            circlePath.reset();
            // commit the path to our offscreen
            mCanvas.drawPath(mPath,  mPaint);
            // kill this so we don't double draw
            mPath.reset();
            //final de tracades
            if(nStrokes < 501) {
                tracades[nStrokes] += "]";
                nStrokes++;
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float x = event.getX();
            float y = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    touch_start(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_MOVE:
                    touch_move(x, y);
                    invalidate();
                    break;
                case MotionEvent.ACTION_UP:
                    touch_up();
                    invalidate();
                    break;
            }
            return true;
        }
    }
}
