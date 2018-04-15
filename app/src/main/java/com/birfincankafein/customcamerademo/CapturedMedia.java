package com.birfincankafein.customcamerademo;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Parcel;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import java.io.FileDescriptor;
import java.io.InputStream;

/**
 * Created by metehantoksoy on 10.04.2018.
 */

public abstract class CapturedMedia implements Parcelable {
    private final Uri fileUri;
    private final FileType fileType;

    private Bitmap thumbnail;
    private String fileName;
    private String fileSize;

    public CapturedMedia(Uri fileUri, FileType fileType, Context context) {
        this.fileUri = fileUri;
        this.fileType = fileType;
        thumbnail = createThumbnail(context);
        initProperties(context);
    }

    protected CapturedMedia(Parcel in) {
        this.fileUri = in.readParcelable(Uri.class.getClassLoader());
        int tmpFileType = in.readInt();
        this.fileType = tmpFileType == -1 ? null : FileType.values()[tmpFileType];
        this.thumbnail = in.readParcelable(Bitmap.class.getClassLoader());
        this.fileName = in.readString();
        this.fileSize = in.readString();
    }

    private void initProperties(Context mContext){
        Cursor returnCursor = mContext.getContentResolver().query(fileUri, null, null, null, null);
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
        returnCursor.moveToFirst();
        fileName = returnCursor.getString(nameIndex);
        Long tmpFileSize = returnCursor.getLong(sizeIndex);
        fileSize = tmpFileSize + " B - " + (tmpFileSize/1048576) + "MB";
        returnCursor.close();
    }

    protected abstract Bitmap createThumbnail(Context mContext);
    protected abstract void initSpecificProperties(Context mContext);

    public Bitmap getThumbnail(){
        return thumbnail;
    }
    public Uri getFileUri() {
        return fileUri;
    }
    public FileType getFileType() {
        return fileType;
    }
    public String getFileName() {
        return fileName;
    }
    public String getFileSize() {
        return fileSize;
    }
    protected void setThumbnail(Bitmap thumbnail){
        this.thumbnail = thumbnail;
    }

    public enum FileType{
        PICTURE(0), VIDEO(1);

        private final int type;
        FileType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }
}
