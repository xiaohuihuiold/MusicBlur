package com.xhhold.musicblur.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

public class PhoneUtil {

    public static String getBrand() {
        return Build.BRAND;
    }

    public static String getModel() {
        return Build.MODEL;
    }

    public static String getProduct() {
        return Build.PRODUCT;
    }

    public static String getAndroidVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String getAppVersion(Context context) {
        String result = "null";
        PackageManager packageManager = context.getPackageManager();
        try {
            result = packageManager.getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return result;
    }
}