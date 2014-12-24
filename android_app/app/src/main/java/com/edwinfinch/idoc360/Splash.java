package com.edwinfinch.idoc360;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by edwinfinch on 14-12-21.
 */
public class Splash extends Activity {
    Timer timer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getActionBar().hide();

        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Intent launchMain = new Intent(Splash.this, Main.class);
                Splash.this.startActivity(launchMain);
            }
        }, 3000);
    }

    protected void onPause(){
        super.onDestroy();
        this.finish();
    }
}
