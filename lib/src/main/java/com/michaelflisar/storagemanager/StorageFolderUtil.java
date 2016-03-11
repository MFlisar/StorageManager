package com.michaelflisar.storagemanager;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.provider.DocumentFile;

import com.michaelflisar.storagemanager.data.MediaStoreFolderData;
import com.michaelflisar.storagemanager.files.StorageDocument;
import com.michaelflisar.storagemanager.folders.BaseFolder;
import com.michaelflisar.storagemanager.folders.DocumentFolder;
import com.michaelflisar.storagemanager.folders.FileFolder;
import com.michaelflisar.storagemanager.interfaces.IFile;
import com.michaelflisar.storagemanager.interfaces.IFolder;
import com.michaelflisar.storagemanager.utils.DocumentUtil;
import com.michaelflisar.storagemanager.utils.ExternalStorageHelperPriorAndroidM;
import com.michaelflisar.storagemanager.utils.FileUtil;
import com.michaelflisar.storagemanager.utils.MediaStoreUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created by flisar on 04.03.2016.
 */
public class StorageFolderUtil
{
    // --------------------------------
    // Path listing functions
    // --------------------------------

    public static FileFolder getMainRootFolderWithoutFolderData()
    {
        File root = Environment.getExternalStorageDirectory();
        FileFolder f = new FileFolder(root);
        return f;
    }

    public static IFolder getSDCardRootFolder(FileFolder mainRoot)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
        {
            Map<String, File> externalLocations = ExternalStorageHelperPriorAndroidM.getAllStorageLocations();
            File sdCard = externalLocations.get(ExternalStorageHelperPriorAndroidM.EXTERNAL_SD_CARD);
            if (sdCard == null || sdCard.getAbsolutePath().equals(mainRoot.getFolder().getPath()))
                sdCard = null;

            if (sdCard != null)
            {
                FileFolder f = new FileFolder(sdCard);
                return f;
            }
        }
        else
        {
            File[] fs = StorageManager.get().getContext().getExternalFilesDirs(null);
            if (fs != null && fs.length == 2)
            {
                String mainPath = fs[0].getAbsolutePath();
                String subPath = mainPath.replace(mainRoot.getFolder().getPath(), "");
                String pathSDCard = fs[1].getAbsolutePath().replace(subPath, "");
                String sdCardIdNoColon = pathSDCard.replace("/storage/", "");

                // TODO: check if this is the root directory!!!

                Uri treeUri = Uri.withAppendedPath(Uri.parse(StorageDefinitions.AUTHORITY), sdCardIdNoColon + StorageDefinitions.AUTHORITY_COLON);
                DocumentFolder f = new DocumentFolder(treeUri);

                return f;
            }
        }
        return null;
    }

    // --------------------------------
    // Folder listing functions
    // --------------------------------



//    public static ArrayList<IFolder> listAllPrimaryStorageFolders(Boolean visible, List<StorageDefinitions.MediaType> typeToContain, boolean useMediaStoreIfPossible)
//    {
//        ArrayList<IFolder> folders = new ArrayList<>();
//
//        if (!useMediaStoreIfPossible || (visible != null && !visible))
//        {
//            File root = Environment.getExternalStorageDirectory();
//            folders.addAll(FileUtil.getAllFoldersWithoutContent(root, typeToContain, visible));
//        }
//
//        if (useMediaStoreIfPossible && (visible == null || visible))
//        {
//            List<StorageDefinitions.MediaType> typesToGet = typeToContain == null ? Arrays.asList(StorageDefinitions.MediaType.values()) : typeToContain;
//            for (int i = 0; i < typesToGet.size(); i++)
//                folders.addAll(MediaStoreUtil.getAllFoldersWithoutContent(typesToGet.get(i), true));
//
//            // security check => sometimes the media store is behind, so we check manually for .nomedia folder if necessary
//            if (visible != null && visible)
//            {
//                for (int i = folders.size() - 1; i >= 0; i--)
//                {
//                    if (FileUtil.hasNoMediaFileRecursive(((FileFolder) folders.get(i)).getFolder()))
//                        folders.remove(i);
//                }
//            }
//        }
//
//        return folders;
//    }
//
//    public static ArrayList<IFolder> listAllSecondaryStorageFolders(DocumentFile root, Boolean visible, List<StorageDefinitions.MediaType> typeToContain)
//    {
//        ArrayList<IFolder> folders = new ArrayList<>();
//        folders.addAll(DocumentUtil.getAllFoldersWithoutContent(root, typeToContain, visible));
//        return folders;
//    }
}
