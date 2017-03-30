package org.giasalfeusi.blewithbeaconlib;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by salvy on 10/03/17.
 */

public class DeviceHost {
    /*
     * Android force to have a Context to interact with itself.
     * Unfortunately Context is a 'base class' of an Activity hence
     * change during an app lifecycle.
     * Special care must be taken:
     *  - avoiding to calling these methods during an Activity transition
     *  - Change the context as soon as the Activity does change.
     *
     * So our initial approach is to provide the Activity/Context at the
     * beginning of every parameter list.
     */

    final private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;


    public DeviceHost(Context context)
    {
        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
    }

    Boolean hasBluetoothLE(Context context)
    {
        Boolean ret = Boolean.FALSE;
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            ret = Boolean.TRUE;
        }

        return ret;
    }

    Boolean isEnabled()
    {
        Boolean ret = Boolean.FALSE;
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            ret = Boolean.TRUE;
        }

        return ret;
    }

    void startScan(DeviceScanCallBack deviceScanCallBack)
    {
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();
            }
            scanLeDevice(Boolean.TRUE, deviceScanCallBack);
        }

    void stopScan(DeviceScanCallBack deviceScanCallBack)
    {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(Boolean.FALSE, deviceScanCallBack);
        }
    }

    void exiting()
    {
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
    }

    private void scanLeDevice(final Boolean enable, DeviceScanCallBack mScanCallBack) {
        if (enable) {
            if (Build.VERSION.SDK_INT < 21) {
                //mBluetoothAdapter.startLeScan(mLeScanCallBack);
                throw new AssertionError("Support for SDK < 21 not implemented");
            } else {
                mLEScanner.startScan(filters, settings, mScanCallBack);
            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                //mBluetoothAdapter.stopLeScan(mLeScanCallBack);
                throw new AssertionError("Support for SDK < 21 not implemented");
            } else {
                if (mLEScanner != null) {
                    mLEScanner.stopScan(mScanCallBack);
                }
            }
        }
    }

    public void connectToDevice(BluetoothDevice device, DeviceGattCallBack gattCallBack, Activity activity, DeviceScanCallBack deviceScanCallBack) {
        if (mGatt == null) {
            mGatt = device.connectGatt(activity, false, gattCallBack);
            scanLeDevice(Boolean.FALSE, deviceScanCallBack);// will stop after first device detection
        }
    }
}
