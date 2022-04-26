//Main Activity
package com.example.coolercontrol;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
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

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, EasyPermissions.PermissionCallbacks, AdapterView.OnItemClickListener {
    DrawerLayout drawer;

    BluetoothConnectionService mBluetoothConnection;
    private static final UUID myUUID = UUID.fromString("7044e056-b61a-11ec-b909-0242ac120002");
    private static String address = "04:33:C2:68:28:E1";
    BluetoothDevice mBTDevice;
    BluetoothAdapter bluetoothAdapter;//setup bluetooth
    BluetoothSocket btSocket = null;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>(); //BluetoothDevice array list to hold discovered devices
    public DeviceListAdapter mDeviceListAdapter;
    ListView lvNewDevices;
    EditText etSend;
    private OutputStream outStream = null;

    private Set <BluetoothDevice> mPairedDevices;

    private static final String[] BLUETOOTH_PERM = {
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_PRIVILEGED
    };
    private static final int BLUETOOTH_PERMISSION_CODE = 104;

    public String str_count= "20";
    TextView showCountTextView;
    Integer temp_count;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkBTPermissions();

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        etSend = (EditText) findViewById(R.id.editText);


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
        findViewById(R.id.btnONOFF).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(hasBluetoothPermissions()){
                    enableDisableBT();
                }
            }
        });
        findViewById(R.id.btnDiscover).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btnDiscover();
            }
        });
        findViewById(R.id.btnStartConnection).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConnection();
            }
        });
        findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                byte[] bytes = etSend.getText().toString().getBytes(Charset.defaultCharset());
                try{
                    outStream.write(bytes);
                    etSend.clearAnimation();
                }catch (IOException e){
                    Log.e(TAG, " Failed to send message to server");
                }
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

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        //Broadcast when bond state changes
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        registerReceiver(mBroadcastReceiver4, filter);
        lvNewDevices.setOnItemClickListener(MainActivity.this);
    }

    public void startConnection(){
        startBTConnection(mBTDevice, myUUID);
    }

    public void startBTConnection(BluetoothDevice device, UUID uuid){
        Log.d(TAG, "startBTConnection: Initializing Rfcomm Bluetooth Connection.");
        mBluetoothConnection.startClient(device, uuid);
    }
    public void countUp(View view) {
        //Get the value of the text view
        String countString = showCountTextView.getText().toString();
        //Convert value to a number and increment it
        temp_count = Integer.parseInt(countString);
        temp_count++;
        //Display the new value in text view
        showCountTextView.setText(temp_count.toString());
    }
    public void countDwn(View view) {
        //Get the value of the text view
        String countString = showCountTextView.getText().toString();
        //Convert value to a number and increment it
        temp_count= Integer.parseInt(countString);
        temp_count--;
        //Display the new value in text view
        showCountTextView.setText(temp_count.toString());
    }

    public void enableDisableBT(){
        checkBTPermissions();
        if(bluetoothAdapter == null){
            Log.d(TAG,"enableDisableBT: Does not have bluetooth capabilities.");
        }
        if(!bluetoothAdapter.isEnabled()){
            Log.d(TAG,"enableDisableBT: enabling bluetooth.");
            Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enableBTIntent);

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
        if(bluetoothAdapter.isEnabled()){
            Log.d(TAG,"enableDisableBT: turning off bluetooth");
            bluetoothAdapter.disable();

            IntentFilter BTIntent = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(mBroadcastReceiver1, BTIntent);
        }
    }
    //TODO: Crashes but ZTE Phone does not show logcat
/*    public void enableDisableDiscovery(){
        Log.d(TAG, "btnEnableDisableDiscovery: Making device discoverable for 300 seconds.");

        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);

        IntentFilter intentFilter = new IntentFilter(bluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
        registerReceiver(mBroadcastReceiver2, intentFilter);
    }*/
    public void btnDiscover(){
        checkBTPermissions();
        Log.d(TAG, "btnDiscover: Looking for unpaired devices.");

        if(bluetoothAdapter.isDiscovering()){
            bluetoothAdapter.cancelDiscovery();
            Log.d(TAG, "btnDiscover: Cancelling discovery.");

            //check BT permissions
            checkBTPermissions();

            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);
            }
        if(!bluetoothAdapter.isDiscovering()){
            checkBTPermissions();

            bluetoothAdapter.startDiscovery();
            IntentFilter discoverDevicesIntent = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(mBroadcastReceiver3, discoverDevicesIntent);

        }
    }

    private boolean hasBluetoothPermissions(){
        return EasyPermissions.hasPermissions(this, BLUETOOTH_PERM);
    }

    private void checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.S) {
            if(!EasyPermissions.hasPermissions(this, BLUETOOTH_PERM)){
                EasyPermissions.requestPermissions(this,getString(R.string.rationale_bluetooth), BLUETOOTH_PERMISSION_CODE, BLUETOOTH_PERM);
            }
        }
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver mBroadcastReceiver1 = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // When discovery finds a device
            if (action.equals(bluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, bluetoothAdapter.ERROR);

                switch(state){
                    case BluetoothAdapter.STATE_OFF:
                        Log.d(TAG, "onReceive: STATE OFF");
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING OFF");
                        break;
                    case BluetoothAdapter.STATE_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE ON");
                        break;
                    case BluetoothAdapter.STATE_TURNING_ON:
                        Log.d(TAG, "mBroadcastReceiver1: STATE TURNING ON");
                        break;
                }
            }
        }
    };
    //Create a BroadcastReceiver for ACTION_FOUND
