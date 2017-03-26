package org.giasalfeusi.blewithbeaconlib;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.util.Log;

import java.util.List;

/**
 * Created by salvy on 10/03/17.
 */

public class DeviceScanCallBack extends ScanCallback {
    private final String TAG = "DeviceScanCallBack";

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        Log.i("callbackType", String.valueOf(callbackType));
        Log.i("result", result.toString());
        BluetoothDevice btDevice = result.getDevice();
        String bdName = String.format("%s@%s", btDevice.getName(), btDevice.getAddress());

        if (!DevicesList.getDevices().contains(btDevice))
        {
            Log.i(TAG, String.format("Adding to Devices!!! %s", bdName));
            DevicesList.getDevices().add(btDevice);
        }
        else
        {
            Log.i(TAG, String.format("Already present in Devices!!! %s", bdName));
        }
        //connectToDevice(btDevice);
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {
        for (ScanResult sr : results) {
            Log.i("ScanResult - Results", sr.toString());
        }
    }

    @Override
    public void onScanFailed(int errorCode) {
        Log.e("Scan Failed", "Error Code: " + errorCode);
    }
}

