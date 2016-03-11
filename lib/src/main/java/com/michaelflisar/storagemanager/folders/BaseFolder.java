package com.michaelflisar.storagemanager.folders;

import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.data.MediaStoreFolderData;
import com.michaelflisar.storagemanager.interfaces.IFile;
import com.michaelflisar.storagemanager.interfaces.IFolder;
import com.michaelflisar.storagemanager.interfaces.IMediaStoreFolder;

import java.util.List;

/**
 * Created by flisar on 03.02.2016.
 */
public abstract class BaseFolder<T> implements IFolder<T>, IMediaStoreFolder<T>
{
    protected StorageDefinitions.FolderStatus status = StorageDefinitions.FolderStatus.NotLoaded;
    protected IFile<T> folder;
    protected List<StorageDefinitions.MediaType> fileTypesToList;
    protected List<IFile<T>> files;

    // MediaStore
    protected MediaStoreFolderData mediaStoreFolderData;

    public BaseFolder(IFile folder)
    {
        this.folder = folder;
    }

    // --------------------------------
    // Media Store data
    // --------------------------------

    @Override
    public void setMediaStoreFolderData(MediaStoreFolderData data)
    {
        mediaStoreFolderData = data;
    }

    @Override
    public MediaStoreFolderData getMediaStoreFolderData()
    {
        return mediaStoreFolderData;
    }

    // ------------------------
    // Folder
    // ------------------------

    @Override
    public final void reset()
    {
        fileTypesToList = null;
        files = null;
        status = StorageDefinitions.FolderStatus.NotLoaded;
    }

    @Override
    public final void loadFiles(List<StorageDefinitions.MediaType> typeToContain, boolean loadFromMediaStore, StorageDefinitions.FileSortingType sortingType, StorageDefinitions.FileSortingOrder order, Integer limit)
    {
        if (status == StorageDefinitions.FolderStatus.Loaded)
            return;

        fileTypesToList = typeToContain;
        files = loadFilesInternally(loadFromMediaStore, sortingType, order, limit);

        status = StorageDefinitions.FolderStatus.Loaded;
    }

    @Override
    public final StorageDefinitions.FolderStatus getStatus()
    {
        return status;
    }

    @Override
    public final IFile<T> getFolder()
    {
        return folder;
    }

    @Override
    public final List<IFile<T>> getFiles()
    {
        return files;
    }

    @Override
    public final void initFiles(List<StorageDefinitions.MediaType> typeToContain, List<IFile<T>> files)
    {
        this.fileTypesToList = typeToContain;
        this.files = files;
        status = StorageDefinitions.FolderStatus.Loaded;
    }
}
