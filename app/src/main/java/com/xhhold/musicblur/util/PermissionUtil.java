package com.xhhold.musicblur.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionUtil {

    public static final String[] PERMISSION_STORAGE=new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    public static boolean request(Activity activity,String[] permissions,int code){
        if(!checkPermissions(activity,permissions)){
            requestPermission(activity,permissions,code);
            return false;
        }else {
            return true;
        }
    }

    private static void requestPermission(Activity activity, String[] permissions, int code){
        ActivityCompat.requestPermissions(activity,permissions,code);
    }

    private static boolean checkPermissions(Activity activity, String[] permissions){
        for(String permission:permissions){
            if(!checkPermission(activity,permission)){
                return false;
            }
        }
        return true;
    }

    public static boolean checkPermissions(int[] grantResults){
        for(int gr:grantResults){
            if(gr== PackageManager.PERMISSION_DENIED){
                return false;
            }
        }
        return true;
    }

    private static boolean checkPermission(Activity activity, String permission){
        return ContextCompat.checkSelfPermission(activity,permission)== PackageManager.PERMISSION_GRANTED;
    }


}