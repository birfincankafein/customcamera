package com.birfincankafein.customcamera;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Size;
import android.widget.Toast;

public class CameraActivity extends AppCompatActivity implements onFragmentListener {
    // Const variables for creating intent
    public static String EXTRA_FILE_URI = "com.birfincankafein.extra.file_uri";
    protected static String EXTRA_RESOLUTION_HEIGHT = "com.birfincankafein.extra.resolution_height";
    protected static String EXTRA_RESOLUTION_WIDTH = "com.birfincankafein.extra.resolution_width";
    protected static String EXTRA_FRONT_CAMERA_ENABLED = "com.birfincankafein.extra.front_camera_enabled";
    protected static final String EXTRA_FRAME_RATE = "com.birfincankafein.extra.frame_rate";
    protected static final String EXTRA_VIDEO_ENCODING_BITRATE = "com.birfincankafein.extra.encoding_bitrate";;
    protected static final String EXTRA_VIDEO_DURATION = "com.birfincankafein.extra.video_duration";
    protected static final String EXTRA_ORIENTATION = "com.birfincankafein.extra.orientation";
    protected final static String EXTRA_TYPE = "com.birfincankafein.extra.type";

    private final static String TAG_LATESTFRAGMENT = "latestfragment";
    protected final static String TAG = "CameraActivity";

    private CameraActivity.Type type = CameraActivity.Type.PICTURE;
    private Bundle bundleExtras;
    private Fragment fragmentInstance;

