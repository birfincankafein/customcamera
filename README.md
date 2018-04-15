
Android Custom Camera Library
===================================

This is an Android library for support both Camera and Camera2 API's. After Android API 21 (Lollipop) library uses [Camera2 API][1], otherwise uses [Camera API][2]. 
This library includes codes from [googlesamples/android-Camera2Video][3] and [googlesamples/android-Camera2Basic][4]. The samples edited for supporting custom features like setting frame rate, setting resolution etc.


Introduction
------------

This library is an interface for uses both Camera and Camera2 api with the same code. You do not need to worry about compatibility. This library still under development and has bugs.
Feel free to contribute.


Demo
------------
[app/][5] folder includes both video and image capture features.

Features
--------
- Automatic Camera API selection.
- Permission handling. No need to check CAMERA and RECORD_AUDIO permissions.
- Rotation handling.
- Result screen support. User can recapture or confirm the captured media.
- Supports Android API >= 16
- Configuration by attributes
  - Save File
  - Resolution
  - Front Camera Access
  - Frame Rate for Video
  - Video Encoding BitRate
  - Max Video Duration

Usage
------------
```java
    // Start video recorder.
    Intent videoIntent = new CameraActivity.Builder(mContext)
            .setActionType(CameraActivity.Type.VIDEO)
            .setSaveFileUri(mPendingMediaUri)
            .setResolution(1080, 1920)
            .setFrontCameraEnabled(true)
            .setFrameRate(30)
            .setMaxVideoDuration(15000)
            .setVideoEncodingBitRate(10000000)
            .build();
    startActivityForResult(videoIntent, ACTIVITY_REQUESTCODEVIDEO);

    // Handle video recorder response
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            ...
            case ACTIVITY_REQUESTCODEVIDEO:
                if(resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        Uri fileUri = bundle.getParcelable(CameraActivity.EXTRA_FILE_URI);
                        ...
                    }
                }
                else{
                    Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();

                }
                break;
            ...
        }
    }
```


Screenshots
-------------

Image Capture

<img src="screenshots/capture_image.png" height="250" alt="Screenshot"/>


Image Picture

<img src="screenshots/capture_video.png" height="250" alt="Screenshot"/>


Image Picture

<img src="screenshots/preview.png" height="250" alt="Screenshot"/>


Demo App

<img src="screenshots/demo.png" height="250" alt="Screenshot"/>


Known Issues
------------
- Image rotation on Samsung Devices.
- Preview strach issue for Camera API
- Result screen MediaController leaks sometimes.

[1]: https://developer.android.com/reference/android/hardware/camera2/package-summary.html
[2]: https://developer.android.com/guide/topics/media/camera.html
[3]: https://github.com/googlesamples/android-Camera2Video
[4]: https://github.com/googlesamples/android-Camera2Basic
[5]: ./app