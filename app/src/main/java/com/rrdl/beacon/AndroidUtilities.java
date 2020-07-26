package com.rrdl.beacon;

public class AndroidUtilities {
    public static void runOnUIThread(Runnable runnable) {
        runOnUIThread(runnable, 0);
    }

    public static void runOnUIThread(Runnable runnable, long delay) {
        if (delay == 0) {
            TestBeacon.applicationHandler.post(runnable);
        } else {
            TestBeacon.applicationHandler.postDelayed(runnable, delay);
        }
    }

    public static void cancelRunOnUIThread(Runnable runnable) {
        TestBeacon.applicationHandler.removeCallbacks(runnable);
    }
}
