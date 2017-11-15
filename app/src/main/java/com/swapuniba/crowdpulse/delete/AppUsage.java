package com.swapuniba.crowdpulse.delete;

import android.app.Application;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;

import com.swapuniba.crowdpulse.utility.Utility;

import java.util.Calendar;
import java.util.List;

/**
 * Created by fabio on 12/09/17.
 */

public class AppUsage {


    public static void uso(Application application) {

        Utility.printLog("sono quiiiiii");

        UsageStatsManager mUsageStatsManager = (UsageStatsManager) application.getSystemService(Context.USAGE_STATS_SERVICE);


        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, -1);
        List<UsageStats> queryUsageStats = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            queryUsageStats = mUsageStatsManager
                    .queryUsageStats(UsageStatsManager.INTERVAL_DAILY, cal.getTimeInMillis(), System.currentTimeMillis());
            Utility.printLog("eccomiii");
            for (UsageStats u : queryUsageStats){

                Utility.printLog("*******************************************");
                Utility.printLog(u.getPackageName() + " tempo aperta: " + u.getTotalTimeInForeground()+"" +
                        " prima volta monitorata: " + u.getFirstTimeStamp()+ " ---" +
                        " ultima volta monitorata: " + u.getLastTimeStamp()+ " ---" +
                        " ultima volta attiva: " + u.getLastTimeUsed()+ " ---");
                Utility.printLog("*******************************************");

            }

        }


    }
}