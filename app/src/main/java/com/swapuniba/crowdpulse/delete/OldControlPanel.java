package com.swapuniba.crowdpulse.delete;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.swapuniba.crowdpulse.R;
import com.swapuniba.crowdpulse.config.Constants;
import com.swapuniba.crowdpulse.config.SettingFile;
import com.swapuniba.crowdpulse.handlers.AccountHandler;
import com.swapuniba.crowdpulse.utility.NotificationUtility;
import com.swapuniba.crowdpulse.utility.Utility;

import java.util.ArrayList;
import java.util.HashMap;


public class OldControlPanel extends Activity {

    static Boolean confirmExit = false;
    TextView textView7;


    class SettingItem {
        Boolean enabled;
        Integer seekBarValue;
        ArrayList<Integer> admissibleValues;

        void SettingItem(Boolean enabled, Integer seekBarValue, ArrayList<Integer> admissibleValues){
            this.enabled = enabled;
            this.seekBarValue = seekBarValue;
            this.admissibleValues = admissibleValues;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.old_control_panel);

        //BUTTON ENABLE/DISABLE PERMISSION
        HashMap<String, String> settings = SettingFile.getSettings(getApplicationContext());


        ArrayList<Integer> switches_resources = new ArrayList<Integer>() {{
            add(R.id.switch_gps);
            add(R.id.switch_contacts);
            add(R.id.switch_accounts);
            add(R.id.switch_calendar);
            add(R.id.switch_sms);
            add(R.id.switch_app);
            add(R.id.switch_netstats);
            add(R.id.switch_display);
        }};





        ArrayList<Switch> switches = new ArrayList<Switch>();

        for (int i = 0; i < Constants.setting_permission_keys.size(); i++) {

            final String setting_key = Constants.setting_permission_keys.get(i);

            Switch switchButton = (Switch) findViewById(switches_resources.get(i));
            if (settings.get(setting_key).equalsIgnoreCase(Constants.record)) {
                switchButton.setChecked(true);
            } else {
                switchButton.setChecked(false);
            }
            switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        SettingFile.setSetting(setting_key, Constants.record, getApplicationContext(), Constants.sourceThisSmarthone);
                    } else {
                        SettingFile.setSetting(setting_key, Constants.no_record, getApplicationContext(), Constants.sourceThisSmarthone);
                    }
                }
            });

            switches.add(switchButton);

        }
        //END BUTTON ENABLE/DISABLE PERMISSION




        SeekBar seekbar = (SeekBar) findViewById(R.id.seekBar2);
        textView7 = (TextView) findViewById(R.id.textView7);

        final ArrayList<Integer> avaiable = new ArrayList<>();
        avaiable.add(1);
        avaiable.add(3);
        avaiable.add(5);
        avaiable.add(7);
        avaiable.add(9);

        seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                Utility.printLog("progress" + progress );
                textView7.setText(progress + "");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(!avaiable.contains(seekBar.getProgress())){
                    seekBar.setProgress(0);
                }
            }
        });









        test();

    }

    void test(){

        //NetStatsHandler.readNetworkStats(getApplicationContext());
        //AppInfoHandler.readAppInfo(getApplicationContext());
        //DeviceInfoHandler.readDeviceInfo(getApplicationContext()).print();

        AccountHandler.readAccounts(getApplicationContext());
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



