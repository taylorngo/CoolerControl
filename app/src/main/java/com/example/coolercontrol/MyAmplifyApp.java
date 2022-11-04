package com.example.coolercontrol;

import android.app.Application;
import android.util.Log;

import com.amplifyframework.AmplifyException;
import com.amplifyframework.api.aws.AWSApiPlugin;
import com.amplifyframework.core.Amplify;

public class MyAmplifyApp extends Application {
    public void onCreate(){
        super.onCreate();

        try{
            Amplify.addPlugin(new AWSApiPlugin());
            Amplify.configure(getApplicationContext());
            Log.i("MyAmplifyApp", "Initialize Amplify");
        }catch (AmplifyException error){
            Log.e("MyAmplifyApp", "Could not initialize Amplify", error);
        }
    }
}
