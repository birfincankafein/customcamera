package com.birfincankafein.customcamerademo;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Parcel;
import android.provider.MediaStore;

import java.io.IOException;

/**
 * Created by metehantoksoy on 10.04.2018.
 */

public class CapturedVideo extends CapturedMedia {
    private String resolution = "";
    private String bitRate = "";
    private String frameRate = "";
    private String rotation = "";
    private String duration = "";
    private String hasAudioVideo = "";
    private String mimeType = "";

    public CapturedVideo(Uri fileUri, Context context) {
        super(fileUri, FileType.VIDEO, context);
        initSpecificProperties(context);
    }

    @Override
    protected void initSpecificProperties(Context mContext) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mContext, getFileUri());
        resolution = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH) + "x" +
                        retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT);
        bitRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
        if(bitRate != null){
            bitRate += " bps - " + String.valueOf(Integer.parseInt(retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE))/8192) + " KBps";
        }
        duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        if(duration != null){

            duration = Math.ceil(Double.parseDouble(duration)/1000) + " sec";
        }
        mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
        hasAudioVideo = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_AUDIO) +
                " / " + retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_HAS_VIDEO);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            rotation = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION);
        }

        frameRate = getFrameRate(retriever, mContext);
        retriever.release();
    }

    @Override
    protected Bitmap createThumbnail(Context mContext){
        Bitmap thumbnail = ThumbnailUtils.createVideoThumbnail(getFileUri().getPath(), MediaStore.Images.Thumbnails.MINI_KIND);
        if(thumbnail == null){
            thumbnail = ThumbnailUtils.createVideoThumbnail(getFileUri().getPath(), MediaStore.Images.Thumbnails.MICRO_KIND);
            if(thumbnail == null){
                thumbnail = ThumbnailUtils.createVideoThumbnail(getFileUri().getPath(), MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
                if (thumbnail == null) {
                    try {
                        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                        retriever.setDataSource(mContext, getFileUri());
                        thumbnail = retriever.getFrameAtTime();

                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        }

        int desiredWidth, desiredHeight;
        if(thumbnail != null){
            if(thumbnail.getWidth() > thumbnail.getHeight()){
                desiredWidth = 512;
                desiredHeight = desiredWidth*thumbnail.getHeight()/thumbnail.getWidth();
            }
            else{
                desiredHeight = 512;
                desiredWidth = desiredHeight*thumbnail.getWidth()/thumbnail.getHeight();
            }
            thumbnail = Bitmap.createScaledBitmap(thumbnail, desiredWidth, desiredHeight, false);
        }
        return thumbnail;
    }

    private String getFrameRate(MediaMetadataRetriever retriever, Context mContext){
        String frameRateTmp = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            frameRateTmp = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE);
        }

        if(frameRateTmp == null){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                MediaExtractor extractor = new MediaExtractor();
                try {
                    //Adjust data source as per the requirement if file, URI, etc.
                    extractor.setDataSource(mContext.getContentResolver().openAssetFileDescriptor(getFileUri(), "r"));
//                    extractor.setDataSource(getFileUri().getPath());
                    int numTracks = extractor.getTrackCount();
                    for (int i = 0; i < numTracks; ++i) {
                        MediaFormat format = extractor.getTrackFormat(i);
                        String mime = format.getString(MediaFormat.KEY_MIME);
                        if (mime.startsWith("video/")) {
                            if (format.containsKey(MediaFormat.KEY_FRAME_RATE)) {
                                frameRate = String.valueOf(format.getInteger(MediaFormat.KEY_FRAME_RATE));
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    //Release stuff
                    extractor.release();
                }
            }
        }

        if(frameRateTmp == null){
            frameRateTmp = "-1";
        }
        return frameRateTmp;
    }

    public String getResolution() {
        return resolution;
    }

    public String getBitRate() {
        return bitRate;
    }

    public String getFrameRate() {
        return frameRate;
    }

    public String getRotation() {
        return rotation;
    }

    public String getDuration() {
        return duration;
    }

    public String getHasAudioVideo() {
        return hasAudioVideo;
    }

    public String getMimeType() {
        return mimeType;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.resolution);
        dest.writeString(this.bitRate);
        dest.writeString(this.frameRate);
        dest.writeString(this.rotation);
        dest.writeString(this.duration);
        dest.writeString(this.hasAudioVideo);
        dest.writeString(this.mimeType);
        dest.writeParcelable(getFileUri(), flags);
        dest.writeInt(getFileType() == null ? -1 : getFileType().ordinal());
        dest.writeParcelable(getThumbnail(), flags);
        dest.writeString(getFileName());
        dest.writeString(getFileSize());
    }

    protected CapturedVideo(Parcel in) {
        super(in);
        this.resolution = in.readString();
        this.bitRate = in.readString();
        this.frameRate = in.readString();
        this.rotation = in.readString();
        this.duration = in.readString();
        this.hasAudioVideo = in.readString();
        this.mimeType = in.readString();
    }

    public static final Creator<CapturedVideo> CREATOR = new Creator<CapturedVideo>() {
        @Override
        public CapturedVideo createFromParcel(Parcel source) {
            return new CapturedVideo(source);
        }

        @Override
        public CapturedVideo[] newArray(int size) {
            return new CapturedVideo[size];
        }
    };
}
