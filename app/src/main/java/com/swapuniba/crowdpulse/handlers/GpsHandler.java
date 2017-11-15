package com.swapuniba.crowdpulse.handlers;


import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.swapuniba.crowdpulse.business_object.AbstractData;
import com.swapuniba.crowdpulse.business_object.GPS;
import com.swapuniba.crowdpulse.config.Constants;
import com.swapuniba.crowdpulse.database.DbManager;
import com.swapuniba.crowdpulse.utility.Utility;

import java.util.ArrayList;
import java.util.List;

//import io.socket.client.Ack;
//import io.socket.client.Socket;

/**
 *
 */
public class GpsHandler {


    private static LocationManager lManager;
    private static LocationListener lListener;
    private static Location location = null;

    //Per ottenere coordinate gps
    private static void getCurrentLocation(Application application) {

        lManager = (LocationManager) application.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        lListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location arg0) {
                location = arg0;
            }

            @Override
            public void onProviderDisabled(String arg0) {
            }

            @Override
            public void onProviderEnabled(String arg0) {
            }

            @Override
            public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
            }
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (application.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    application.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Utility.printLog("No GPS permission!");
                return;
            }
        }

        lManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, lListener);

        //if no one location is obtained, use the location stored from other app
        List<String> providers = lManager.getProviders(true);
        for (String provider : providers) {
            lManager.requestLocationUpdates(provider, 1000, 0,
                    new LocationListener() {

                        public void onLocationChanged(Location location) {
                        }

                        public void onProviderDisabled(String provider) {
                        }

                        public void onProviderEnabled(String provider) {
                        }

                        public void onStatusChanged(String provider, int status,
                                                    Bundle extras) {
                        }
                    });
            if(lManager.getLastKnownLocation(provider)!= null){
                location = lManager.getLastKnownLocation(provider);
            }

        }
    }


    public static GPS readGPS(Application application) {

        GPS gps = new GPS();

        getCurrentLocation(application);

        if (location != null) {

            gps.timestamp = String.valueOf(location.getTime());
            gps.latitude = String.valueOf(location.getLatitude());
            gps.longitude = String.valueOf(location.getLongitude());
            gps.speed = String.valueOf(location.getSpeed());
            gps.accuracy = String.valueOf(location.getAccuracy());
            gps.send = false;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (application.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        application.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Utility.printLog("No GPS permission!");
                    return null;
                }
            }
            lManager.removeUpdates(lListener);

        }
        else{
            Utility.printLog("Error: Gps timestamp is null");
            gps = null;
        }

        return gps;

    }

    /**
     * check if is pass the right time between 2 get
     * @param context
     * @return
     */
    public static Boolean checkTimeBetweenRequest(Context context){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        return  Long.parseLong(preferences.getString(Constants.last_gps_send, "0")) < System.currentTimeMillis();

    }

    /**
     * check the next time to register the data
     * @param context
     * @return
     */
    public static void setNetxTime(Context context){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        Long netxTime = Long.parseLong(preferences.getString(Constants.setting_time_read_gps, "0")) + System.currentTimeMillis();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.last_gps_send, netxTime+ "");
        editor.apply();

    }

    public static Boolean saveGPS(GPS gps, Context context){

        Boolean done = false;
        DbManager db = new DbManager(context);

        GPS db_gps = db.getGPS(gps.timestamp);

        if(db_gps == null){
            done = db.saveGps(gps);
        }
        else {
            done = db.updateGPS(gps);
        }

        return done;

    }

    public static ArrayList<? extends AbstractData> getNotSendGPS(Context context){
        DbManager dbManager = new DbManager(context);
        return dbManager.getNotSendGPS();
    }

}



