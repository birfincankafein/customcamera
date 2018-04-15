package com.birfincankafein.customcamera;

/**
 * Created by metehantoksoy on 9.04.2018.
 */

public enum CameraState {
    FRONT(R.drawable.ic_camera_front), REAR(R.drawable.ic_camera_rear);

    private final int resourceId;

    CameraState(int resourceId){
        this.resourceId = resourceId;
    }

    public static CameraState next(CameraState state){
        switch (state){
            case FRONT:
                return REAR;
            case REAR:default:
                return FRONT;
        }
    }

    public int getResourceId() {
        return resourceId;
    }
}
