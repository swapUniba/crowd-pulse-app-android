package com.example.fabio.crowdpulse.main;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;


import com.example.fabio.crowdpulse.R;
import com.example.fabio.crowdpulse.business_object.AppInfo;
import com.example.fabio.crowdpulse.config.Constants;
import com.example.fabio.crowdpulse.config.ControlPanel;
import com.example.fabio.crowdpulse.config.SettingFile;
import com.example.fabio.crowdpulse.handlers.AccountHandler;
import com.example.fabio.crowdpulse.handlers.AppInfoHandler;
import com.example.fabio.crowdpulse.utility.NotificationUtility;
import com.example.fabio.crowdpulse.utility.Utility;

import java.util.ArrayList;
import java.util.HashMap;


public class Main extends Activity {

    static Boolean confirmExit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        Button button = (Button) findViewById(R.id.button2);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ControlPanel.class);
                startActivity(intent);
            }
        });




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



