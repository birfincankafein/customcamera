package com.birfincankafein.customcamera;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by metehantoksoy on 4.04.2018.
 */

public interface onFragmentListener {
    void onRequestFragmentChange(FragmentType type, @Nullable Bundle intentData);
    void onRequestFragmentChange(CameraActivity.Type type, @Nullable Bundle intentData);

    enum FragmentType{
        PICTURE1FRAGMENT("TAG_FRAGMENT_PICTURE1", CameraActivity.Type.PICTURE), PICTURE2FRAGMENT("TAG_FRAGMENT_PICTURE2", CameraActivity.Type.PICTURE), VIDEO1FRAGMENT("TAG_FRAGMENT_VIDEO1", CameraActivity.Type.VIDEO), VIDEO2FRAGMENT("TAG_FRAGMENT_VIDEO2", CameraActivity.Type.VIDEO), PREVIEWFRAGMENT("TAG_FRAGMENT_RESULT", null);

        private final String tag;
        private final CameraActivity.Type activityType;

        FragmentType(String tag, CameraActivity.Type type){
            this.tag = tag;
            this.activityType = type;
        }

        public String getTag() {
            return tag;
        }

        public static FragmentType fromActivityType(CameraActivity.Type activityType){
            switch (activityType){
                case VIDEO:
                    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? VIDEO2FRAGMENT : VIDEO1FRAGMENT;
                default:
                    return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? PICTURE2FRAGMENT : PICTURE1FRAGMENT;
            }
        }

        public static FragmentType fromFragmentTag(String tag){
            switch (tag) {
                case "TAG_FRAGMENT_PICTURE2":
                    return PICTURE2FRAGMENT;
                case "TAG_FRAGMENT_VIDEO1":
                    return VIDEO1FRAGMENT;
                case "TAG_FRAGMENT_VIDEO2":
                    return VIDEO2FRAGMENT;
                case "TAG_FRAGMENT_RESULT":
                    return PREVIEWFRAGMENT;
                default:
                    return PICTURE1FRAGMENT;
            }
        }

        public CameraActivity.Type getActivityType() {
            return activityType;
        }
    }
}
