package com.swapuniba.crowdpulse.utility;

import android.text.TextUtils;
import android.util.Log;

import com.swapuniba.crowdpulse.config.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Random;

/**
 * Created by fabio on 09/09/17.
 */

public class Utility {

    public static void printLog(String message){
        if(Constants.print_log){
            Log.i(Constants.log_tag, message);
        }
    }
    public static void printLog(String tag, String message){
        if(Constants.print_log){
            Log.i(tag, message);
        }
    }

    /**
     * today midnight
     * @return
     */
    public static long currentMidnightTimestamp(){

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        cal.add(Calendar.DAY_OF_YEAR, 0);

        return cal.getTimeInMillis() + cal.getTimeZone().getRawOffset();

    }

    public static long yesterdayMidnightTimestamp(){

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        cal.add(Calendar.DAY_OF_YEAR, -1);

        return cal.getTimeInMillis() + cal.getTimeZone().getRawOffset();
    }


    public static String threeDaysAgoTimestamp(){

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        cal.add(Calendar.DAY_OF_YEAR, -3);

        long t = cal.getTimeInMillis() + cal.getTimeZone().getRawOffset();

        return t + "";
    }


    static public ArrayList<Long> splitTime(Long startTime, Long endTime, Long interval){
        ArrayList<Long> intervalTimeArrayList = new ArrayList<Long>();

        while (endTime >= startTime){
            intervalTimeArrayList.add(startTime);
            startTime += interval;
        }

        return intervalTimeArrayList;
    }


    static String ARRAY_DIVIDER = "#xix#";


    static public String serialize(ArrayList<String> content){
        return TextUtils.join(ARRAY_DIVIDER, content);
    }

    static public ArrayList<String> derialize(String content){
        return new ArrayList<String>(Arrays.asList(content.split(ARRAY_DIVIDER)));
    }


    static public String randomString(){
        char[] chars = "abcdefghijklmnopqrstuvwxyzBCDEFGHIJKLMNOPQRSTUVWXYZ0123456789".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        return sb.toString();
    }


    static public int millisecondInUinit(String unit){

        switch (unit){
            case Constants.type_minute:
                return 60000;

            case Constants.type_hour:
                return 3600000;

            case Constants.type_day:
                return 86400000;

            default:
                return 0;
        }

    }


}
