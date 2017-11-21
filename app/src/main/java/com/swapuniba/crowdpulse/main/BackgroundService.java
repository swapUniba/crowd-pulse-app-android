package com.swapuniba.crowdpulse.main;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;


import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.swapuniba.crowdpulse.business_object.Account;
import com.swapuniba.crowdpulse.business_object.ActivityData;
import com.swapuniba.crowdpulse.business_object.AppInfo;
import com.swapuniba.crowdpulse.business_object.Contact;
import com.swapuniba.crowdpulse.business_object.GPS;
import com.swapuniba.crowdpulse.business_object.NetStats;
import com.swapuniba.crowdpulse.comunication.SocketApplication;
import com.swapuniba.crowdpulse.comunication.TransfertData;
import com.swapuniba.crowdpulse.config.Constants;
import com.swapuniba.crowdpulse.config.SettingFile;
import com.swapuniba.crowdpulse.database.DbManager;
import com.swapuniba.crowdpulse.handlers.AccountHandler;
import com.swapuniba.crowdpulse.handlers.ActivityHandler;
import com.swapuniba.crowdpulse.handlers.AppInfoHandler;
import com.swapuniba.crowdpulse.handlers.ContactHandler;
import com.swapuniba.crowdpulse.handlers.GpsHandler;
import com.swapuniba.crowdpulse.handlers.DisplayHandler;
import com.swapuniba.crowdpulse.handlers.NetStatsHandler;
import com.swapuniba.crowdpulse.utility.Utility;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by fabio on 08/09/17.
 */
public class BackgroundService extends IntentService {

    static String threadName = null;
    private Handler handlergps = new Handler(Looper.getMainLooper());

    static IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
    BroadcastReceiver mReceiver = new DisplayHandler();

    private Thread t = null;
    private Intent i = null;
    GoogleApiClient mApiClient;

    //Service time variable
    static int thread_lifetime = 0; //millisecond lifetime of the thread
    static int background_service_repeat_time = Constants.background_service_repeat_time;
    static int background_service_restart_time = Constants.background_service_restart_time;


    public BackgroundService(String name) {
        super(name);
    }

    public BackgroundService() {
        //service name
        super("Crowdpulse_BackgroundService");

    }

