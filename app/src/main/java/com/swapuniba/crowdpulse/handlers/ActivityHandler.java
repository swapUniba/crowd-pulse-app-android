package com.swapuniba.crowdpulse.handlers;


import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.swapuniba.crowdpulse.business_object.AbstractData;
import com.swapuniba.crowdpulse.business_object.ActivityData;
import com.swapuniba.crowdpulse.business_object.GPS;
import com.swapuniba.crowdpulse.config.Constants;
import com.swapuniba.crowdpulse.database.DbManager;
import com.swapuniba.crowdpulse.utility.Utility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import io.socket.client.Ack;
//import io.socket.client.Socket;

/**
 *
 */
public class ActivityHandler {

    public static ActivityData readActivity(Intent intent, Context context, GoogleApiClient mApiClient) {
        ActivityData activityData = null;
        PendingIntent pendingIntent = PendingIntent.getService( context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 0, pendingIntent );

        if(ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            activityData = handleDetectedActivities( result.getProbableActivities() );
        }

        return activityData;

    }

    private static ActivityData handleDetectedActivities(List<DetectedActivity> probableActivities) {
        ActivityData activityData = new ActivityData();

        activityData.timestamp = System.currentTimeMillis() + "";

        for( DetectedActivity activity : probableActivities ) {
            switch( activity.getType() ) {
                case DetectedActivity.IN_VEHICLE: {
                    Utility.printLog("In Vehicle: " + activity.getConfidence() );
                    activityData.inVehicle = activity.getConfidence() + "";
                    break;
                }
                case DetectedActivity.ON_BICYCLE: {
                    Utility.printLog("On Bicycle: " + activity.getConfidence() );
                    activityData.onBicycle = activity.getConfidence() + "";
                    break;
                }
                case DetectedActivity.ON_FOOT: {
                    Utility.printLog("On Foot: " + activity.getConfidence() );
                    activityData.onFoot = activity.getConfidence() + "";
                    break;
                }
                case DetectedActivity.RUNNING: {
                    Utility.printLog("Running: " + activity.getConfidence() );
                    activityData.running = activity.getConfidence() + "";
                    break;
                }
                case DetectedActivity.STILL: {
                    Utility.printLog("Still: " + activity.getConfidence() );
                    activityData.still = activity.getConfidence() + "";
                    break;
                }
                case DetectedActivity.TILTING: {
                    Utility.printLog("Tilting: " + activity.getConfidence() );
                    activityData.tilting = activity.getConfidence() + "";
                    break;
                }
                case DetectedActivity.WALKING: {
                    Utility.printLog("Walking: " + activity.getConfidence() );
                    activityData.walking = activity.getConfidence() + "";
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    Utility.printLog("Unknown: " + activity.getConfidence() );
                    activityData.unknown = activity.getConfidence() + "";
                    break;
                }
            }
        }

        activityData.send = false;

        return activityData;

    }

    /**
     * check if is pass the right time between 2 get
     * @param context
     * @return
     */
    public static Boolean checkTimeBetweenRequest(Context context){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        return  Long.parseLong(preferences.getString(Constants.last_activity_send, "0")) < System.currentTimeMillis();

    }

    /**
     * check the next time to register the data
     * @param context
     * @return
     */
    public static void setNetxTime(Context context){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        Long netxTime = Long.parseLong(preferences.getString(Constants.setting_time_read_activity, "0")) + System.currentTimeMillis();

        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(Constants.last_activity_send, netxTime+ "");
        editor.apply();

    }

    public static Boolean saveActivity(ActivityData activity, Context context){

        Boolean done = false;
        DbManager db = new DbManager(context);

        ActivityData db_activity = db.getActivity(activity.timestamp);

        if(db_activity == null){
            done = db.saveActivity(activity);
        }
        else {
            done = db.updateActivity(activity);
        }

        return done;

    }

    public static ArrayList<? extends AbstractData> getNotSendActivity(Context context){
        DbManager dbManager = new DbManager(context);
        return dbManager.getNotSendActivity();
    }

}



