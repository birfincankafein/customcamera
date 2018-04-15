package com.birfincankafein.customcamera;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.VideoView;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;

import static com.birfincankafein.customcamera.CameraActivity.EXTRA_FILE_URI;
import static com.birfincankafein.customcamera.CameraActivity.EXTRA_TYPE;


/**
 * A simple {@link Fragment} subclass.
 */
public class ResultFragment extends Fragment {

    private ImageButton mImageButton_Recapture;
    private ImageButton mImageButton_Done;
    private VideoView mVideoView_Preview;
    private ImageView mImageView_Preview;
    private MediaController mMediaController;

    private Bundle bundleExtras;
    private Uri fileUri;
    private CameraActivity.Type type;
    private onFragmentListener fragmentListener;
    private int mVideoPosition = 0;

    public ResultFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        // Inflate the layout for this fragment
        bundleExtras = getArguments();
        if(bundleExtras != null){
            fileUri = bundleExtras.getParcelable(EXTRA_FILE_URI);
            type = (CameraActivity.Type) bundleExtras.getSerializable(EXTRA_TYPE);
        }
        else{
            ((CameraActivity)getActivity()).returnResultAndFinish( null);
        }

        View fragmentView = inflater.inflate(R.layout.fragment_result, container, false);

        mImageButton_Recapture = fragmentView.findViewById(R.id.imageButton_recapture);
        mImageButton_Done = fragmentView.findViewById(R.id.imageButton_done);
        mVideoView_Preview = fragmentView.findViewById(R.id.videoView_preview);
        mImageView_Preview = fragmentView.findViewById(R.id.imageView_preview);

        if(type.equals(CameraActivity.Type.PICTURE)){
            mVideoView_Preview.setVisibility(View.GONE);
            mImageView_Preview.setVisibility(View.VISIBLE);
            mImageView_Preview.setImageURI(fileUri);
        }
        else{
            mVideoView_Preview.setVisibility(View.VISIBLE);
            mImageView_Preview.setVisibility(View.GONE);

            // Set the media controller buttons
            if (mMediaController == null) {
                mMediaController = new MediaController(getContext()){
                    @Override
                    public void hide() {
                        // TODO Auto-generated method stub
                        super.show();
                    }
                };
                // Set the videoView that acts as the anchor for the MediaController.
                mMediaController.setAnchorView(mVideoView_Preview);
                // Set MediaController for VideoView
                mVideoView_Preview.setMediaController(mMediaController);
            }

            try {
                mVideoView_Preview.setVideoURI(fileUri);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            mVideoView_Preview.requestFocus();

            // When the video file ready for playback.
            mVideoView_Preview.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                public void onPrepared(MediaPlayer mediaPlayer) {
                    if (mVideoPosition != 0) {
                        mVideoView_Preview.seekTo(mVideoPosition);
                    }
                    mVideoView_Preview.start();
                    mMediaController.show(250);
                    // When video Screen change size.
                    mediaPlayer.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                        @Override
                        public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {

                            // Re-Set the videoView that acts as the anchor for the MediaController
                            mMediaController.setAnchorView(mVideoView_Preview);
                        }
                    });
                }
            });
        }

        mImageButton_Recapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AnimationUtil.startShakeRotateAnimation(v,new AnimationUtil.AnimationListener() {
                    @Override
                    public void onStart() {

                    }

                    @Override
                    public void onEnd() {
                        fragmentListener.onRequestFragmentChange(type, bundleExtras);
                    }
                });
                getActivity().getContentResolver().delete(fileUri, null, null);
            }
        });

        mImageButton_Done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((CameraActivity)getActivity()).returnResultAndFinish(fileUri);
//                returnResultAndFinish(createThumbnail(), fileUri);
            }
        });

        return fragmentView;
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

    // When you change direction of phone, this method will be called.
    // It store the state of video (Current position)
    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        // Store current position.
        savedInstanceState.putInt("CurrentPosition", mVideoView_Preview.getCurrentPosition());
        savedInstanceState.putAll(bundleExtras);
        mVideoView_Preview.pause();
    }

    // After rotating the phone. This method is called.
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey("CurrentPosition") ) {
            // Restore last state for checked position.
            // Get saved position.
            mVideoPosition = savedInstanceState.getInt("CurrentPosition");
            bundleExtras = savedInstanceState;
            mVideoView_Preview.seekTo(mVideoPosition);
        }
    }
}