    //override by onStartCommand!!!!!!
    @Override
    protected void onHandleIntent(Intent workIntent) {
        // Gets data from the incoming Intent
        String dataString = workIntent.getDataString();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        i= intent;


        mApiClient = new GoogleApiClient.Builder(getApplicationContext())
                .addApi(ActivityRecognition.API)
                .build();

        mApiClient.connect();

        Utility.printLog("Start background service...");

        //create a listener for screen on/off
        if (SettingFile.getSettings(getApplication()).get(Constants.setting_read_display).
                                                                equalsIgnoreCase(Constants.record)){
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            registerReceiver(mReceiver, filter);
        }else {
            try {
                unregisterReceiver(mReceiver);
            }
            catch (Exception e){
                e.printStackTrace();
            }

        }

        DbManager db = new DbManager(getApplication());
        db.clearSendData();

        if(!SocketApplication.sending){

            SocketApplication.sending = true;

            //SEND DATA AFTER 10 SECOND
            final TransfertData transfertData = new TransfertData(getApplicationContext());
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    transfertData.send();

                    Handler handler2 = new Handler();
                    handler2.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            SocketApplication.sending=false;
                        }
                    }, 10000);

                }
            }, 10000);
        }


        t = new Thread(
                new Runnable() {

                    @Override
                    public void run() {

                        try{

                            Utility.printLog("Background service is running...");


                            HashMap<String, String> settings = SettingFile.getSettings(getApplication());
                            for (String setting_key : Constants.setting_permission_keys){
                                //Utility.printLog(setting_key + "is set: " + settings.get(setting_key));
                                //execute only enabled service
                                if (settings.get(setting_key).equalsIgnoreCase(Constants.record)){

                                    switch (setting_key){
                                        case Constants.setting_read_gps:

                                            if (GpsHandler.checkTimeBetweenRequest(getApplicationContext())){
                                                GPS gps = GpsHandler.readGPS(getApplication());
                                                if(gps != null){
                                                    GpsHandler.saveGPS(gps, getApplicationContext());
                                                    GpsHandler.setNetxTime(getApplicationContext());
                                                    }
                                                else{
                                                    Utility.printLog("Error to get GPS");
                                                }
                                            }

                                            break;

                                        case Constants.setting_read_contacts:
                                            if (ContactHandler.checkTimeBetweenRequest(getApplicationContext())){
                                                ArrayList<Contact> contactArrayList = ContactHandler.readContact(getApplicationContext());
                                                ContactHandler.saveContactArray(contactArrayList, getApplicationContext());
                                                ContactHandler.setNetxTime(getApplicationContext());
                                            }

                                            break;

                                        case Constants.setting_read_accounts:
                                            if (AccountHandler.checkTimeBetweenRequest(getApplicationContext())){
                                                ArrayList<Account> accountArrayList = AccountHandler.readAccounts(getApplicationContext());
                                                AccountHandler.saveAccountArray(accountArrayList, getApplicationContext());
                                                AccountHandler.setNetxTime(getApplicationContext());
                                            }
                                            break;
                                        /*
                                        case Constants.setting_read_calendar:

                                            break;

                                        case Constants.setting_read_sms:

                                            break;
                                         */
                                        case Constants.setting_read_app:
                                            if (AppInfoHandler.checkTimeBetweenRequest(getApplicationContext())){
                                                ArrayList<AppInfo> appInfoArrayList = AppInfoHandler.readAppInfo(getApplicationContext());
                                                AppInfoHandler.saveAppInfoArray(appInfoArrayList, getApplicationContext());
                                                AppInfoHandler.setNetxTime(getApplicationContext());
                                            }

                                            break;

                                        case Constants.setting_read_netstats:
                                            if (NetStatsHandler.checkTimeBetweenRequest(getApplicationContext())){
                                            ArrayList<NetStats> netStatsArrayList = NetStatsHandler.readNetworkStats(getApplicationContext());
                                            NetStatsHandler.saveNetStatsArray(netStatsArrayList, getApplicationContext());
                                            NetStatsHandler.setNetxTime(getApplicationContext());
                                            }

                                            break;

                                        case Constants.setting_read_activity:

                                            System.out.println("Activity: " + "unooo");

                                            if ( mApiClient.isConnected() && ActivityHandler.checkTimeBetweenRequest(getApplicationContext())){
                                                System.out.println("Activity: " + "dueeee");
                                                ActivityData activityData = ActivityHandler.readActivity(i, getApplicationContext(), mApiClient);
                                                ActivityHandler.saveActivity(activityData, getApplicationContext());
                                                ActivityHandler.setNetxTime(getApplicationContext());
                                            }

                                        default:

                                            break;

                                    }
                                }

                            }

                        }
                        catch (Exception e){
                            e.printStackTrace();
                        }
                        finally {

                            thread_lifetime = thread_lifetime + background_service_repeat_time;
                            //used to reset the thread (prevents the auto kill for inactivity)
                            if(thread_lifetime < background_service_restart_time){
                                handlergps.postDelayed(this, background_service_repeat_time);
                            }else{
                                onDestroy();
                            }

                        }

                    }//END RUN

                });


        t.setPriority(Thread.MAX_PRIORITY);

        t.setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            public void uncaughtException(Thread t, Throwable e) {
                onDestroy();
            }
        });


        //Start only one thread with this name
        if(threadName == null){
            handlergps.postDelayed(t, 0);
            threadName = t.getName();
        }

        //resetted automatically the service if killed
        return START_STICKY;
    }



    @Override
    public void onDestroy() {

        t.interrupt();
        threadName = null;
        t = null;
        thread_lifetime = 0;

        super.onDestroy();

        Utility.printLog("destory and resetted the backgroundservice");

        Intent mServiceIntent = new Intent(this, BackgroundService.class);

        // Starts the IntentService
        this.startService(mServiceIntent);

    }


}
