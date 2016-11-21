package com.michaelflisar.storagemanager.files;

import android.location.Location;

import com.michaelflisar.storagemanager.MediaStoreUpdateManager;
import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.StorageManager;
import com.michaelflisar.storagemanager.StorageUtil;
import com.michaelflisar.storagemanager.data.MediaStoreFileData;
import com.michaelflisar.storagemanager.interfaces.IExifFile;
import com.michaelflisar.storagemanager.interfaces.IFile;
import com.michaelflisar.storagemanager.interfaces.IHideableFile;
import com.michaelflisar.storagemanager.interfaces.IMediaStoreFile;
import com.michaelflisar.storagemanager.utils.ExifFileUtil;
import com.michaelflisar.storagemanager.utils.MediaStoreUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * Created by flisar on 08.03.2016.
 */
public abstract class BaseFile<File> implements IFile<File>, IMediaStoreFile<File>, IExifFile<File>, IHideableFile<File>
{
    // MediaStore
    protected boolean mediaStoreQueried = false;
    protected MediaStoreFileData mediaStoreFileData = null;
    protected boolean hiddenStateChecked = false;
    protected Boolean hidden = null;

    protected boolean exifQueried = false;
    protected HashMap<String, String> exifData = null;

    // --------------------------------
    // Constructor
    // --------------------------------

    public BaseFile(Boolean hidden)
    {
        if (hidden != null)
            setHidden(hidden);
    }

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
    public MediaStoreFileData getCopyOrCreateBestPossibleMediaStoreFileData(StorageDefinitions.MediaStoreUpdateType mediaStoreUpdateType)
    {
        // this update type does not need any data
        if (mediaStoreUpdateType == StorageDefinitions.MediaStoreUpdateType.None)
            return null;

        MediaStoreFileData mediaStoreFileData = getMediaStoreFileData(true);
        if (mediaStoreFileData != null)
            return new MediaStoreFileData(mediaStoreFileData);

        if (!isFile())
            return null;

        HashMap<String, String> exifInformations = StorageManager.get().getMetaDataHandler().getExifInformations(this);

        Location loc = ExifFileUtil.getLocation(exifInformations);
        Integer w = ExifFileUtil.getWidth(exifInformations);
        Integer h = ExifFileUtil.getHeight(exifInformations);
        Integer orientation = ExifFileUtil.getOrientation(exifInformations, false);

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
                orientation
        );
    }

    @Override
    public Boolean getHiddenState(StorageDefinitions.MediaStoreUpdateType mediaStoreUpdateType)
    {
        // this update type does not need any data
        if (mediaStoreUpdateType == StorageDefinitions.MediaStoreUpdateType.None)
            return null;

        return isHidden(true);
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

    // --------------------------------
    // Exif data
    // --------------------------------


    @Override
    public HashMap<String, String> getExifData(boolean loadIfMissing)
    {
        if (exifData != null)
            return exifData;

        // don't query exif data more than once!
        if (!loadIfMissing || exifQueried)
            return null;

        internalLoadExifData();
        return exifData;
    }

    @Override
    public void setExifData(HashMap<String, String> data)
    {
        exifData = data;
        exifQueried = true;
    }

    @Override
    public void resetExifData()
    {
        exifData = null;
        exifQueried = false;
    }

    private void internalLoadExifData()
    {
        setExifData(StorageManager.get().getMetaDataHandler().getExifInformations(this));
    }

    // --------------------------------
    // Hidden state
    // --------------------------------

    @Override
    public Boolean isHidden(boolean loadIfMissing)
    {
        if (!hiddenStateChecked && loadIfMissing)
            setHidden(StorageUtil.hasNoMediaFile(getParent(), true));
        return hidden;
    }

    @Override
    public void setHidden(boolean hidden)
    {
        this.hidden = hidden;
        hiddenStateChecked = true;
    }

    @Override
    public byte[] getBytes() throws IOException
    {
        byte[] data = new byte[16384];
        InputStream is = null;
        IOException e = null;
        try
        {
            is = getInputStream();
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            while ((nRead = is.read(data, 0, data.length)) != -1)
                buffer.write(data, 0, nRead);
            buffer.flush();
            is.close();
            is = null;
            return buffer.toByteArray();
        }
        catch (IOException ex)
        {
            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch (IOException ex2)
                {

                }
            }
            throw e;
        }
    }
}