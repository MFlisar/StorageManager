package com.michaelflisar.storagemanager.utils;

import com.michaelflisar.storagemanager.StorageManager;
import com.michaelflisar.storagemanager.StoragePermissionManager;
import com.michaelflisar.storagemanager.StorageUtil;
import com.michaelflisar.storagemanager.data.MediaStoreFileData;
import com.michaelflisar.storagemanager.interfaces.IFile;
import com.michaelflisar.storagemanager.interfaces.IFolder;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by flisar on 10.03.2016.
 */
public class StorageDebugUtil
{
    public static final ArrayList<String> debugAvailableInfos()
    {
        ArrayList<String> messages = new ArrayList<>();

        // 1) add paths
        IFolder sdCard = StorageManager.get().getSDCardRoot();
        String sdCardId = StorageManager.get().getSDCardID();
        IFolder root = StorageManager.get().getRoot();

        messages.add("SD Card: " + (sdCard != null ? sdCard.getFolder().getPath() : "NOT FOUND!"));
        messages.add("SD Card ID: " + (sdCardId != null ? sdCardId : "NOT FOUND!"));
        messages.add("Root: " + (root != null ? root.getFolder().getPath() : "NOT FOUND!"));

        // 2) add permissions
        boolean hasSDCardPermissions = StoragePermissionManager.hasSDCardPermissions();
        messages.add("SD Card Permissions granted: " + hasSDCardPermissions);

        return messages;
    }

    public static final ArrayList<String> debugPathState(String info, String path)
    {
        ArrayList<String> messages = new ArrayList<>();

        // 1) add info line
        messages.add(info);

        // 2) add file state
        IFile file = StorageUtil.getFileByPath(path, null);
        if (file == null || !file.exists())
            messages.add("File exists: NO");
        else
            messages.add("File exists: YES");

        // 3) add media store state
        MediaStoreFileData mediaStoreFileData = MediaStoreUtil.loadMediaStoreData(ExtensionUtil.getMediaType(path), path);
        if (mediaStoreFileData == null)
            messages.add("Media store entry exists: NO");
        else
            messages.add("Media store entry exists: YES");

        // 4) add folder state
        File f = new File(path);
        File fFolder = f.getParentFile();
        IFile folder = StorageUtil.getFileByPath(fFolder.getAbsolutePath(), null);
        if (folder != null && folder.exists())
        {
            if (StorageUtil.hasNoMediaFile(folder, true))
                messages.add("Parent folder is HIDDEN");
            else
                messages.add("Parent folder is VISIBLE");
        }
        else
            messages.add("Parent folder NOT FOUND");

        return messages;
    }

    public static final String debugCheckPathState(String info, String path, boolean folderShouldBeHidden, boolean mediaStoreDataShouldExist, boolean fileShouldExist)
    {
        IFile file = StorageUtil.getFileByPath(path, null);
        boolean fileExists = file != null && file.exists();

        MediaStoreFileData mediaStoreFileData = MediaStoreUtil.loadMediaStoreData(ExtensionUtil.getMediaType(path), path);
        boolean mediaStoreDataExists = mediaStoreFileData != null;

        Boolean hasNoMediaFile = null;
        File f = new File(path);
        File fFolder = f.getParentFile();
        IFile folder = StorageUtil.getFileByPath(fFolder.getAbsolutePath(), null);
        if (folder != null && folder.exists())
            hasNoMediaFile = StorageUtil.hasNoMediaFile(folder, true);

        // validate
        ArrayList<String> errors = new ArrayList<>();
        if (fileExists != fileShouldExist)
            errors.add("wrong file status");
        if (mediaStoreDataExists != mediaStoreDataShouldExist)
            errors.add("wrong media store status");
        if (hasNoMediaFile == null)
            errors.add("parent folder does not exist");
        else if (hasNoMediaFile != folderShouldBeHidden)
            errors.add("wrong folder hidden status");

        // create info result text
        String result = "";
        if (info != null)
            result += info + " - ";
        if (errors.size() == 0)
            result += "STATUS OK";
        else
        {
            result += errors.size() + " ERRORS: ";
            for (int i = 0; i < errors.size(); i++)
            {
                if (i > 0)
                    result += " | ";
                result += errors.get(i);
            }
        }

        return result;
    }
}
