//Main Activity (need comments)
package com.example.coolercontrol;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, EasyPermissions.PermissionCallbacks {
    private static final String TAG = "MainActivity";
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    public ArrayList<BluetoothDevice> pairedList = new ArrayList<>();
    public DeviceListAdapter mDeviceListAdapter;
    public String str_count= "20";

    DrawerLayout drawer;
    String deviceName = null;
    String deviceAddress = null;
    BluetoothDevice mBTDevice;
    ListView lvNewDevices;
    TextView showCountTextView;
    TextView status;
    Integer temp_count;

    private BluetoothConnectionService mBluetoothConnection = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private String mConnectedDeviceName = null;
    private StringBuffer mOutStringBuffer;
    private StringBuffer mInStringBuffer;
    private static final int BLUETOOTH_PERMISSION_CODE = 104;
    private static final String[] BLUETOOTH_PERM = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check for bluetooth permissions on start up
        checkBTPermissions();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        status = (TextView) findViewById(R.id.status);

        // Initialize the BluetoothConnectionServer to perform bluetooth connections
        mBluetoothConnection = new BluetoothConnectionService(this, mHandler);

        // Initialize the buffer for outgoing and incoming messages
        mOutStringBuffer = new StringBuffer("");
        mInStringBuffer = new StringBuffer("");




        // Button Functionality
        findViewById(R.id.down_count_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countDwn(view);
            }
        });
        findViewById(R.id.up_count_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                countUp(view);
            }
        });
        findViewById(R.id.btnStartConnection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startBTConnection(mBTDevice);
            }
        });

        showCountTextView = (TextView) findViewById(R.id.textview_first);
        showCountTextView.setText(str_count);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        lvNewDevices = (ListView) findViewById(R.id.lvNewDevices);
        mBTDevices = new ArrayList<>();

        // Controls action of Drawer menu
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    protected void onResume(){
        super.onResume();
        pairedDevicesList();
    }

    public void startBTConnection(BluetoothDevice device){
        Log.d(TAG, "startBTConnection: Initializing Rfcomm Bluetooth Connection.");
        mBluetoothConnection.connect(device, 1, true);
    }
    public void countUp(View view) {
        //Get the value of the text view
        String countString = showCountTextView.getText().toString();
        //Convert value to a number and increment it
        temp_count = Integer.parseInt(countString);
        temp_count++;
        //Display the new value in text view
        showCountTextView.setText(temp_count.toString());

        //Send msg to server
        send(buildMessage("1",1));
    }
    public void countDwn(View view) {
        //Get the value of the text view
        String countString = showCountTextView.getText().toString();
        //Convert value to a number and increment it
        temp_count= Integer.parseInt(countString);
        temp_count--;
        //Display the new value in text view
        showCountTextView.setText(temp_count.toString());

        //Send msg to server
        send(buildMessage("2",1));
    }

    private void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
            if(!EasyPermissions.hasPermissions(this, BLUETOOTH_PERM)){
                EasyPermissions.requestPermissions(this,getString(R.string.rationale_bluetooth), BLUETOOTH_PERMISSION_CODE, BLUETOOTH_PERM);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //Forward results to EasyPermissions
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    public void onPermissionsGranted(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsGranted:" + requestCode + ":" + perms.size());
    }

    public void onPermissionsDenied(int requestCode, @NonNull List<String> perms) {
        Log.d(TAG, "onPermissionsDenied:" + requestCode + ":" + perms.size());

        // (Optional) Check whether the user denied any permissions and checked "NEVER ASK AGAIN."
        // This will display a dialog directing them to enable the permission in app settings.
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            new AppSettingsDialog.Builder(this).build().show();
        }
    }

    //Navigation Drawer handle click events
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_cooler:
                break;
            case R.id.nav_cloud:
                Intent i = new Intent(MainActivity.this,CloudActivity.class);
                startActivity(i);
                break;
            case R.id.nav_gps:
                i = new Intent(MainActivity.this,MapsActivity.class);
                startActivity(i);
                break;
            case R.id.nav_help:
                i = new Intent(MainActivity.this,HelpActivity.class);
                startActivity(i);
                break;
            case R.id.nav_share:
                Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    //Handles opening and closing NavDrawer
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outstate){
        super.onSaveInstanceState(outstate);
        outstate.putString("bun_count", str_count);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        str_count = savedInstanceState.getString("bun_count");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void pairedDevicesList(){
        pairedList.clear();
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        for(BluetoothDevice bt : pairedDevices)
            pairedList.add(bt);

        mDeviceListAdapter = new DeviceListAdapter(getApplicationContext(), R.layout.device_adapter_view, pairedList);
        lvNewDevices.setAdapter(mDeviceListAdapter);
        lvNewDevices.setOnItemClickListener(myListClickListener);
    }

    private AdapterView.OnItemClickListener myListClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            Log.d(TAG, "onItemClick: You clicked a device");
            mBTDevice = pairedList.get(i);
            deviceName = mBTDevice.getName();
            deviceAddress = mBTDevice.getAddress();

            Log.d(TAG, "onItemClick: deviceName = " + deviceName);
            Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        }
    };

    //used to build message to send
    private String buildMessage(String operation, int value){
        return (operation + "," + String.valueOf(value) + "\n");
    }
    /*
     used to send data to bluetooth server
     send(buildMessage("1", 1)
     assign operations to "1", "2", "3",...
    */
    public void send(String message){
        // Check that we're actually connected before trying to send anything
        if(mBluetoothConnection.getState() != BluetoothConnectionService.STATE_CONNECTED){
            Toast.makeText(getApplicationContext(), "can't send message - not connected", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0){
            byte[] send = message.getBytes();
            mBluetoothConnection.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
        }
    }

    private void disconnect(){
        if (mBluetoothConnection != null){
            mBluetoothConnection.stop();
        }
    }

    //displays message in text view
    private void msg(String message){
        TextView statusView = (TextView) findViewById(R.id.status);
        statusView.setText(message);
    }

    private void parseData(String data){
        // add message to the buffer
        mInStringBuffer.append(data);

        // find any complete messages
        String[] messages = mInStringBuffer.toString().split("\\n");
        int noOfMessages = messages.length;

        // does the last message end in a \n, if not its incomplete and should be ignored
        if(!mInStringBuffer.toString().endsWith("\n")){
            noOfMessages = noOfMessages - 1;
        }

        // clean the data buffer of any processed messages
        if (mInStringBuffer.lastIndexOf("\n") > -1)
            mInStringBuffer.delete(0, mInStringBuffer.lastIndexOf("\n") + 1);

    }


    private final Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg){

            switch(msg.what){
                case Constants.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1){
                        case BluetoothConnectionService.STATE_CONNECTED:
                            Log.d("status","connected");
                            msg("Connected to " + deviceName);
                            send("3," + Constants.PROTOCOL_VERSION + "," + Constants.CLIENT_NAME + "\n");
                            break;
                            case BluetoothConnectionService.STATE_CONNECTING:
                                Log.d("status","connecting");
                                msg("Connecting to " + deviceName);
                                break;
                                case BluetoothConnectionService.STATE_LISTEN:
                                    case BluetoothConnectionService.STATE_NONE:
                                        Log.d("status", "not connected'");
                                        msg("Not connected");
                                        disconnect();
                                        break;
                    }
                    break;
                    case Constants.MESSAGE_WRITE:
                        byte[] writeBuf = (byte[]) msg.obj;
                        // construct a string from the buffer
                        String writeMessage = new String(writeBuf);
                        break;
                        case Constants.MESSAGE_READ:
                            byte[] readBuf = (byte[]) msg.obj;
                            // construct a string from the valid bytes in the buffer
                            String readData = new String(readBuf, 0 , msg.arg1);
                            // message received
                            parseData(readData);
                            break;
                            case Constants.MESSAGE_DEVICE_NAME:
                                //save the connected devices name
                                mConnectedDeviceName = msg.getData().getString(Constants.DEVICE_NAME);
                                if( null != this) {
                                    Toast.makeText(getApplicationContext(), "Connected to " + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                                }
                                break;
                                case Constants.MESSAGE_TOAST:
                                    if (null != this){
                                        Toast.makeText(getApplicationContext(), msg.getData().getString(Constants.TOAST), Toast.LENGTH_SHORT).show();
                                    }
                                    break;
            }
        }
    };
}