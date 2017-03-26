package org.giasalfeusi.blewithbeaconlib;

import android.bluetooth.BluetoothGattCharacteristic;
import android.util.Log;

/**
 * Created by salvy on 24/03/17.
 */

public class CharacteristicDes
{
    final private String TAG = "CharDes";
    private String title;
    private CharacteristicValueType type;
    private BluetoothGattCharacteristic blueCh;

    public CharacteristicDes(String t, CharacteristicValueType typ) {
        title = t;
        type = typ;
    }

    public CharacteristicDes(String t)
    {
        this(t, CharacteristicValueType.BYTE_ARRAY);
    }

    public String getTitle() { return title; }

    public CharacteristicValueType getType() { return type; }

    public BluetoothGattCharacteristic getCharacteristic()
    {
        return blueCh;
    }

    public void setCharacteristic(BluetoothGattCharacteristic characteristic)
    {
        Log.d(TAG, String.format("setChar %s", characteristic.getUuid().toString()));
        blueCh = characteristic;
    }

    public String valueToString()
    {
        String ret = null;

        if (blueCh != null && blueCh.getValue() != null) {
            if (type == CharacteristicValueType.BYTE_ARRAY || type == CharacteristicValueType.BIT) {
                ret = Utils.bytesToHex(blueCh.getValue());
            } else if (type == CharacteristicValueType.STRING) {
                // Cause an Exception!
                // java.lang.IllegalStateException: The content of the adapter has changed but ListView did not receive a notification. Make sure the content of your adapter is not modified from a background thread, but only from the UI thread. Make sure your adapter calls notifyDataSetChanged() when its content changes. [in ListView(-1, class android.widget.ListView) with Adapter(class org.giasalfeusi.blewithbeaconlib.DeviceAttrAdapter)]
                // ret = blueCh.getStringValue(0);
                ret = new String(blueCh.getValue());
            }
        }

        return ret;
    }
}