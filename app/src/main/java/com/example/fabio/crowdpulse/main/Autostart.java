package com.example.fabio.crowdpulse.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.example.fabio.crowdpulse.database.DbManager;
import com.example.fabio.crowdpulse.handlers.NetStatsHandler;

/**
 * Created by fabio on 17/09/17.
 */

public class Autostart extends BroadcastReceiver
{
    public void onReceive(Context context, Intent arg1){

        Intent intent = new Intent(context,BackgroundService.class);
        context.startService(intent);

    }

}