package com.michaelflisar.storagemanager.folders;

import com.michaelflisar.storagemanager.data.FileFolderData;
import com.michaelflisar.storagemanager.data.MediaStoreFolderData;
import com.michaelflisar.storagemanager.interfaces.IFolder;

import java.io.File;

/**
 * Created by flisar on 03.02.2016.
 */
public class FileFolder implements IFolder
{
    // File
    protected File file;

    // MediaStore
    protected MediaStoreFolderData mediaStoreFolderData;

    // File Folder Data
    protected FileFolderData fileFolderData;

    public FileFolder(String path)
    {
        this.file = new File(path);
        mediaStoreFolderData = null;
        fileFolderData = null;
    }

    public FileFolder(File file)
    {
        this.file = file;
        mediaStoreFolderData = null;
        fileFolderData = null;
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
    // Media Store data
    // --------------------------------

    public MediaStoreFolderData getMediaStoreFolderData()
    {
        return mediaStoreFolderData;
    }

    public void setMediaStoreFolderData(MediaStoreFolderData data)
    {
        mediaStoreFolderData = data;
    }

    // --------------------------------
    // File Folder data
    // --------------------------------

    public FileFolderData getFileFolderData()
    {
        return fileFolderData;
    }

    public void setFileFolderData(FileFolderData data)
    {
        fileFolderData = data;
    }
}