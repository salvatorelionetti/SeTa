package org.giasalfeusi.blewithbeaconlib;

import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Observer;
import java.util.UUID;

import static org.giasalfeusi.blewithbeaconlib.DevicesList.getDevices;

/**
 * Created by salvy on 17/03/17.
 */

public class SensorTagOrchestrator {
    static final private String TAG = "STagOrch";
    static SensorTagOrchestrator sto;

    private DeviceScanCallBack deviceScanCallBack;
    private DeviceHost deviceHost;
    private Activity activity; // AAARGH!
    private HashMap<BluetoothDevice, DeviceWithObservers> deviceWithObserversMap;
    private HashMap<String,CharacteristicDes> conf;

    static public SensorTagOrchestrator singleton()
    {
        if (sto == null) {
            sto = new SensorTagOrchestrator();
        }

        return sto;
    }

    private SensorTagOrchestrator()
    {
        DevicesList.setDevices(null);
        deviceScanCallBack = new DeviceScanCallBack();
        deviceWithObserversMap = new HashMap<BluetoothDevice, DeviceWithObservers>();
    }

    public void setActivity(Activity act)
    {
        activity = act;
        if (deviceHost == null) {
            deviceHost = new DeviceHost(activity);
        }
    }

    public boolean isEnabled()
    {
        return deviceHost.isEnabled(activity);
    }

    public void startScan()
    {
        deviceHost.startScan(activity, deviceScanCallBack);
    }

    public void stopScan()
    {
        deviceHost.stopScan(activity, deviceScanCallBack);
    }

    public void connectToDevice(BluetoothDevice blueDev)
    {
        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        if (dwo != null)
        {
            blueDev.connectGatt(activity, false, dwo);
            stopScan();
        }
    }

    public void disconnectFromDevice(BluetoothDevice blueDev)
    {
        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        if (dwo != null)
        {
            if (dwo.getGattConnection() != null)
            {
                // .close() does not generate GattCallback
                dwo.getGattConnection().disconnect();
            }
        }
    }

    public List<BluetoothGattCharacteristic> getCharacteristicsList(BluetoothDevice blueDev)
    {
        List<BluetoothGattCharacteristic> ret = null;
        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        if (dwo != null) {
            ret = dwo.getCharacteristicsList();
        }
        return ret;
    }

    public void observeDevice(BluetoothDevice blueDev, Observer observer)
    {
        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        if (dwo == null)
        {
            dwo = new DeviceWithObservers(blueDev);
            deviceWithObserversMap.put(blueDev, dwo);
        }
        Log.i(TAG, String.format("addObserve %s<=%s:", blueDev.getAddress(), observer));
        dwo.addObserver(observer);
    }

    public void noMoreObserveDevice(BluetoothDevice blueDev, Observer observer)
    {
        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        if (dwo != null)
        {
            Log.i(TAG, String.format("noMoreObserve %s<=%s:", blueDev.getAddress(), observer));
            dwo.deleteObserver(observer);
            deviceWithObserversMap.remove(blueDev);
        }
    }

    public void setConfiguration(HashMap<String,CharacteristicDes> configuration) {
        conf = configuration;
    }

    public HashMap<String,CharacteristicDes> getConfiguration() {
        return conf;
    }

    public boolean readCharacteristic(BluetoothDevice blueDev, BluetoothGattCharacteristic ch) {

        boolean ret = false;

        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        if (dwo != null && dwo.getGattConnection() != null) {
            ret = dwo.getGattConnection().readCharacteristic(ch);
            if (!ret) {
                Log.e(TAG, String.format("readChar Failed for dev %s %s", blueDev.getAddress(), ch.getUuid()));
            }
        }

        return ret;
    }

/*    public boolean readCharacteristics(BluetoothDevice blueDev, List<BluetoothGattCharacteristic> chs) {

        boolean ret = false;

        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        if (dwo != null) {
            ret = dwo.readCharacteristics(chs);
            if (!ret) {
                Log.e(TAG, String.format("readChar Failed for dev %s %s", blueDev.getAddress(), ch.getUuid()));
            }
        }

        return ret;
    }*/

    public boolean writeCharacteristic(BluetoothDevice blueDev, BluetoothGattCharacteristic ch, byte[] val) {

        boolean ret = false;

        DeviceWithObservers dwo = deviceWithObserversMap.get(blueDev);
        if (dwo != null) {
            ch.setValue(val);
            ret = dwo.getGattConnection().writeCharacteristic(ch);

            if (!ret) {
                Log.e(TAG, String.format("writeChar Failed for dev %s %s", blueDev.getAddress(), ch.getUuid()));
            }
        }

        return ret;
    }

    public void serviceDiscovered(BluetoothDevice deviceObj, BluetoothGattService s) {
        for (BluetoothGattCharacteristic gattChar : s.getCharacteristics())
        {
            String uuid = gattChar.getUuid().toString();
            CharacteristicDes des = conf.get(uuid);
            //Log.d(TAG, String.format("servDisc conf %s", conf)));
            if (des != null)
            {
                des.setCharacteristic(gattChar);
            }
        }
    }
}
