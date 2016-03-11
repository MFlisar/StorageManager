package com.michaelflisar.storagemanager;

import android.content.Context;
import android.os.Build;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.michaelflisar.storagemanager.files.StorageDocument;
import com.michaelflisar.storagemanager.files.StorageFile;
import com.michaelflisar.storagemanager.folders.DocumentFolder;
import com.michaelflisar.storagemanager.interfaces.IFile;
import com.michaelflisar.storagemanager.utils.DocumentUtil;
import com.michaelflisar.storagemanager.utils.ExtensionUtil;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by flisar on 03.02.2016.
 */
public class StorageUtil
{
    // --------------------------------
    // File functions
    // --------------------------------

    public static boolean exists(String path)
    {
        IFile f = getFileByPath(path, false, null);
        if (f == null)
            return false;
        // actually returning true directly would be fine as well...
        return f.exists();
    }

    public static boolean canCreate(String path)
    {
        IFile f = getFileByPath(path, false, null);
        if (f != null)
            return false;

        // create file and delete it again => no media store update!
        f = getFileByPath(path, true, null);
        boolean success = f.exists();
        f.delete(StorageDefinitions.MediaStoreUpdateType.None, null);
        return success;
    }

    public static IFile getFileByPath(String path)
    {
        return getFileByPath(path, false, null);
    }

    public static IFile getFileByPath(String path, boolean createEmptyFile, String mimeType)
    {
        // before android M we just return a normal File...
        // 1) check if file is on internal storage
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || path.startsWith(StorageManager.get().getRoot().getFolder().getPath()))
        {
            File f = new File(path);
            if (!f.exists() && createEmptyFile)
            {
                try
                {
                    if (!f.createNewFile())
                        f = null;
                }
                catch (IOException e)
                {
                    f = null;
                }
            }
            else if (!f.exists())
                f = null;
            // TODO: decide via paramter if media store data should be loaded
            if (f != null)
                return new StorageFile(f, false);
            return null;
        }

