package org.giasalfeusi.blewithbeaconlib;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.UUID;

/**
 * Created by salvy on 20/03/17.
 */

/* BluetoothDevice is final! */
public class DeviceWithObservers extends BluetoothGattCallback {
    private final String TAG = "DevWithObs";

    private ObservableAllPublic observable = null;

    private BluetoothDevice obj = null;

    private List<BluetoothGattCharacteristic> characteristicsList;

    List<BluetoothGattCharacteristic> gattChars;
    HashMap<String, BluetoothGattCharacteristic> gattCharsByUuid;
    BluetoothGatt mGatt;
    Integer gattCharIndex;
    Integer n;

    public DeviceWithObservers(BluetoothDevice _obj)
    {
        obj = _obj;
        observable = new ObservableAllPublic();
        characteristicsList = new ArrayList<BluetoothGattCharacteristic>();
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
                    break;
                default:
                    Log.e(TAG, "STATE_OTHER "+Integer.valueOf(newState));
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
            gattChars = new ArrayList<BluetoothGattCharacteristic>();
            gattCharsByUuid = new HashMap<String, BluetoothGattCharacteristic>();
            gattCharIndex = 0;
            n = 0;

            for (BluetoothGattService gattService: services)
            {
                addChars(gattService);
                observable.setChanged();
                observable.notifyObservers(gattService);
            }

            //startReadGattChars();
        } else {
            Log.w(TAG, "onServicesDiscovered received: " + status);
        }
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic
                                             characteristic, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            gattCharIndex++;
            Log.w(TAG, String.format("onCharRead %s => %s", characteristic.getUuid(), Utils.bytesToHex(characteristic.getValue())));
            //startReadGattChars();
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
            BluetoothGattCharacteristic gattChar;

            gattChar = gatt.getService(UUID.fromString("f000aa00-0451-4000-b000-000000000000")).getCharacteristic(UUID.fromString("f000aa02-0451-4000-b000-000000000000"));
            Log.w(TAG, String.format("onCharWrite %s => %s", characteristic.getUuid(), Utils.bytesToHex(characteristic.getValue())));
            boolean res = gatt.readCharacteristic(gattChar);
            if (!res) {
                Log.e(TAG, String.format("startReadGattChars: Failed to read a characteristic %s!", gattChars.get(gattCharIndex).getUuid()));
            }
        } else {
            Log.w(TAG, "onCharRead failed: received: " + status);
        }
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt,
                                        BluetoothGattCharacteristic characteristic) {
        Log.i(TAG, String.format("onCharChanged %s %s ", Utils.bytesToHex(characteristic.getValue()), characteristic.getUuid().toString()));
    }

    public void startReadGattChars()
    {
        if (gattChars.size()>gattCharIndex) {
            //Log.e(TAG, String.format("startReadGattChars: start read char %s!", gattChars.get(gattCharIndex).getUuid()));
            boolean res = mGatt.readCharacteristic(gattChars.get(gattCharIndex));
            if (!res) {
                Log.e(TAG, String.format("startReadGattChars: Failed to read a characteristic %s!", gattChars.get(gattCharIndex).getUuid()));
                /*try {
                    Thread.sleep(10000);
                } catch (InterruptedException ex) {
                    Log.e(TAG, String.format("startReadGattChars: sleep interrupted!"));
                }
                res = gatt.readCharacteristic(gattChars.get(gattCharIndex));
                if (!res) {
                    Log.e(TAG, String.format("startReadGattChars: Failed after sleep!"));
                }*/
                gattCharIndex++;
                startReadGattChars();
            }
        } else {
            Log.i(TAG, "startReadGattChars finished!");

            if (n==0) {
                byte[] enableT = {0x01};
                BluetoothGattCharacteristic gattChar;

                gattChar = mGatt.getService(UUID.fromString("f000aa00-0451-4000-b000-000000000000")).getCharacteristic(UUID.fromString("f000aa02-0451-4000-b000-000000000000"));
                gattChar.setValue(enableT);
                mGatt.writeCharacteristic(gattChar);
            }
            n++;
        }
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
        return mGatt;
    }

    public List<BluetoothGattCharacteristic> getCharacteristicsList() { return characteristicsList;}

    public boolean readCharacteristics(List<BluetoothGattCharacteristic> chs) {
        return false;
    }
}
