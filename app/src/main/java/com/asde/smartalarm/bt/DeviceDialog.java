package com.asde.smartalarm.bt;

import android.app.*;
import android.bluetooth.*;
import android.content.*;
import android.support.design.widget.Snackbar;
import android.util.*;
import android.view.*;
import android.widget.*;

import com.asde.smartalarm.MainActivity;
import com.asde.smartalarm.R;
import com.asde.smartalarm.util.GlobalVariable;

import java.util.Set;


/**
 * Class to instantiate available device dialog list to connect
 */
public class DeviceDialog  {
    private Context context;
    protected BluetoothAdapter btAdapter = null;
    protected GlobalVariable global;
    protected BroadcastReceiver btDevicesReceiver;
    protected BroadcastReceiver pairedDevicesReceiver;
    protected AlertDialog btDevicesDialog = null;
    protected ArrayAdapter<String> devicesArrayAdapter;
    protected ArrayAdapter<String> discoverArrayAdapter;
    protected ListView pairedDevices;
    protected ListView availableDevices;
    protected ProgressBar discoveringProgressBar = null;
    protected AlertDialog.Builder dialogBuilder;
    protected LayoutInflater inflater;
    protected View dialogView;
    protected Set<BluetoothDevice> setBtPairedDevices;
    protected IntentFilter availableDevicesFilter, pairedDevicesFilter;
    protected BluetoothDevice btDevice;
    private String deviceInfo;
    private ConnectThread connectThread;

    /**
     * @param context of the main activity
     */
    public DeviceDialog(Context context) {
        this.context = context;
    }

    /**
     * To instantiate and create device list dialog
     */
    public void showDeviceListDialog() {
        devicesArrayAdapter = createArrayAdapter(context, R.layout.device_name);
        //discoverArrayAdapter = createArrayAdapter(context, R.layout.device_name);
        dialogBuilder = new AlertDialog.Builder(context);//instantiate builder
        dialogView = inflateCustomView((Activity) context, R.layout.dialog_paired_devices);
        dialogBuilder.setView(dialogView);
        //instantiate and link components from class to custom view
        instantiateItems();
        //set custom views
        pairedDevices.setAdapter(devicesArrayAdapter);
        //availableDevices.setAdapter(discoverArrayAdapter);
        //enable again bt adapter
        setBluetoothAdapter();
        // Get a set of currently paired devices and append to 'pairedDevices'
        setBtPairedDevices = btAdapter.getBondedDevices();
        //instantiate a broadcast reciever to check bt discovering devices
        //btDevicesReceiver = new BluetoothCastReceiver();
        pairedDevicesReceiver = new BluetoothPairCastReceiver();
        instantiateFilter(pairedDevicesReceiver);
        buildAvailablePairedDevicesList();
        btDevicesDialog = dialogBuilder.create();
        btDevicesDialog.show();
        //add lists items click liste
        registerListeners();
        btDevicesDialog.setCanceledOnTouchOutside(false);//make not cancel when press outside
    }

