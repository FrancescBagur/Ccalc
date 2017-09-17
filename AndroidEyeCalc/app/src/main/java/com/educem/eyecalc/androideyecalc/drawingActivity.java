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
    //intents
    Intent goToResultActivity;
    Intent goToInitialActivity;

    //buttons
    private Button clearButton;
    private Button sendButton;

    //Drawing vars
    private DrawingView drawingView ;
    private Paint mPaint;
    private LinearLayout llPrinc;
    private String[] traces = new String[500];
    private String[] strokes;
    private int nStrokes=0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawing);

        getViewsById();

        addClickListeners();

        obrirCanvas();
    }

    private void addClickListeners(){
        clearButton.setOnClickListener(new buttonsListener());
        sendButton.setOnClickListener(new buttonsListener());
    }

    private void getViewsById(){
        llPrinc = (LinearLayout) findViewById(R.id.llPrinc);
        clearButton = (Button) findViewById(R.id.btClear);
        sendButton = (Button) findViewById(R.id.btSend);
    }

    private void obrirCanvas(){
        //per escriure per pantalla
        drawingView = new DrawingView(this);
        drawingView.setBackgroundColor(Color.WHITE);
        drawingView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1f));
        llPrinc.addView(drawingView);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.parseColor("#FF4A659E"));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(15);
    }

    public boolean isNetworkAvailable(final Context context) {
        final ConnectivityManager connectivityManager = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE));
        return connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();
    }

    public class buttonsListener implements Button.OnClickListener {
        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.btClear){
                drawingActivity.this.recreate();
            }
            else {
                if(!isNetworkAvailable(getApplicationContext())){
                    clearButton.setText("Connection lost, try again");
                } else {

                    if(traces[0]!=null) {
                        preparaDades();
                        goToResultActivity = new Intent(drawingActivity.this, DrawResultActivity.class);
                        goToResultActivity.putExtra("strokes", strokes);
                        startActivity(goToResultActivity);
                        drawingActivity.this.finish();
                    }
                }
            }
        }
    }

    private void preparaDades(){
        int i=0;
        while (traces[i]!=null)i++;
        strokes = new String[i];
        for(i=0; i<strokes.length; i++) strokes[i] = traces[i];
    }

    public class DrawingView extends View {

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
        private static final float TOUCH_TOLERANCE = 1;

        private void touch_start(float x, float y) {
            mPath.reset();
            mPath.moveTo(x, y);
            mX = x;
            mY = y;
            //inici de stroke en x,y
            if (nStrokes < 500) {
                String puntIni = "[[" + Math.round(x) + "," + Math.round(y) + "]";
                traces[nStrokes] = puntIni;
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
                    traces[nStrokes] += puntMove;
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
                traces[nStrokes] += "]";
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

    //obra la activity inicial
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        goToInitialActivity = new Intent(drawingActivity.this,InitialActivity.class);
        startActivity(goToInitialActivity);
        drawingActivity.this.finish();
    }
}
