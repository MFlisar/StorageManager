package com.michaelflisar.storagemanager;

import android.net.Uri;
import android.os.Build;

import com.michaelflisar.storagemanager.interfaces.IFile;
import com.michaelflisar.storagemanager.utils.DocumentUtil;
import com.michaelflisar.storagemanager.utils.FileUtil;

/**
 * Created by flisar on 09.06.2016.
 */
public class StorageUtilNew
{
    // this class summarisies all public library functions

    // ---------------------
    // file functions
    // ---------------------

    public static IFile getFileByPath(String path, Boolean isHidden, boolean createEmptyFile, String mimeType)
    {
        // before android M we just return a normal File...
        // 1) check if file is on internal storage
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ||
                path.startsWith(StorageManager.get().getRoot().getFolder().getPath()))
        {
            return FileUtil.createFile(path, isHidden, createEmptyFile);
        }

        // 2) check if file is on sd card storage
        // first 3 parts are an empty string, the storage and the id of the storage
        return DocumentUtil.createFileFromPath(path, mimeType, isHidden, createEmptyFile);
    }

//    public static IFile getFileByUri(Uri uri, Boolean isHidden, boolean createEmptyFile, String mimeType)
//    {
//        // before android M we just return a normal File...
//        // 1) check if file is on internal storage
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ||
//                path.startsWith(StorageManager.get().getRoot().getFolder().getPath()))
//        {
//            return FileUtil.createFile(path, isHidden, createEmptyFile);
//        }
//
//        // 2) check if file is on sd card storage
//        // first 3 parts are an empty string, the storage and the id of the storage
//        return DocumentUtil.createFileFromPath(path, mimeType, isHidden, createEmptyFile);
//    }

    // ---------------------
    // folder functions
    // ---------------------


}
