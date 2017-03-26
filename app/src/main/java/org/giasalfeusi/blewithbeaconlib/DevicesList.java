package org.giasalfeusi.blewithbeaconlib;


import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;

/**
 * Created by salvy on 10/03/17.
 */


/* Unfortunately java don't support multiple inheritance nor
 * it define an interface for Observable.
 * Moreover w.o. multiple inheritance it's hard to work with
 * protected visibility on {clear, set}Changed methods.
 */
/*interface ObservableImplInterface extends ObservableInterface
{
/*    void clearChanged();
    void setChanged();
    boolean hasChanged();* /
    void notifyObservers(Object arg);
    void notifyObservers();
}*/

/* Singleton */
class DevicesList<E> extends ArrayList<E> {

    private static final String TAG = "DevList";

    private static DevicesList devicesList = null;

    //private ArrayList<String> devicesList = null;

    private static ObservableAllPublic observable = null;

    /* Why constructor is required by singleton initialization? */
    public DevicesList(Collection<E> c)
    {
        super(c);
    }

    /* Compilation error: Error:(45, 31) error: non-static type variable E cannot be referenced from a static context */
    public static DevicesList getDevices()
    {
        return devicesList;
    }

    public static DevicesList setDevices(String[] devices)
    {
        String[] _devices;

        if (devices == null)
        {
            String[] s = {};
            _devices = s;
        } else {
            _devices = devices;
        }

        if (devicesList == null) {
            devicesList = new DevicesList<String>(Arrays.asList(_devices));
            observable = new ObservableAllPublic();
            Log.i("DevicesList", String.format("CREATING DEVICESLIST %s,%s", devicesList, observable));
        } else {
            Log.i("DevicesList", String.format("REPLACING DEVICESLIST %s,%s", devicesList, observable));
            devicesList.replaceAll(Arrays.asList(_devices));
            /* Automatically invokes clearChanged() method */
            observable.setChanged();
            observable.notifyObservers(devicesList);
        }

        return devicesList;
    }

    public void replaceAll(Collection<E> elements)
    {
        Log.i("DevicesList", elements.toString());
        Log.i("DevicesList", this.observable.toString());
        this.clear();
        this.addAll(elements);
    }

    @Override
    public boolean add(E object)
    {
        boolean ret;

        ret = super.add(object);
        if (ret)
        {
            observable.setChanged();
            observable.notifyObservers(devicesList);
        }

        return ret;
    }

    /* Proxy part */
    static public void addObserver(Observer o)
    {
        Log.i(TAG, "addObs: " + o.toString());
        observable.addObserver(o);
    }

    static public void deleteObserver(Observer o)
    {
        Log.i(TAG, "delObs: " + o.toString());
        observable.deleteObserver(o);
    }
}
