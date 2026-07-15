package com.rern.portfolio;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final int SPLASH_DURATION = 2500;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Fade in the logo for a dramatic effect
        ImageView logo = findViewById(R.id.splashLogo);
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(1200);
        fadeIn.setFillAfter(true);
        logo.startAnimation(fadeIn);

        new Handler().postDelayed(() -> {
            // Fade out before transitioning
            AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
            fadeOut.setDuration(400);
            fadeOut.setFillAfter(true);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override public void onAnimationStart(Animation a) {}
                @Override public void onAnimationRepeat(Animation a) {}
                @Override
                public void onAnimationEnd(Animation a) {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                }
            });
            logo.startAnimation(fadeOut);
        }, SPLASH_DURATION);
    }
}
