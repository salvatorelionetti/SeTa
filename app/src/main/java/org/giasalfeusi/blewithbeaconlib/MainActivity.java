package org.giasalfeusi.blewithbeaconlib;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Observable;
import java.util.Observer;

public class MainActivity extends AppCompatActivity implements Observer, AdapterView.OnItemClickListener, View.OnClickListener
{
    private final static String TAG = "MainAct";

    private int REQUEST_ENABLE_BT = 1;

    /* M: Model */
    private DevicesList devicesList = null;

    /* V: View */
    private ListView devicesListView;

    /* C: Controller/Adapter/Activity */
    private DeviceListAdapter adapter;
    private SensorTagOrchestrator sto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, String.format("onCreate %s", this));
        setContentView(R.layout.activity_main);

        ensureBle();

        long nextTick = Utils.getPrefLong(this, "nextTick");
        Log.w(TAG, String.format("nextTick is %d", nextTick));

        this.sendBroadcast(new Intent("org.giasalfeusi.ble.poll"));

        sto = SensorTagOrchestrator.singleton();
        DevicesList.addObserver(this);

        adapter = new DeviceListAdapter(this, DevicesList.getDevices());
        devicesListView = (ListView) findViewById(R.id.deviceListViewId);
        devicesListView.setAdapter(adapter);
        devicesListView.setOnItemClickListener(this);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.startScanButton);
        fab.setOnClickListener(this);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.i(TAG, String.format("onResume %s", this));
        sto.setActivity(this); /* What a wrong if */
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Log.i(TAG, String.format("onPause %s", this));
        sto.stopScan();
        sto.setActivity(null);
    }

    @Override
    public void onClick(View view) {
        Snackbar.make(view, "Starting BLE scanning", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();
        startScan();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view,
                            int position, long id) {

        BluetoothDevice o = (BluetoothDevice) devicesListView.getItemAtPosition(position);
        Toast.makeText(getBaseContext(), String.format("Connecting to %s", o.getName()),Toast.LENGTH_SHORT).show();
        Log.i("onItemClick", String.format("Connecting to %s@%s", o.getName(), o.getAddress()));
        //sto.connectToDevice(o);
        Intent deviceIntent = new Intent(MainActivity.this, Main2Activity.class);
        deviceIntent.putExtra("deviceObject", o);
        Log.i(TAG, "deviceObject "+o.toString());
        startActivity(deviceIntent); // Take care: could arise pb accessing sto.activity!!!
    }

    private void ensureBle()
    {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "This phone does not support BLE!",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                this.startScan();
            } else {
                //Bluetooth not enabled.
                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void startScan() {
        if (!sto.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            this.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            sto.startScan();
        }
    }

    public void update(Observable o, Object arg)
    {
        Toast.makeText(this, "UPDATE CALLED!", Toast.LENGTH_SHORT).show();
        Log.i("MainActivity.update o", o.toString());
        Log.i("MainActivity.update arg", arg.toString());

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
