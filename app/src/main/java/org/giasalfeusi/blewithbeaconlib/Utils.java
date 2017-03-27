package org.giasalfeusi.blewithbeaconlib;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Iterator;
import java.util.Set;

/**
 * Created by salvy on 16/03/17.
 */

public class Utils {
    final protected static char[] hexArray = "0123456789ABCDEF ".toCharArray();
    final private static String TAG = "Utils";
    public static String bytesToHex(byte[] bytes) {

        if (bytes == null)
        {
            return "<null>";
        }

        char[] hexChars = new char[bytes.length * 3 - 1];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
            if (j < (bytes.length - 1)) {
                hexChars[2 * (j + 1)] = hexArray[0x10];
            }
        }
        return new String(hexChars);
    }

    public static void dumpIntent(Intent i){

        Bundle bundle = i.getExtras();
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            Iterator<String> it = keys.iterator();
            Log.e(TAG,"Dumping Intent start");
            while (it.hasNext()) {
                String key = it.next();
                Log.e(TAG,"[" + key + "=" + bundle.get(key)+"]");
            }
            Log.e(TAG,"Dumping Intent end");
        }
    }

    public static String describeIntentExtra(Intent i)
    {
        String ret;
        Bundle bundle = i.getExtras();

        ret = "{";
        if (bundle != null) {
            Set<String> keys = bundle.keySet();
            Iterator<String> it = keys.iterator();
            while (it.hasNext()) {
                String key = it.next();
                ret += String.format("key %s => '%s',", key, bundle.get(key));
            }
        }
        ret += "}";
        return ret;
    }

    public static void setPref(Context context, String key, String val)
    {
        SharedPreferences pref;

        pref = PreferenceManager.getDefaultSharedPreferences(context);

        pref.edit().putString(key, val).commit();
    }

    public static void setPref(Context context, String key, long val)
    {
        SharedPreferences pref;

        pref = PreferenceManager.getDefaultSharedPreferences(context);

        pref.edit().putLong(key, val).commit();
    }

    public static String getPrefString(Context context, String key)
    {
        SharedPreferences pref;

        pref = PreferenceManager.getDefaultSharedPreferences(context);

        return pref.getString(key, "");
    }

    public static long getPrefLong(Context context, String key)
    {
        SharedPreferences pref;

        pref = PreferenceManager.getDefaultSharedPreferences(context);

        return pref.getLong(key, 0);
    }
}
