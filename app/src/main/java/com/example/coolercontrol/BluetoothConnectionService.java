package com.example.coolercontrol;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService {
    private static final String TAG = BluetoothConnectionService.class.getSimpleName();
    private static final String appName = "CoolerController";
    private static final UUID myUUID = UUID.fromString("7044e056-b61a-11ec-b909-0242ac120002");

    private final BluetoothAdapter mBluetoothAdapter;
    Context mContext;

    private AcceptThread mAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;


    public BluetoothConnectionService(Context context){
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        start();
    }

    /*This thread runs while listening for incoming connections. It behaves
            like a server-side client. It runs until a connection is accepted
            (or until cancelled).*/
    private class AcceptThread extends Thread{
        //The local server socket
        private final BluetoothServerSocket mmServerSocket;

        public AcceptThread(){
            BluetoothServerSocket tmp = null;

            //Create a new listening server socket
            try {
                tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(appName, myUUID);

                Log.d(TAG, "AcceptThread: Setting up Server using: " + myUUID);
            }catch (IOException e){
                Log.e(TAG,"AcceptThread: IOException: " + e.getMessage());
            }
            mmServerSocket = tmp;
        }

        public void run(){
            Log.d(TAG, "run: AcceptThread Running");

            BluetoothSocket socket = null;
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                Log.d(TAG, "run: RFCOM server socket start....");
                socket = mmServerSocket.accept();
                Log.d(TAG,"run:RFCOM server accepted connection.");
            }catch (IOException e){
                Log.e(TAG,"AcceptThread: IOException: " + e.getMessage());
            }

            //3rd video
            if(socket != null){
                connected(socket, mmDevice);
            }

            Log.i(TAG, "END mAcceptThread");

        }
        public void cancel(){
            Log.d(TAG,"Cancel: Canceling AcceptThread");
            try{
                mmServerSocket.close();
            }catch (IOException e){
                Log.e(TAG,"cancel: Close of AcceptThread Server Socket failed." + e.getMessage());
            }
        }
    }

    /*This thread runs while attempting to make an outgoing connection
            with a device. It runs straight through; connection either
            succeeds or fails.*/
    private class ConnectThread extends Thread{
        private BluetoothSocket mmSocket;

        public ConnectThread(BluetoothDevice device, UUID uuid){
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            deviceUUID = uuid;
        }

        public void run(){
            BluetoothSocket tmp = null;
            Log.i(TAG, "RUN: mConnectThread ");

            try {
                Log.d(TAG, "ConnectThread: Trying to create InsecureRFCommSocket using UUID: " + myUUID);
                //Get a BLuetoothSocket for a connection with the given BluetoothDevice
                tmp = mmDevice.createRfcommSocketToServiceRecord(deviceUUID);
            }catch (IOException e){
                Log.e(TAG,"ConnectThread: Could not create InsecureRfcommSocket: " + e.getMessage());
            }

            mmSocket = tmp;

            //Always cancel discovery because it will slow down connection
            mBluetoothAdapter.cancelDiscovery();

            //Make a connection to the BluetoothSocket
            try{
                //This is a blocking call and will only return successful connection or an exception
                mmSocket.connect();

                Log.d(TAG, "run: ConnectThread connected");
            }catch (IOException e){
                //Close the socket
                try{
                    mmSocket.close();
                    Log.d(TAG,"run: Closed socket");
                }catch (IOException e1) {
                    Log.e(TAG, "mConnectThread: run: Unable to close connection in socket: " + e1.getMessage());
                }
                Log.d(TAG, "run: ConnectThread: Could not connect to UUID: " + myUUID);
            }

            //3rd video
            connected(mmSocket, mmDevice);
        }
        public void cancel(){
            try{
                Log.d(TAG, "cancel: Closing Client Socket");
                mmSocket.close();
            } catch (IOException e){
                Log.e(TAG, "cancel: close() of mmSocket in ConnectThread failed. " + e.getMessage());
            }
        }
    }
    //Start AcceptThread to begin a session in listening (server) mode. Called by Activity onResume()
    public synchronized  void start(){
        Log.d(TAG, "start");

        //Cancel any thread attempting to make a connection
        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }
        if(mAcceptThread == null){
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();
        }
    }

    //AcceptThread starts and sits waiting for a connection
    //Then ConnectThread starts and attempts to make a connection with other devices AcceptThread
    public void startClient(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startClient: Started.");

        //init progress diag
        mProgressDialog = ProgressDialog.show(mContext, "Connecting Bluetooth", "Please Wait...",  true);
        mConnectThread = new ConnectThread(device, uuid);
        mConnectThread.start();
    }

    private class ConnectedThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket){
            Log.d(TAG, "ConnectedThread: Starting.");

            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            //dismiss the progressdialog when connection is established
            try {
                mProgressDialog.dismiss();
            } catch(NullPointerException e){
                Log.e(TAG, "No Progress Dialog box exists " + e.getMessage());
            }

            try{
                tmpIn = mmSocket.getInputStream();
                tmpOut = mmSocket.getOutputStream();
            } catch (IOException e){
                Log.e(TAG, "ConnectedThread: Failed to create an input/output stream: " + e.getMessage());
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run(){
            byte[] buffer = new byte[1024]; //buffer store for the stream
            int bytes; //bytes returned from read()

            //Keep listening to the InputStream until an exception occurs
            while (true){
                //Read from the Input Stream
                try {
                    bytes = mmInStream.read(buffer);
                    String incomingMessage = new String(buffer, 0, bytes);

                    Intent incomingMessageIntent = new Intent("incomingMessage");
                    incomingMessageIntent.putExtra("theMessage", incomingMessage);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);

                    Log.d(TAG, "InputStream: " + incomingMessage);
                } catch (IOException e){
                    Log.e(TAG, "InputStream error reading input " + e.getMessage());
                    break;
                }
            }
        }

        //Call this from main activity to send data to remote device
        public void write(byte[] bytes){
            String text = new String(bytes, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to the outputstream " + text);
            try{
                mmOutStream.write(bytes);
            } catch (IOException e){
                Log.e(TAG, "write: error writing to the outputstream " + e.getMessage());
            }
        }

        //Call this from main activity to shutdown connection
        public void cancel(){
            try{
                mmSocket.close();
            }catch (IOException e){
                Log.e(TAG, "cancel: error canceling connectedThread ");
            }
        }
    }

    private void connected(BluetoothSocket mmSocket, BluetoothDevice mmDevice){
        Log.d(TAG, "connected: Starting.");

        //Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(mmSocket);
        mConnectedThread.start();
    }

    public void write(byte[] out){
        //Create temporary object
        ConnectedThread r;

        //Synchronize a copy of the ConnectedThread
        Log.d(TAG, "Write: write called.");
        //perform the write
        mConnectedThread.write(out);
    }

}
