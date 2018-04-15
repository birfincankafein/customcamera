package com.birfincankafein.customcamera;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.util.Pair;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static com.birfincankafein.customcamera.CameraActivity.TAG;

/**
 * Created by metehantoksoy on 29.03.2018.
 */

public class Video1Fragment extends Fragment {

    private AutoFitTextureView mTextureView;
    private boolean isInPreview;

    // Save bundle to send it to the result fragment
    // Save bundle to send it to the result fragment
    private Bundle bundleExtras;
    private Uri saveFileUri = null;
    private int resolutionHeight = -1;
    private int resolutionWidth = -1;
    private int frameRate = 30;
    private int videoEncodingBitRate = 10000000;
    private long maxVideoDuration = 0;
    private boolean isFrontCameraEnabled = true;
    private CameraActivity.Type type = CameraActivity.Type.PICTURE;
    private onFragmentListener fragmentListener;
    private CameraState cameraState = CameraState.REAR;
    private FlashState flashState = FlashState.OFF;
    private ImageButton mImageButton_Breathe;
    private TextView mTextView_TimeCounter;

    private Camera mCamera;

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

    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private HandlerThread mBackgroundThread;

    /**
     * A {@link Handler} for running tasks in the background.
     */
    private Handler mBackgroundHandler;

    /**
     * Whether the app is recording video now
     */
    private boolean mIsRecordingVideo;

    private MediaRecorder mMediaRecorder;

    private final SimpleDateFormat timeFormat = new SimpleDateFormat("mm:ss");
    private Date initialDate = null;

    private int mCameraOrientation = 0;
    private int mCameraId;

    // Handler for the video record animation
    private final Handler breatheHandler = new Handler();

    // Handler for the video recorder timer.
    private final Handler timerHandler = new Handler();

