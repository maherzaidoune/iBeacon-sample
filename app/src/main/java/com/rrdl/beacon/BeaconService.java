package com.rrdl.beacon;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.RemoteException;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import java.util.Collection;

public class BeaconService extends Service implements BeaconConsumer {
    private static final int NOTIFICATION_ID = 12345678;
    PowerManager.WakeLock wakeLock;
    PowerManager powerManager;
    String PACKAGE_NAME;
    private BeaconManager beaconManager;


    @Override
    public void onCreate() {
        super.onCreate();
        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout("m:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24"));
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        powerManager = (PowerManager) getSystemService(POWER_SERVICE);

        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "Beacon::beacon");

        wakeLock.acquire(10*60*1000L /*10 minutes*/);

        PACKAGE_NAME = getApplicationContext().getPackageName();



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, createChannel());
            builder.setPriority(NotificationCompat.PRIORITY_HIGH);
            builder.setVisibility(NotificationCompat.VISIBILITY_SECRET);
            startForeground(NOTIFICATION_ID, builder.build());

        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, new Notification.Builder(this, "default").build());
        } else {
            startForeground(NOTIFICATION_ID, new NotificationCompat.Builder(this, "default").build());
        }


        beaconManager.bind(this);
        return START_NOT_STICKY;
    }
    @RequiresApi(api = Build.VERSION_CODES.O)
    @NonNull
    private synchronized String createChannel() {
        NotificationManager mNotificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);

        String name = "Beacon";

        int importance = NotificationManager.IMPORTANCE_LOW;

        NotificationChannel mChannel = new NotificationChannel("Beacon", name, importance);

        mChannel.enableLights(true);
        mChannel.setLightColor(Color.BLUE);
        if (mNotificationManager != null) {
            mNotificationManager.createNotificationChannel(mChannel);
        } else {
            stopSelf();
        }
        return "Beacon";
    }

    @Override
    public void onDestroy() {
        Intent broadcastIntent = new Intent("restartservice");
        sendBroadcast(broadcastIntent);
    }


    @Override
    public void onBeaconServiceConnect() {
        beaconManager.removeAllMonitorNotifiers();
        beaconManager.addMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i("test", "I just saw an beacon for the first time! == " + region);
                if(wakeLock != null)
                    wakeLock.acquire(10*60*1000L /*10 minutes*/);
                final int num = 200;
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(BeaconService.this, "channel_id");
                notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);  // heads-up
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationBuilder.setAutoCancel(false)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle("Attention")
                        .setContentText("didEnterRegion")
                        .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("didEnterRegion"))
                        .setOngoing(true);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_HIGH);
                    channel.setDescription("channel description");
                    channel.setShowBadge(true);
                    channel.canShowBadge();
                    channel.enableLights(true);
                    channel.enableVibration(true);
                    channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
                    notificationManager.createNotificationChannel(channel);
                }

                notificationManager.notify(num, notificationBuilder.build());
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i("test", "I no longer see an beacon == " + region);
                if(wakeLock != null)
                    wakeLock.acquire(10*60*1000L /*10 minutes*/);
                final int num = 201;
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(BeaconService.this, "channel_id");
                notificationBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);  // heads-up
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                notificationBuilder.setAutoCancel(false)
                        .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setDefaults(Notification.DEFAULT_VIBRATE)
                        .setContentTitle("Attention")
                        .setContentText("didExitRegion")
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText("didExitRegion"))
                        .setOngoing(true);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel channel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_HIGH);
                    channel.setDescription("channel description");
                    channel.setShowBadge(true);
                    channel.canShowBadge();
                    channel.enableLights(true);
                    channel.enableVibration(true);
                    channel.setVibrationPattern(new long[]{100, 200, 300, 400, 500});
                    notificationManager.createNotificationChannel(channel);
                }

                notificationManager.notify(num, notificationBuilder.build());
            }

            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                if(wakeLock != null)
                    wakeLock.acquire(10*60*1000L /*10 minutes*/);
                Log.i("test", "I have just switched from seeing/not seeing beacons: "+state);
            }
        });
        try {
            beaconManager.startMonitoringBeaconsInRegion(new Region("myMonitoringUniqueId", null, null, null));
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        beaconManager.removeAllRangeNotifiers();
        beaconManager.addRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                if (beacons.size() > 0) {
                    if (beacons.size() > 0) {
                        Log.d("test", "didRangeBeaconsInRegion called with beacon count:  "+beacons.size());
                        Beacon firstBeacon = beacons.iterator().next();
                    }
                        Log.i("test", "The first beacon I see is about "+beacons.iterator().next().getDistance()+" meters away.");
                    }
                }
        });
        try {
            beaconManager.startRangingBeaconsInRegion(new Region("myRangingUniqueId", null, null, null));
        } catch (RemoteException e) {    }



    }
}
