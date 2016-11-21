package com.michaelflisar.storagemanager;

import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.michaelflisar.storagemanager.folders.DocumentFolder;
import com.michaelflisar.storagemanager.folders.FileFolder;
import com.michaelflisar.storagemanager.interfaces.IFolder;
import com.michaelflisar.storagemanager.utils.ExternalStorageHelperPriorAndroidM;

import java.io.File;
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
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
                if (fs[0] != null && fs[1] != null)
                {
                    String mainPath = fs[0].getAbsolutePath();
                    String subPath = mainPath.replace(mainRoot.getFolder().getPath(), "");
                    String pathSDCard = fs[1].getAbsolutePath().replace(subPath, "");
                    String sdCardIdNoColon = pathSDCard.replace("/storage/", "");

                    // TODO: check if this is the root directory!!!
                    Uri treeUri = Uri.withAppendedPath(Uri.parse(StorageDefinitions.AUTHORITY_TREE), sdCardIdNoColon + StorageDefinitions.AUTHORITY_COLON);
                    DocumentFolder f = new DocumentFolder(treeUri);

                    return f;
                }
                else
                {
                    Log.d(StorageManager.TAG, "fs[0]=" + (fs[0] == null ? "NULL" : fs[0]) + ", fs[1]=" + (fs[1] == null ? "NULL" : fs[1]));
                }
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