        // 2) check if file is on sd card storage
        // first 3 parts are an empty string, the storage and the id of the storage
        List<String> parts = Arrays.asList(path.split("/"));
        return DocumentUtil.createFromPath(parts, mimeType, createEmptyFile);
    }

    public static IFile getFileByPath(String folder, String name, String mimeType, boolean createEmptyFile)
    {
        // before android M we just return a normal File...
        // 1) check if file is on internal storage
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || folder.startsWith(StorageManager.get().getRoot().getFolder().getPath()))
        {
            File f = new File(folder, name);
            if (!f.exists() && createEmptyFile)
            {
                try
                {
                    if (!f.createNewFile())
                        f = null;
                }
                catch (IOException e)
                {
                    f = null;
                }
            }
            else if (!f.exists())
                f = null;
            // TODO: decide via parameter if media store data should be loaded
            if (f != null)
                return new StorageFile(f, false);
            return null;
        }

        // 2) check if file is on sd card storage
        // first 3 parts are an empty string, the storage and the id of the storage
        List<String> parts = Arrays.asList(folder.split("/"));
        parts.add(name);
        return DocumentUtil.createFromPath(parts, mimeType, createEmptyFile);
    }

    public static String getUnusedFileName(String path, String... namesThatAreNotAllowed)
    {
        IFile file = null;
        ArrayList<String> notAllowedNames = new ArrayList<>();
        if (namesThatAreNotAllowed != null && namesThatAreNotAllowed.length > 0)
            notAllowedNames.addAll(Arrays.asList(namesThatAreNotAllowed));

        File f = new File(path);
        String folder = f.getParentFile().getAbsolutePath() + "/";
        String filename = f.getPath().substring(folder.length());
        String name = filename;
        String ext = "";
        String mimeType = ExtensionUtil.getMimeType(path);
        //IFile file = StorageUtil.getFileByPath(path, false, mimeType);

        boolean search = false;
        // 1) check if path itself contains names that are not allowed
        if (notAllowedNames != null && notAllowedNames.contains(name))
            search = true;
        // 2) check if path exists
        else
        {
            file = StorageUtil.getFileByPath(path, false, mimeType);
            if (file != null && file.exists())
            {
                search = true;
                file = null;
            }
        }

        // if necessary, find next free path...
        String freePath = null;
        if (!search)
            freePath = path;
        else
        {
            long startNumber = 1;
            if (name.contains("."))
            {
                String[] tokens = filename.split("\\.(?=[^\\.]+$)");
                name = tokens[0];
                ext = "." + tokens[1];

                // TODO: limit length? Name could contain invalid number (too long number). Currently this case is handled by the exception handler...
                // TODO: Eventuell Länge limitieren? Sonst könnte eine ungültig lange Zahl dastehen!!! Wird aber eh von Overflow korrigiert und von Catch...
//                // check name for number
                Pattern p = Pattern.compile("(.*)-(\\d+)$");
                Matcher m = p.matcher(name);
                if (m.find()) {
                    name = m.group(1);
                    try {
                        startNumber = Long.valueOf(m.group(2));
                    } catch (NumberFormatException e) {
                        startNumber = 1;
                    }
                }
            }

            while (true)
            {
                freePath = folder + name + "-" + startNumber + ext;
                if (canCreate(freePath))
                    break;
                startNumber++;
            }
        }

        return freePath;
    }

    public static IFile createUnusedFile(String path, String... namesThatAreNotAllowed)
    {
        String mimeType = ExtensionUtil.getMimeType(path);
        return getFileByPath(getUnusedFileName(path, namesThatAreNotAllowed), true, mimeType);
    }

    // -------------------
    // No Media File
    // -------------------

    public static boolean hasNoMediaFile(File fileFolder, boolean recursive)
    {
        IFile folder = getFileByPath(fileFolder.getAbsolutePath(), false, null);
        return hasNoMediaFile(folder, recursive);
    }

    public static boolean hasNoMediaFile(String folderPath, boolean recursive)
    {
        IFile folder = getFileByPath(folderPath, false, null);
        return hasNoMediaFile(folder, recursive);
    }

    public static boolean hasNoMediaFile(IFile folder, boolean recursive)
    {
        if (folder == null)
            return false;

        boolean hasNoMediaFile = getFileByPath(folder.getPath() + "/.nomedia", false, null) != null;
        if (!hasNoMediaFile && recursive)
        {
            IFile parent = folder.getParent();
            while (parent != null && !hasNoMediaFile)
            {
                hasNoMediaFile = hasNoMediaFile(parent, false);
                parent = parent.getParent();
            }
        }
        return hasNoMediaFile;
    }

//    public static boolean hasNoMediaFile(String folderPath)
//    {
//        return hasNoMediaFile(new File(folderPath));
//    }

//    public static boolean hasNoMediaFile(File folder)
//    {
//        if (!folder.isDirectory())
//            return false;
//
//        File[] files = folder.listFiles();
//        if (files == null || files.length == 0)
//            return false;
//
//        // 1) prüfen ob der Ordner ein .nomedia folder enthält
//        File fNoMedia = new File(folder, ".nomedia");
//        if (!Arrays.asList(files).contains(fNoMedia))
//            return false;
//
//        return true;
//    }

//    public static boolean hasNoMediaFileRecursive(IFile folder)
//    {
//        return hasNoMediaFileRecursive(folder.getPath());
//    }
//
//    public static boolean hasNoMediaFileRecursive(String folderPath)
//    {
//        return hasNoMediaFileRecursive(new File(folderPath));
//    }
//
//    public static boolean hasNoMediaFileRecursive(File folder)
//    {
//        boolean hasNoMediaFile = hasNoMediaFile(folder);
//        if (hasNoMediaFile)
//            return true;
//
//        if (folder.getParentFile() != null)
//            return hasNoMediaFileRecursive(folder.getParentFile());
//        else
//            return false;
//    }


}