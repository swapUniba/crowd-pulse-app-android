package com.example.fabio.crowdpulse.delete;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;

import com.example.fabio.crowdpulse.R;
import com.example.fabio.crowdpulse.config.Constants;
import com.example.fabio.crowdpulse.config.SettingFile;
import com.example.fabio.crowdpulse.utility.Utility;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by fabio on 10/10/17.
 */

public class AdapterSettingItem extends SimpleAdapter {
    Context context;
    List<? extends Map<String, ?>> data;
    SharedPreferences preferences;
    /**
     * Constructor
     *
     * @param context  The context where the View associated with this SimpleAdapter is running
     * @param data     A List of Maps. Each entry in the List corresponds to one row in the list. The
     *                 Maps contain the data for each row, and should include all the entries specified in
     *                 "from"
     * @param resource Resource identifier of activity view layout that defines the views for this list
     *                 item. The layout file should include at least those named views defined in "to"
     * @param from     A list of column names that will be added to the Map associated with each
     *                 item.
     * @param to       The views that should display column in the "from" parameter. These should all be
     *                 TextViews. The first N views in this list are given the values of the first N columns
     */
    public AdapterSettingItem(Context context, List<? extends Map<String, ?>> data, int resource, String[] from, int[] to) {
        super(context, data, resource, from, to);
        this.data = data;
        this.context = context;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    /**
     * Display rows in alternating colors
     */
    @Override
    public View getView(final int position, final View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        final String setting_key = data.get(position).get("settingKey").toString();

        //BUTTON ENABLE/DISABLE PERMISSION
        Switch switchButton = (Switch) view.findViewById(R.id.switchSetting);
        if (SettingFile.getSettings(context).get(setting_key).equalsIgnoreCase(Constants.record)) {
            switchButton.setChecked(true);
        } else {
            switchButton.setChecked(false);
        }
        switchButton.setText(setting_key);
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    SettingFile.setSetting(setting_key, Constants.record, context, Constants.sourceThisSmarthone);
                } else {
                    SettingFile.setSetting(setting_key, Constants.no_record, context, Constants.sourceThisSmarthone);
                }

                ControlPanel3.updateItemListView();
            }
        });
        //END BUTTON ENABLE/DISABLE PERMISSION


        //SEEDK BAR
        final ArrayList<Integer> admissibleValues = (ArrayList<Integer>) data.get(position).get("admissibleValues");

        LinearLayout layoutSeekBar = (LinearLayout) view.findViewById(R.id.layoutSeekBar);
        LinearLayout layoutSeekBarText = (LinearLayout) view.findViewById(R.id.layoutSeekBarText);

        if (admissibleValues.size() <= 0){
            layoutSeekBar.setVisibility(view.GONE);
            layoutSeekBarText.setVisibility(view.GONE);
        }
        else {

            layoutSeekBar.setVisibility(view.VISIBLE);
            layoutSeekBarText.setVisibility(view.VISIBLE);

            SeekBar seekbar = (SeekBar) view.findViewById(R.id.seekBarSetting);

            seekbar.setMax(admissibleValues.get(0));
            seekbar.setMax(admissibleValues.get(admissibleValues.size()-1));


            final TextView textSeekbarCurrent = (TextView) view.findViewById(R.id.textSeekbarCurrent);

            seekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    textSeekbarCurrent.setText(progress + "");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                    if(!admissibleValues.contains(seekBar.getProgress())){
                        int i = 0;
                        while(admissibleValues.get(i) < seekBar.getProgress()){
                            ++i;
                        }
                        seekBar.setProgress(admissibleValues.get(i));

                    }


                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putString(Constants.mappingSettingTimeRead.get(setting_key),
                            seekBar.getProgress()*Utility.millisecondInUinit(data.get(position).get("type").toString()) + "");
                    editor.apply();

                    ControlPanel3.updateItemListView();
                }
            });

            int progress = Integer.parseInt(preferences.getString(Constants.mappingSettingTimeRead.get(setting_key), Constants.default_setting.get(setting_key)));

            progress = progress / Utility.millisecondInUinit(data.get(position).get("type").toString());

            seekbar.setProgress(progress);



            TextView textSeekbarIntervalType = (TextView) view.findViewById(R.id.textSeekbarIntervalType);
            textSeekbarIntervalType.setText(data.get(position).get("type").toString());

            TextView textSeekbarStart = (TextView) view.findViewById(R.id.textSeekbarStart);
            textSeekbarStart.setText(admissibleValues.get(0) +"");
            TextView textSeekbarEnd = (TextView) view.findViewById(R.id.textSeekbarEnd);
            textSeekbarEnd.setText(admissibleValues.get(admissibleValues.size()-1) + "");

            //TextView textEvery = (TextView) view.findViewById(R.id.textEvery);
        }

        if (position % 2 == 1) {
            view.setBackgroundColor(Color.GRAY);
        } else {
            view.setBackgroundColor(Color.WHITE);
        }

        return view;
    }




}

