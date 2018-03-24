package com.swapuniba.crowdpulse.main;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.swapuniba.crowdpulse.R;
import com.swapuniba.crowdpulse.business_object.DeviceInfo;
import com.swapuniba.crowdpulse.comunication.SocketApplication;
import com.swapuniba.crowdpulse.config.Constants;
import com.swapuniba.crowdpulse.config.SettingFile;
import com.swapuniba.crowdpulse.handlers.DeviceInfoHandler;
import com.swapuniba.crowdpulse.utility.NotificationUtility;
import com.swapuniba.crowdpulse.utility.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;


public class Login extends Activity {

    static Boolean confirmExit = false;

    EditText editTextUsername;
    EditText editTextPassword;
    EditText editTextPhoneNumber;

    ProgressBar progressBarLogin;

    SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.login);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        progressBarLogin = (ProgressBar) findViewById(R.id.progressLogin);
        progressBarLogin.setVisibility(View.VISIBLE);

        editTextUsername = (EditText) findViewById(R.id.editText_username);
        assert editTextUsername != null;
        editTextUsername.setText(preferences.getString(Constants.pref_email, ""));

        editTextPassword = (EditText) findViewById(R.id.editText_password);
        assert editTextPassword != null;
        editTextPassword.setText(preferences.getString(Constants.pref_password, ""));

        editTextPhoneNumber = (EditText) findViewById(R.id.editText_phoneNumber);
        assert editTextPhoneNumber != null;
        editTextPhoneNumber.setText(preferences.getString(Constants.pref_phoneNumber, ""));

        Button button_show_password = (Button) findViewById(R.id.button_show_password);
        button_show_password.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {

                switch ( event.getAction() ) {
                    case MotionEvent.ACTION_DOWN:
                        editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT);
                        break;
                    case MotionEvent.ACTION_UP:
                        editTextPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        break;
                }
                return true;
            }
        });


        final Button login_button = (Button) findViewById(R.id.button_login);
        login_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Socket socket = SocketApplication.getSocket();

                JSONObject jsonObject = new JSONObject();

                try {
                    jsonObject.put(Constants.j_email, editTextUsername.getText());
                    jsonObject.put(Constants.j_password, editTextPassword.getText());
                    DeviceInfo deviceInfo = DeviceInfoHandler.readDeviceInfo(getApplicationContext());
                    jsonObject.put(Constants.j_deviceinfo_deviceId,deviceInfo.deviceId);
                    jsonObject.put(Constants.j_deviceinfo_brand,deviceInfo.brand);
                    jsonObject.put(Constants.j_deviceinfo_sdk,deviceInfo.sdk);
                    jsonObject.put(Constants.j_deviceinfo_model,deviceInfo.model);

                    deviceInfo.phoneNumbers.add(editTextPhoneNumber.getText().toString());

                    JSONArray jsonArrayPhoneNumbers = new JSONArray();
                    for (String phoneNumber : deviceInfo.phoneNumbers){
                        jsonArrayPhoneNumbers.put(phoneNumber);
                    }

                    jsonObject.put(Constants.j_deviceinfo_phoneNumbers, jsonArrayPhoneNumbers);


                } catch (JSONException e) {
                    e.printStackTrace();
                }

                socket.emit(Constants.channel_login, jsonObject);

                Emitter.Listener onLogin = new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        try {

                            JSONObject data = (JSONObject) args[0];

                            final Context context = getApplicationContext();

                            // NotificationUtility.showToastSocket(context, data.getString(Constants.j_description));

                            if(data.getInt(Constants.j_code) == Constants.response_success){

                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putString(Constants.pref_username, data.getString(Constants.j_username));
                                editor.putString(Constants.pref_email, editTextUsername.getText().toString());
                                editor.putString(Constants.pref_password, editTextPassword.getText().toString());
                                editor.putString(Constants.pref_phoneNumber, editTextPhoneNumber.getText().toString());
                                editor.apply();

                                //IF IS THE FIRST TIME LOGGED
                                if (preferences.getBoolean(Constants.pref_firstLogin, false)){
                                    //SEND INITIAL CONFIG
                                    SocketApplication.getSocket().emit(Constants.channel_config, SettingFile.getJSON(getApplicationContext()));
                                }
                                else {
                                    JSONObject coingingJSON = new JSONObject();
                                    socket.emit(Constants.channel_config, coingingJSON);
                                }

                                startBackgroundService();
                                Intent intent = new Intent(getApplicationContext(), Main.class);
                                startActivity(intent);
                            }
                            else {
                                login_button.setEnabled(true);
                                progressBarLogin.setVisibility(View.GONE);
                            }

                            Utility.printLog(data.toString());

                        } catch (JSONException e) {
                            e.printStackTrace();
                            login_button.setEnabled(true);
                            progressBarLogin.setVisibility(View.GONE);
                        }
                    }

                };

                socket.on(Constants.channel_login, onLogin);

                //Intent intent = new Intent(getApplicationContext(), ControlPanel.class);
                //startActivity(intent);
            }
        });

        if (!preferences.getString(Constants.pref_password, "").equalsIgnoreCase("")){
            login_button.callOnClick();
            login_button.setEnabled(false);
        }
        else{
            progressBarLogin.setVisibility(View.GONE);
            login_button.setEnabled(true);
        }
        
    }


    private void startBackgroundService(){

        Intent mServiceIntent = new Intent(this, BackgroundService.class);
        mServiceIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        //Starts the IntentService
        startService(mServiceIntent);

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



