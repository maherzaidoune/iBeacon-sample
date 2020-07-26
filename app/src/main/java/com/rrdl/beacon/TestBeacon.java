package com.rrdl.beacon;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;

public class TestBeacon  extends Application {

    public static volatile Context applicationContext;
    public static volatile Handler applicationHandler;

    @Override
    public void onCreate() {
        try {
            applicationContext = getApplicationContext();
        } catch (Throwable ignore) {

        }

        super.onCreate();

        if (applicationContext == null) {
            applicationContext = getApplicationContext();
        }
        applicationHandler = new Handler(applicationContext.getMainLooper());

        AndroidUtilities.runOnUIThread(TestBeacon::startService);
    }

    public static void startService(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            applicationContext.startForegroundService(new Intent(applicationContext, BeaconService.class));
        } else {
            applicationContext.startService(new Intent(applicationContext, BeaconService.class));
        }
    }


}
