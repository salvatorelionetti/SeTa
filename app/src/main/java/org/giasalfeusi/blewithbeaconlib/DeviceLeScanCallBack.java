package org.giasalfeusi.blewithbeaconlib;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

/*
 * Created by salvy on 10/03/17.
 */
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;

public class DeviceLeScanCallBack implements BluetoothAdapter.LeScanCallback {
    @Override
    public void onLeScan(final BluetoothDevice device, int rssi,
                         byte[] scanRecord) {
        Log.i("onLeScan", device.toString());
    }
}
