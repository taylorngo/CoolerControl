package com.example.coolercontrol;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.UUID;

public class BluetoothConnectionService {
    // Debugging
    private static final String TAG = BluetoothConnectionService.class.getSimpleName();

    // Name for the SDP record when creating server socket
    private static final String NAME_SECURE = "CoolerControllerSecure";
    private static final String NAME_INSECURE = "CoolerControllerInsecure";

    // Unique UUID for this application
    private static final UUID MY_UUID_SECURE = UUID.fromString("657bb757-d8e8-44f9-b824-fa2058146822");
    private static final UUID MY_UUID_INSECURE = UUID.fromString("7044e056-b61a-11ec-b909-0242ac120002");

    // Member fields
    private final BluetoothAdapter mBluetoothAdapter;
    private final Handler mHandler;
    Context mContext;
    private AcceptThread mSecureAcceptThread;
    private AcceptThread mInsecureAcceptThread;
    private ConnectThread mConnectThread;
    private ConnectedThread mConnectedThread;
    private BluetoothDevice mmDevice;
    private UUID deviceUUID;
    ProgressDialog mProgressDialog;
    private int mState;
    private int mNewState;

    //Constants that indicate the current connection state
    public static final int STATE_NONE = 0; //we're doing nothing
    public static final int STATE_LISTEN = 1; //now listening for incoming connections
    public static final int STATE_CONNECTING = 2; //now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3; //now connected to a remote device

    
    public BluetoothConnectionService(Context context, Handler handler){
        mContext = context;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        mNewState = mState;
        mHandler = handler;
    }

    //Update UI title according to current state of the chat connection
    private synchronized void updateUserInterfaceTitle(){
        mState = getState();
        Log.d(TAG, "updateUserInterfaceTitle() " + mNewState + " -> " + mState);
        mNewState = mState;

        //Give the new state to the Handler so the UI Activity can update
        mHandler.obtainMessage(Constants.MESSAGE_STATE_CHANGE, mNewState, -1).sendToTarget();
    }
    // Return the current connection state
    public synchronized int getState() {
        return mState;
    }

