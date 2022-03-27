package com.example.coolercontrol;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    public BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }
    private String[] PERMISSIONS;
    private Button AskPermissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AskPermissions = findViewById(R.id.askPermissions);
        PERMISSIONS = new String[] {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT
        };

        AskPermissions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!hasPermissions(MainActivity.this,PERMISSIONS)){
                    ActivityCompat.requestPermissions(MainActivity.this,PERMISSIONS,1);
                }
            }
        });

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

       if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                    new CoolerFragment()).commit();
            navigationView.setCheckedItem(R.id.nav_cooler);
        }
    }
    private boolean hasPermissions(Context context, String... PERMISSIONS){
        if (context != null && PERMISSIONS != null){
            for(String permission: PERMISSIONS){
                if (ActivityCompat.checkSelfPermission(context,permission) != PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Internet permission is granted",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Internet permission is denied",Toast.LENGTH_SHORT).show();
            }
            if(grantResults[1] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Coarse location permission is granted",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Coarse location permission is denied",Toast.LENGTH_SHORT).show();
            }
            if(grantResults[2] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Background location Permission is granted",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Background location Permission is denied",Toast.LENGTH_SHORT).show();
            }
            if(grantResults[3] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Bluetooth Admin Permission is granted",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Bluetooth Admin Permission is denied",Toast.LENGTH_SHORT).show();
            }
            if(grantResults[4] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission is granted",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Permission is denied",Toast.LENGTH_SHORT).show();
            }
            if(grantResults[5] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission is granted",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Permission is denied",Toast.LENGTH_SHORT).show();
            }
            if(grantResults[6] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission is granted",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(this,"Permission is denied",Toast.LENGTH_SHORT).show();
            }
        }
    }
    //Navigation Drawer handle click events
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_cooler:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new CoolerFragment()).commit();
                break;
            case R.id.nav_cloud:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new CloudFragment()).commit();
                break;
            case R.id.nav_gps:
                Intent i = new Intent(MainActivity.this,MapsActivity.class);
                startActivity(i);
                break;
            case R.id.nav_help:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,
                        new HelpFragment()).commit();
                break;
            case R.id.nav_share:
                Toast.makeText(this, "Share", Toast.LENGTH_SHORT).show();
                break;
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
    //Handles opening and closing NavDrawer
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}