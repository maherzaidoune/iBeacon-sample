package com.rrdl.beacon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Restarter extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        AndroidUtilities.runOnUIThread(() -> {
            TestBeacon.startService();
        });
    }
}

