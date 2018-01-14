package com.asde.smartalarm.bt;

import android.bluetooth.BluetoothSocket;

import com.asde.smartalarm.MainActivity;
import com.asde.smartalarm.util.GlobalVariable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


/**
 * Created by Angelo on 12/11/2016.
 */
public class BluetoothConnectionThread extends Thread {
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private GlobalVariable global;
    private MainActivity main;
    private static boolean keepRunning;

    public BluetoothConnectionThread(BluetoothSocket socket, MainActivity main) {
        this.main = main;
        global = global.getInstance();
        global.setBtSocket(socket);
        //this.mMessageHandler = handler;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;

        try {
            //Create I/O streams for connection
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) {
        }
        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    @Override
    public void run() {

        byte[] buffer = new byte[256];//create a temp array of bytes
        int bytes;//size of bytes
        keepRunning = true;
        // Keep looping to listen for received messages
        while (keepRunning) {
            try {
                global.isConnected(true);
                bytes = mmInStream.read(buffer);            //read bytes from input buffer
                String readMessage = new String(buffer, 0, bytes);
                // Send the obtained bytes to the UI Activity via handler
                global.isConnected(true);
                //updater.debugMessage(readMessage);
            } catch (IOException e) {
                e.printStackTrace();
                global.isConnected(false);
                main.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        main.showToast("Conexión perdida");
                    }
                });
                break;
            }
        }
    }


    public static void stopThread(){
        keepRunning = false;
    }

    //write to arduino method
    public void write(String input) {
        byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
        try {
            mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
        } catch (IOException e) {}
    }
}
