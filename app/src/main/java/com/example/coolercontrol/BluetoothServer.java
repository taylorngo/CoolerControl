package com.example.coolercontrol;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class BluetoothServer extends AppCompatActivity {
    private final String TAG = BluetoothServer.class.getSimpleName();

    private final BluetoothAdapter mBluetoothAdapter;
    private static BroadcastReceiver mReceiver;
    private static Map<Integer, BluetoothDevice> btDevice;
    private static int discoveryTime, deviceSlot, DISCOVERY_TIME = 120;
    private static boolean btOn, waiting;
    private static Activity activity;
    private static long waitTime;
    private static short btDeviceCount;
    private static UUID serverUUID;

    public BluetoothServer() {
        serverUUID = UUID.fromString("7044e056-b61a-11ec-b909-0242ac120002");
        initializeControlVars();
        if(btDevice == null)
            btDevice = new HashMap<>();

        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        discoveryTime = DISCOVERY_TIME;
        mReceiver = null;
    }
    public BluetoothServer(UUID ourUUID, int discoverytime, String key1, String key2){
        serverUUID = ourUUID;
        DISCOVERY_TIME = discoveryTime;
        initializeControlVars();
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(discoverytime <= 0) {
            discoverytime = 1;
        }

        discoveryTime = discoverytime;
        mReceiver = null;

        //Sets keys
        BTConnectionManager.setKeys(key1, key2);
    }
    private static void initializeControlVars() {
        btDeviceCount = 0;
        waitTime = -1L;
        waiting = false;
        btOn = false;
        deviceSlot = 0;
    }
    /**
     * Starts an intent to request that bluetooth is enabled
     * @param act The activity utilized to present the activity from
     * @return
     */
    public int setBTEnable(Activity act) {
        if(activity == null)
            activity = act;

        if(act == null)
            act = activity;

        byte REQUEST_ENABLE_BT = 1;
        if(this.mBluetoothAdapter != null) {
            if(!this.mBluetoothAdapter.isEnabled()) {
                Intent enableBtIntent = new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE");
                act.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                waiting = true;
                btOn = false;
                return 0;
            } else {
                waiting = false;
                btOn = true;
                return 1;
            }
        } else {
            waiting = false;
            btOn = false;
            return 2;
        }
    }

    /**
     * Start discovering available bluetooth devices and caching them locally
     * @return Number of devices discovered
     */
    public int setBTDiscovery() {
        btDevice.clear();
        deviceSlot = 0;
        if(btOn && !waiting) {
            Intent discoverableIntent = new Intent("android.bluetooth.adapter.action.REQUEST_DISCOVERABLE");
            discoverableIntent.putExtra("android.bluetooth.adapter.extra.DISCOVERABLE_DURATION", discoveryTime);
            activity.startActivity(discoverableIntent);
            //Queue Paired Devices
            Set<BluetoothDevice> pairedDevices = this.mBluetoothAdapter.getBondedDevices();
            if(pairedDevices != null && pairedDevices.size() > 0) {
                for(BluetoothDevice device : pairedDevices){
                    String deviceName = device.getName();
                    String deviceMAC = device.getAddress();
                }
/*                Iterator discoveryActive = pairedDevices.iterator();

                while(discoveryActive.hasNext()) {
                    BluetoothDevice device = (BluetoothDevice)discoveryActive.next();
                    if(device.getUuids() != null) {
                        ParcelUuid[] arr$ = device.getUuids();
                        for(ParcelUuid pu: arr$) {
                            if(pu.getUuid().equals(serverUUID)) {
                                btDevice.put(deviceSlot, device);
                                ++deviceSlot;
                                break;
                            }
                        }
                    }
                }*/
            }

            boolean searching = this.mBluetoothAdapter.startDiscovery();
            waitTime = System.currentTimeMillis();
            if(searching) {
                mReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        String action = intent.getAction();
                        if("android.bluetooth.device.action.FOUND".equals(action)) {
                            BluetoothDevice device = intent.getParcelableExtra("android.bluetooth.device.extra.DEVICE");
                            if(device != null) {
                                boolean duplicate = false;
                                Iterator i$ = BluetoothServer.btDevice.entrySet().iterator();

                                while(i$.hasNext()) {
                                    Map.Entry item = (Map.Entry)i$.next();
                                    BluetoothDevice daD = (BluetoothDevice)item.getValue();
                                    if(daD == null) {
                                        break;
                                    }

                                    if(daD.equals(device)) {
                                        duplicate = true;
                                        break;
                                    }
                                }

                                if(!duplicate && device.getName() != null) {
                                    btDevice.put(deviceSlot, device);
                                }
                            }
                        }

                    }
                };
                activity.registerReceiver(mReceiver, new IntentFilter("android.bluetooth.device.action.FOUND"));
            }
        }
        return deviceSlot;
    }

    /**
     * Updates the state of the connection
     * @param autoStartClient Boolean indicating whether or not the client should automatically fire up
     */
    public void updateState(boolean autoStartClient) {
        if(System.currentTimeMillis() - waitTime > 10000L && waitTime != -1L) {
            waitTime = -1L;
            if(autoStartClient) {
                this.startClient();
            }
        }

        BTConnectionManager.updateState();
    }

    /**
     * Handles callback for turning on bluetooth
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == -1 && !btOn) {
            btOn = true;
            waiting = false;
            this.setBTDiscovery();
        }

    }

    /**
     * Returns whether or not bluetooth is on
     * @return
     */
    public static boolean getBluetoothOn() {
        return btOn;
    }

    /**
     * Unregisters broadcast receiver for finding bluetooth devices
     */
    public void unregisterBroadcastReceiver() {
        if(mReceiver != null) {
            try {
                activity.unregisterReceiver(mReceiver);
                mReceiver = null;
            } catch (IllegalArgumentException iae) {
                iae.printStackTrace();
            }
        }

    }

    /**
     * Starts up a client instance using the default bluetooth adapter, device, and UUID
     */
    public void startClient() {
        BTConnectThread btct = new BTConnectThread(this.mBluetoothAdapter, btDevice, serverUUID);
        btct.start();
    }

    /**
     * Returns the last bluetooth device added to the stack
     * @return last bluetooth device added
     */
    public BluetoothDevice retrieveFoundDevice() {
        if(btDeviceCount < btDevice.size()) {
            ++btDeviceCount;
            if(btDevice.get(btDeviceCount - 1) == null) {
                btDeviceCount = 0;
                return null;
            } else {
                return btDevice.get(btDeviceCount - 1);
            }
        } else {
            btDeviceCount = 0;
            return null;
        }
    }

    /**
     * Returns number of bluetooth devices found
     * @return # devices found
     */
    public int retrieveFoundDeviceCount() {
        return btDevice.size();
    }

    /**
     * Fetches a bluetooth device from a given index
     * @param index index
     * @return Bluetooth device at index
     */
    public BluetoothDevice retrieveDeviceWithIndex(int index) {
        if(index > btDevice.size() - 1)
            index = btDevice.size() - 1;
        else if(index < 0)
            index = 0;
        return btDevice.get(index);
    }

    /**
     * Starts up a bluetooth client instance with a given bluetooth device
     * @param btd BluetoothDevice passed on to start client
     */
    public void startClient(BluetoothDevice btd) {
        new BTConnectThread(this.mBluetoothAdapter, btd, serverUUID).start();
    }

    /**
     * Starts up a bluetooth server instance
     */
    public void startServer() {
        new BTAcceptThread(this.mBluetoothAdapter, serverUUID).start();
    }

    /**
     * Gets our device name
     * @return device name
     */
    public String getOurName() {
        return (this.mBluetoothAdapter != null)?this.mBluetoothAdapter.getName():null;
    }

    /**
     * Sets our device's bluetooth name
     * @param newName New device name
     */
    public void setOurName(String newName) {
        if(this.mBluetoothAdapter != null)
            this.mBluetoothAdapter.setName(newName);
    }

    /**
     * Shuts down all bluetooth activity related to the application asap, no mercy here this is a kill call
     */
    public void stopBluetooth() {
        if(this.mBluetoothAdapter != null)
            this.mBluetoothAdapter.cancelDiscovery();
        BTConnectThread.cancel();
        BTAcceptThread.cancel();
        BTConnectionManager.cancel();
    }

    }


