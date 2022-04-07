package com.example.coolercontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;
public class BTAcceptThread extends Thread{
    private static BluetoothServerSocket btServerSocket;
    private static boolean connected;
    private static final String TAG = BTAcceptThread.class.getSimpleName();

    /**
     * Takes a bluetooth adapter and a given UUID
     * @param ba -Bluetooth Adapter object
     * @param inId -UUID for application to use
     */
    public BTAcceptThread(BluetoothAdapter ba, UUID inId) {
        cancel();
        BluetoothServerSocket tempServerSocket = null;

        try {
            tempServerSocket = ba.listenUsingRfcommWithServiceRecord("MedicalTransportCooler", inId);
        } catch (IOException d) {
            Log.d(TAG, "Socket's listen() method failed", d);
        }
        btServerSocket = tempServerSocket;
    }

    /**
     * Handles accepting a connection
     */
    public void run() {
        BluetoothSocket socket = null;
        while (true) {
            try {
                socket = btServerSocket.accept();
            } catch (IOException e) {
                Log.e(TAG, "Socket's accept() method failed", e);
                break;
            }

            if (socket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                new ConnectedThread(socket);
                try {
                    btServerSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Socket's close() method failed", e);

                }
                break;
            }
        }
/*
        if(btServerSocket != null) {
            try {
                socket = btServerSocket.accept();
                btServerSocket.close();
            } catch (IOException ioe) {
                try {
                    btServerSocket.close();
                } catch (IOException ioe2) {
                    ioe2.printStackTrace();
                }
            }

            if(socket != null && !BTConnectionManager.getConnectedState()) {
                connected = true;
                new BTConnectionManager(socket, true).start();
            } else {
                cancel();
            }
        }
*/

    }

    /**
     * Returns whether the socket is currently connected
     * @return -connected state as a boolean
     */
    public static boolean getConnection() {
        return connected;
    }

    /**
     * Disconnects the socket
     */
    public static void cancel() {
        connected = false;
        if(btServerSocket != null) {
            try {
                btServerSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "Could not close the connect socket", e);
            }
        }

    }

}
