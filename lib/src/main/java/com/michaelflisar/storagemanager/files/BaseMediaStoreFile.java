package com.michaelflisar.storagemanager.files;

import android.location.Location;

import com.michaelflisar.storagemanager.MediaStoreUpdateManager;
import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.StorageManager;
import com.michaelflisar.storagemanager.StorageUtil;
import com.michaelflisar.storagemanager.data.MediaStoreFileData;
import com.michaelflisar.storagemanager.interfaces.IFile;
import com.michaelflisar.storagemanager.interfaces.IMediaStoreFile;
import com.michaelflisar.storagemanager.utils.ExifFileUtil;
import com.michaelflisar.storagemanager.utils.MediaStoreUtil;

import java.util.HashMap;

/**
 * Created by flisar on 08.03.2016.
 */
public abstract class BaseMediaStoreFile<File> implements IFile<File>, IMediaStoreFile<File>
{
    // MediaStore
    protected boolean mediaStoreQueried = false;
    protected MediaStoreFileData mediaStoreFileData;

    // --------------------------------
    // Media Store data
    // --------------------------------

    @Override
    public MediaStoreFileData getMediaStoreFileData(boolean loadIfMissing)
    {
        if (mediaStoreFileData != null)
            return mediaStoreFileData;

        // don't query media store more than once!
        if (!loadIfMissing || mediaStoreQueried)
            return null;

        internalLoadMediaStoreDataForFile();
        return mediaStoreFileData;
    }

    @Override
    public MediaStoreFileData getCopyOrCreateBestPossibleMediaStoreFileData()
    {
        MediaStoreFileData mediaStoreFileData = getMediaStoreFileData(true);
        if (mediaStoreFileData != null)
            return new MediaStoreFileData(mediaStoreFileData);

        if (!isFile())
            return null;

        HashMap<String, String> exifInformations = StorageManager.get().getMetaDataHandler().getExifInformations(this);

        Location loc = ExifFileUtil.getLocation(exifInformations);
        Integer w = ExifFileUtil.getWidth(exifInformations);
        Integer h = ExifFileUtil.getHeight(exifInformations);
        Integer rotation = ExifFileUtil.getRotation(exifInformations);

        return new MediaStoreFileData(
                getMediaType(),
                getUri(),
                -1,
                getName(),
                getPath(),
                created(),
                lastModified(),
                getMimeType(),
                w != null ? w : 0,
                h != null ? h : 0,
                loc != null ? loc.getLatitude() : null,
                loc != null ? loc.getLongitude() : null,
                rotation != null ? rotation : 0
        );
    }

    @Override
    public void setMediaStoreFileData(MediaStoreFileData data)
    {
        mediaStoreFileData = data;
        mediaStoreQueried = true;
    }

    @Override
    public void resetMediaStoreData()
    {
        mediaStoreFileData = null;
        mediaStoreQueried = false;
    }

    protected abstract void internalLoadMediaStoreDataForFile();
}
