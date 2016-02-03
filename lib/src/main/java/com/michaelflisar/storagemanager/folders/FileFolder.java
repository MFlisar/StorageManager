package com.michaelflisar.storagemanager.folders;

import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.data.FileFolderData;
import com.michaelflisar.storagemanager.data.MediaStoreFolderData;
import com.michaelflisar.storagemanager.interfaces.IFile;
import com.michaelflisar.storagemanager.interfaces.IFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by flisar on 03.02.2016.
 */
public class FileFolder extends BaseFolder
{
    // File + Content
    protected File folder;

    // MediaStore
    protected MediaStoreFolderData mediaStoreFolderData;

    // File Folder Data
    protected FileFolderData fileFolderData;

    public FileFolder(String path)
    {
        this(new File(path));
    }

    public FileFolder(File file)
    {
        folder = file;
        mediaStoreFolderData = null;
        fileFolderData = null;
        status = StorageDefinitions.FolderStatus.NotLoaded;
        files = new ArrayList<>();
    }

    // --------------------------------
    // Properties
    // --------------------------------

    public File getFolder()
    {
        return folder;
    }

    @Override
    public String getName()
    {
        return folder.getName();
    }

    @Override
    public Integer getCount()
    {
        if (status == StorageDefinitions.FolderStatus.Loaded)
            return files.size();

        if (mediaStoreFolderData != null)
            return mediaStoreFolderData.getCount();
        return null;
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