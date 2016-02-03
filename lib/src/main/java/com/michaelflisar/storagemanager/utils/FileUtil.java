package com.michaelflisar.storagemanager.utils;

import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.data.FileFolderData;
import com.michaelflisar.storagemanager.folders.FileFolder;
import com.michaelflisar.storagemanager.interfaces.IFolder;

import java.io.File;
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

    public static List<IFolder> getAllFoldersWithoutContent(File root, List<StorageDefinitions.MediaType> type, Boolean visible)
    {
        List<IFolder> folders = new ArrayList<>();
        checkFolderForMedia(type, folders, root, visible);
        return folders;
    }

    private static void checkFolderForMedia(List<StorageDefinitions.MediaType> type, List<IFolder> folders, File folder, Boolean visible)
    {
        if (!folder.isDirectory())
            return;

        File[] files = folder.listFiles();

        // 1) recall this function recursively for all sub folders
        for (int i = 0; i < files.length; i++)
            checkFolderForMedia(type, folders, files[i], visible);

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


    // --------------------------------
    // Functions
    // --------------------------------

    public static boolean hasNoMediaFile(File folder)
    {
        if (!folder.isDirectory())
            return false;

        File[] files = folder.listFiles();
        if (files == null || files.length == 0)
            return false;

        // 1) prüfen ob der Ordner ein .nomedia folder enthält
        File fNoMedia = new File(folder, ".nomedia");
        if (!Arrays.asList(files).contains(fNoMedia))
            return false;

        return true;
    }

    public static boolean hasNoMediaFileRecursive(File folder)
    {
        boolean hasNoMediaFile = hasNoMediaFile(folder);
        if (hasNoMediaFile)
            return true;

        if (folder.getParentFile() != null)
            return hasNoMediaFileRecursive(folder.getParentFile());
        else
            return false;
    }


}
