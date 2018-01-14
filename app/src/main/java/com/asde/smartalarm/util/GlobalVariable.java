package com.asde.smartalarm.util;

import android.bluetooth.BluetoothSocket;

import com.asde.smartalarm.bt.BluetoothConnectionThread;

/**
 * Created by Angelo on 08/11/2017.
 */

public class GlobalVariable {
    private static GlobalVariable instance;
    private boolean connected;
    private BluetoothSocket btSocket;
    private String address;
    private long timeout;
    private BluetoothConnectionThread connectionInstance;
    private boolean alarmOn;


    public static synchronized GlobalVariable getInstance() {
        if (instance == null) instance = new GlobalVariable();
        return instance;
    }

    public boolean isConnected() {
        return connected;
    }

    public void isConnected(boolean connected) {
        this.connected = connected;
    }

    public void setBtSocket(BluetoothSocket btSocket) {
        this.btSocket = btSocket;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setConnectionInstance(BluetoothConnectionThread connectionInstance) {
        this.connectionInstance = connectionInstance;
    }

    public BluetoothConnectionThread getConnectionInstance() {
        return connectionInstance;
    }

    public boolean isAlarmOn() {
        return alarmOn;
    }

    public void isAlarmOn(boolean alarmOn) {
        this.alarmOn = alarmOn;
    }
}
