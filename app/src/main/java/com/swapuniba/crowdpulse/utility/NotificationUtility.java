package com.swapuniba.crowdpulse.utility;


import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.widget.Toast;

import com.swapuniba.crowdpulse.R;

public class NotificationUtility {

  private static final String APP_NAME = "CrowdPulse";

  public static void showToast(Context context, String mess) {
    Toast toast = Toast.makeText(context, mess, Toast.LENGTH_SHORT);
    toast.setGravity(Gravity.CENTER, 0, 0);
    toast.show();
  }

  public static void showToastSocket(final Context context, final String mess) {
    Handler mHandler = new Handler(Looper.getMainLooper());
    mHandler.post(new Runnable() {

      @Override
      public void run() {
        Toast toast = Toast.makeText(context, mess, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
      }
    });
  }

  public static void sendNotification(final Context context, final String mess) {
    Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

    //Get an instance of NotificationManager
    NotificationCompat.Builder mBuilder =
        new NotificationCompat.Builder(context)
            .setSmallIcon(R.drawable.logo)
            .setContentTitle(APP_NAME)
            .setContentText(mess)
            .setSound(soundUri);

    // Gets an instance of the NotificationManager service
    NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    mNotificationManager.notify(001, mBuilder.build());
  }



}