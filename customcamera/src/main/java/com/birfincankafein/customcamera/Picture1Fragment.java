package com.birfincankafein.customcamera;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static com.birfincankafein.customcamera.CameraActivity.TAG;

/**
 * Created by metehantoksoy on 29.03.2018.
 */

public class Picture1Fragment extends Fragment {

    //
    private AutoFitTextureView mTextureView;
    private boolean isInPreview;

    // Save bundle to send it to the result fragment
    private Bundle bundleExtras;

    // The parameters obtained from Intent call.
    private Uri saveFileUri = null;
    private int resolutionHeight = -1;
    private int resolutionWidth = -1;
    private boolean isFrontCameraEnabled = true;
    private CameraActivity.Type type = CameraActivity.Type.PICTURE;
    private onFragmentListener fragmentListener;
    private CameraState cameraState = CameraState.REAR;
    private FlashState flashState = FlashState.OFF;

    private Camera mCamera;
    private Camera.CameraInfo mBackCameraInfo;

    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            openCamera(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            if (mCamera != null) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };

    private Camera.PictureCallback photoCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            mBackgroundHandler.post(new ImageSaver(data, saveFileUri, getActivity()));
        }
    };

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    public Picture1Fragment(){
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        bundleExtras = getArguments();
        if(bundleExtras != null){
            saveFileUri = bundleExtras.getParcelable(CameraActivity.EXTRA_FILE_URI);
            resolutionHeight = bundleExtras.getInt(CameraActivity.EXTRA_RESOLUTION_HEIGHT);
            resolutionWidth = bundleExtras.getInt(CameraActivity.EXTRA_RESOLUTION_WIDTH);
            isFrontCameraEnabled = bundleExtras.getBoolean(CameraActivity.EXTRA_FRONT_CAMERA_ENABLED);
        }
        else{
            ((CameraActivity)getActivity()).returnResultAndFinish(null);
        }
        return inflater.inflate(R.layout.fragment_picture, container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        view.findViewById(R.id.button_takepicture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimationUtil.startShakeRotateAnimation(v);
                takePicture();
            }
        });
        view.findViewById(R.id.button_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // AnimationUtil.startShakeRotateAnimation(v);
                cameraState = CameraState.next(cameraState);
                ((ImageButton) v).setImageResource(cameraState.getResourceId());
                startCamera();
            }
        });
        view.findViewById(R.id.button_flash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flashState = FlashState.next(flashState);
                ((ImageButton) v).setImageResource(flashState.getResourceId());
            }
        });

        view.findViewById(R.id.button_camera).setEnabled(isFrontCameraEnabled);

        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        startBackgroundThread();
        // When the screen is turned off and turned back on, the SurfaceTexture is already
        // available, and "onSurfaceTextureAvailable" will not be called. In that case, we can open
        // a camera and start preview from here (otherwise, we wait until the surface is ready in
        // the SurfaceTextureListener).
        startCamera();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            this.fragmentListener = (onFragmentListener) context;
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

    private void startCamera(){
        closeCamera();
        if (mTextureView.isAvailable()) {
            openCamera(mTextureView.getWidth(), mTextureView.getHeight());
        } else {
            mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
        }
    }

    private void takePicture() {
        if (isInPreview && mCamera != null) {
            mCamera.takePicture(null, null, photoCallback);
        }
    }

    private void openCamera(int width, int height) {
        if(isInPreview){
            closeCamera();
        }
        Camera.CameraInfo cameraInfo = detectAndOpenDesiredCamera();
        initPreview(width, height, cameraInfo);
        try {
            mCamera.setPreviewTexture(mTextureView.getSurfaceTexture());
            mCamera.startPreview();
            isInPreview = true;
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        if(mCamera != null){
            mCamera.stopPreview();
            mCamera.release();
            isInPreview = false;
        }
        mCamera=null;
    }

    private void initPreview(int width, int height, Camera.CameraInfo cameraInfo) {
        if (mCamera != null && mTextureView.getSurfaceTexture() != null) {
            try {
                mCamera.setPreviewTexture(mTextureView.getSurfaceTexture());
            }
            catch (Throwable t) {
                Log.e(TAG,
                        "Exception in setPreviewDisplay()", t);
                Toast.makeText(getContext(), t.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
            Camera.Parameters parameters = mCamera.getParameters();
            setCameraDisplayOrientation(mCamera, cameraInfo);
            setFlashMode(parameters);
            setFocusMode(parameters);
            setWhiteBalanceMode(parameters);

            Camera.Size size = getBestCameraSize(width, height, parameters, true);
            Camera.Size pictureSize;
            if(resolutionWidth == -1 || resolutionHeight == -1){
                pictureSize = getMaxCameraSize(parameters);
            }
            else if (resolutionWidth == 0 || resolutionHeight == 0){
                pictureSize = getMinCameraSize(parameters);
            }
            else{
                pictureSize = getBestCameraSize(resolutionWidth, resolutionHeight, parameters, false);
            }

            if (size != null && pictureSize != null) {
                parameters.setPreviewSize(size.width, size.height);
                parameters.setPictureSize(pictureSize.width,
                        pictureSize.height);
                parameters.setPictureFormat(ImageFormat.JPEG);
                mCamera.setParameters(parameters);
            }

        }
    }

    public void setCameraDisplayOrientation(Camera camera, Camera.CameraInfo camInfo) {
        Display display = ((WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        int rotation = display.getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (camInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (camInfo.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (camInfo.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    public void setFlashMode(Camera.Parameters parameters){
        List<String> supportedFlashModes = parameters.getSupportedFlashModes();
        if(supportedFlashModes != null) {
            if (flashState.equals(FlashState.ON) && supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_ON)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            } else if (flashState.equals(FlashState.AUTO) && supportedFlashModes.contains(Camera.Parameters.FLASH_MODE_AUTO)){
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            }
            else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            }
        }
    }

    public void setFocusMode(Camera.Parameters parameters) {
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
        if(supportedFocusModes != null) {
            if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            } else if (supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            }
        }
    }

    public void setWhiteBalanceMode(Camera.Parameters parameters) {
        List<String> supportedWhiteBalance = parameters.getSupportedWhiteBalance();
        if(supportedWhiteBalance != null) {
            if (supportedWhiteBalance.contains(Camera.Parameters.WHITE_BALANCE_AUTO)) {
                parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
            }
        }
    }

    /**
     * Starts a background thread and its {@link Handler}.
     */
    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    /**
     * Stops the background thread and its {@link Handler}.
     */
    private void stopBackgroundThread() {
        mBackgroundThread.quit();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startPreviewSession() {
        fragmentListener.onRequestFragmentChange(onFragmentListener.FragmentType.PREVIEWFRAGMENT, bundleExtras);
    }

    private Camera.Size getBestCameraSize(int width, int height, Camera.Parameters parameters, boolean isForPreview) {
        Camera.Size result=null;

        for (Camera.Size size : isForPreview ?  parameters.getSupportedPreviewSizes() : parameters.getSupportedVideoSizes()) {
            if (size.width <= width && size.height <= height) {
                if (result == null) {
                    result=size;
                }
                else {
                    int resultArea=result.width * result.height;
                    int newArea=size.width * size.height;

                    if (newArea > resultArea) {
                        result=size;
                    }
                }
            }
        }

        return(result);
    }

    private Camera.Size getMinCameraSize(Camera.Parameters parameters) {
        Camera.Size result=null;
        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            if (result == null) {
                result=size;
            }
            else {
                int resultArea=result.width * result.height;
                int newArea=size.width * size.height;

                if (newArea < resultArea) {
                    result=size;
                }
            }
        }
        return(result);
    }

    private Camera.Size getMaxCameraSize(Camera.Parameters parameters) {
        Camera.Size result=null;

        for (Camera.Size size : parameters.getSupportedPictureSizes()) {
            if (result == null) {
                result=size;
            }
            else {
                int resultArea = result.width * result.height;
                int newArea = size.width * size.height;

                if (newArea > resultArea) {
                    result=size;
                }
            }
        }

        return(result);
    }

    private Camera.CameraInfo detectAndOpenDesiredCamera() {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        final int numberOfCameras = Camera.getNumberOfCameras();

        for (int i = 0; i < numberOfCameras; ++i) {
            Camera.getCameraInfo(i, cameraInfo);

            if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT && !cameraState.equals(CameraState.FRONT)){
                continue;
            }
            else if(cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK && !cameraState.equals(CameraState.REAR)){
                continue;
            }
            mCamera = Camera.open(i);
            return cameraInfo;
        }
        return null;
    }

    /**
     * Saves a JPEG {@link Image} into the specified {@link Uri}.
     */
    private class ImageSaver implements Runnable {

        /**
         * The JPEG image
         */
        private final byte[] data;
        /**
         * The uri we save the image into.
         */
        private final Uri mFileUri;

        private final Context mContext;

        ImageSaver(byte[] imageData, Uri fileUri, Context context) {
            data = imageData;
            mFileUri = fileUri;
            mContext = context;
        }

        @Override
        public void run() {
            try {
                OutputStream stream = mContext.getContentResolver().openOutputStream(mFileUri);
                stream.write(data);
                stream.close();
                startPreviewSession();
            }
            catch (IOException e){
                e.printStackTrace();
                Toast.makeText(mContext, "File cannot be saved.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
