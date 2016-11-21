package com.michaelflisar.storagemanager;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
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

    private ArrayList<ContentProviderOperation> mOperations = new ArrayList<>();

    private ArrayList<String> mFilesDeletionPaths = new ArrayList<>();
    private ArrayList<String> mFileCreationsPaths = new ArrayList<>();
    private ArrayList<MediaStoreFileData> mFileCreationsMediaStoreFileData = new ArrayList<>();
    private ArrayList<String> mFileRenameOldPaths = new ArrayList<>();
    private ArrayList<String> mFileRenameNewPaths = new ArrayList<>();

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

    public void addRename(IFile fileOld, IFile fileNew)
    {
        mFileRenameOldPaths.add(fileOld.getPath());
        mFileRenameNewPaths.add(fileNew.getPath());
    }

    public void addRename(String oldPath, String newPath)
    {
        mFileRenameOldPaths.add(oldPath);
        mFileRenameNewPaths.add(newPath);
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

    public void addOperation(ContentProviderOperation operation)
    {
        mOperations.add(operation);
    }

    // -------------------------------
    // executing operations
    // -------------------------------

    public ContentProviderResult[] execute() throws StorageException
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

        // 3) create all file rename operations
        for (int i = 0; i < mFileRenameNewPaths.size(); i++)
        {
            String pathOld = mFileRenameOldPaths.get(i);
            String pathNew = mFileRenameNewPaths.get(i);
            String nameNew = new File(pathNew).getName();
            StorageDefinitions.MediaType mediaType = ExtensionUtil.getMediaType(pathOld);
            operations.add(MediaStoreUtil.renameMediaOperation(mediaType, pathOld, pathNew, nameNew));
        }

        // 4) add all custom operations
        for (int i = 0; i < mOperations.size(); i++)
            operations.add(mOperations.get(i));

        // 5) execute direct operations as batch
        ContentProviderResult[] result = null;
        if (operations.size() > 0)
            result = MediaStoreUtil.applyBatch(operations);

        // 5) reset
        reset();

        return result;
    }

    public void reset()
    {
        mOperations.clear();

        mFilesDeletionPaths.clear();
        mFileCreationsPaths.clear();
        mFileCreationsMediaStoreFileData.clear();
        mFileRenameOldPaths.clear();
        mFileRenameNewPaths.clear();
    }
}
