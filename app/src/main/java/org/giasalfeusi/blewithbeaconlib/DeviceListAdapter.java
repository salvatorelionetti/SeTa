package org.giasalfeusi.blewithbeaconlib;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by salvy on 07/03/17.
 */

public class DeviceListAdapter extends ArrayAdapter<BluetoothDevice>
{

    private final Context context;
    private ArrayList<BluetoothDevice> values;

    public DeviceListAdapter(Context context, ArrayList<BluetoothDevice> values) {
        super(context, -1, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) this.context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.device_list_row_layout, parent, false);
        TextView textView = (TextView) rowView.findViewById(R.id.firstLine);
        TextView desView = (TextView) rowView.findViewById(R.id.secondLine);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
        //String bdAddress = ((BluetoothDevice) values.get(position))
        BluetoothDevice bd = values.get(position);
        textView.setText(bd.getName());
        desView.setText(bd.getAddress());
        // change the icon for Windows and iPhone
        imageView.setImageResource(R.drawable.ic_bluetooth_searching_black_24dp);

        return rowView;
    }
}
