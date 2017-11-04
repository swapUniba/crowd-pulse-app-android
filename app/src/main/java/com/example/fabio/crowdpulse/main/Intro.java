package com.example.fabio.crowdpulse.main;

import android.Manifest;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;

import com.example.fabio.crowdpulse.R;
import com.example.fabio.crowdpulse.config.SettingFile;
import com.example.fabio.crowdpulse.utility.NotificationUtility;
import com.example.fabio.crowdpulse.utility.Utility;

import java.util.HashMap;

public class Intro extends Activity {

    Handler mHandler = new Handler();
    Runnable mUpdateTimeTask;

    static boolean check_ACCESS_SETTINGS = false;
    static boolean first = true;

    /** code to post/handler request for permission */
    public final static int REQUEST_CODE = -1010101;

    private static final int REQUEST_MANAGER = 0x11;

    //TODO modify the permission here and in the manifest
    private static String[] PERMISSIONS = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            //Manifest.permission.WRITE_EXTERNAL_STORAGE,
            //Manifest.permission.READ_EXTERNAL_STORAGE,
            //Manifest.permission.INTERNET,
            //Manifest.permission.MODIFY_AUDIO_SETTINGS,
            Manifest.permission.READ_SMS,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.READ_PHONE_STATE,
            //Manifest.permission.PACKAGE_USAGE_STATS,
            Manifest.permission.GET_ACCOUNTS,
            Manifest.permission.READ_PHONE_STATE
            //Manifest.permission.BIND_NOTIFICATION_LISTENER_SERVICE
        };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.intro);

        SettingFile.inizialize(getApplicationContext());

        HashMap<String, String> a = SettingFile.getSettings(getApplicationContext());

        //check permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            /** check if we already  have permission to draw over other apps */
            if (isAccessGranted()) {
                requestPermissions(PERMISSIONS, REQUEST_MANAGER);
            }
            else{
                check_ACCESS_SETTINGS = true;
                //startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), REQUEST_CODE);
            }
        }
        else{
            initialOperations();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (first){
            first = false;
        }
        else{
            if (check_ACCESS_SETTINGS) {
                check_ACCESS_SETTINGS = false;
                NotificationUtility.showToast(getApplicationContext(), getString(R.string.access_setting_false));
                reload();
            }
        }

    }

    private boolean isAccessGranted() {
        try {
            PackageManager packageManager = getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(getPackageName(), 0);
            AppOpsManager appOpsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = 0;
            if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.KITKAT) {
                mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        applicationInfo.uid, applicationInfo.packageName);
            }
            return (mode == AppOpsManager.MODE_ALLOWED);

        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }




    private void initialOperations(){
        startBackgroundService();
        openLoginActivity();
    }



    private void startBackgroundService(){

        Intent mServiceIntent = new Intent(this, BackgroundService.class);
        //TODO riattivare
        //mServiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        // Starts the IntentService
        //startService(mServiceIntent);

    }


    private void openLoginActivity(){
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), Login.class);//Main.class);
                startActivity(intent);
            }
        }, 3000);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        /** check if received result code
         is equal our requested code for draw permission  */
        if (requestCode == REQUEST_CODE) {
            //reload();
        }
    }




    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_MANAGER) {
            boolean all_accepted = true;
            for (int i = 0; i < grantResults.length; i++ ){
                //Utility.printLog("permissions: " + permissions[i] + "grant: " + grantResults[i]);
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED){
                    all_accepted = false;
                }
            }

            if (all_accepted) {
                initialOperations();
            }
            else{
                NotificationUtility.showToast(getApplicationContext(),getString(R.string.permission_not_acccept));
                reload();
            }

        }
    }



    private void reload(){

        onCreate(getIntent().getExtras());

        //Intent mServiceIntent = new Intent(getApplicationContext(), Intro.class);
        //mServiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // Starts the IntentService
        //startService(mServiceIntent);
    }


}



