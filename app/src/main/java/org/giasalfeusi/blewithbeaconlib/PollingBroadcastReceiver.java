package org.giasalfeusi.blewithbeaconlib;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by salvy on 26/03/17.
 */

public class PollingBroadcastReceiver extends BroadcastReceiver {
    static final private String TAG = "PollBroadRec";
    static private boolean started = false;

    static public boolean isStarted() { return started; }

    static public void startPoll(Context context)
    {
        Log.d(TAG, "called startPoll");
        if (!started) {
            String msg = "startPoll will start";
            Log.i(TAG, msg);
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

            AlarmManager alarmMgr;
            Intent intent;
            PendingIntent alarmIntent;

            intent = new Intent(context, PollingBroadcastReceiver.class);
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmMgr.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60 * 1000, 60 * 1000, alarmIntent);
            started = true;
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = String.format("onReceive %s %s %s", context, intent, Utils.describeIntentExtra(intent));

        Log.i(TAG, msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

        //if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            startPoll(context);
        //}
    }
}
