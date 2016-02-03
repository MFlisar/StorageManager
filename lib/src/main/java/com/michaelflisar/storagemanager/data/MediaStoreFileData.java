package com.michaelflisar.storagemanager.data;

import android.net.Uri;

import com.michaelflisar.storagemanager.StorageDefinitions;

/**
 * Created by flisar on 03.02.2016.
 */
public class MediaStoreFileData
{
    private StorageDefinitions.MediaType mType;

    // MediaStore
    private Uri mUri;
    private long mId;
    private String mName;
    private String mData;
    private long mDateTaken;
    private long mDateModified;
    private String mMimeType;
    private int mWidth;
    private int mHeight;
    private double mLatitude;
    private double mLongitude;
    private int mOrientation;

    public MediaStoreFileData(StorageDefinitions.MediaType type, Uri uri, long id, String name, String data, long dateTaken, long dateModified, String mimeType, int width, int height, double latitude, double longitude, int orientation)
    {
        mType = type;
        mId = id;
        mName = name;
        mData = data;
        mDateTaken = dateTaken;
        mDateModified = dateModified;
        mMimeType = mimeType;
        mWidth = width;
        mHeight = height;
        mLatitude = latitude;
        mLongitude = longitude;
        mOrientation = orientation;
    }

    // --------------------------------
    // Getter
    // --------------------------------
    
    public StorageDefinitions.MediaType getType()
    {
        return mType;
    }

    public Uri getUri()
    {
        return mUri;
    }

    public long getId()
    {
        return mId;
    }

    public String getName()
    {
        return mName;
    }

    public String getData()
    {
        return mData;
    }

    public long getDateTaken()
    {
        return mDateTaken;
    }

    public long getDateModified()
    {
        return mDateModified;
    }

    public String getMimeType()
    {
        return mMimeType;
    }

    public int getWidth()
    {
        return mWidth;
    }

    public int getHeight()
    {
        return mHeight;
    }

    public double getLatitude()
    {
        return mLatitude;
    }

    public double getLongitude()
    {
        return mLongitude;
    }

    public int getOrientation()
    {
        return mOrientation;
    }
}
