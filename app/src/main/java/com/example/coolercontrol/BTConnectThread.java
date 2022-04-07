package com.example.coolercontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import com.example.coolercontrol.BTAcceptThread;
import com.example.coolercontrol.BTConnectionManager;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BTConnectThread extends Thread{
    private static BluetoothSocket btSocket;
    private BluetoothAdapter bluetoothAdapter;
    private Map<Integer, BluetoothDevice> btDeviceList;
    private static byte working;
    private static boolean connected;
    private static UUID ourUUID;

    public BTConnectThread(BluetoothAdapter ba, Map<Integer, BluetoothDevice> btd, UUID inId) {
        cancel();
        this.bluetoothAdapter = ba;
        this.btDeviceList = btd;
        ourUUID = inId;
    }

    public BTConnectThread(BluetoothAdapter ba, BluetoothDevice btd, UUID inId) {
        cancel();
        this.bluetoothAdapter = ba;
        this.btDeviceList = new HashMap<>();
        this.btDeviceList.put(Integer.valueOf(0), btd);
        ourUUID = inId;
    }

    //todo on the first run the app never seems to be able to find anyone...i wonder why that is..
    public void run() {
        working = 1;
        this.bluetoothAdapter.cancelDiscovery();

        while(this.bluetoothAdapter.isDiscovering()) {
            //todo this empty spinner is not really a good idea...
        }

        for(int x = 0; x < this.btDeviceList.size(); ++x) {
            if(this.btDeviceList.get(Integer.valueOf(x)) != null) {
                try {
                    btSocket = ((BluetoothDevice)this.btDeviceList.get(Integer.valueOf(x))).createRfcommSocketToServiceRecord(ourUUID);
                } catch (IOException var5) {

                }

                try {
                    btSocket.connect();
                } catch (Exception var6) {
                    try {
                        btSocket.close();
                    } catch (Exception var4) {

                    }

                    if(x == this.btDeviceList.size() - 1) {
                        working = 0;
                    }
                    continue;
                }

                if(!BTAcceptThread.getConnection()) {
                    connected = true;
                    BTConnectionManager btcm = new BTConnectionManager(btSocket, false);
                    btcm.start();
                    working = 0;
                    break;
                }

                cancel();
            } else if(this.btDeviceList.get(Integer.valueOf(x)) == null) {
                working = 0;
                break;
            }
        }

    }

    public static boolean getConnected() {
        return connected;
    }

    public static void cancel() {
        connected = false;
        if(btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException var1) {

            }
        }

        btSocket = null;
    }

    public static byte getWorkingState() {
        return working;
    }
}
