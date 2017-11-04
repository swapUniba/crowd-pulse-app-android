package com.example.fabio.crowdpulse.utility;


import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.widget.Toast;

public class NotificationUtility {

    public static void showToast(Context context, String mess) {

        Toast toast = Toast.makeText(context, mess, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();

    }

    public static void showToastSocket(final Context context, final String mess) {

        Handler mHandler = new Handler(Looper.getMainLooper());

        mHandler.post(new Runnable(){

            @Override
            public void run (){
                Toast toast = Toast.makeText(context, mess, Toast.LENGTH_SHORT);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        });



    }



}