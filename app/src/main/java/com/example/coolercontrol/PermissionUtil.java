package com.example.coolercontrol;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.List;

public class PermissionUtil {
    private static final int REQUEST_CODE = 100;
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean checkAndRequestPermissions(Activity activity, String...permissions){

        List<String> permissionList = new ArrayList<>();
        for(String permission: permissions){
            int permissionState = activity.checkSelfPermission(permission);
            if(permissionState == PackageManager.PERMISSION_DENIED){
                permissionList.add(permission);
            }
        }
        if(!permissionList.isEmpty()){
            ActivityCompat.requestPermissions(activity, permissionList.toArray(new String[permissionList.size()]), REQUEST_CODE);
            return false;
        }
        return true;
    }
    public static void onRequestPermissionsResult(final Activity activity, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults, final PermissionsCallBack callBack) {
        if (requestCode == PermissionUtil.REQUEST_CODE && grantResults.length > 0) {

            final List<String> permissionsList = new ArrayList<>();
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    permissionsList.add(permissions[i]);
                }
            }

            if (permissionsList.isEmpty() && callBack != null) {
                callBack.permissionsGranted();
            } else {
                boolean showRationale = false;
                for (String permission : permissionsList) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)) {
                        showRationale = true;
                        break;
                    }
                }

                if (showRationale) {
                    showAlertDialog(activity, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.M)
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            checkAndRequestPermissions(activity, permissionsList.toArray(new String[permissionsList.size()]));
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (callBack != null) {
                                callBack.permissionsDenied();
                            }
                        }
                    });
                }
            }
        }
    }

    /**
     * Show alert if any permission is denied and ask again for it.
     *
     * @param context
     * @param okListener
     * @param cancelListener
     */
    private static void showAlertDialog(Context context,
                                        DialogInterface.OnClickListener okListener,
                                        DialogInterface.OnClickListener cancelListener) {
        new AlertDialog.Builder(context)
                .setMessage("Some permissions are not granted. Application may not work as expected. Do you want to grant them?")
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", cancelListener)
                .create()
                .show();
    }

    interface PermissionsCallBack {
        void permissionsGranted();

        void permissionsDenied();
    }
}

