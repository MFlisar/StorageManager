package com.michaelflisar.storagemanager;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.michaelflisar.storagemanager.files.StorageDocument;
import com.michaelflisar.storagemanager.files.StorageFile;
import com.michaelflisar.storagemanager.folders.DocumentFolder;
import com.michaelflisar.storagemanager.folders.FileFolder;
import com.michaelflisar.storagemanager.interfaces.IFile;
import com.michaelflisar.storagemanager.interfaces.IFolder;
import com.michaelflisar.storagemanager.utils.DocumentUtil;
import com.michaelflisar.storagemanager.utils.ExtensionUtil;
import com.michaelflisar.storagemanager.utils.FileUtil;
import com.michaelflisar.storagemanager.utils.StorageDebugUtil;

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
    public static final String TAG = StorageUtil.class.getName();

    // --------------------------------
    // File functions
    // --------------------------------

    public static boolean exists(String path)
    {
        IFile f = getFileByPath(path, null, false, null);
        if (f == null)
            return false;
        // actually returning true directly would be fine as well...
        return f.exists();
    }

    public static boolean canCreate(String path)
    {
        IFile f = getFileByPath(path, null, false, null);
        if (f != null)
            return false;

        // create file and delete it again => no media store update!
        f = getFileByPath(path, null, true, null);
        boolean success = f.exists();
        f.delete(StorageDefinitions.MediaStoreUpdateType.None);
        return success;
    }

    public static boolean isPathAFile(String path)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ||
                path.startsWith(StorageManager.get().getRoot().getFolder().getPath()))
            return true;
        return false;
    }

    public static IFile getFileByPath(String path, Boolean isHidden)
    {
        return getFileByPath(path, isHidden, false, null);
    }

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
        if (StoragePermissionManager.hasSDCardPermissions())
            return DocumentUtil.createFileFromPath(path, mimeType, isHidden, createEmptyFile);
        return null;
    }

    public static IFile getFileByPath(String folder, String name, String mimeType, Boolean isHidden, boolean createEmptyFile)
    {
        // before android M we just return a normal File...
        // 1) check if file is on internal storage
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || folder.startsWith(StorageManager.get().getRoot().getFolder().getPath()))
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
                return new StorageFile(f, isHidden, false);
            return null;
        }

        // 2) check if file is on sd card storage
        // first 3 parts are an empty string, the storage and the id of the storage