/*    private BroadcastReceiver mBroadcastReceiver2 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            if (action.equals(BluetoothAdapter.ACTION_SCAN_MODE_CHANGED)){
                int mode = intent.getIntExtra(BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.ERROR);

                switch(mode){
                    //Device is in discoverable mode
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Log.d(TAG, "BR2: Discoverability enabled");
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Log.d(TAG, "BR2: Discoverability disabled. Able to receive connections from paired devices.");
                        break;
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Log.d(TAG, "BR2: Discoverability disabled. Not able to receive connections.");
                        break;
                    case BluetoothAdapter.STATE_CONNECTING:
                        Log.d(TAG, "BR2: Connecting...");
                        break;
                    case BluetoothAdapter.STATE_CONNECTED:
                        Log.d(TAG, "BR2: Connected");
                        break;
                }
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };*/

    private BroadcastReceiver mBroadcastReceiver3 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action =  intent.getAction();
            Log.d(TAG, "BR3: ACTION_FOUND");

            if(action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "BR3: " + device.getName() + ": " + device.getAddress());
                mDeviceListAdapter = new DeviceListAdapter(context, R.layout.device_adapter_view, mBTDevices);
                lvNewDevices.setAdapter(mDeviceListAdapter);
            }
        }
    };

    private BroadcastReceiver mBroadcastReceiver4 = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "BR4: ACTION FOUND");

            if(action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice mDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //3 cases:
                //case1 : bonded already
                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDED){
                    Log.d(TAG, "BR4: BOND_BONDED");
                    mBTDevice = mDevice;
                }
                //case2: Creating a bond
                if(mDevice.getBondState() == BluetoothDevice.BOND_BONDING){
                    Log.d(TAG, "BR4: BOND_BONDING");
                }
                //case3: breaking a bond
                if(mDevice.getBondState() == BluetoothDevice.BOND_NONE){
                    Log.d(TAG, "BR4: BOND_NONE");
                }
            }
        }
    };

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

    public void onRationaleAccepted(int requestCode) {
        Log.d(TAG, "onRationaleAccepted:" + requestCode);
    }

    public void onRationaleDenied(int requestCode) {
        Log.d(TAG, "onRationaleDenied:" + requestCode);
    }

    @Override
    protected void onDestroy() {
        // Don't forget to unregister the ACTION_FOUND receiver.
        unregisterReceiver(mBroadcastReceiver1);
        unregisterReceiver(mBroadcastReceiver3);
        unregisterReceiver(mBroadcastReceiver4);
        super.onDestroy();
    }

    @Override
    public void onResume(){
        super.onResume();

        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);

        try{
            btSocket = device.createRfcommSocketToServiceRecord(myUUID);
        } catch (IOException e){
            Log.e(TAG, "Failed to create a connection to the UUID: " + e.getMessage());
        }

        bluetoothAdapter.cancelDiscovery();

        try{
            btSocket.connect();
            Log.d(TAG, "Connection Established");
        } catch (IOException e){
            Log.e(TAG, "Connection failed to establish: " + e.getMessage());
        }

        try{
            outStream = btSocket.getOutputStream();
        }catch (IOException e){
            Log.e(TAG, "Output stream connection failed" );
        }
    }

    @Override
    public void onPause(){
        super.onPause();
        if(outStream != null){
            try{
                outStream.flush();
            } catch (IOException e){
                Log.e(TAG, "Failed to flush outstream: " + e.getMessage());
            }
        }
        try{
            btSocket.close();
        } catch (IOException e){
            Log.e(TAG, "Failed to close socket: " + e.getMessage());
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        //first cancel discovery because it is very memory intensive
        bluetoothAdapter.cancelDiscovery();

        Log.d(TAG, "onItemClick: You clicked a device");
        String deviceName = mBTDevices.get(i).getName();
        String deviceAddress = mBTDevices.get(i).getAddress();

        Log.d(TAG, "onItemClick: deviceName = " + deviceName);
        Log.d(TAG, "onItemClick: deviceAddress = " + deviceAddress);

        //Create the bond
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2){
            Log.d(TAG, "Trying to pair with " + deviceName);
            mBTDevices.get(i).createBond();
        }
    }
}