    public CameraActivity(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        if (savedInstanceState != null) {
            //Restore the fragment's instance
            try {
                fragmentInstance = getSupportFragmentManager().getFragment(savedInstanceState, TAG_LATESTFRAGMENT);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else{
            bundleExtras = getIntent().getExtras();
            if(bundleExtras != null){
                type = (Type) bundleExtras.getSerializable(EXTRA_TYPE);
                onRequestFragmentChange(FragmentType.fromActivityType(type), bundleExtras);
            }
            else{
                returnResultAndFinish( null);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putAll(bundleExtras);
        if(fragmentInstance != null) {
            getSupportFragmentManager().putFragment(outState, TAG_LATESTFRAGMENT, fragmentInstance);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null){
            bundleExtras = savedInstanceState;
            type = (Type) bundleExtras.getSerializable(EXTRA_TYPE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        PermissionUtil.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected void returnResultAndFinish(Uri saveFileUri){
        Intent intent = new Intent();
        intent.putExtra(EXTRA_FILE_URI, saveFileUri);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onRequestFragmentChange(Type type, @Nullable Bundle bundleExtras) {
        onRequestFragmentChange(FragmentType.fromActivityType(type), bundleExtras);
    }

    @Override
    public void onRequestFragmentChange(final FragmentType type, @Nullable final Bundle bundleExtras) {
        String[] permissions = null;
        if(type.getActivityType() != null && type.getActivityType().equals(Type.PICTURE)){
            permissions = new String[]{Manifest.permission.CAMERA};
        }
        else if(type.getActivityType() != null && type.getActivityType().equals(Type.VIDEO)){
            permissions = new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        }

        if(permissions != null){
            PermissionUtil.requestPermission(this, permissions, new PermissionUtil.onPermissionResultListener() {
                @Override
                public void onPermissionResult(boolean isSuccess, int requestCode) {
                    if(isSuccess){
                        requestFragmentChange(type, bundleExtras);
                    }
                    else{
                        Toast.makeText(getApplicationContext(), R.string.toast_permission, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        else{
            requestFragmentChange(type, bundleExtras);
        }
    }

    private void requestFragmentChange(FragmentType type, @Nullable Bundle bundleExtras){
        switch (type){
            case PICTURE2FRAGMENT:
                fragmentInstance = new Picture2Fragment();
                break;
            case VIDEO2FRAGMENT:
                fragmentInstance = new Video2Fragment();
                break;
            case VIDEO1FRAGMENT:
                fragmentInstance = new Video1Fragment();
                break;
            case PREVIEWFRAGMENT:
                fragmentInstance = new ResultFragment();
                break;
            default:
                fragmentInstance = new Picture1Fragment();
                break;
        }

        fragmentInstance.setArguments(bundleExtras);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainer, fragmentInstance, type.getTag())
                .commit();
    }

    public static class Builder {
        private final Context mContext;
        private Uri saveFileUri = null;
        private int resolutionHeight = -1;
        private int resolutionWidth = -1;
        private Type type = Type.PICTURE;
        private boolean frontCameraEnabled = true;
        private int frameRate = 30;
        private int videoEncodingBitRate = 10000000;
        private long maxVideoDuration = 0;

        public Builder(Context context){
            mContext = context;
        }
        /**
         * Set save image resolution. Pass -1,-1 for max, 0,0 for min.
         * @param saveFileUri the uri from file provider.
         * @return this builder instance
         */
        public Builder setSaveFileUri(Uri saveFileUri){
            this.saveFileUri = saveFileUri;
            return this;
        }

        /**
         * Set save image resolution. Pass -1,-1 for max, 0,0 for min.
         * @param resolution of the save image/video, default -1, -1
         * @return this builder instance
         */
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public Builder setResolution(Size resolution){
            this.resolutionWidth = resolution.getWidth();
            this.resolutionHeight = resolution.getHeight();
            return this;
        }

        /**
         * Set save image resolution. Pass -1,-1 for max, 0,0 for min.
         * @param resolutionWidth of image/video, default -1
         * @param resolutionHeight of image/video, default -1
         * @return this builder instance
         */
        public Builder setResolution(int resolutionWidth, int resolutionHeight){
            this.resolutionWidth = resolutionWidth;
            this.resolutionHeight = resolutionHeight;
            return this;
        }

        /**
         * Set front camera status.
         * @param frontCameraEnabled If device supports front camera, control will be enabled if this true, default true
         * @return this builder instance
         */
        public Builder setFrontCameraEnabled(boolean frontCameraEnabled){
            this.frontCameraEnabled = frontCameraEnabled;
            return this;
        }

        /**
         * Set frame rate of the video.
         * @param frameRate of the video. Not effects preview but save file, default 30
         * @return this builder instance
         */
        public Builder setFrameRate(int frameRate){
            this.frameRate = frameRate;
            return this;
        }

        /**
         * Set video encoding bitrate of the video encoder.
         * @param videoEncodingBitRate of the video. Not effects preview but save file, default 10000000
         * @return this builder instance
         */
        public Builder setVideoEncodingBitRate(int videoEncodingBitRate){
            this.videoEncodingBitRate = videoEncodingBitRate;
            return this;
        }

        /**
         * Set maximum duration of the video in milliseconds. 0 for no limit.
         * @param maxVideoDuration of the video. default 0
         * @return this builder instance
         */
        public Builder setMaxVideoDuration(long maxVideoDuration){
            this.maxVideoDuration = maxVideoDuration;
            return this;
        }

        /**
         * Set action type for this activity action.
         * @param actionType Default {@link Type#PICTURE}
         * @return this builder instance
         */
        public Builder setActionType(Type actionType){
            this.type = actionType;
            return this;
        }

        public Intent build(){
            Intent cameraIntent = new Intent(mContext, CameraActivity.class);
            cameraIntent.putExtra(CameraActivity.EXTRA_FILE_URI, saveFileUri);
            cameraIntent.putExtra(CameraActivity.EXTRA_RESOLUTION_WIDTH, resolutionWidth);
            cameraIntent.putExtra(CameraActivity.EXTRA_RESOLUTION_HEIGHT, resolutionHeight);
            cameraIntent.putExtra(CameraActivity.EXTRA_FRONT_CAMERA_ENABLED, frontCameraEnabled);
            cameraIntent.putExtra(CameraActivity.EXTRA_FRAME_RATE, frameRate);
            cameraIntent.putExtra(CameraActivity.EXTRA_VIDEO_ENCODING_BITRATE, videoEncodingBitRate);
            cameraIntent.putExtra(CameraActivity.EXTRA_VIDEO_DURATION, maxVideoDuration);

            cameraIntent.putExtra(EXTRA_TYPE, type);
            return cameraIntent;
        }

    }
    public enum Type{
        VIDEO("video"), PICTURE("picture");
        private final String typeStr;
        Type(String typeStr) {
            this.typeStr = typeStr;
        }
    }
    public enum Orientation{
        PORTRAIT, LANDSCAPE_REVERSE, LANDSCAPE, LANDSCAPE_AUTO, AUTO
    }
}
