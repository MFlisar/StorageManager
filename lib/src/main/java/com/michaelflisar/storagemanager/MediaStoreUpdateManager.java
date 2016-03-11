package com.michaelflisar.storagemanager;

import android.content.ContentProviderOperation;
import android.location.Location;

import com.michaelflisar.storagemanager.data.MediaStoreFileData;
import com.michaelflisar.storagemanager.exceptions.StorageException;
import com.michaelflisar.storagemanager.files.StorageFile;
import com.michaelflisar.storagemanager.interfaces.IFile;
import com.michaelflisar.storagemanager.utils.ExtensionUtil;
import com.michaelflisar.storagemanager.utils.MediaStoreUtil;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by flisar on 07.03.2016.
 */
public class MediaStoreUpdateManager
{
    private static MediaStoreUpdateManager INSTANCE = null;

    public static MediaStoreUpdateManager get()
    {
        if (INSTANCE == null)
            INSTANCE = new MediaStoreUpdateManager();
        return INSTANCE;
    }

    private MediaStoreUpdateManager()
    {
    }

    private ArrayList<String> mFilesDeletionPaths = new ArrayList<>();
    private ArrayList<String> mFileCreationsPaths = new ArrayList<>();
    private ArrayList<MediaStoreFileData> mFileCreationsMediaStoreFileData = new ArrayList<>();

    //private ArrayList<String> mPathDocuments = new ArrayList<>();

    // -------------------------------
    // functions to add operations
    // -------------------------------

    public void addCreation(IFile file, MediaStoreFileData mediaStoreFileData)
    {
        if (file.getType() == StorageDefinitions.FileType.File)
            addCreation(file.getPath(), mediaStoreFileData);
        else
            addCreation(file.getPath(), mediaStoreFileData);
    }

    public void addCreation(String path, MediaStoreFileData mediaStoreFileData)
    {
        mFileCreationsPaths.add(path);
        mFileCreationsMediaStoreFileData.add(mediaStoreFileData);
    }

    public void addDeletion(IFile file)
    {
        if (file.getType() == StorageDefinitions.FileType.File)
            addDeletion(file.getPath());
        else
            addDeletion(file.getPath());
    }

    public void addDeletion(String path)
    {
        mFilesDeletionPaths.add(path);
    }

    // -------------------------------
    // executing operations
    // -------------------------------

    public void execute() throws StorageException
    {
        ArrayList<ContentProviderOperation> operations = new ArrayList<>();

        // 1) create all file deletions operations
        for (int i = 0; i < mFilesDeletionPaths.size(); i++)
        {
            String path = mFilesDeletionPaths.get(i);
            StorageDefinitions.MediaType mediaType = ExtensionUtil.getMediaType(path);
            operations.add(MediaStoreUtil.deleteOperation(mediaType, path));
        }

        // 2) create all file creation operations
        for (int i = 0; i < mFileCreationsPaths.size(); i++)
        {
            String path = mFileCreationsPaths.get(i);
            StorageDefinitions.MediaType mediaType = ExtensionUtil.getMediaType(path);
            operations.add(MediaStoreUtil.createOperation(mediaType, path,
                    mFileCreationsMediaStoreFileData.get(i).getDateTaken(),
                    mFileCreationsMediaStoreFileData.get(i).getDateModified(),
                    mFileCreationsMediaStoreFileData.get(i).getLatitude(),
                    mFileCreationsMediaStoreFileData.get(i).getLongitude(),
                    mFileCreationsMediaStoreFileData.get(i).getRotation()));
        }

        // 3) execute direct operations as batch
        if (operations.size() > 0)
            MediaStoreUtil.applyBatch(operations);

        // 4) reset
        reset();
    }

    public void reset()
    {
        mFilesDeletionPaths.clear();
        mFileCreationsPaths.clear();
        mFileCreationsMediaStoreFileData.clear();
    }
}
