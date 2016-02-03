package com.michaelflisar.storagemanager.data;

/**
 * Created by flisar on 03.02.2016.
 */
public class MediaStoreFolderData
{
    private long mBucketId;
    private String mBucket;
    private int mCount;

    public MediaStoreFolderData(long bucketId, String bucket, int count)
    {
        mBucketId = bucketId;
        mBucket = bucket;
        mCount = count;
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
}