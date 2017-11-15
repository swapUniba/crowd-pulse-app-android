package com.swapuniba.crowdpulse.handlers;

import android.app.usage.NetworkStats;
import android.app.usage.NetworkStatsManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;

import com.swapuniba.crowdpulse.business_object.AbstractData;
import com.swapuniba.crowdpulse.business_object.NetStats;
import com.swapuniba.crowdpulse.config.Constants;
import com.swapuniba.crowdpulse.database.DbManager;
import com.swapuniba.crowdpulse.utility.Utility;

import java.util.ArrayList;

/**
 * Created by fabio on 15/09/17.
 */

public class NetStatsHandler {

    public static ArrayList<NetStats> readNetworkStats(Context context) {

        ArrayList<NetStats> netStatsArray = new ArrayList<NetStats>();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            NetworkStatsManager service = context.getSystemService(NetworkStatsManager.class);

            try {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

                ArrayList<Long> intervaTimeArrayList =
                        Utility.splitTime(Utility.yesterdayMidnightTimestamp(),
                                Utility.currentMidnightTimestamp(),
                                Long.parseLong(preferences.getString(Constants.setting_time_read_netstats, "0")));

                for(int i = 0; i < intervaTimeArrayList.size() - 1; i++) {

                    TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);

                    NetStats netStats_mobile = new NetStats();

                    NetworkStats.Bucket b = service.querySummaryForDevice(ConnectivityManager.TYPE_MOBILE, tm.getSubscriberId(),
                            intervaTimeArrayList.get(i), intervaTimeArrayList.get(i+1));

                    netStats_mobile.networkType = Constants.TYPE_MOBILE;
                    netStats_mobile.timestamp = b.getStartTimeStamp() + "";
                    netStats_mobile.rxBytes = b.getRxBytes() + "";
                    netStats_mobile.txBytes = b.getTxBytes() + "";
                    netStats_mobile.send = false;


                    //netStats_mobile.print();

                    netStatsArray.add(netStats_mobile);

                    NetStats netStats_wifi = new NetStats();

                    b = service.querySummaryForDevice(ConnectivityManager.TYPE_WIFI, "",
                            intervaTimeArrayList.get(i), intervaTimeArrayList.get(i+1));

                    netStats_wifi.networkType = Constants.TYPE_WIFI;
                    netStats_wifi.timestamp = b.getStartTimeStamp() + "";
                    netStats_wifi.rxBytes = b.getRxBytes() + "";
                    netStats_wifi.txBytes = b.getTxBytes() + "";
                    netStats_wifi.send = false;

                    //netStats_wifi.print();

                    netStatsArray.add(netStats_wifi);
                }

            } catch (RemoteException e) {
                e.printStackTrace();
                netStatsArray = null;
            }

        }

        return netStatsArray;
    }


    /**
     * check if is pass the right time between 2 get
     * @param context
     * @return
     */
    public static Boolean checkTimeBetweenRequest(Context context){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        return  Long.parseLong(preferences.getString(Constants.last_netstats_send, "0")) < System.currentTimeMillis();

    }

    /**
     * check the next time to register the data
     * @param context
     * @return
     */
    public static void setNetxTime(Context context){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        //use the last interval to calcolate the netx time relevation
        ArrayList<Long> intervaTimeArrayList =
                Utility.splitTime(Utility.yesterdayMidnightTimestamp(),
                        Utility.currentMidnightTimestamp(),
                        Long.parseLong(preferences.getString(Constants.setting_time_read_netstats, "0")));

        Long netxTime = Long.parseLong(preferences.getString(Constants.setting_time_read_netstats, "0")) +
                intervaTimeArrayList.get(intervaTimeArrayList.size()-1);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.last_netstats_send, netxTime+ "");
        editor.apply();

    }

    public static Boolean saveNetStats(NetStats netStats, Context context){
        Boolean done = true;

        DbManager db = new DbManager(context);

        NetStats dbNetStat = db.getNetStat(netStats.networkType, netStats.timestamp);

        if (dbNetStat == null){
            done = db.saveNetStats(netStats);
        }
        else {
            //if false nothing to update
            if (netStats.send!=false){
                done = db.updateNetStats(netStats);
            }
        }

        netStats.print();


        return done;
    }


    public static Boolean saveNetStatsArray(ArrayList<NetStats> netStatsArrayList, Context context){
        Boolean done = true;

        for (NetStats netStats : netStatsArrayList){
            saveNetStats(netStats, context);
        }

        return done;

    }

    public static ArrayList<? extends AbstractData> getNotSendNetStats(Context context){
        DbManager dbManager = new DbManager(context);
        return dbManager.getNotSendNetStats();
    }

}
