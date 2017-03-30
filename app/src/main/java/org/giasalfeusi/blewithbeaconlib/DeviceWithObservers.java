package org.giasalfeusi.blewithbeaconlib;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Observer;

/**
 * Created by salvy on 20/03/17.
 */

/* BluetoothDevice is final! */
public class DeviceWithObservers extends BluetoothGattCallback {
    private final String TAG = "DevWithObs";

    private ObservableAllPublic observable = null;

    private BluetoothDevice obj = null;

    private List<BluetoothGattCharacteristic> characteristicsList;

    BluetoothGatt mGatt;

    public DeviceWithObservers(BluetoothDevice _obj)
    {
        obj = _obj;
        observable = new ObservableAllPublic();
        //characteristicsList = new ArrayList<BluetoothGattCharacteristic>();
    }

    public BluetoothDevice getBluetoothDevice()
    {
        return obj;
    }

    public void addChars(BluetoothGattService gattService)
    {
        Log.i(TAG, String.format("S%s", gattService.getUuid()));

        for (BluetoothGattCharacteristic gattChar : gattService.getCharacteristics())
        {
            Log.i(TAG, String.format(" %s", gattChar.getUuid()));
        }
        characteristicsList.addAll(gattService.getCharacteristics());
    }

    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        Log.i(TAG, String.format("onConnStateChange Status %d, newState %d, gatt %s", status, newState, gatt));

        if (status == BluetoothGatt.GATT_SUCCESS) {
            mGatt = gatt;
        }
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i(TAG, "STATE_CONNECTED");
                    characteristicsList = new ArrayList<BluetoothGattCharacteristic>();
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i(TAG, "STATE_DISCONNECTED");
                    mGatt = null;
                    characteristicsList = null;
                    break;
                default:
                    Log.e(TAG, "STATE_OTHER "+Integer.valueOf(newState));
                    characteristicsList = null;
                    mGatt = null;
            }
            observable.setChanged();
            observable.notifyObservers(Integer.valueOf(newState));
/*        } else {
            Log.e(TAG, "onConnStateChange Failed!");
            // #define  GATT_ERROR                          0x85
            if (status == 0x85)
            { // called when we disconnect (es stand-by), device is owered down, then exit from stand-by and retry connection
              // Also gatt pointer does change!
                if (gatt != null)
                {
                    gatt.close();
                }

            }
        }*/
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt _gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            mGatt = _gatt;
            List<BluetoothGattService> services = mGatt.getServices();
            Log.i("GATT.onServDiscovered", services.toString());

            for (BluetoothGattService gattService: services)
            {
                addChars(gattService);
                observable.setChanged();
                observable.notifyObservers(gattService);
            }
        } else {
            mGatt = null;
            Log.w(TAG, "onServicesDiscovered received: " + status);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic
                                             characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.w(TAG, String.format("onCharRead %s => %s", characteristic.getUuid(), Utils.bytesToHex(characteristic.getValue())));
            observable.setChanged();
            observable.notifyObservers(characteristic);
        } else {
            Log.w(TAG, "onCharRead failed: received: " + status);
        }
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt,
                                      BluetoothGattCharacteristic
                                              characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            Log.w(TAG, String.format("onCharWrite %s => %s", characteristic.getUuid(), Utils.bytesToHex(characteristic.getValue())));
        } else {
            Log.w(TAG, "onCharWrite failed: received: " + status);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic) {
        Log.i(TAG, String.format("onCharChanged %s %s ", Utils.bytesToHex(characteristic.getValue()), characteristic.getUuid().toString()));
    }

    /* Proxy part */
    public void addObserver(Observer o)
    {
        Log.i(TAG, "addObs: " + o.toString());
        observable.addObserver(o);
    }

    public void deleteObserver(Observer o)
    {
        Log.i(TAG, "delObs: " + o.toString());
        observable.deleteObserver(o);
    }

    public BluetoothGatt getGattConnection()
    {
        Log.d(TAG, String.format("gattConn is %s", mGatt));
        return mGatt;
    }

    public List<BluetoothGattCharacteristic> getCharacteristicsList() { return characteristicsList;}

    public boolean readCharacteristics(List<BluetoothGattCharacteristic> chs) {
        return false;
    }
}
