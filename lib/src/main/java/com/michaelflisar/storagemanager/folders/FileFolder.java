package com.michaelflisar.storagemanager.folders;

import android.util.Log;

import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.StorageUtil;
import com.michaelflisar.storagemanager.data.FileFolderData;
import com.michaelflisar.storagemanager.data.MediaStoreFileData;
import com.michaelflisar.storagemanager.data.MediaStoreFolderData;
import com.michaelflisar.storagemanager.files.StorageFile;
import com.michaelflisar.storagemanager.interfaces.IFile;
import com.michaelflisar.storagemanager.interfaces.IFolderData;
import com.michaelflisar.storagemanager.utils.FileUtil;
import com.michaelflisar.storagemanager.utils.MediaStoreUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by flisar on 03.02.2016.
 */
public class FileFolder extends BaseFolder<File>
{
    // MediaStore
    protected MediaStoreFolderData mediaStoreFolderData;

    // File Folder Data
    protected FileFolderData fileFolderData;

    public FileFolder(StorageFile file)
    {
        super(file);
        mediaStoreFolderData = null;
        fileFolderData = null;
        status = StorageDefinitions.FolderStatus.NotLoaded;
        files = new ArrayList<>();
    }

    public FileFolder(File file)
    {
        this(new StorageFile(file, null, null));
    }

    @Override
    public List<IFile<File>> loadFilesInternally(boolean loadFromMediaStore, StorageDefinitions.FileSortingType limitSortingType, StorageDefinitions.FileSortingOrder limitSortingOrder, Integer limit, Long minDate, Long maxDate, MediaStoreUtil.DateType dateType)
    {
        List<IFile<File>> files = new ArrayList<>();

        boolean isHidden = StorageUtil.hasNoMediaFile(getFolder().getParent(), true);
        if (loadFromMediaStore)
        {
            if (mediaStoreFolderData != null)
            {
                List<MediaStoreFileData> folderFiles = MediaStoreUtil.loadFilesInFolder(mediaStoreFolderData.getBucketId(), fileTypesToList, limitSortingType, limitSortingOrder, limit, minDate, maxDate, dateType);
                for (int i = 0; i < folderFiles.size(); i++)
                {
                    StorageFile file = (StorageFile) StorageUtil.getFileByPath(folderFiles.get(i).getData(), isHidden);
                    // check if file exists,
                    // maybe there is an relict in the media store, not all apps correctly update it
                    if (file != null)
                    {
                        file.setMediaStoreFileData(folderFiles.get(i));
                        files.add(file);
                    }
                }
            }
        }
        else
        {
            // TODO: use sorting and order? then you would have to load all files even if limit is set!
            // TODO: minCreationDate not yet supported!
            List<File> folderFiles = FileUtil.getFolderFiles(folder.getWrapped(), fileTypesToList, limit, minDate, maxDate, dateType);
            for (int i = 0; i < folderFiles.size(); i++)
                files.add(new StorageFile(folderFiles.get(i), isHidden, null)); // MediaStoreData is set to null, not using media store does only make sense when loading data in a hidden folder!
        }

        return files;
    }

    // --------------------------------
    // Properties
    // --------------------------------

    @Override
    public final StorageDefinitions.FolderType getType()
    {
        return StorageDefinitions.FolderType.FileFolder;
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

        if (fileFolderData != null && fileFolderData.knowsCount())
            return fileFolderData.getCount();

        if (mediaStoreFolderData != null)
            return mediaStoreFolderData.getCount();

        return null;
    }

    @Override
    public IFolderData getFolderData()
    {
        return fileFolderData;
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