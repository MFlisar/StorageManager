package com.michaelflisar.storagemanager.utils;

import android.webkit.MimeTypeMap;

import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.data.FileFolderData;
import com.michaelflisar.storagemanager.data.MediaStoreFileData;
import com.michaelflisar.storagemanager.folders.FileFolder;
import com.michaelflisar.storagemanager.interfaces.IFile;
import com.michaelflisar.storagemanager.interfaces.IFolder;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by flisar on 03.02.2016.
 */
public class FileUtil
{
    // --------------------------------
    // Definitions
    // --------------------------------

    public final static FilenameFilter FOLDER_FILTER = new FilenameFilter() {
        @Override
        public boolean accept(File current, String name) {
            return new File(current, name).isDirectory();
        }
    };

    public final static FilenameFilter IMAGE_FILTER = new FilenameFilter() {

        @Override
        public boolean accept(final File dir, final String name) {
            for (final String ext : StorageDefinitions.IMG_FORMATS) {
                if (name.toLowerCase().endsWith("." + ext)) {
                    return (true);
                }
            }
            return (false);
        }
    };

    public final static FilenameFilter VIDEO_FILTER = new FilenameFilter() {

        @Override
        public boolean accept(final File dir, final String name) {
            for (final String ext : StorageDefinitions.VID_FORMATS) {
                if (name.toLowerCase().endsWith("." + ext)) {
                    return (true);
                }
            }
            return (false);
        }
    };

    // --------------------------------
    // Listings
    // --------------------------------

    public static List<IFolder> getAllFoldersWithoutContent(File root, StorageDefinitions.MediaType type, Boolean visible)
    {
        return getAllFoldersWithoutContent(root, Arrays.asList(type), visible);
    }

    public static List<IFolder> getAllFoldersWithoutContent(File root, List<StorageDefinitions.MediaType> type, Boolean visible)
    {
        List<IFolder> folders = new ArrayList<>();
        checkFolderForMedia(type, folders, root, visible, true);
        return folders;
    }

    public static void checkFolderForMedia(List<StorageDefinitions.MediaType> type, List<IFolder> folders, File folder, Boolean visible, boolean recursiveIntoDepth)
    {
        if (!folder.isDirectory())
            return;

        File[] files = folder.listFiles();

        // 1) recall this function recursively for all sub folders
        if (recursiveIntoDepth)
        {
            for (int i = 0; i < files.length; i++)
                checkFolderForMedia(type, folders, files[i], visible, recursiveIntoDepth);
        }

        // 2) check if folder is hidden/visible if necessary
        if (visible != null)
        {
            File fNoMedia = new File(folder, ".nomedia");
            boolean hasNoMediaFile = Arrays.asList(files).contains(fNoMedia);
            if ((visible && hasNoMediaFile) || (!visible && !hasNoMediaFile))
                return;
        }

        // 3) count relevant files in folder
        int count = 0;
        if (type == null)
        {
            for (File f : folder.listFiles())
            {
                if (!f.isDirectory())
                    count++;
            }
        }
        else
        {
            for (int i = 0; i < type.size(); i++)
            {
                // TODO...
                switch (type.get(i))
                {
                    case Image:
                        count = folder.listFiles(IMAGE_FILTER).length;
                        break;
                    case Video:
                        count = folder.listFiles(VIDEO_FILTER).length;
                        break;
                    default:
                        throw new RuntimeException("Type not handled!");
                }

            }
        }

        FileFolder f = new FileFolder(folder);
        f.setFileFolderData(new FileFolderData(count));
        folders.add(f);
    }

    public static List<File> getFolderFiles(File folder, List<StorageDefinitions.MediaType> type, Integer limit)
    {
        List<File> files = new ArrayList<>();
        if (type == null)
        {
            for (File f : folder.listFiles())
            {
                files.add(f);
                if (limit != null && files.size() == limit)
                    break;
            }
        }
        else
        {
            for (int i = 0; i < type.size(); i++)
            {
                switch (type.get(i))
                {
                    case Image:
                        files.addAll(Arrays.asList(folder.listFiles(IMAGE_FILTER)));
                        break;
                    case Video:
                        files.addAll(Arrays.asList(folder.listFiles(VIDEO_FILTER)));
                        break;
                    default:
                        throw new RuntimeException("Type not handled!");
                }

                if (limit != null)
                    files = new ArrayList<>(files.subList(0, Math.min(limit, files.size())));
            }
        }
        return files;
    }

    // --------------------------------
    // Functions
    // --------------------------------
}
