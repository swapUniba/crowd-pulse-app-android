package com.swapuniba.crowdpulse.main;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

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