package com.example.fabio.crowdpulse.delete;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ListView;

import com.example.fabio.crowdpulse.R;
import com.example.fabio.crowdpulse.config.Constants;
import com.example.fabio.crowdpulse.utility.NotificationUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ControlPanel3 extends Activity {

    static Boolean confirmExit = false;
    ListView listView;

    static AdapterSettingItem adapter;
    List<Map<String, Object>> data;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.control_panel3);


        displaySettings();


    }

    public void displaySettings(){

        listView = (ListView) findViewById(R.id.settingList);

        /*
        String[] from = SettingItem.getAdmissibleValues();
        int[] to = new int[] {R.id.switchSetting, R.id.seekBarSetting, R.id.textSeekbarStart,
                R.id.textSeekbarEnd, R.id.textEvery, R.id.textSeekbarCurrent,R.id.textSeekbarIntervalType};
         */
        String[] from = new String[]{};

        int[] to = new int[] {};

        int nativeLayout = R.layout.setting_item;

        data = new ArrayList<Map<String, Object>> ();

        for (int i = 0; i < Constants.settingItemList.size(); i++){

            data.add(Constants.settingItemList.get(i).getMap());
        }

        adapter = new AdapterSettingItem(this, data, nativeLayout , from, to);

        listView.setAdapter(adapter);


    }


    public static void updateItemListView(){

        adapter.notifyDataSetChanged();

    }











    @Override
    public void onBackPressed() {

        if(confirmExit){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else {

            confirmExit = true;

            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    confirmExit = false;
                }
            }, 2000);

            NotificationUtility.showToast(getApplicationContext(), getString(R.string.press_again_exit));
        }
    }


}



