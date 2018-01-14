package com.asde.smartalarm;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.asde.smartalarm.bt.DeviceDialog;
import com.asde.smartalarm.util.GlobalVariable;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private BluetoothAdapter btAdapter;
    private ImageView mSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mSwitch = (ImageView) findViewById(R.id.alarm_switch);
        mSwitch.setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(80);
        if (GlobalVariable.getInstance().isAlarmOn()) {
            mSwitch.setImageResource(R.drawable.ic_switch_off);
            GlobalVariable.getInstance().isAlarmOn(false);
        } else {
            mSwitch.setImageResource(R.drawable.ic_switch_on);
            GlobalVariable.getInstance().isAlarmOn(true);
        }
    }

    /**
     * @return true if the bluetooth is enabled, also if is not, request bluetooth activation to the user
     */
    public boolean isBluetoothEnabled() {
        setBluetoothAdapter();
        if (btAdapter != null) {
            if (!btAdapter.isEnabled()) {
                //INSTANTIATE A NEW ACTIVITY FROM SYSTEM
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
                if (btAdapter.isEnabled()) return true;
                else return false;
            } else return true;
        } else return false;
    }


    /**
     * Instantiate Bluetooth adapter
     */
    public void setBluetoothAdapter() {
        btAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    /**
     * Show device list connect dialog
     */
    private void showDialog() {
        try {
            new DeviceDialog(this).showDeviceListDialog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param text the id of string resource for show message
     */
    public void showToast(String text) {
        /*Find the root view*/
        View view = findViewById(android.R.id.content);
        /*Show the snackbar*/
        Snackbar.make(view, text, Snackbar.LENGTH_LONG).show();
        //Toast.makeText(getApplicationContext(), getResources().getString(id),
        //      Toast.LENGTH_SHORT).show();

    }
}
