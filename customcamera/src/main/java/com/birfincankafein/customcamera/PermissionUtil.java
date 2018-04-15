package com.birfincankafein.customcamera;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.SparseArray;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

/**
 * Created by metehantoksoy on 9.04.2018.
 */

class PermissionUtil {
    private static final SparseArray<onPermissionResultListener> callbacks = new SparseArray<>();
    private static int requestCodeCounter = 1000;

    protected static void requestPermission(Fragment fragment, String permission, onPermissionResultListener onPermissionResultListener){
        int requestCode = generateRequestCode();
        callbacks.put(requestCode, onPermissionResultListener);

        if( ActivityCompat.checkSelfPermission(fragment.getActivity(), permission) == PackageManager.PERMISSION_GRANTED) {
            onRequestPermissionsResult(requestCode, new String[]{permission}, new int[]{PackageManager.PERMISSION_GRANTED});
        }
        else {
            fragment.requestPermissions(new String[]{permission}, requestCode);
        }
    }
    protected static void requestPermission(Fragment fragment, String[] permissions, onPermissionResultListener onPermissionResultListener){
        int requestCode = generateRequestCode();
        callbacks.put(requestCode, onPermissionResultListener);

        if(isAllPermissionGrantedBefore(fragment.getActivity(), permissions)){
            int[] grantResults = new int[permissions.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        else{
            fragment.requestPermissions(permissions, requestCode);
        }
    }
    protected static void requestPermission(Activity activity, String permission, onPermissionResultListener onPermissionResultListener){
        int requestCode = generateRequestCode();
        callbacks.put(requestCode, onPermissionResultListener);

        if( ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
        }
        else {
            onRequestPermissionsResult(requestCode, new String[]{permission}, new int[]{PackageManager.PERMISSION_GRANTED});
        }
    }
    protected static void requestPermission(Activity activity, String[] permissions, onPermissionResultListener onPermissionResultListener){
        int requestCode = generateRequestCode();
        callbacks.put(requestCode, onPermissionResultListener);

        if(isAllPermissionGrantedBefore(activity, permissions)){
            int[] grantResults = new int[permissions.length];
            Arrays.fill(grantResults, PackageManager.PERMISSION_GRANTED);
            onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        else {
            ActivityCompat.requestPermissions(activity, permissions, requestCode);
        }
    }

    protected static void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        onPermissionResultListener listener = callbacks.get(requestCode);
        if(listener != null){
            callbacks.remove(requestCode);
            listener.onPermissionResult(isAllEqual(grantResults, PackageManager.PERMISSION_GRANTED), requestCode);
        }
    }

    protected interface onPermissionResultListener{
        void onPermissionResult(boolean isSuccess, int requestCode);
    }

    private static boolean isAllPermissionGrantedBefore(Activity activity, String[] permissions){
        for(String permission : permissions){
            if(ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED){
                return false;
            }
        }
        return true;
    }

    private static boolean isAllEqual(int[] collection, int value){
        for(int tmp : collection){
            if(tmp != value) {
                return false;
            }
        }
        return true;
    }
    private static int generateRequestCode(){
        // return new Random().nextInt()/1000 + 1000;
        return ++requestCodeCounter;
    }
}
