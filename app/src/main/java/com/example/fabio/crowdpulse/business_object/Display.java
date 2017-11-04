package com.example.fabio.crowdpulse.business_object;

import android.util.Log;

import com.example.fabio.crowdpulse.config.Constants;
import com.example.fabio.crowdpulse.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by fabio on 10/09/17.
 */

public class Display extends AbstractData{

    public String timestamp;
    public String state;
    public Boolean send;

    public Display(){}

    public Display(String timestamp, String state, Boolean send){
        this.timestamp = timestamp;
        this.state = state;
        this.send = send;
    }

    @Override
    public String getSource() {
        return Constants.j_type_display;
    }

    @Override
    public JSONObject toJSON(){

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put(Constants.j_source_type, getSource());
            jsonObject.put(Constants.j_display_timestamp, Long.parseLong(timestamp));
            jsonObject.put(Constants.j_display_state, state);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }

    @Override
    public void fromJSON(JSONObject jsonObject){

        try {
            this.timestamp = jsonObject.getString(Constants.j_display_timestamp);
            this.state = jsonObject.getString(Constants.j_display_state);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * used for debug
     * @return
     */
    public void print() {
        String separator = "---";
        String s = "AppInfo: ";
        s += "timestamp: " + timestamp + separator;
        s += "state: " + state + separator;

        Utility.printLog(s);
    }

}
