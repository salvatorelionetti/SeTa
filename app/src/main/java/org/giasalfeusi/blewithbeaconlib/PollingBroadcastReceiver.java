package org.giasalfeusi.blewithbeaconlib;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.support.v4.os.AsyncTaskCompat;
import android.util.Log;
import android.widget.Toast;

import java.util.Observable;
import java.util.Observer;

/**
 * Created by salvy on 26/03/17.
 */

public class PollingBroadcastReceiver extends /*Wakeful*/ BroadcastReceiver implements Observer
{
    static final private String TAG = "PollBroadRec";
    static private boolean startedAtBoot = false;
    static private final SensorTagOrchestrator sto = SensorTagOrchestrator.singleton();
    static BluetoothDevice blueDev;

    private boolean isBootIntent(Intent intent)
    {
        boolean ret = false;
        if (intent.getAction()!=null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            ret = true;
        }
        return ret;
    }

    private boolean isGuiIntent(Intent intent) {
        boolean ret = false;

        if (intent.getAction() != null && intent.getAction().equals("org.giasalfeusi.ble.gui")) {
            ret = true;
        }
        return ret;
    }

    private boolean isBgIntent(Intent intent) {
        boolean ret = false;

        if (intent.getAction() != null && intent.getAction().equals("org.giasalfeusi.ble.bg")) {
            ret = true;
        }
        return ret;
    }

    private boolean isStartBgPollIntent(Intent intent) {
        boolean ret = false;

        if (intent.getAction() != null && intent.getAction().equals("org.giasalfeusi.ble.poll")) {
            ret = true;
        }
        return ret;
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

    @Override
    public void onReceive(Context context, Intent intent) {
        String msg = String.format("onReceive startedAtBoot %s, %s %s %s", startedAtBoot?"BOOT_RECV":"", context, intent, Utils.describeIntentExtra(intent));
        Log.i(TAG, msg);
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();

        /* Initialize after the boot */
        if (isBootIntent(intent)) {
            startedAtBoot = true;
            sto.setContext(context);
            if (!sto.getDeviceHost().hasBluetoothLE(context)) {
                Log.e(TAG, "BLE not supported! Exiting");
                return;
            }
        }

        /* Check Bluetooth is enabled */
        if (sto.getDeviceHost()!=null && !sto.getDeviceHost().isEnabled()) {
            Log.e(TAG, "BLE not enabled! Exiting");
            return;
        }

        /* Check we have a device to connect with */
        if (Utils.getPrefString(context, "devAddr") == null) {
            Log.e(TAG, "Device not configured. Bg stopped!");
            return;
        }

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        Ringtone r = RingtoneManager.getRingtone(context, notification);
        r.play();

        Log.i(TAG, String.format("onReceive nextTick %d", Utils.getPrefLong(context, "nextTick")));
        setNextAlarm(context);

        if (isStartBgPollIntent(intent))
        {
            /* Extract deviceObj */
            blueDev = intent.getExtras().getParcelable("deviceObject");
            Log.i(TAG, String.format("startBgPoll for blueDev %s", blueDev));
            return;
        }

        //startWakefulService(context, new Intent(context, PollingIntentService.class));
        final PendingResult result = goAsync();

        AsyncTaskCompat.executeParallel(

                new AsyncTask<Void, Void, Void>() {

                    @Override

                    protected Void doInBackground(Void... params) {

                        try {
                            // ... do some work here, for up to 10 seconds
                            Log.i(TAG, "doInBg started");
                            String uuidStr = "f000aa01-0451-4000-b000-000000000000";
                            BluetoothGattCharacteristic gattCh = sto.getCharacteristic(blueDev, uuidStr);
                            if (gattCh != null) {
                                sto.readCharacteristic(blueDev, uuidStr);
                                try {
                                    Thread.sleep(1000);
                                    gattCh = sto.getCharacteristic(blueDev, uuidStr);
                                    Log.i(TAG, String.format("doInBack: sendVal %s", Utils.bytesToHex(gattCh.getValue())));
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            } else {
                                Log.e(TAG, "doInBack: gattCh is NULL!!!");
                            }
                        } finally {
                            Log.i(TAG, "doInBg finally");
                            result.setResultCode(-1);
                            result.finish();
                        }

                        return null;

                    }

                });
    }

    public void update(Observable o, Object arg)
    {
        Log.i(TAG, String.format("update o %s", o.toString()));
        Log.i(TAG, String.format("update arg %s", arg.toString()));
    }
}
