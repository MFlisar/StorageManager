package com.michaelflisar.storagemanager.folders;

import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.data.MediaStoreFolderData;
import com.michaelflisar.storagemanager.interfaces.IFile;
import com.michaelflisar.storagemanager.interfaces.IFolder;
import com.michaelflisar.storagemanager.interfaces.IMediaStoreFolder;
import com.michaelflisar.storagemanager.utils.MediaStoreUtil;

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

    @Override
    public List<StorageDefinitions.MediaType> getFileTypes()
    {
        return fileTypesToList;
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
    public final void loadFiles(List<StorageDefinitions.MediaType> typeToContain, boolean loadFromMediaStore, StorageDefinitions.FileSortingType sortingType, StorageDefinitions.FileSortingOrder order, Integer limit, Long minDate, Long maxDate, MediaStoreUtil.DateType dateType)
    {
        if (status == StorageDefinitions.FolderStatus.Loaded)
            return;

        fileTypesToList = typeToContain;
        files = loadFilesInternally(loadFromMediaStore, sortingType, order, limit, minDate, maxDate, dateType);

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

    @Override
    public boolean addFile(IFile file)
    {
        if (files == null)
            throw new RuntimeException("Folder files have not been loaded yet!");
        files.add(file);
        return true;
    }

    @Override
    public boolean removeFile(IFile file)
    {
        if (files == null)
            throw new RuntimeException("Folder files have not been loaded yet!");
//        files.remove(file);
       return removeFile(file.getPath());
    }

    @Override
    public boolean removeFile(String path)
    {
        if (files == null)
            throw new RuntimeException("Folder files have not been loaded yet!");
//        files.remove(file);
        for (int i = 0; i < files.size(); i++)
        {
            if (files.get(i).getPath().equals(path))
            {
                files.remove(i);
                return true;
            }
        }
        return false;
    }
}
