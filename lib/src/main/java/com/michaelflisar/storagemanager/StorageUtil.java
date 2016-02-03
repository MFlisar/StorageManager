package com.michaelflisar.storagemanager;

import android.content.Context;
import android.os.Environment;
import android.support.v4.provider.DocumentFile;

import com.michaelflisar.storagemanager.folders.FileFolder;
import com.michaelflisar.storagemanager.utils.DocumentUtil;
import com.michaelflisar.storagemanager.utils.FileUtil;
import com.michaelflisar.storagemanager.utils.MediaStoreUtil;
import com.michaelflisar.storagemanager.interfaces.IFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by flisar on 03.02.2016.
 */
public class StorageUtil
{
    // --------------------------------
    // Folder listing functions
    // --------------------------------

    public static ArrayList<IFolder> listAllPrimaryStorageFolders(Context context, Boolean visible, List<StorageDefinitions.MediaType> typeToContain, boolean useMediaStoreIfPossible)
    {
        ArrayList<IFolder> folders = new ArrayList<>();

        if (!useMediaStoreIfPossible || (visible != null && !visible))
        {
            File root = Environment.getExternalStorageDirectory();
            folders.addAll(FileUtil.getAllFoldersWithoutContent(root, typeToContain, visible));
        }

        if (useMediaStoreIfPossible && (visible == null || visible))
        {
            List<StorageDefinitions.MediaType> typesToGet = typeToContain == null ? Arrays.asList(StorageDefinitions.MediaType.values()) : typeToContain;
            for (int i = 0; i < typesToGet.size(); i++)
                folders.addAll(MediaStoreUtil.getAllFoldersWithoutContent(context, typesToGet.get(i), true));

            // security check => sometimes the media store is behind, so we check manually for .nomedia folder if necessary
            if (visible != null && visible)
            {
                for (int i = folders.size() - 1; i >= 0; i--)
                {
                    if (FileUtil.hasNoMediaFileRecursive(((FileFolder) folders.get(i)).getFolder()))
                        folders.remove(i);
                }
            }
        }

        return folders;
    }

    public static ArrayList<IFolder> listAllSecondaryStorageFolders(DocumentFile root, Boolean visible, List<StorageDefinitions.MediaType> typeToContain)
    {
        ArrayList<IFolder> folders = new ArrayList<>();
        folders.addAll(DocumentUtil.getAllFoldersWithoutContent(root, typeToContain, visible));
        return folders;
    }

    // --------------------------------
    // File listing functions
    // --------------------------------

    // --------------------------------
    // Access functions
    // --------------------------------


}