    public void buildAvailablePairedDevicesList() {
        try {
            try {
                //Log.d("RECEIVER", btDevicesReceiver+"");
                //context.registerReceiver(btDevicesReceiver, availableDevicesFilter);//register listeners and the filters
                if (btAdapter.isDiscovering()) {
                    btAdapter.cancelDiscovery();
                }
                btAdapter.startDiscovery();//start bt devices discovery
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (setBtPairedDevices.size() > 0) { // Add previosuly paired devices to the array
                for (BluetoothDevice device : setBtPairedDevices)
                    devicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            } else {//if isnt devices available show only one item
                devicesArrayAdapter.add("no se encontraron dispositivos");
            }
        } catch (Exception e) {e.printStackTrace();
        }
    }

    public void instantiateFilter(BroadcastReceiver mPairReceiver) {
        pairedDevicesFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        context.registerReceiver(mPairReceiver, pairedDevicesFilter);

        availableDevicesFilter = new IntentFilter();//instatiate a availableDevicesFilter for listen bt changes
        availableDevicesFilter.addAction(BluetoothDevice.ACTION_FOUND);//add the actions
        availableDevicesFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        availableDevicesFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
    }

    public void registerListeners() {
        pairedDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                deviceInfo = ((TextView) v).getText().toString();
                if (!deviceInfo.equals("no se encontraron dispositivos")) {//if arent empty paired dev list
                    btDevicesDialog.dismiss();
                    connectToDevice(deviceInfo.substring(deviceInfo.length() - 17), btAdapter);
                    //context.unregisterReceiver(btDevicesReceiver);
                    context.unregisterReceiver(pairedDevicesReceiver);
                }
            }
        });

        /*availableDevices.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                deviceInfo = ((TextView) v).getText().toString();
                if (!deviceInfo.equals(context.getString(R.string.dialog_discovery_devices_not_found))) {
                    btDevice = btAdapter.getRemoteDevice(deviceInfo
                            .substring(deviceInfo.length() - 17));
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Method method = btDevice.getClass().getMethod("createBond"
                                        , (Class[]) null);
                                method.invoke(btDevice, (Object[]) null);
                                btDevicesDialog.dismiss();
                                global.setAddress(deviceInfo.substring(deviceInfo.length() - 17));
                                Log.d("adress", global.getAddress());
                                context.unregisterReceiver(btDevicesReceiver);
                                context.unregisterReceiver(pairedDevicesReceiver);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
            }
        });*/
        btDevicesDialog.setOnKeyListener(new Dialog.OnKeyListener() {//handle back button
            @Override
            public boolean onKey(DialogInterface arg0, int keyCode,
                                 KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    btDevicesDialog.dismiss();
                    context.unregisterReceiver(pairedDevicesReceiver);
                    //context.unregisterReceiver(btDevicesReceiver);
                }
                return true;
            }
        });
    }

    /**
     * @param address   the mac address of the device to connect
     * @param btAdapter the bt adapter object
     */
    public void connectToDevice(String address, BluetoothAdapter btAdapter) {
        global = global.getInstance();//get global bt data
        global.setAddress(address);//get the mac address
        connectThread = new ConnectThread(btAdapter, (MainActivity)context);//start connect attempt thread
        connectThread.start();
        final long start = System.currentTimeMillis();//get the start of the thread
        final boolean[] tryToConnect = {true};
        new Thread(new Runnable() {
            public void run() {
                while (tryToConnect[0]) {
                    if (System.currentTimeMillis() - start == global.getTimeout()) {//timeout 20 seg default
                        tryToConnect[0] = false;
                        ((MainActivity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showToast("Tiempo fuera");
                            }
                        });
                        connectThread.closeConnection();
                        break;
                    }
                    if (global.isConnected()) {
                        ((MainActivity)context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                        connectThread.startConnectedThread();
                        tryToConnect[0] = false;
                    }
                }
            }
        }).start();
    }

    public ArrayAdapter<String> createArrayAdapter(Context context, int res) {
        return new ArrayAdapter<String>(context, res);
    }

    public View inflateCustomView(Activity activity, int layoutid) {
        LayoutInflater inflater = activity.getLayoutInflater();//get inflater for custom
        return inflater.inflate(layoutid, null);//inflate view;
    }

    public void instantiateItems() {
        //connectionSwitch = (Switch) ((Activity) context).findViewById(R.id.bluetooth_switch);
        //connectionTextView = (TextView) ((Activity) context).findViewById(R.id.bluetooth_tv);
        pairedDevices = (ListView) dialogView.findViewById(R.id.paired_devices);
        //availableDevices = (ListView) dialogView.findViewById(R.id.available_devices);
        //discoveringProgressBar = (ProgressBar) dialogView.findViewById(R.id.discover_progressbar);
        //discoveringProgressBar.setVisibility(ProgressBar.INVISIBLE);
    }


    private class BluetoothPairCastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                int state = intent.getIntExtra
                        (BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                int prevState = intent.getIntExtra
                        (BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING)
                    connectToDevice(global.getAddress(), btAdapter);
            }
        }
    }

    public void showToast(String text) {
        View view = ((MainActivity)context).findViewById(android.R.id.content);
        /*Show the snackbar*/
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).show();
    }


    /**
     * Instantiate Bluetooth adapter
     */
    public void setBluetoothAdapter() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

}
