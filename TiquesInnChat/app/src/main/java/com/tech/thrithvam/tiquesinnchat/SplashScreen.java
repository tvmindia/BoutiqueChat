package com.tech.thrithvam.tiquesinnchat;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        startService(new Intent(this, Services.class)); //calling the service
        //---------------Making it fullscreen----------------------
        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
        else
        {
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
        //-----------------Image scrolling---------------------
        ImageView splashImage=(ImageView)findViewById(R.id.splashImage);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int width=displaymetrics.widthPixels;
        ObjectAnimator Anim1 = ObjectAnimator.ofFloat(splashImage, "x", 0, -1920 + width);
        Anim1.setDuration(6000);
        Anim1.start();

        //-----------------Titles---------------------------------------
        TextView title=(TextView)findViewById(R.id.title);
        TextView tagLine=(TextView)findViewById(R.id.tagLine);
        Typeface type = Typeface.createFromAsset(getAssets(),"fonts/avenirnextregular.ttf");
        title.setTypeface(type);
        type = Typeface.createFromAsset(getAssets(),"fonts/handwriting.ttf");
        tagLine.setTypeface(type);
        ObjectAnimator scaleXb1 = ObjectAnimator.ofFloat(tagLine, "scaleX", 0.5f, 1.0f);
        scaleXb1.setDuration(4000);
        scaleXb1.start();

        //---------------------Moving to next screen--------------------------
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                Intent goHome = new Intent(SplashScreen.this, Login.class);
                goHome.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                goHome.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(goHome);
                finish();
            }
        },6000);
    }
}
