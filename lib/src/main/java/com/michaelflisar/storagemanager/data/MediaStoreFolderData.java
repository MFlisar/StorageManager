package com.michaelflisar.storagemanager.data;

/**
 * Created by flisar on 03.02.2016.
 */
public class MediaStoreFolderData
{
    private long mBucketId;
    private String mBucket;
    private int mCount;
    private long mMinDateTaken;
    private long mMaxDateTaken;
    private long mMinDateModified;
    private long mMaxDateModified;

    public MediaStoreFolderData(long bucketId, String bucket, int count, long minDateTaken, long maxDateTaken, long minDateModified, long maxDateModified)
    {
        mBucketId = bucketId;
        mBucket = bucket;
        mCount = count;
        mMinDateTaken = minDateTaken;
        mMaxDateTaken = maxDateTaken;
        mMinDateModified = minDateModified;
        mMaxDateModified = maxDateModified;
    }

    // --------------------------------
    // Getter
    // --------------------------------

    public long getBucketId()
    {
        return mBucketId;
    }

    public String getBucket()
    {
        return mBucket;
    }

    public int getCount()
    {
        return mCount;
    }

    public long getMinDateTaken()
    {
        return mMinDateTaken;
    }

    public long getMaxDateTaken()
    {
        return mMaxDateTaken;
    }

    public long getMinDateModified()
    {
        return mMinDateModified;
    }

    public long getMaxDateModified()
    {
        return mMaxDateModified;
    }
}