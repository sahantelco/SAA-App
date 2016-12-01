package org.wso2telco.authenticator.client.activity;

import android.os.Bundle;

import org.wso2telco.authenticator.client.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.view.Window;

public class ActivitySplash extends Activity {

    private static int SPLASH_TIME_OUT = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                Intent i = new Intent(ActivitySplash.this, ActivityMain.class);
                startActivity(i);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
