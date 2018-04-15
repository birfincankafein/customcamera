package com.birfincankafein.customcamera;

/**
 * Created by metehantoksoy on 9.04.2018.
 */

public enum FlashState {
    ON(R.drawable.ic_flash_on), AUTO(R.drawable.ic_flash_auto), OFF(R.drawable.ic_flash_off);

    private final int resourceId;
    FlashState(int resourceId) {
        this.resourceId = resourceId;
    }

    public int getResourceId(){
        return resourceId;
    }

    public static FlashState next(FlashState state){
        switch (state){
            case AUTO:
                return ON;
            case OFF:
                return AUTO;
            case ON:
            default:
                return OFF;
        }
    }
}
