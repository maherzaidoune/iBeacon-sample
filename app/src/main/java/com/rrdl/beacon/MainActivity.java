package com.rrdl.beacon;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.nabinbhandari.android.permissions.PermissionHandler;
import com.nabinbhandari.android.permissions.Permissions;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    Intent intentService = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] permissions = new String[0];
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            permissions = new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION };
        }else{
            permissions = new String[]{ Manifest.permission.ACCESS_COARSE_LOCATION , Manifest.permission.ACCESS_FINE_LOCATION};
        }
        Permissions.check(this, permissions, null, null, new PermissionHandler() {
            @SuppressLint("MissingPermission")
            @Override
            public void onGranted() {

                intentService = new Intent(MainActivity.this, BeaconService.class);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    getApplicationContext().startForegroundService(intentService);
                } else {
                    getApplicationContext().startService(intentService);
                }
            }

            @Override
            public void onDenied(Context context, ArrayList<String> deniedPermissions) {
            }

            @Override
            public boolean onBlocked(Context context, ArrayList<String> blockedList) {
                return true;
            }
        });
        Toast.makeText(getApplicationContext(), "Allow beacon to run on background", Toast.LENGTH_LONG).show();
        if (getPackageManager().resolveActivity(new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")), PackageManager.MATCH_DEFAULT_ONLY) != null) {
            Intent INTENT_XIAOMI = new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"));
            startActivity(INTENT_XIAOMI);
        }
    }
}