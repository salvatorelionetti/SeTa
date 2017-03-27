package org.giasalfeusi.blewithbeaconlib;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
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
        if (!started) {
            String msg = "startPoll will start";
            Log.i(TAG, msg);
            Toast.makeText(context, msg, Toast.LENGTH_LONG);
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
            final Ringtone r = RingtoneManager.getRingtone(context, notification);
            r.play();
            final Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    r.stop();
                }
            }, 3*1000);

            AlarmManager alarmMgr;
            Intent intent;
            PendingIntent alarmIntent;

            intent = new Intent(context, PollingBroadcastReceiver.class);
            alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
            alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmMgr.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + 60 * 1000, 60 * 1000, alarmIntent);
            started = true;
        }
    }

    public void setNextAlarm(Context context)
    {
        AlarmManager alarmMgr;
        Intent intent;
        PendingIntent alarmIntent;
        long nextTick;

        intent = new Intent(context, PollingBroadcastReceiver.class);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        nextTick = SystemClock.elapsedRealtime() + 60 * 1000;
        alarmMgr.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, nextTick, alarmIntent);
        long nextTickKm1 = Utils.getPrefLong(context, "nextTick");
        Utils.setPref(context, "nextTick", nextTick);
        Log.w(TAG, String.format("nextTick %d -> %d, %d ticks", nextTickKm1, nextTick, (nextTick - nextTickKm1)));
    }

    public void getNextAlarm(Context context)
    {
        AlarmManager alarmMgr;
        alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmMgr.getNextAlarmClock();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = String.format("onReceive %s %s %s", context, intent, Utils.describeIntentExtra(intent));

        Log.i(TAG, msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(context, notification);
        r.play();

        Log.i(TAG, String.format("onReceive nextTick %d", Utils.getPrefLong(context, "nextTick")));
/*        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            startPoll(context);
        }*/
        setNextAlarm(context);
    }
}
