package com.example.coolercontrol;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    DrawerLayout drawer;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }
    private String[] PERMISSIONS= new String[] {
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BLUETOOTH_ADMIN,
            Manifest.permission.BLUETOOTH,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_CONNECT
    };
    private static final int INTERNET_PERMISSION_CODE = 100;
    private static final int LOCATIONC_PERMISSION_CODE = 101;
    private static final int LOCATIONF_PERMISSION_CODE = 102;
    private static final int BLUETOOTHA_PERMISSION_CODE = 103;
    private static final int BLUETOOTH_PERMISSION_CODE = 104;
    private static final int BLUETOOTHS_PERMISSION_CODE = 105;
    private static final int BLUETOOTHC_PERMISSION_CODE = 106;
    public String str_count= "20";
    TextView showCountTextView;
    Integer temp_count;

    private void countUp(View view) {
        //Get the value of the text view
        String countString = showCountTextView.getText().toString();
        //Convert value to a number and increment it
        temp_count = Integer.parseInt(countString);
        temp_count++;
        //Display the new value in text view
        showCountTextView.setText(temp_count.toString());
    }

    private void countDwn(View view) {
        //Get the value of the text view
        String countString = showCountTextView.getText().toString();
        //Convert value to a number and increment it
        temp_count= Integer.parseInt(countString);
        temp_count--;
        //Display the new value in text view
        showCountTextView.setText(temp_count.toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button askPermissions = findViewById(R.id.askPermissions);

        findViewById(R.id.refresh_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast myToast = Toast.makeText(MainActivity.this, "System Updated!", Toast.LENGTH_SHORT);
                myToast.show();
            }
        });

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

        askPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermissions(Manifest.permission.ACCESS_COARSE_LOCATION, LOCATIONC_PERMISSION_CODE);
            }
        });

        showCountTextView = (TextView) findViewById(R.id.textview_first);
        showCountTextView.setText(str_count);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

    }

    private void checkPermissions(String permission, int requestCode){
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{ permission }, requestCode);
            }else{
            Toast.makeText(MainActivity.this, "Permission already granted", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){

            case INTERNET_PERMISSION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Internet permission is granted",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this,"Internet permission is denied. Internet usage will be unavailable.",Toast.LENGTH_SHORT).show();
                }
                return;
            case LOCATIONC_PERMISSION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Coarse location permission is granted",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this,"Coarse location permission is denied",Toast.LENGTH_SHORT).show();
                }
                return;
            case LOCATIONF_PERMISSION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Fine location permission is granted",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this,"Fine location permission is denied",Toast.LENGTH_SHORT).show();
                }
                return;
            case BLUETOOTHA_PERMISSION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Bluetooth admin permission is granted",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this,"Bluetooth admin permission is denied",Toast.LENGTH_SHORT).show();
                }
                return;
            case BLUETOOTH_PERMISSION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Bluetooth permission is granted",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this,"Bluetooth permission is denied",Toast.LENGTH_SHORT).show();
                }
                return;
            case BLUETOOTHS_PERMISSION_CODE:
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Bluetooth scan permission is granted",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Bluetooth scan permission is denied",Toast.LENGTH_SHORT).show();
            }
                return;
            case BLUETOOTHC_PERMISSION_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this,"Bluetooth connect permission is granted",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(this,"Bluetooth connect permission is denied",Toast.LENGTH_SHORT).show();
                }
                return;
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
}