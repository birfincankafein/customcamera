package com.birfincankafein.customcamerademo;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by metehantoksoy on 10.04.2018.
 */

public class MediaListAdapter extends RecyclerView.Adapter {

    private final List<CapturedMedia> capturedMedia;
    private final Context mContext;
    private OnItemClickedListener onItemClickListener;

    public MediaListAdapter(Context mContext, List<CapturedMedia> capturedMedia){
        this.capturedMedia = capturedMedia;
        this.mContext = mContext;
    }
    public MediaListAdapter(Context mContext){
        this(mContext, new ArrayList<CapturedMedia>());
    }

    public void addMediaItem(CapturedMedia capturedMedia){
        this.capturedMedia.add(capturedMedia);
        notifyItemChanged(this.capturedMedia.size()-1);
    }
    public void addMediaItems(CapturedMedia[] mediaItems) {
        addMediaItems(Arrays.asList(mediaItems));
    }

    public void addMediaItems(List<CapturedMedia> mediaItems) {
        int beforeCount = capturedMedia.size();
        this.capturedMedia.addAll(mediaItems);
        notifyItemRangeChanged(beforeCount, mediaItems.size());
    }

    public List<CapturedMedia> getCapturedMedia() {
        return capturedMedia;
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return viewType == 0 ? new ImageViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_image, null))
                : new VideoViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_video, null));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CapturedMedia media = capturedMedia.get(position);
        if(media.getFileType().equals(CapturedMedia.FileType.PICTURE)){
            CapturedImage capturedImage = (CapturedImage) media;
            ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
            imageViewHolder.mTextView_FileName.setText(capturedImage.getFileName());
            imageViewHolder.mTextView_FileSize.setText(capturedImage.getFileSize());
            imageViewHolder.mTextView_Resolution.setText(capturedImage.getResolution());
            imageViewHolder.mTextView_Orientation.setText(capturedImage.getOrientation());
            imageViewHolder.mTextView_OwnerName.setText(capturedImage.getCameraOwnerName());
            imageViewHolder.mTextView_DateTime.setText(capturedImage.getDateTime());
            imageViewHolder.mTextView_TakeModel.setText(capturedImage.getTakeModel());
            imageViewHolder.mTextView_Software.setText(capturedImage.getSoftware());
            imageViewHolder.mTextView_FNumber.setText(capturedImage.getfNumber());
            imageViewHolder.mImageView_Thumbnail.setImageBitmap(capturedImage.getThumbnail());
            imageViewHolder.mImageView_Thumbnail.setOnClickListener(getClickListener(position));
            imageViewHolder.mImageView_Thumbnail.setOnLongClickListener(getLongClickListener(position));
            imageViewHolder.mLinearLayout_Root.setBackgroundColor(mContext.getResources().getColor(position % 2 == 0 ? R.color.transparent_darkwhite : R.color.transparent_white));
        }
        else{
            CapturedVideo capturedVideo = (CapturedVideo) media;
            VideoViewHolder videoViewHolder = (VideoViewHolder) holder;
            videoViewHolder.mTextView_FileName.setText(capturedVideo.getFileName());
            videoViewHolder.mTextView_FileSize.setText(capturedVideo.getFileSize());
            videoViewHolder.mTextView_Duration.setText(capturedVideo.getDuration());
            videoViewHolder.mTextView_Rotation.setText(capturedVideo.getRotation());
            videoViewHolder.mTextView_Resolution.setText(capturedVideo.getResolution());
            videoViewHolder.mTextView_FrameRate.setText(capturedVideo.getFrameRate());
            videoViewHolder.mTextView_BitRate.setText(capturedVideo.getBitRate());
            videoViewHolder.mTextView_MimeType.setText(capturedVideo.getMimeType());
            videoViewHolder.mTextView_AudioVideo.setText(capturedVideo.getHasAudioVideo());
            videoViewHolder.mImageView_Thumbnail.setImageBitmap(capturedVideo.getThumbnail());
            videoViewHolder.mImageView_Thumbnail.setOnClickListener(getClickListener(position));
            videoViewHolder.mImageView_Thumbnail.setOnLongClickListener(getLongClickListener(position));
            videoViewHolder.mLinearLayout_Root.setBackgroundColor(mContext.getResources().getColor(position % 2 == 0 ? R.color.transparent_darkwhite : R.color.transparent_white));
        }
    }

    @Override
    public int getItemCount() {
        return capturedMedia.size();
    }

    @Override
    public int getItemViewType(int position) {
        return capturedMedia.get(position).getFileType().getType();
    }

    public void setOnItemClickListener(OnItemClickedListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private View.OnClickListener getClickListener(final int position){
        return new View.OnClickListener() {
            final int finalPosition = position;
            @Override
            public void onClick(View view) {
                if(onItemClickListener != null){
                    onItemClickListener.onItemClick(finalPosition, view);
                }
            }
        };
    }
    private View.OnLongClickListener getLongClickListener(final int position){
        return new View.OnLongClickListener() {
            final int finalPosition = position;
            @Override
            public boolean onLongClick(View view) {
                if(onItemClickListener != null){
                    onItemClickListener.onItemLongClick(finalPosition, view);
                    return true;
                }
                return false;
            }
        };
    }

    public void permanentlyRemoveItem(int position) {
        mContext.getContentResolver().delete(capturedMedia.remove(position).getFileUri(), null, null);
        notifyItemRemoved(position);
    }

    private static class ImageViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout mLinearLayout_Root;
        private TextView mTextView_FileNameTitle;
        public TextView mTextView_FileName;
        private TextView mTextView_FileSizeTitle;
        public TextView mTextView_FileSize;
        private TextView mTextView_ResolutionTitle;
        public TextView mTextView_Resolution;
        private TextView mTextView_OrientationTitle;
        public TextView mTextView_Orientation;
        private TextView mTextView_OwnerNameTitle;
        public TextView mTextView_OwnerName;
        private TextView mTextView_DateTimeTitle;
        public TextView mTextView_DateTime;
        private TextView mTextView_TakeModelTitle;
        public TextView mTextView_TakeModel;
        private TextView mTextView_SoftwareTitle;
        public TextView mTextView_Software;
        private TextView mTextView_FNumberTitle;
        public TextView mTextView_FNumber;
        public ImageView mImageView_Thumbnail;

        public ImageViewHolder(View view) {
            super(view);
            mLinearLayout_Root = (LinearLayout) view;
            mTextView_FileNameTitle = view.findViewById(R.id.textView_filenametitle);
            mTextView_FileName = view.findViewById(R.id.textView_filename);
            mTextView_FileSizeTitle = view.findViewById(R.id.textView_filesizetitle);
            mTextView_FileSize = view.findViewById(R.id.textView_filesize);
            mTextView_ResolutionTitle = view.findViewById(R.id.textView_resolutiontitle);
            mTextView_Resolution = view.findViewById(R.id.textView_resolution);
            mTextView_OrientationTitle = view.findViewById(R.id.textView_orientationtitle);
            mTextView_Orientation = view.findViewById(R.id.textView_orientation);
            mTextView_OwnerNameTitle = view.findViewById(R.id.textView_cameraownernametitle);
            mTextView_OwnerName = view.findViewById(R.id.textView_cameraownername);
            mTextView_DateTimeTitle = view.findViewById(R.id.textView_datetimetitle);
            mTextView_DateTime = view.findViewById(R.id.textView_datetime);
            mTextView_TakeModelTitle = view.findViewById(R.id.textView_takemodeltitle);
            mTextView_TakeModel = view.findViewById(R.id.textView_takemodel);
            mTextView_SoftwareTitle = view.findViewById(R.id.textView_softwaretitle);
            mTextView_Software = view.findViewById(R.id.textView_software);
            mTextView_FNumberTitle = view.findViewById(R.id.textView_fnumbertitle);
            mTextView_FNumber = view.findViewById(R.id.textView_fnumber);
            mImageView_Thumbnail = view.findViewById(R.id.imageView_thumbnail);
        }
    }

    private static class VideoViewHolder extends RecyclerView.ViewHolder {
        private LinearLayout mLinearLayout_Root;
        private TextView mTextView_FileNameTitle;
        public TextView mTextView_FileName;
        private TextView mTextView_FileSizeTitle;
        public TextView mTextView_FileSize;
        private TextView mTextView_DurationTitle;
        public TextView mTextView_Duration;
        private TextView mTextView_RotationTitle;
        public TextView mTextView_Rotation;
        private TextView mTextView_ResolutionTitle;
        public TextView mTextView_Resolution;
        private TextView mTextView_FrameRateTitle;
        public TextView mTextView_FrameRate;
        private TextView mTextView_BitRateTitle;
        public TextView mTextView_BitRate;
        private TextView mTextView_MimeTypeTitle;
        public TextView mTextView_MimeType;
        private TextView mTextView_AudioVideoTitle;
        public TextView mTextView_AudioVideo;
        public ImageView mImageView_Thumbnail;

        public VideoViewHolder(View view) {
            super(view);
            mLinearLayout_Root = (LinearLayout) view;
            mTextView_FileNameTitle = view.findViewById(R.id.textView_filenametitle);
            mTextView_FileName = view.findViewById(R.id.textView_filename);
            mTextView_FileSizeTitle = view.findViewById(R.id.textView_filesizetitle);
            mTextView_FileSize = view.findViewById(R.id.textView_filesize);
            mTextView_DurationTitle = view.findViewById(R.id.textView_durationtitle);
            mTextView_Duration = view.findViewById(R.id.textView_duration);
            mTextView_RotationTitle = view.findViewById(R.id.textView_rotationtitle);
            mTextView_Rotation = view.findViewById(R.id.textView_rotation);
            mTextView_ResolutionTitle = view.findViewById(R.id.textView_resolutiontitle);
            mTextView_Resolution = view.findViewById(R.id.textView_resolution);
            mTextView_FrameRateTitle = view.findViewById(R.id.textView_frameratetitle);
            mTextView_FrameRate = view.findViewById(R.id.textView_framerate);
            mTextView_BitRateTitle = view.findViewById(R.id.textView_bitratetitle);
            mTextView_BitRate = view.findViewById(R.id.textView_bitrate);
            mTextView_MimeTypeTitle = view.findViewById(R.id.textView_mimetypetitle);
            mTextView_MimeType = view.findViewById(R.id.textView_mimetype);
            mTextView_AudioVideoTitle = view.findViewById(R.id.textView_audiovideotitle);
            mTextView_AudioVideo = view.findViewById(R.id.textView_audiovideo);
            mImageView_Thumbnail = view.findViewById(R.id.imageView_thumbnail);
        }
    }

    public interface OnItemClickedListener{
        void onItemClick(int position, View itemView);
        void onItemLongClick(int position, View itemView);
    }

}
