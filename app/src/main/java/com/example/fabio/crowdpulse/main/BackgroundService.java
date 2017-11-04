package com.example.fabio.crowdpulse.main;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;


import com.example.fabio.crowdpulse.business_object.Account;
import com.example.fabio.crowdpulse.business_object.AppInfo;
import com.example.fabio.crowdpulse.business_object.Contact;
import com.example.fabio.crowdpulse.business_object.GPS;
import com.example.fabio.crowdpulse.business_object.NetStats;
import com.example.fabio.crowdpulse.comunication.SocketApplication;
import com.example.fabio.crowdpulse.comunication.TransfertData;
import com.example.fabio.crowdpulse.config.Constants;
import com.example.fabio.crowdpulse.config.SettingFile;
import com.example.fabio.crowdpulse.database.DbManager;
import com.example.fabio.crowdpulse.handlers.AccountHandler;
import com.example.fabio.crowdpulse.handlers.AppInfoHandler;
import com.example.fabio.crowdpulse.handlers.ContactHandler;
import com.example.fabio.crowdpulse.handlers.GpsHandler;
import com.example.fabio.crowdpulse.handlers.DisplayHandler;
import com.example.fabio.crowdpulse.handlers.NetStatsHandler;
import com.example.fabio.crowdpulse.utility.Utility;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import io.socket.client.Socket;


/**
 * Created by fabio on 08/09/17.
 */
public class BackgroundService extends IntentService {

    static String threadName = null;
    private Handler handlergps = new Handler(Looper.getMainLooper());

    static IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
    BroadcastReceiver mReceiver = new DisplayHandler();

    private Thread t = null;

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
        //db.clearSendData();

        TransfertData transfertData = new TransfertData(getApplicationContext());
        transfertData.send();

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
                                            //if (NetStatsHandler.checkTimeBetweenRequest(getApplicationContext())){
                                            ArrayList<NetStats> netStatsArrayList = NetStatsHandler.readNetworkStats(getApplicationContext());
                                            NetStatsHandler.saveNetStatsArray(netStatsArrayList, getApplicationContext());
                                            NetStatsHandler.setNetxTime(getApplicationContext());
                                            //}

                                            break;

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
