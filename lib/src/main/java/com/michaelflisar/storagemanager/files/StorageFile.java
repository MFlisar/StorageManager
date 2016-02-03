package com.michaelflisar.storagemanager.files;

import android.content.Context;

import com.michaelflisar.storagemanager.data.MediaStoreFileData;
import com.michaelflisar.storagemanager.utils.MediaStoreUtil;
import com.michaelflisar.storagemanager.interfaces.IFile;

import java.io.File;

/**
 * Created by flisar on 03.02.2016.
 */
public class StorageFile implements IFile
{
    // File
    protected File file;

    // MediaStore
    protected MediaStoreFileData mediaStoreFileData;

    public StorageFile(String path)
    {
        this.file = new File(path);
        mediaStoreFileData = null;
    }

    public StorageFile(File file)
    {
        this.file = file;
        mediaStoreFileData = null;
    }

    // --------------------------------
    // Media Store data
    // --------------------------------

    public MediaStoreFileData getMediaStoreFileData()
    {
        return mediaStoreFileData;
    }

    public void setMediaStoreFileData(MediaStoreFileData data)
    {
        mediaStoreFileData = data;
    }

    // --------------------------------
    // Properties
    // --------------------------------

    public File getFile()
    {
        return file;
    }

    @Override
    public String getName()
    {
        return file.getName();
    }

    // --------------------------------
    // File operations
    // --------------------------------

    public boolean delete(Context context)
    {
        boolean success = false;

        // 1) try to delete via media store => this will delete the folder from the storage as well
        if (mediaStoreFileData != null)
            success = MediaStoreUtil.delete(context, mediaStoreFileData);

        // 2) Fallback + if no media store data is set
        if (!success)
        {
            success = file.delete();
            // check if entry in media store exists and delete it if possible => we don't check for success as we already know if the real folder was deleted
            MediaStoreUtil.delete(context, file, null);
        }

        return success;
    }
}
