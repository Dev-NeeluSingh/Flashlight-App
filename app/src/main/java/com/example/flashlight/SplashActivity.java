package com.example.flashlight;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.util.Timer;
import java.util.TimerTask;

public class SplashActivity extends Activity {
    private final Timer timer1 = new Timer();
    private TimerTask timerTask1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_layout);        // set layout her mine -> splash_layout
        timerTask1 = new TimerTask() {
            @Override
            public void run() {                //timer starts to perform task in given time
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        toSplash();                     //toSplash function called here
                        timerTask1.cancel();            //timer task cancel on given time complete
                    }
                });
            }
        };
        timer1.schedule(timerTask1, (int) (4000));     // set timer duration or timer time 1000 = 1sec
    }

    public void toSplash() {
        Intent intent = new Intent(this, MainActivity.class);  // used intent to move from this activity to another activity
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);                    //use intent clear flag helps in clearing last activity and starts new activity
        startActivity(intent);                                              //intent started to move from here to another activity
    }
}

