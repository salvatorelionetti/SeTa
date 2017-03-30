package org.giasalfeusi.blewithbeaconlib;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class Main2Activity extends AppCompatActivity implements Observer, AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnClickListener {

    private final static String TAG = "Main2Act";

    /* M: Model */
    private HashMap<String, CharacteristicDes> conf;
    private SharedPreferences pref;

    /* V: View */
    private ListView characteristicsListView;
    private View content_main2_view;

    /* C: Controller/Adapter/Activity */
    private SensorTagOrchestrator sto;
    private BluetoothDevice deviceObj;
    private DeviceAttrAdapter adapter;
    private int newStatus;
    private FloatingActionButton fab;
    private List<BluetoothGattCharacteristic> characteristicsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Log.i(TAG, String.format("onCreate %s", this));

        conf = new HashMap<String, CharacteristicDes>();
        conf.put("00002a26-0000-1000-8000-00805f9b34fb", new CharacteristicDes("Firmware Revision String", CharacteristicValueType.STRING));
        conf.put("00002a25-0000-1000-8000-00805f9b34fb", new CharacteristicDes("Serial Number String", CharacteristicValueType.STRING));
        conf.put("00002a27-0000-1000-8000-00805f9b34fb", new CharacteristicDes("HW Revision String", CharacteristicValueType.STRING));
        conf.put("00002a28-0000-1000-8000-00805f9b34fb", new CharacteristicDes("SW Revision String", CharacteristicValueType.STRING));
        conf.put("00002a29-0000-1000-8000-00805f9b34fb", new CharacteristicDes("Manufacturer Name String", CharacteristicValueType.STRING));

        conf.put("f000aa01-0451-4000-b000-000000000000", new CharacteristicDes("IR Temperature Data"));
        conf.put("f000aa02-0451-4000-b000-000000000000", new CharacteristicDes("IR Temperature Config", CharacteristicValueType.BIT));
        conf.put("f000aa03-0451-4000-b000-000000000000", new CharacteristicDes("IR Temperature Period"));

        sto = SensorTagOrchestrator.singleton();
        sto.setConfiguration(conf);
        deviceObj = getIntent().getExtras().getParcelable("deviceObject");
        newStatus = BluetoothProfile.STATE_DISCONNECTED;
        characteristicsList = new ArrayList<BluetoothGattCharacteristic>();

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        content_main2_view = (View) findViewById(R.id.content_main2);

        adapter = new DeviceAttrAdapter(this, characteristicsList);
        adapter.setConfiguration(conf);

        characteristicsListView = (ListView) ((ViewGroup)content_main2_view).getChildAt(1);
        characteristicsListView.setAdapter(adapter);
        characteristicsListView.setOnItemClickListener(this);
        characteristicsListView.setOnItemLongClickListener(this);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(this);

        View headView = ((ViewGroup)content_main2_view).getChildAt(0);
        TextView textView = (TextView) headView.findViewById(R.id.firstLine);
        TextView desView = (TextView) headView.findViewById(R.id.secondLine);

        textView.setText(deviceObj.getName());
        desView.setText(deviceObj.getAddress());
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.i(TAG, String.format("onResume %s", this));
        sto.setContext(this); /* What a wrong if */
        sto.observeDevice(deviceObj, this);
        sto.connectToDevice(deviceObj);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.i(TAG, String.format("onPause %s", this));
        //sto.disconnectFromDevice(deviceObj);
        sto.setContext(null); /* What a wrong if */
        sto.noMoreObserveDevice(deviceObj, this);
    }

    public void update(Observable o, Object arg)
    {
        //java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
        //Toast.makeText(this, "Main2Act UPDATE CALLED!", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "update o " + o.toString());
        if (arg == null)
        {
            Log.i(TAG, "update arg <null>");
        } else {
            Log.i(TAG, "update arg " + arg.toString());

            if (arg instanceof Integer) {
                /* Connection status */
                newStatus = (int) arg;

                Log.i(TAG, "count "+((ViewGroup)content_main2_view).getChildCount());
                View rowView = ((ViewGroup)content_main2_view).getChildAt(0);
                final ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (newStatus) {
                            case BluetoothProfile.STATE_CONNECTED:
                                imageView.setImageResource(R.drawable.ic_bluetooth_connected_black_24dp);
                                pref.edit().putString("devName", deviceObj.getName()).commit();
                                pref.edit().putString("devAddr", deviceObj.getAddress()).commit();

                                /* Start bg T sampling */
                                Intent bgPollIntent = new Intent("org.giasalfeusi.ble.poll");
                                bgPollIntent.putExtra("deviceObject", deviceObj);
                                Main2Activity.this.sendBroadcast(bgPollIntent);

                                break;
                            case BluetoothProfile.STATE_DISCONNECTED:
                                imageView.setImageResource(R.drawable.ic_bluetooth_black_24dp);
                                characteristicsList.clear();
                                if (adapter != null)
                                {
                                    adapter.notifyDataSetChanged();
                                }
                                break;
                            default:
                                Log.e(TAG, "STATE_OTHER "+Integer.valueOf(newStatus));
                                imageView.setImageResource(R.drawable.ic_bluetooth_searching_black_24dp);
                                break;
                        }
                    }
                });
            } else if (arg instanceof BluetoothGattService) {
                /* Discovered a Service */
                final BluetoothGattService s = (BluetoothGattService) arg;

                    runOnUiThread(new Runnable() {
                                      @Override
                                      public void run() {
                                          /* Must be on UI thread otherwise I got exception
                                           * java.lang.IllegalStateException: The content of the adapter has changed but ListView did not receive a notification. Make sure the content of your adapter is not modified from a background thread, but only from the UI thread. Make sure your adapter calls notifyDataSetChanged() when its content changes. [in ListView(-1, class android.widget.ListView) with Adapter(class org.giasalfeusi.blewithbeaconlib.DeviceAttrAdapter)]
                                           */
                                          characteristicsList.addAll(s.getCharacteristics());
                                          sto.serviceDiscovered(deviceObj, s);

                                          if (adapter != null) {
                                              adapter.notifyDataSetChanged();
                                          }
                                      }
                                  });
            } else if (arg instanceof BluetoothGattCharacteristic) {
                /* Char changed */
                BluetoothGattCharacteristic ch = (BluetoothGattCharacteristic) arg;

                if (adapter != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }

/*        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }*/
    }

    @Override
    public void onClick(View view) {
        String msg;

        switch (newStatus)
        {
            case BluetoothProfile.STATE_CONNECTING:
            case BluetoothProfile.STATE_CONNECTED:
                msg = "Start Disconnecting";
                sto.disconnectFromDevice(deviceObj);
                break;
            case BluetoothProfile.STATE_DISCONNECTING:
            case BluetoothProfile.STATE_DISCONNECTED:
                msg = "Start Connecting";
                sto.connectToDevice(deviceObj);
                break;
            default:
                msg = "Status is unknown " + newStatus;
                break;
        }

        Snackbar.make(view, msg, Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {
        Log.d(TAG, String.format("onItemClick pos %d", position));
        sto.readCharacteristic(deviceObj, characteristicsList.get(position));
        return;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view,
                                int position, long id)
    {
        BluetoothGattCharacteristic ch = characteristicsList.get(position);
        Log.d(TAG, String.format("onItemLongClick pos %d val %s", position, ch.getValue()));
        if (ch.getValue() == null)
        {
            sto.readCharacteristic(deviceObj, ch);
        } else {
            CharacteristicDes des = conf.get(ch.getUuid().toString());
            if (des != null && des.getType() == CharacteristicValueType.BIT)
            {
                // Toggle
                byte[] val = ch.getValue();
                val[0] ^= 1;
                Log.d(TAG, String.format("onItemLongClick val %s -> %s", Utils.bytesToHex(ch.getValue()), Utils.bytesToHex(val)));
                sto.writeCharacteristic(deviceObj, ch, val);
            }
        }
        //sto.readCharacteristic(deviceObj, characteristicsList.get(position));
        return true;
    }
}