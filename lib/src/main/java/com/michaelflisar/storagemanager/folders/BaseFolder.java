package com.michaelflisar.storagemanager.folders;

import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.interfaces.IFile;
import com.michaelflisar.storagemanager.interfaces.IFolder;

import java.util.List;

/**
 * Created by flisar on 03.02.2016.
 */
public abstract class BaseFolder implements IFolder
{
    protected StorageDefinitions.FolderStatus status = StorageDefinitions.FolderStatus.NotLoaded;
    protected List<IFile> files;

    @Override
    public StorageDefinitions.FolderStatus getStatus()
    {
        return status;
    }

    @Override
    public List<IFile> getFiles()
    {
        return files;
    }

    @Override
    public void initFiles(List<IFile> files)
    {
        this.files = files;
        status = StorageDefinitions.FolderStatus.Loaded;
    }
}
