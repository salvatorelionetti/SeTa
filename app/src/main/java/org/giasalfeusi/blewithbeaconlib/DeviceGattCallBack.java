package org.giasalfeusi.blewithbeaconlib;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

/**
 * Created by salvy on 10/03/17.
 */

/*
 * Read conf.enable
 * if 0 => write conf.enable, 1
 * read t once a second
 */

/* Actually this is used only by DeviceWithObservers, so there is a direct link */
public class DeviceGattCallBack extends BluetoothGattCallback {
/*    static final String TAG = "DevGattCb";
    static private BluetoothGattCallbackIf proxyObj = null;

    static public void setProxy(DeviceWithObservers devWithObs)
    {
        deviceWithObservers = devWithObs;
    }

    static public DeviceWithObservers getDevice()
    {
        return deviceWithObservers;
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        if (deviceWithObservers != null)
        {
            deviceWithObservers.onConnectionStateChange(gatt, status, newState);
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt _gatt, int status) {
        if (deviceWithObservers != null) {
            deviceWithObservers.onServicesDiscovered(_gatt, status);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic
                                             characteristic, int status) {
        if (deviceWithObservers != null) {
            deviceWithObservers.onCharacteristicRead(gatt, characteristic, status);
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic
                                             characteristic, int status) {
        if (deviceWithObservers != null) {
            deviceWithObservers.onCharacteristicWrite(gatt, characteristic, status);
        }
    }*/
}
