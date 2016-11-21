package com.michaelflisar.storagemanager.data;

import android.location.Location;
import android.net.Uri;

import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.utils.ExifFileUtil;
import com.michaelflisar.storagemanager.utils.MediaStoreUtil;

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
    private Double mLatitude;
    private Double mLongitude;
    private int mRotation;

    public MediaStoreFileData(StorageDefinitions.MediaType type, Uri uri, long id, String name, String data, long dateTaken, long dateModified, String mimeType, int width, int height, Double latitude, Double longitude, int rotation)
    {
        mType = type;
        mUri = uri;
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
        mRotation = rotation;
    }

    public MediaStoreFileData(MediaStoreFileData source)
    {
        mType = source.mType;
        mUri = source.mUri;
        mId = source.mId;
        mName = source.mName;
        mData = source.mData;
        mDateTaken = source.mDateTaken;
        mDateModified = source.mDateModified;
        mMimeType = source.mMimeType;
        mWidth = source.mWidth;
        mHeight = source.mHeight;
        mLatitude = source.mLatitude;
        mLongitude = source.mLongitude;
        mRotation = source.mRotation;
    }

    public MediaStoreFileData copy()
    {
        return new MediaStoreFileData(this);
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

    public Double getLatitude()
    {
        return mLatitude;
    }

    public Double getLongitude()
    {
        return mLongitude;
    }

    public int getOrientation()
    {
        return ExifFileUtil.convertNormalisedDegreesToExif(mRotation);
    }

    public int getRotation()
    {
        return mRotation;
    }

    public Location getLocation()
    {
        if (mLatitude == null || mLongitude == null)
            return null;

        Location location = new Location("");
        location.setLatitude(mLatitude);
        location.setLongitude(mLongitude);
        return location;
    }

    // --------------------------------
    // Setter (Updater)
    // --------------------------------

    public void updateUri(Uri uri)
    {
        mUri = uri;
        mId = Long.valueOf(uri.getLastPathSegment());
    }

    public void updateRotation(int degrees)
    {
        mRotation = degrees;
    }

    public void updateOrientation(int orientation)
    {
        mRotation = ExifFileUtil.convertExifOrientationToDegrees(orientation);
    }

    public void updateName(String path, String name)
    {
        mData = path;
        mName = name;
    }

    public void updateSize(int w, int h)
    {
        mWidth = w;
        mHeight = h;
    }

}
