package com.example.fabio.crowdpulse.config;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.fabio.crowdpulse.comunication.SocketApplication;
import com.example.fabio.crowdpulse.handlers.DeviceInfoHandler;
import com.example.fabio.crowdpulse.utility.Utility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import io.socket.client.Socket;

/**
 * Created by fabio on 09/09/17.
 */

public class SettingFile {


    public static HashMap<String, String> getSettings(Context context){

        HashMap<String, String> settings_map= new HashMap<String, String>();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);

        for (String preference : Constants.setting_keys){
            //if (Constants.setting_permission_keys.contains(preference)){
                settings_map.put(preference, preferences.getString(preference, Constants.default_setting.get(preference)));
            //}
        }

        return settings_map;

    }

    /**
     *
     * @param key
     * @param value
     * @param context
     * @param source 0 if from this smarthone, 1 from UI
     * @return
     */
    public static Boolean setSetting(String key, String value, Context context, int source){

        Boolean is_valid = false;

        if (Constants.setting_keys.contains(key)){
            //TODO check value param consistence

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, value);
            editor.apply();

            Utility.printLog("Update default_setting " + key + " with value: " + value);

            Utility.printLog(getSettings(context).toString());

            is_valid = true;
        }

        if (is_valid){
            if(source == Constants.sourceThisSmarthone){
                Socket socket = SocketApplication.getSocket();
                socket.emit(Constants.channel_config, getJSON(context));
            }
        }


        return is_valid;

    }



    public static void inizialize(Context context){

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();


        for(String setting_key :Constants.setting_keys){
            String value = preferences.getString(setting_key, "");
            if (value.equalsIgnoreCase("")){
                editor.putString(setting_key, Constants.default_setting.get(setting_key));
            }
        }
        editor.apply();

    }

    public static JSONObject getJSON(Context context){

        JSONObject jsonObject = new JSONObject();



        HashMap<String, String> settings = getSettings(context);
        try {
            for (String settingKey : settings.keySet()){
                    jsonObject.put(settingKey, settings.get(settingKey));
                }
            jsonObject.put(Constants.j_deviceinfo_deviceId, DeviceInfoHandler.readDeviceInfo(context).deviceId);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            jsonObject.put(Constants.j_displayName, preferences.getString(Constants.pref_displayName, ""));

            } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsonObject;

    }





}