//        List<String> parts = Arrays.asList(folder.split("/"));
//        parts.add(name);
        if (StoragePermissionManager.hasSDCardPermissions())
            return DocumentUtil.createFileFromPath(folder + "/" + name, mimeType, isHidden, createEmptyFile);
        return null;
    }

    public static IFolder createFolderFromFile(IFile file)
    {
        if (file.getType() == StorageDefinitions.FileType.File)
            return new FileFolder(((StorageFile)file).getWrapped());
        else
            return new DocumentFolder(((StorageDocument)file));
    }

    public static IFolder getFolderFromUriStringAndUpdatePermissions(Context context, String uriOrPath, boolean loadFolderContent)
    {
//        IFile folderFile = getFileFromUriStringAndUpdatePermissions(context, uriOrPath);
        if (uriOrPath.startsWith("content"))
        {
            if (uriOrPath.endsWith("/"))
                uriOrPath = uriOrPath.substring(0, uriOrPath.length() - 1);
//            uriOrPath = uriOrPath.replace(StorageDefinitions.AUTHORITY_COLON, ":");
//            uriOrPath = uriOrPath.replace(StorageDefinitions.AUTHORITY_SLASH, "/");
            Uri treeUri = Uri.parse(uriOrPath);

//            DocumentFile doc = DocumentFile.fromTreeUri(StorageManager.get().getContext(), Uri.parse(uriOrPath));
//            DocumentFolder f = new DocumentFolder(new StorageDocument(doc, null, false));

//            DocumentFolder f = new DocumentFolder((StorageDocument)StorageUtil.getFileByPath(uriOrPath, null));
             DocumentFolder f = new DocumentFolder(treeUri);
            if (StoragePermissionManager.checkPersistUriPermissionsAndUpdate(context, f))
                Log.d(TAG, "Permissions available and updated");
            else
                Log.d(TAG, "Permissions lost!");

            if (loadFolderContent)
                f.loadFiles(null, false, null, null, null, null, null, null);
            return f;
        }
        else
        {
            FileFolder f = new FileFolder(new File(uriOrPath));
            if (loadFolderContent)
                f.loadFiles(null, false, null, null, null, null, null, null);
            return f;
        }
    }

    public static IFile getFileFromUriStringAndUpdatePermissions(Context context, String uriOrPath)
    {
        if (uriOrPath.startsWith("content"))
        {
            if (uriOrPath.endsWith("/"))
                uriOrPath = uriOrPath.substring(0, uriOrPath.length() - 1);
            Uri treeUri = Uri.parse(uriOrPath);
            DocumentFile f = DocumentFile.fromTreeUri(context, treeUri);
            StorageDocument storageDocument = new StorageDocument(f, null, null);
            if (StoragePermissionManager.checkPersistUriPermissionsAndUpdate(context, storageDocument))
                Log.d(TAG, "Permissions available and updated");
            else
                Log.d(TAG, "Permissions lost!");

            return storageDocument;
        }
        else
        {
            return new StorageFile(new File(uriOrPath), null, null);
        }
    }

    public static String cleanPath(String path)
    {
        // we don't know if other apps use the API calls, so we consider following:
        // "/storage/emulated/0" == "/storage/emulated/legacy"
        // we replace any of those strings with our API reference!
        // useful if another app shares a PATH (instead of a URI) with your app and you want to clean this path

        if (path == null)
            return path;

        String apiRootPath = StorageManager.get().getRoot().getFolder().getPath();
        path = path.replace("/storage/emulated/0", apiRootPath);
        path = path.replace("/storage/emulated/legacy", apiRootPath);
        return path;
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
            file = StorageUtil.getFileByPath(path, null, false, mimeType);
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

    public static IFile createUnusedFile(String path, Boolean isHidden, String... namesThatAreNotAllowed)
    {
        String mimeType = ExtensionUtil.getMimeType(path);
        return getFileByPath(getUnusedFileName(path, namesThatAreNotAllowed), isHidden, true, mimeType);
    }

    public static IFile createNewFile(String path, Boolean isHidden)
    {
        if (exists(path))
            return null;
        String mimeType = ExtensionUtil.getMimeType(path);
        return getFileByPath(path, isHidden, true, mimeType);
    }

    public static IFile createNewFile(IFolder folder, String name, Boolean isHidden)
    {
        String path = folder.getFolder().getPath() + "/" + name;
        return createNewFile(path, isHidden);
    }

    public static IFile createFolder(IFile folder, String name)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP || folder.getType() == StorageDefinitions.FileType.File)
        {
            File f = new File(folder.getPath(), name);
            if (!f.exists())
                f.mkdir();
            if (f != null && f.exists() && f.isDirectory())
                return new StorageFile(f, null, false);
            return null;
        }

        // 2) check if file is on sd card storage
        // first 3 parts are an empty string, the storage and the id of the storage
        ArrayList<String> parts = new ArrayList(Arrays.asList(folder.getPath().split("/")));
        parts.add(name);
        return DocumentUtil.createFolderFromPath(parts, null, true);
    }

    // -------------------
    // No Media File
    // -------------------

    public static boolean hasNoMediaFile(File fileFolder, boolean recursive)
    {
        IFile folder = getFileByPath(fileFolder.getAbsolutePath(), null, false, null);
        return hasNoMediaFile(folder, recursive);
    }

//    public static boolean hasNoMediaFile(String folderPath, boolean recursive)
//    {
//        IFile folder = getFileByPath(folderPath, null, false, null);
//        return hasNoMediaFile(folder, recursive);
//    }

    public static boolean hasNoMediaFile(String folderPath, boolean recursive)
    {
        if (folderPath == null)
            return false;

        // for checking if file exists, we can always use the File class with the path
        File noMediaFile = new File(folderPath + "/.nomedia");
        return noMediaFile.exists();
    }

    public static boolean hasNoMediaFile(IFile folder, boolean recursive)
    {
        if (folder == null)
            return false;

        // for checking if file exists, we can always use the File class with the path
        File noMediaFile = new File(folder.getPath() + "/.nomedia");
        return noMediaFile.exists();

//        boolean hasNoMediaFile = getFileByPath(folder.getPath() + "/.nomedia", null, false, null) != null;
//        if (!hasNoMediaFile && recursive)
//        {
//            IFile parent = folder.getParent();
//            while (parent != null && !hasNoMediaFile)
//            {
//                hasNoMediaFile = hasNoMediaFile(parent, false);
//                parent = parent.getParent();
//            }
//        }
//        return hasNoMediaFile;
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