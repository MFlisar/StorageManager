package com.michaelflisar.storagemanager.files;

import android.content.Context;
import android.support.v4.provider.DocumentFile;

import com.michaelflisar.storagemanager.interfaces.IFile;

/**
 * Created by flisar on 03.02.2016.
 */
public class StorageDocument implements IFile
{
    protected DocumentFile file;

    public StorageDocument(DocumentFile file)
    {
        this.file = file;
    }

    // --------------------------------
    // Properties
    // --------------------------------

    @Override
    public String getName()
    {
        return file.getName();
    }

    // --------------------------------
    // File operations
    // --------------------------------

    @Override
    public boolean delete(Context context)
    {
        return file.delete();
    }
}
