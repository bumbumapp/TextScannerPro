package com.bumbumapps.studytextscan;

import android.os.CountDownTimer;

import com.bumbumapps.studytextscan.Globals;


public  class Timers {

    public static CountDownTimer timer(){
        return new CountDownTimer(120000, 1000) {

            public void onTick(long millisUntilFinished) {}

            public void onFinish() {
                 Globals.TIMER_FINISHED = true;
            }
        };
    }

}