package com.birfincankafein.customcamerademo;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.support.media.ExifInterface;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by metehantoksoy on 10.04.2018.
 */

public class CapturedImage extends CapturedMedia {
    private String resolution;
    private String orientation;
    private String cameraOwnerName;
    private String dateTime;
    private String takeModel;
    private String software;
    private String fNumber;

    public CapturedImage(Uri fileUri, Context context) {
        super(fileUri, FileType.PICTURE, context);
        initSpecificProperties(context);
    }

    @Override
    protected void initSpecificProperties(Context mContext) {
        try {
            ExifInterface exifInterface = new ExifInterface(mContext.getContentResolver().openInputStream(getFileUri()));
            String width = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH) != null ? exifInterface.getAttribute(ExifInterface.TAG_IMAGE_WIDTH) : exifInterface.getAttribute(ExifInterface.TAG_PIXEL_X_DIMENSION);
            String height = exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH) != null ? exifInterface.getAttribute(ExifInterface.TAG_IMAGE_LENGTH) : exifInterface.getAttribute(ExifInterface.TAG_PIXEL_Y_DIMENSION);
            resolution = width + "x" + height;
            orientation = getOrientationString(exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1));
            cameraOwnerName = exifInterface.getAttribute(ExifInterface.TAG_CAMARA_OWNER_NAME);
            dateTime = exifInterface.getAttribute(ExifInterface.TAG_DATETIME) != null ? exifInterface.getAttribute(ExifInterface.TAG_DATETIME) : exifInterface.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);
            takeModel = exifInterface.getAttribute(ExifInterface.TAG_MAKE) + " - " + exifInterface.getAttribute(ExifInterface.TAG_MODEL);
            software = exifInterface.getAttribute(ExifInterface.TAG_SOFTWARE);
            fNumber = exifInterface.getAttribute(ExifInterface.TAG_F_NUMBER);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String getOrientationString(int tagOrientation) {
        switch (tagOrientation){
            case ExifInterface.ORIENTATION_NORMAL:
                return "Normal";
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                return "Left-right reversed mirror";
            case ExifInterface.ORIENTATION_ROTATE_180:
                return "Rotated 180 degree clockwise";
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                return "Upside down";
            case ExifInterface.ORIENTATION_TRANSPOSE:
                return "Flipped top-left <--> bottom-right";
            case ExifInterface.ORIENTATION_ROTATE_90:
                return "Rotated 90 degree clockwise";
            case ExifInterface.ORIENTATION_TRANSVERSE:
                return "Flipped top-right <-> bottom-left";
            case ExifInterface.ORIENTATION_ROTATE_270:
                return "Rotated 270 degree clockwise";
            case ExifInterface.ORIENTATION_UNDEFINED:
            default:
                return "UNDEFINED";
        }
    }

    @Override
    protected Bitmap createThumbnail(Context mContext) {
        Bitmap thumbnail = null;
        try {
            thumbnail = MediaStore.Images.Media.getBitmap(mContext.getContentResolver(), getFileUri());
        } catch (Exception e1) {
            try {
                ParcelFileDescriptor parcelFileDescriptor = mContext.getContentResolver().openFileDescriptor(getFileUri(), "r");
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                thumbnail = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();
            } catch (Exception e2) {
                try {
                    InputStream inputStream = mContext.getContentResolver().openInputStream(getFileUri());
                    thumbnail = BitmapFactory.decodeStream(inputStream);
                    inputStream.close();
                } catch (Exception e3) {
                    e1.printStackTrace();
                    e2.printStackTrace();
                    e3.printStackTrace();
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

    public String getResolution() {
        return resolution;
    }

    public String getOrientation() {
        return orientation;
    }

    public String getCameraOwnerName() {
        return cameraOwnerName;
    }

    public String getDateTime() {
        return dateTime;
    }

    public String getTakeModel() {
        return takeModel;
    }

    public String getSoftware() {
        return software;
    }

    public String getfNumber() {
        return fNumber;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.resolution);
        dest.writeString(this.orientation);
        dest.writeString(this.cameraOwnerName);
        dest.writeString(this.dateTime);
        dest.writeString(this.takeModel);
        dest.writeString(this.software);
        dest.writeString(this.fNumber);
        dest.writeParcelable(getFileUri(), flags);
        dest.writeInt(getFileType() == null ? -1 : getFileType().ordinal());
        dest.writeParcelable(getThumbnail(), flags);
        dest.writeString(getFileName());
        dest.writeString(getFileSize());
    }

    protected CapturedImage(Parcel in) {
        super(in);
        this.resolution = in.readString();
        this.orientation = in.readString();
        this.cameraOwnerName = in.readString();
        this.dateTime = in.readString();
        this.takeModel = in.readString();
        this.software = in.readString();
        this.fNumber = in.readString();
    }

    public static final Creator<CapturedImage> CREATOR = new Creator<CapturedImage>() {
        @Override
        public CapturedImage createFromParcel(Parcel source) {
            return new CapturedImage(source);
        }

        @Override
        public CapturedImage[] newArray(int size) {
            return new CapturedImage[size];
        }
    };
}