    // Timer for video record animation.
    private final Runnable breatheRunnable = new Runnable() {
        @Override
        public void run() {
            ValueAnimator anim = new ValueAnimator();
            anim.setIntValues(Color.TRANSPARENT, Color.RED, Color.TRANSPARENT);
            anim.setEvaluator(new ArgbEvaluator());
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    mImageButton_Breathe.setColorFilter((Integer)valueAnimator.getAnimatedValue());
                }
            });

            anim.setDuration(900);
            anim.start();
            breatheHandler.postDelayed(breatheRunnable, 1000);
        }
    };

    // Timer for video recording text and max video duration.
    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            Date currentDate = new Date();
            long diff = currentDate.getTime() - initialDate.getTime();
            String updateStr = timeFormat.format(new Date(diff));
            if(maxVideoDuration > 0){
                updateStr += " / " + timeFormat.format(new Date(maxVideoDuration));
            }
            mTextView_TimeCounter.setText(updateStr);
            if(maxVideoDuration == 0 || diff < maxVideoDuration) {
                timerHandler.postDelayed(timerRunnable, 1000);
            }
            else{
                stopRecording();
            }
        }
    };

    private static ArrayList<Integer> camcorderProfilIds;

    static {
        camcorderProfilIds = new ArrayList<>();
        camcorderProfilIds.add(CamcorderProfile.QUALITY_LOW);
        camcorderProfilIds.add(CamcorderProfile.QUALITY_HIGH);
        camcorderProfilIds.add(CamcorderProfile.QUALITY_QCIF);
        camcorderProfilIds.add(CamcorderProfile.QUALITY_CIF);
        camcorderProfilIds.add(CamcorderProfile.QUALITY_480P);
        camcorderProfilIds.add(CamcorderProfile.QUALITY_720P);
        camcorderProfilIds.add(CamcorderProfile.QUALITY_1080P);
        camcorderProfilIds.add(CamcorderProfile.QUALITY_QVGA);
        camcorderProfilIds.add(CamcorderProfile.QUALITY_TIME_LAPSE_LOW);
        camcorderProfilIds.add(CamcorderProfile.QUALITY_TIME_LAPSE_HIGH);
        camcorderProfilIds.add(CamcorderProfile.QUALITY_TIME_LAPSE_QCIF);
        camcorderProfilIds.add(CamcorderProfile.QUALITY_TIME_LAPSE_CIF);
        camcorderProfilIds.add(CamcorderProfile.QUALITY_TIME_LAPSE_480P);
        camcorderProfilIds.add(CamcorderProfile.QUALITY_TIME_LAPSE_720P);
        camcorderProfilIds.add(CamcorderProfile.QUALITY_TIME_LAPSE_1080P);
        camcorderProfilIds.add(CamcorderProfile.QUALITY_TIME_LAPSE_QVGA);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            camcorderProfilIds.add(CamcorderProfile.QUALITY_2160P);
            camcorderProfilIds.add(CamcorderProfile.QUALITY_TIME_LAPSE_2160P);
            camcorderProfilIds.add(CamcorderProfile.QUALITY_HIGH_SPEED_LOW);
            camcorderProfilIds.add(CamcorderProfile.QUALITY_HIGH_SPEED_HIGH);
            camcorderProfilIds.add(CamcorderProfile.QUALITY_HIGH_SPEED_480P);
            camcorderProfilIds.add(CamcorderProfile.QUALITY_HIGH_SPEED_720P);
            camcorderProfilIds.add(CamcorderProfile.QUALITY_HIGH_SPEED_1080P);
            camcorderProfilIds.add(CamcorderProfile.QUALITY_HIGH_SPEED_2160P);
        }
    }

    public Video1Fragment(){
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
            frameRate = bundleExtras.getInt(CameraActivity.EXTRA_FRAME_RATE);
            videoEncodingBitRate = bundleExtras.getInt(CameraActivity.EXTRA_VIDEO_ENCODING_BITRATE);
            maxVideoDuration = bundleExtras.getLong(CameraActivity.EXTRA_VIDEO_DURATION);
        }
        else{
            // Parameters not supplied, return failed.
            ((CameraActivity)getActivity()).returnResultAndFinish(null);
        }
        return inflater.inflate(R.layout.fragment_video, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mTextureView = (AutoFitTextureView) view.findViewById(R.id.texture);
        view.findViewById(R.id.button_camera).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // AnimationUtil.startShakeRotateAnimation(v);
                cameraState = CameraState.next(cameraState);
                ((ImageButton) v).setImageResource(cameraState.getResourceId());
                closeCamera();
                if (mTextureView.isAvailable()) {
                    openCamera(mTextureView.getWidth(), mTextureView.getHeight());
                } else {
                    mTextureView.setSurfaceTextureListener(mSurfaceTextureListener);
                }
            }
        });
        view.findViewById(R.id.button_flash).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flashState = FlashState.next(flashState);
                ((ImageButton) v).setImageResource(flashState.getResourceId());
            }
        });
        view.findViewById(R.id.button_takevideo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mIsRecordingVideo) {
                    stopRecording();
                } else {
                    startRecording();
                }
            }
        });
        ((ImageButton)view.findViewById(R.id.button_takevideo)).setColorFilter(getActivity().getResources().getColor(android.R.color.white));

        mImageButton_Breathe = view.findViewById(R.id.button_takevideo_breathe);

        mTextView_TimeCounter = view.findViewById(R.id.textView_timecount);
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
        stopBreathe();
        stopTimeCounter();
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
            if (null != mMediaRecorder) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }
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
                pictureSize = getMaxCameraSize(parameters, false);
            }
            else if (resolutionWidth == 0 || resolutionHeight == 0){
                pictureSize = getMinCameraSize(parameters, false);
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

    public int getCameraDisplayOrientationDegree(Camera camera, Camera.CameraInfo camInfo) {
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
        return result;
    }

    public void setCameraDisplayOrientation(Camera camera, Camera.CameraInfo camInfo) {
        mCameraOrientation = getCameraDisplayOrientationDegree(camera, camInfo);
        camera.setDisplayOrientation(mCameraOrientation);
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

    private Camera.Size getMinCameraSize(Camera.Parameters parameters, boolean isForPreview) {
        Camera.Size result=null;
        for (Camera.Size size : isForPreview ?  parameters.getSupportedPreviewSizes() : parameters.getSupportedVideoSizes()) {
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

    private Camera.Size getMaxCameraSize(Camera.Parameters parameters, boolean isForPreview) {
        Camera.Size result=null;

        for (Camera.Size size : isForPreview ?  parameters.getSupportedPreviewSizes() : parameters.getSupportedVideoSizes()) {
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

    private int getBestFrameRate(int frameRate) {
        int nearestFrameRate = frameRate;
        int nearestFrameRateDiff = Integer.MAX_VALUE;
        for(int profileId : camcorderProfilIds){
            if (CamcorderProfile.hasProfile(mCameraId, profileId)) {
                CamcorderProfile profile = CamcorderProfile.get(mCameraId, profileId);
                if(Math.abs(profile.videoFrameRate - nearestFrameRate) < nearestFrameRateDiff){
                    nearestFrameRate = profile.videoFrameRate;
                    nearestFrameRateDiff = Math.abs(profile.videoFrameRate - frameRate);
                }
            }
        }
        return nearestFrameRate;
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
            mCameraId = i;
            return cameraInfo;
        }
        return null;
    }

    private void startBreathe(){
        breatheHandler.postDelayed(breatheRunnable, 1000);
    }

    private void stopBreathe(){
        breatheHandler.removeCallbacks(breatheRunnable);
    }

    private void startTimeCounter(){
        initialDate = new Date();
        mTextView_TimeCounter.setVisibility(View.VISIBLE);
        timerHandler.postDelayed(timerRunnable, 1000);
    }

    private void stopTimeCounter(){
        mTextView_TimeCounter.setVisibility(View.GONE);
        timerHandler.removeCallbacks(timerRunnable);
    }

    protected void stopRecording() {
        // UI
        mIsRecordingVideo = false;
        stopBreathe();
        stopTimeCounter();

        mMediaRecorder.stop();
        mMediaRecorder.release();

        startPreviewSession();
    }

    protected void startRecording() {
        try {
            mMediaRecorder = new MediaRecorder();  // Works well
            mMediaRecorder.setCamera(mCamera);

//        //mMediaRecorder.setPreviewDisplay(mTextureView.getSurfaceTexture());
//        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
//        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//
////        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
////        mMediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.));
//        mMediaRecorder.setVideoSize();
//        mMediaRecorder.setPreviewDisplay(surfaceHolder.getSurface());
//        mMediaRecorder.setOutputFile("/sdcard/zzzz.3gp");

            Camera.Parameters parameters = mCamera.getParameters();

            Camera.Size videoSize;
            if (resolutionWidth == -1 || resolutionHeight == -1) {
                videoSize = getMaxCameraSize(parameters, false);
            } else if (resolutionWidth == 0 || resolutionHeight == 0) {
                videoSize = getMinCameraSize(parameters, false);
            } else {
                videoSize = getBestCameraSize(resolutionWidth, resolutionHeight, parameters, false);
            }
            int supportedFrameRate = getBestFrameRate(frameRate);


            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mMediaRecorder.setOutputFile(getActivity().getContentResolver().openFileDescriptor(saveFileUri, "w").getFileDescriptor());
            mMediaRecorder.setVideoEncodingBitRate(videoEncodingBitRate);
            mMediaRecorder.setVideoFrameRate(supportedFrameRate);
            mMediaRecorder.setVideoSize(videoSize.width, videoSize.height);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setOrientationHint(mCameraOrientation);

            mCamera.unlock();
            mMediaRecorder.prepare();
            mMediaRecorder.start();

            startBreathe();
            startTimeCounter();
            mIsRecordingVideo = true;
        } catch (IOException e) {
            Toast.makeText(getActivity(), "Cannot access the file.", Toast.LENGTH_SHORT).show();
            ((CameraActivity)getActivity()).returnResultAndFinish(null);
        }
    }

}
