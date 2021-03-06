package com.swapuniba.crowdpulse.comunication;

import android.app.Application;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;

import com.swapuniba.crowdpulse.R;
import com.swapuniba.crowdpulse.business_object.DeviceInfo;
import com.swapuniba.crowdpulse.config.Constants;
import com.swapuniba.crowdpulse.config.SettingFile;
import com.swapuniba.crowdpulse.handlers.DeviceInfoHandler;
import com.swapuniba.crowdpulse.main.BackgroundService;
import com.swapuniba.crowdpulse.main.Intro;
import com.swapuniba.crowdpulse.utility.NotificationUtility;
import com.swapuniba.crowdpulse.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.Iterator;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class SocketApplication extends Application {

  static private Socket socket = null;

  static public Boolean sending = false; //Check if somone are sending data

  @Override
  public void onCreate() {
    super.onCreate();
    initializeSocket();
  }

  static public Socket getSocket() {
    return socket;
  }

  public void initializeSocket() {
    try {
      if (socket == null) {

        //Accessibile in all class
        Emitter.Listener onConnectError = new Emitter.Listener() {
          @Override
          public void call(Object... args) {
            //sendBroadcast(new Intent(Constants.erete));
            Utility.printLog(getResources().getString(R.string.connection_error).toString());
            // NotificationUtility.showToastSocket(getApplicationContext(), getResources().getString(R.string.connection_error).toString());

          }
        };

        Emitter.Listener onReload = new Emitter.Listener() {
          @Override
          public void call(Object... args) {
            Intent mServiceIntent = new Intent(getApplicationContext(), Intro.class);
            mServiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            // Starts the IntentService
            startService(mServiceIntent);
          }
        };

        Emitter.Listener onNewConfiguration = new Emitter.Listener() {
          @Override
          public void call(Object... args) {
            try {
              // NotificationUtility.showToastSocket(getApplicationContext(), "nuova configurazione");
              NotificationUtility.sendNotification(getApplicationContext(), "New configuration");


              JSONObject data = (JSONObject) args[0];
              if (data.getInt(Constants.j_code) == Constants.response_receiving) {
                data = data.getJSONObject(Constants.j_config_config);

                Iterator<String> keys = data.keys();
                while (keys.hasNext()) {
                  String setting = keys.next();
                  SettingFile.setSetting(
                      setting,
                      data.getString(setting),
                      getApplicationContext(),
                      Constants.sourceWebUI
                  );
                }
              }
            } catch (JSONException e) {
              e.printStackTrace();
            }

          }

        };

        Emitter.Listener onSendData = new Emitter.Listener() {
          @Override
          public void call(Object... args) {
            try {
              JSONObject data = (JSONObject) args[0];
              if (data.getInt(Constants.j_code) == Constants.response_receiving) {
                if (!sending) {
                  Intent mServiceIntent = new Intent(SocketApplication.this, BackgroundService.class);
                  mServiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

                  //Starts the IntentService
                  startService(mServiceIntent);
                } else {
                  Handler handler2 = new Handler(Looper.getMainLooper());
                  handler2.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                      sending = false;
                    }
                  }, 10000);
                }
              }
            } catch (JSONException e) {
              e.printStackTrace();
            }
          }

        };

        Emitter.Listener connesso = new Emitter.Listener() {
          @Override
          public void call(Object... args) {
            Utility.printLog("connect");
          }

        };
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String server_ip = preferences.getString(Constants.pref_server_ip, "");

        IO.Options option = new IO.Options();
        option.reconnection = true;
        if (server_ip.equalsIgnoreCase("")) {
          socket = IO.socket(Constants.SERVER_URL_MASTER, option);
        } else {
          socket = IO.socket(server_ip, option);
        }

        //register the function
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        socket.on(Constants.channel_reload, onReload);
        socket.on(Constants.channel_config, onNewConfiguration);
        socket.on(Constants.channel_send_data, onSendData);
        socket.on(Socket.EVENT_CONNECT, connesso);
        socket.connect();

      }
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

}

