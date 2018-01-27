package com.swapuniba.crowdpulse.comunication;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.swapuniba.crowdpulse.business_object.AbstractData;
import com.swapuniba.crowdpulse.business_object.Account;
import com.swapuniba.crowdpulse.business_object.ActivityData;
import com.swapuniba.crowdpulse.business_object.AppInfo;
import com.swapuniba.crowdpulse.business_object.Contact;
import com.swapuniba.crowdpulse.business_object.Display;
import com.swapuniba.crowdpulse.business_object.GPS;
import com.swapuniba.crowdpulse.business_object.NetStats;
import com.swapuniba.crowdpulse.config.Constants;
import com.swapuniba.crowdpulse.handlers.AccountHandler;
import com.swapuniba.crowdpulse.handlers.ActivityHandler;
import com.swapuniba.crowdpulse.handlers.AppInfoHandler;
import com.swapuniba.crowdpulse.handlers.ContactHandler;
import com.swapuniba.crowdpulse.handlers.DeviceInfoHandler;
import com.swapuniba.crowdpulse.handlers.DisplayHandler;
import com.swapuniba.crowdpulse.handlers.GpsHandler;
import com.swapuniba.crowdpulse.handlers.NetStatsHandler;
import com.swapuniba.crowdpulse.utility.NotificationUtility;
import com.swapuniba.crowdpulse.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Created by fabio on 05/10/17.
 */

public class TransfertData {

    JSONObject jsonObject = new JSONObject();
    String dataIdentifier = null;
    static Context context;

    //ArrayList<AbstractData> abstractDatas = new ArrayList<AbstractData>();

    public TransfertData(Context context){
        this.context = context;
        dataIdentifier =  Utility.randomString();
        try {
            jsonObject.put(Constants.j_deviceinfo_deviceId, DeviceInfoHandler.readDeviceInfo(context).deviceId);
            jsonObject.put(Constants.j_dataIdentifier, dataIdentifier);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            jsonObject.put(Constants.j_username, preferences.getString(Constants.pref_username, ""));
            jsonObject.put(Constants.j_data, new JSONArray());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        setData();

    }

    public void addList(ArrayList<AbstractData> arrayList ){

        if (arrayList != null && !arrayList.isEmpty()){
            JSONArray jsonArray = null;
            try {
                jsonArray = jsonObject.getJSONArray(Constants.j_data);

                for (AbstractData abstractData : arrayList){
                    jsonArray.put(abstractData.toJSON());

                    //abstractDatas.add(abstractData);
                }
                jsonObject.put(Constants.j_data, jsonArray);


            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void add(AbstractData abstractData){
        JSONArray jsonArray = null;
        try {
            jsonArray = jsonObject.getJSONArray(Constants.j_data);

            jsonArray.put(abstractData.toJSON());

            jsonObject.put(Constants.j_data, jsonArray);

            //abstractDatas.add(abstractData);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * take the jsonArray and set as send all items represented on the internal DB,
     * this function is used when the sent at the server is done rigth
     */
    void setSent(){
        JSONArray jsonArray = null;
        try {
            jsonArray = jsonObject.getJSONArray(Constants.j_data);

            for (int i = 0; i < jsonArray.length(); i++){
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String source_type = jsonObject.getString(Constants.j_source_type);

                switch (source_type){
                    case Constants.j_type_account:
                        Account account = new Account();
                        account.fromJSON(jsonObject);
                        account.send = true;

                        AccountHandler.saveAccount(account, context);

                        break;

                    case Constants.j_type_appinfo:
                        AppInfo appInfo = new AppInfo();
                        appInfo.fromJSON(jsonObject);
                        appInfo.send = true;

                        AppInfoHandler.saveAppInfo(appInfo, context);

                        break;

                    case Constants.j_type_contact:
                        Contact contact = new Contact();
                        contact.fromJSON(jsonObject);
                        contact.send = true;

                        ContactHandler.saveContact(contact, context);

                        break;

                    case Constants.j_type_display:
                        Display display = new Display();
                        display.fromJSON(jsonObject);
                        display.send = true;

                        DisplayHandler.saveDisplay(display, context);

                        break;

                    case Constants.j_type_gps:
                        GPS gps = new GPS();
                        gps.fromJSON(jsonObject);
                        gps.send = true;

                        GpsHandler.saveGPS(gps, context);

                        break;

                    case Constants.j_type_netstats:
                        NetStats netStats = new NetStats();
                        netStats.fromJSON(jsonObject);
                        netStats.send = true;

                        NetStatsHandler.saveNetStats(netStats, context);

                        break;

                    case Constants.j_type_activity:
                        ActivityData activity = new ActivityData();
                        activity.fromJSON(jsonObject);
                        activity.send = true;

                        ActivityHandler.saveActivity(activity, context);

                        break;


                    default:
                        break;
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * add here all the item to send
     */
    void setData(){

        //ACCOUNT
        ArrayList a = AccountHandler.getNotSendAccount(context);
        addList(a);
        //APPINFO
        a = AppInfoHandler.getNotSendAppInfo(context);
        addList(a);
        //CONTACT
        a = ContactHandler.getNotSendContact(context);
        addList(a);
        //DISPLAY
        a = DisplayHandler.getNotSendDisplay(context);
        addList(a);
        //GPS
        a = GpsHandler.getNotSendGPS(context);
        addList(a);
        //NETSTATS
        a = NetStatsHandler.getNotSendNetStats(context);
        addList(a);
        //ACTIVITY
        a = ActivityHandler.getNotSendActivity(context);
        addList(a);

    }

    public Boolean send(){

        Boolean send = true;
        try {
            if(jsonObject.getString(Constants.j_username).equalsIgnoreCase("")){
                send = false;
            }
        } catch (JSONException e) {
            e.printStackTrace();
            send = false;
        }

        if(send){
            Socket socket = SocketApplication.getSocket();

            socket.emit(Constants.channel_send_data, jsonObject);

            Utility.printLog("data send" + jsonObject.toString());

            Emitter.Listener onDataSend = new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    try {
                        JSONObject data = (JSONObject) args[0];
                        if(data.getInt(Constants.j_code) == Constants.response_success){
                            if (data.getString(Constants.j_dataIdentifier).equalsIgnoreCase(dataIdentifier)){
                                NotificationUtility.showToastSocket(context, "Data recived");
                                setSent();
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            };

            socket.on(Constants.channel_send_data, onDataSend);

        }

        return true;

    }




}
