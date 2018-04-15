package com.birfincankafein.customcamerademo;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import com.birfincankafein.customcamera.CameraActivity;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION;

public class MainActivity extends AppCompatActivity implements Initializer {
    private static final String KEY_MEDIA_LIST = "MEDIA_LIST";
    private static final String KEY_RESOLUTION_WIDTH = "Resolution Width";
    private static final String KEY_RESOLUTION_HEIGHT = "Resolution Height";
    private static final String KEY_FRONT_CAMERA = "Front Camera Enabled";
    private static final String KEY_FRAME_RATE = "Frame Rate";
    private static final String KEY_ENCODING_BITRATE = "Video Encoding BitRate";
    private static final String KEY_VIDEO_DURATION = "Max Video Duration";

    private final SimpleDateFormat DATEFORMAT_FILE =  new SimpleDateFormat("yyyyMMdd-HHmmss");
    private final int PERMISSION_REQUESTCODEPICTURE = 12345;
    private final int PERMISSION_REQUESTCODEVIDEO = 23456;
    private final int ACTIVITY_REQUESTCODEPICTURE = 12345;
    private final int ACTIVITY_REQUESTCODEVIDEO = 23456;

    private RecyclerView mRecyclerView_Media;

    private Uri mPendingMediaUri;
    private MediaListAdapter mediaListAdapter;

