package com.example.fabio.crowdpulse.delete;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by fabio on 10/10/17.
 */

public class SettingItem {

    String settingKey;
    ArrayList<Integer> admissibleValues;
    String type;

    public SettingItem(String permissionKey,ArrayList<Integer> admissibleValues, String type){
        this.settingKey = permissionKey;
        this.admissibleValues = admissibleValues;
        this.type = type;
    }

    public HashMap<String, Object> getMap(){
        HashMap<String, Object> map = new HashMap<>();
        map.put("settingKey", settingKey);
        map.put("admissibleValues", admissibleValues);
        map.put("type", type);

        return map;
    }

    public static String[] getAdmissibleValues() {
        return new String[]{"settingKey", "admissibleValues", "type"};
    }

}