    /* Start the chat service. Specifically start AcceptThread to begin
       a session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start(){
        Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to listen on a BluetoothServerSocket
        if (mSecureAcceptThread == null){
            mSecureAcceptThread = new AcceptThread(true);
            mSecureAcceptThread.start();
        }
        if (mInsecureAcceptThread == null){
            mInsecureAcceptThread = new AcceptThread(false);
            mInsecureAcceptThread.start();
        }
    }

    /*
     Start the ConnectThread to initiate a connection to a remote device.
     */
    public synchronized void connect(BluetoothDevice device, int port, boolean secure){
        Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if(mState == STATE_CONNECTING){
            if(mConnectThread != null){
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if(mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, port, secure);
        mConnectThread.start();
    }

    /*
     Start the ConnectedThread to begin managing a Bluetooth Connection
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
                                       device, final String socketType){
        Log.d(TAG, "connected, Socket Type: " + socketType);

        // Cancel the thread that completed the connection
        if(mConnectThread != null){
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null){
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (mSecureAcceptThread != null){
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }
        if (mInsecureAcceptThread != null){
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }

        // Start the thread to manage the connection and perform transmission
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        // Send the name of the connect device back to the UI Activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        mHandler.sendMessage(msg);
        // Update UI Title
        updateUserInterfaceTitle();
    }

    /*
     Stop all threads
     */
    public synchronized void stop(){
        Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        if (mSecureAcceptThread != null) {
            mSecureAcceptThread.cancel();
            mSecureAcceptThread = null;
        }

        if (mInsecureAcceptThread != null) {
            mInsecureAcceptThread.cancel();
            mInsecureAcceptThread = null;
        }
        mState = STATE_NONE;
    }

    public void write(byte[] out){
        //Create temporary object
        ConnectedThread r;

        //Synchronize a copy of the ConnectedThread
        synchronized (this) {
            if (mState != STATE_CONNECTED) return;
            r = mConnectedThread;
        }
        Log.d(TAG, "Write: write called.");
        //perform the write
        r.write(out);
    }

    /*
     Indicate that the connection attempt failed and notify the UI activity
     */
    private void connectionFailed(){
        // Send a failure message back to UI activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Unable to connect");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mState = STATE_NONE;
        //Update UI title
        updateUserInterfaceTitle();

        //Start the service over to restart listening mode
        BluetoothConnectionService.this.start();
    }

    /*
     Indicate that the connection was lose and notify the UI activity
     */
    private void connectionLost() {
        // Send a failure message back to UI activity
        Message msg = mHandler.obtainMessage(Constants.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(Constants.TOAST, "Connection lost");
        msg.setData(bundle);
        mHandler.sendMessage(msg);

        mState = STATE_NONE;
        // Update UI title
        updateUserInterfaceTitle();
    }

    /*This thread runs while listening for incoming connections. It behaves
            like a server-side client. It runs until a connection is accepted
            (or until cancelled).*/
    private class AcceptThread extends Thread{
        //The local server socket
        private final BluetoothServerSocket mmServerSocket;
        private String mSocketType;

        public AcceptThread(boolean secure){
            BluetoothServerSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            //Create a new listening server socket
            try {
                if (secure) {
                    tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(NAME_SECURE, MY_UUID_SECURE);
                    Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_SECURE);
                } else{
                    tmp = mBluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME_INSECURE, MY_UUID_INSECURE);
                    Log.d(TAG, "AcceptThread: Setting up Server using: " + MY_UUID_INSECURE);
                }
            }catch (IOException e){
                Log.e(TAG,"AcceptThread: IOException: " + e.getMessage());
            }
            mmServerSocket = tmp;
            mState = STATE_LISTEN;
        }

        public void run(){
            Log.d(TAG, "run: AcceptThread Running. Socket Type: " + mSocketType +
                            "BEGIN mAcceptThread" + this);

            BluetoothSocket socket = null;
            while (mState != STATE_CONNECTED) {
                try {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    Log.d(TAG, "run: RFCOM server socket start....");
                    socket = mmServerSocket.accept();
                    Log.d(TAG, "run:RFCOM server accepted connection.");
                } catch (IOException e) {
                    Log.e(TAG, "AcceptThread: IOException: " + e.getMessage());
                    break;
                }

                //If a connection was accepted
                if (socket != null) {
                    synchronized (BluetoothConnectionService.this) {
                        switch (mState) {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                //Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice(), mSocketType);
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                //Either not read or already connected. Terminate new socket.
                                try{
                                    socket.close();
                                } catch (IOException e){
                                    Log.e(TAG, "Could not close unwanted socket" , e);
                                }
                                break;
                        }
                    }
                }
            }

            Log.i(TAG, "END mAcceptThread, socket Type: " + mSocketType);

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
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, int port, boolean secure){
            Log.d(TAG, "ConnectThread: started.");
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";
            mSocketType += (Integer.toString(port));

            // Get a BluetoothSocket for a connection with the given BluetoothDevice
            // if the port is 0 use the service record to connect, otherwise connect direct
            try{
                if (secure) {
                    if (port == 0) {
                        tmp = device.createRfcommSocketToServiceRecord(MY_UUID_SECURE);
                    }
                    else{
                        Method createRfcommSocket = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                        tmp = (BluetoothSocket) createRfcommSocket.invoke(device,port);
                    }
                } else{
                    if (port == 0) {
                        tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
                    }
                    else{
                        Method createInsecureRfcommSocket = device.getClass().getMethod("createInsecureRfcommSocket", new Class[]{int.class});
                        tmp = (BluetoothSocket) createInsecureRfcommSocket.invoke(device, port);
                    }
                }
            } catch (IOException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e){
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
            }
            mmSocket = tmp;
            mState = STATE_CONNECTING;
        }

        public void run(){
            //Always cancel discovery because it will slow down connection
            mBluetoothAdapter.cancelDiscovery();
            Log.i(TAG, "RUN: mConnectThread SocketType: " + mSocketType);

            try {
                if (mmSocket != null){
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    mmSocket.connect();
                }
                else{
                    connectionFailed();
                    return;
                }

            }catch (IOException e){
                // Close the socket
                try{
                    mmSocket.close();
                } catch (IOException e2){
                    Log.e(TAG,"ConnectThread: Could not create InsecureRfcommSocket: " + e2.getMessage());
                }
                connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothConnectionService.this){
                mConnectThread = null;
            }

            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel(){
            try{
                if (mmSocket != null) {
                    Log.d(TAG, "cancel: Closing Client Socket");
                    mmSocket.close();
                }
            } catch (IOException e){
                Log.e(TAG, "cancel: close() of mmSocket in ConnectThread failed. " + e.getMessage());
            }
        }
    }

    private class ConnectedThread extends Thread{
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
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
            //check if the input and output stream was created
            try{
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e){
                Log.e(TAG, "ConnectedThread: Failed to create an input/output stream: " + e.getMessage());
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            mState = STATE_CONNECTED;
        }

        public void run(){

            //Keep listening to the InputStream until an exception occurs
            while (mState == STATE_CONNECTED){
                //Read from the Input Stream
                try {
                    byte[] buffer = new byte[1024];
                    int bytes;
                    //Read from the InputStream
                    bytes = mmInStream.read(buffer);

                    // Send the obtained bytes to the UI Activity
                    mHandler.obtainMessage(Constants.MESSAGE_READ, bytes, -1, buffer).sendToTarget();

                    /*
                    String incomingMessage = new String(buffer, 0, bytes);
                    Intent incomingMessageIntent = new Intent("incomingMessage");
                    incomingMessageIntent.putExtra("theMessage", incomingMessage);
                    LocalBroadcastManager.getInstance(mContext).sendBroadcast(incomingMessageIntent);
                    */
                } catch (IOException e){
                    Log.e(TAG, "InputStream error reading input " + e.getMessage());
                    connectionLost();
                    break;
                }
            }
        }

        //Call this from main activity to send data to remote device
        public void write(byte[] buffer){
            String text = new String(buffer, Charset.defaultCharset());
            Log.d(TAG, "write: Writing to the outputstream " + text);
            try{
                mmOutStream.write(buffer);

                // Share the sent message back to the UI Activity
                mHandler.obtainMessage(Constants.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
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


}