    private HandlerThread handlerThreadMediaLoader;
    private Handler handlerMediaLoader;
    private Runnable runnableMediaLoader;
    private FloatingActionButton mButton_StartVideo;
    private FloatingActionButton mButton_StartPicture;
    private AlertDialog mAlertDialog_Progress;
    private HashMap<String, String> mHashMap_ImageDefaults;
    private HashMap<String, String> mHashMap_VideoDefaults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews(null);
        initializeVariables();
        initializeViews();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQUESTCODEPICTURE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startPictureActivity();
                }
                else{
                    Toast.makeText(this, R.string.toast_permission, Toast.LENGTH_SHORT).show();
                }
                break;
            case PERMISSION_REQUESTCODEVIDEO:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    startVideoActivity();
                }
                else{
                    Toast.makeText(this, R.string.toast_permission, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("fromSavedState", true);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode){
            case ACTIVITY_REQUESTCODEPICTURE:
                if(resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        Uri fileUri = bundle.getParcelable(CameraActivity.EXTRA_FILE_URI);
                        mediaListAdapter.addMediaItem(new CapturedImage(fileUri, this));
                        storeMediaList();
                    }
                }
                else{
                    Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();

                }
                break;
            case ACTIVITY_REQUESTCODEVIDEO:
                if(resultCode == RESULT_OK) {
                    Bundle bundle = data.getExtras();
                    if (bundle != null) {
                        Uri fileUri = bundle.getParcelable(CameraActivity.EXTRA_FILE_URI);
                        mediaListAdapter.addMediaItem(new CapturedVideo(fileUri, this));
                        storeMediaList();
                    }
                }
                else{
                    Toast.makeText(this, "Canceled", Toast.LENGTH_SHORT).show();

                }
                break;

            default: super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void requestPictureActivity() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startPictureActivity();
        }
        else if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            startPictureActivity();
        }
        else{
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUESTCODEPICTURE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void storeMediaList(){
        getSharedPreferences(KEY_MEDIA_LIST, MODE_PRIVATE).edit().putStringSet(KEY_MEDIA_LIST, getUriPaths()).apply();
    }

    private ArrayList<CapturedMedia> restoreMediaList(){
        Set<String> mediaUriSet = getSharedPreferences(KEY_MEDIA_LIST, MODE_PRIVATE).getStringSet(KEY_MEDIA_LIST, null);
        ArrayList<CapturedMedia> mediaArrayList = new ArrayList<>();
        if(mediaUriSet != null){
            for(String uriString : mediaUriSet){
                Uri mediaUri = Uri.parse(uriString);
                mediaArrayList.add(checkIsImage(mediaUri) ? new CapturedImage(mediaUri, this) : new CapturedVideo(mediaUri, this));
            }
        }
        return mediaArrayList;
    }

    private void requestVideoActivity() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            startVideoActivity();
        }
        else if(ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            startVideoActivity();
        }
        else{
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUESTCODEVIDEO);
        }
    }

    private void startVideoActivity() {
        final Context mContext = this;
        File videoFile = null;
        try {
            videoFile = createMediaFile(".mp4","MOV");
        } catch (IOException ex) {
            // Error occurred while creating the File
        }
        // Continue only if the File was successfully created
        if (videoFile != null) {
            mPendingMediaUri = FileProvider.getUriForFile(mContext,
                    "com.birfincankafein.customcamerademo.fileprovider",
                    videoFile);

            DialogUtil.createInputDialog(this, "Setup Video Capture", mHashMap_VideoDefaults, new DialogUtil.onDialogEventListener() {
                @Override
                public void onDialogEvent(boolean isPositiveButton, @Nullable HashMap<String, String> values) {
                    if(isPositiveButton){
                        mHashMap_VideoDefaults = values;

                        Intent videoIntent = new CameraActivity.Builder(mContext)
                                .setActionType(CameraActivity.Type.VIDEO)
                                .setSaveFileUri(mPendingMediaUri)
                                .setResolution(Integer.parseInt(mHashMap_VideoDefaults.get(KEY_RESOLUTION_WIDTH)), Integer.parseInt(mHashMap_VideoDefaults.get(KEY_RESOLUTION_HEIGHT)))
                                .setFrontCameraEnabled(Boolean.parseBoolean(mHashMap_VideoDefaults.get(KEY_FRONT_CAMERA)))
                                .setFrameRate(Integer.parseInt(mHashMap_VideoDefaults.get(KEY_FRAME_RATE)))
                                .setMaxVideoDuration(Long.parseLong(mHashMap_VideoDefaults.get(KEY_VIDEO_DURATION)))
                                .setVideoEncodingBitRate(Integer.parseInt(mHashMap_VideoDefaults.get(KEY_ENCODING_BITRATE)))
                                .build();
                        startActivityForResult(videoIntent, ACTIVITY_REQUESTCODEVIDEO);
                    }
                }
            }).show();


        }
        else{
            Toast.makeText(mContext, R.string.toast_videosavefailed, Toast.LENGTH_SHORT).show();
        }
    }

    private void startPictureActivity() {
        final Context mContext = this;
        File photoFile = null;
        try {
            photoFile = createMediaFile(".png","IMG");
        } catch (IOException ex) {
            // Error occurred while creating the File
        }
        // Continue only if the File was successfully created
        if (photoFile != null) {
            mPendingMediaUri = FileProvider.getUriForFile(mContext,
                    "com.birfincankafein.customcamerademo.fileprovider",
                    photoFile);
            DialogUtil.createInputDialog(this, "Setup Image Capture", mHashMap_ImageDefaults, new DialogUtil.onDialogEventListener() {
                @Override
                public void onDialogEvent(boolean isPositiveButton, @Nullable HashMap<String, String> values) {
                    if(isPositiveButton){
                        mHashMap_ImageDefaults = values;
                        Intent pictureIntent = new CameraActivity.Builder(mContext)
                                .setActionType(CameraActivity.Type.PICTURE)
                                .setSaveFileUri(mPendingMediaUri)
                                .setResolution(Integer.parseInt(mHashMap_ImageDefaults.get(KEY_RESOLUTION_WIDTH)), Integer.parseInt(mHashMap_ImageDefaults.get(KEY_RESOLUTION_HEIGHT)))
                                .setFrontCameraEnabled(Boolean.parseBoolean(mHashMap_ImageDefaults.get(KEY_FRONT_CAMERA)))
                                .build();
                        startActivityForResult(pictureIntent, ACTIVITY_REQUESTCODEPICTURE);
                    }
                }
            }).show();
        }
        else{
            Toast.makeText(mContext, R.string.toast_picturesavefailed, Toast.LENGTH_SHORT).show();
        }
    }

    private File createMediaFile(String extension, String prefix) throws IOException {
        // Create an image file name
        String fileName = "Demo_" + prefix + DATEFORMAT_FILE.format(new Date()) + extension;
        File fileDir = new File(Environment.getExternalStorageDirectory(), "CustomCameraDemo/");
        if(!fileDir.isDirectory() && !fileDir.mkdirs()){
            Toast.makeText(this, R.string.toast_picturesavefailed, Toast.LENGTH_SHORT).show();
            try {
                return createMediaFileInternal(fileName);
            } catch (IOException ex1) {
                ex1.printStackTrace();
            }
        }
        else{
            try {
                File mediaFile = new File(fileDir, fileName);
                mediaFile.createNewFile();
                return mediaFile;
            }
            catch (Exception ex1){
                try {
                    return createMediaFileInternal(fileName);
                } catch (IOException ex2) {
                    ex2.printStackTrace();
                }
            }
        }
        return null;
    }
    private File createMediaFileInternal(String fileName) throws IOException {
        File internalSaveDir = new File(getFilesDir(), "Captures");
        if( !internalSaveDir.isDirectory() ) internalSaveDir.mkdirs();
        File mediaFile = new File(internalSaveDir, fileName);
        mediaFile.createNewFile();
        return mediaFile;
    }

    public Set<String> getUriPaths() {
        HashSet<String> uriPaths = new HashSet<>();
        for(CapturedMedia media : mediaListAdapter.getCapturedMedia()){
            uriPaths.add(media.getFileUri().toString());
        }
        return uriPaths;
    }

    public boolean checkIsImage(Uri uri) {
        ContentResolver contentResolver = getContentResolver();
        String type = contentResolver.getType(uri);
        if (type != null) {
            return  type.startsWith("image/");
        } else {
            // try to decode as image (bounds only)
            boolean isImage = false;
            InputStream inputStream = null;
            try {
                inputStream = contentResolver.openInputStream(uri);
                if (inputStream != null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(inputStream, null, options);
                    isImage = options.outWidth > 0 && options.outHeight > 0;
                    inputStream.close();
                }
            } catch (IOException e) {
                // ignore
            }
            return isImage;
        }
        // default outcome if image not confirmed
    }

    @Override
    public void findViews(View view) {
        mButton_StartPicture = findViewById(R.id.button_startpicture);
        mButton_StartVideo = findViewById(R.id.button_startvideo);
        mRecyclerView_Media = findViewById(R.id.recyclerView_medialist);

    }

    @Override
    public void initializeVariables() {
        mediaListAdapter = new MediaListAdapter(this);

        mHashMap_ImageDefaults = new HashMap<String, String>();
        mHashMap_ImageDefaults.put(KEY_RESOLUTION_WIDTH,"-1");
        mHashMap_ImageDefaults.put(KEY_RESOLUTION_HEIGHT,"-1");
        mHashMap_ImageDefaults.put(KEY_FRONT_CAMERA,"true");

        mHashMap_VideoDefaults = new HashMap<String, String>();
        mHashMap_VideoDefaults.put(KEY_RESOLUTION_WIDTH,"-1");
        mHashMap_VideoDefaults.put(KEY_RESOLUTION_HEIGHT,"-1");
        mHashMap_VideoDefaults.put(KEY_FRONT_CAMERA,"true");
        mHashMap_VideoDefaults.put(KEY_FRAME_RATE,"30");
        mHashMap_VideoDefaults.put(KEY_ENCODING_BITRATE,"10000000");
        mHashMap_VideoDefaults.put(KEY_VIDEO_DURATION,"0");


        if( getSharedPreferences(KEY_MEDIA_LIST, MODE_PRIVATE).contains(KEY_MEDIA_LIST)) {
            mAlertDialog_Progress = DialogUtil.createProgressDialog(this, null, getString(R.string.message_progressrestore));
            mAlertDialog_Progress.show();

            handlerThreadMediaLoader = new HandlerThread("CapturedMediaLoader");
            handlerThreadMediaLoader.start();

            handlerMediaLoader = new Handler(handlerThreadMediaLoader.getLooper());

            final Context mContext = this;
            runnableMediaLoader = new Runnable() {
                @Override
                public void run() {
                    final ArrayList<CapturedMedia> capturedMediaArrayList = restoreMediaList();
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mediaListAdapter.addMediaItems(capturedMediaArrayList);
                            if(mAlertDialog_Progress != null && mAlertDialog_Progress.isShowing()){
                                mAlertDialog_Progress.dismiss();
                            }
                            Toast.makeText(mContext, "Session restored successfully", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            };

            handlerMediaLoader.post(runnableMediaLoader);
        }
    }

    @Override
    public void initializeViews() {
        mButton_StartPicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestPictureActivity();
            }
        });
        mButton_StartVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestVideoActivity();
            }
        });

        final Context mContext = this;
        mediaListAdapter.setOnItemClickListener(new MediaListAdapter.OnItemClickedListener() {
            @Override
            public void onItemClick(int position, View itemView) {
                Intent galleryIntent = new Intent(Intent.ACTION_VIEW, mediaListAdapter.getCapturedMedia().get(position).getFileUri());
                galleryIntent.setFlags(FLAG_GRANT_READ_URI_PERMISSION);
                if (galleryIntent.resolveActivity(getApplicationContext().getPackageManager()) != null) {
                    startActivity(Intent.createChooser(galleryIntent, "Choose app for this action"));
                }
                else{
                    Toast.makeText(getApplicationContext(), "No app for this action", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onItemLongClick(final int position, View itemView) {
                new AlertDialog.Builder(mContext)
                        .setTitle("Confirmation")
                        .setMessage("Are you sure to delete this item?")
                        .setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mediaListAdapter.permanentlyRemoveItem(position);
                                storeMediaList();
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).show();
            }
        });

        mRecyclerView_Media.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView_Media.setAdapter(mediaListAdapter);
    }
}
