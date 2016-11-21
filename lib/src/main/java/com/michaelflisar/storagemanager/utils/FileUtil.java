package com.michaelflisar.storagemanager.utils;

import android.webkit.MimeTypeMap;

import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.data.FileFolderData;
import com.michaelflisar.storagemanager.data.MediaStoreFileData;
import com.michaelflisar.storagemanager.files.StorageFile;
import com.michaelflisar.storagemanager.folders.FileFolder;
import com.michaelflisar.storagemanager.interfaces.IFile;
import com.michaelflisar.storagemanager.interfaces.IFolder;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
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

    public final static FilenameFilter FOLDER_FILTER = new FilenameFilter()
    {
        @Override
        public boolean accept(File current, String name)
        {
            return new File(current, name).isDirectory();
        }
    };

    public final static FilenameFilter IMAGE_FILTER = new FilenameFilter()
    {

        @Override
        public boolean accept(final File dir, final String name)
        {
            for (final String ext : StorageDefinitions.IMG_FORMATS)
            {
                if (name.toLowerCase().endsWith("." + ext))
                {
                    return (true);
                }
            }
            return (false);
        }
    };

    public final static FilenameFilter VIDEO_FILTER = new FilenameFilter()
    {

        @Override
        public boolean accept(final File dir, final String name)
        {
            for (final String ext : StorageDefinitions.VID_FORMATS)
            {
                if (name.toLowerCase().endsWith("." + ext))
                {
                    return (true);
                }
            }
            return (false);
        }
    };

    public static StorageFile createFile(String path, Boolean isHidden, boolean createEmptyFile)
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
            return new StorageFile(f, isHidden, false);
        return null;
    }

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

        if (files == null)
            return;

        // 1) check if folder is hidden/visible if necessary
        Boolean hasNoMediaFile = null;
        if (visible != null)
        {
            File fNoMedia = new File(folder, ".nomedia");
            hasNoMediaFile = Arrays.asList(files).contains(fNoMedia);
        }

        // 2) recall this function recursively for all sub folders
        if (recursiveIntoDepth)
        {
            if (visible != null && visible && hasNoMediaFile)
            {
                // we don't check a hidden folders content if we are only interested in visible files
            }
            else
            {
                for (int i = 0; i < files.length; i++)
                    checkFolderForMedia(type, folders, files[i], visible, recursiveIntoDepth);
            }
        }

        // 3) check if we are interested in this folder
        if (visible != null)
        {
            if ((visible && hasNoMediaFile) || (!visible && !hasNoMediaFile))
                return;
        }

        FileFolder f = new FileFolder(folder);
        // 4) count relevant files in folder
        initFileFolderCount(f, visible, type);
        folders.add(f);
    }

    public static void initFileFolderCount(FileFolder folder, boolean visible, List<StorageDefinitions.MediaType> type)
    {
        int count = 0;
        IFile mainFile = null;
        if (type == null)
        {
            File fileFolder = folder.getFolder().getWrapped();
            for (File f : fileFolder.listFiles())
            {
                if (!f.isDirectory())
                    count++;
                if (count == 1)
                    mainFile = new StorageFile(f, visible, false);
            }
        }
        else
        {
            File fileFolder = folder.getFolder().getWrapped();
            for (int i = 0; i < type.size(); i++)
            {
                // TODO...
                File[] listedFiles = null;
                switch (type.get(i))
                {
                    case Image:
                        listedFiles = fileFolder.listFiles(IMAGE_FILTER);
                        break;
                    case Video:
                        listedFiles = fileFolder.listFiles(VIDEO_FILTER);
                        break;
                    default:
                        throw new RuntimeException("Type not handled!");
                }
                count = listedFiles.length;
                if (count > 0)
                    mainFile = new StorageFile(listedFiles[0], visible, false);
            }
        }
        folder.setFileFolderData(new FileFolderData(mainFile, count));
    }

    public static List<File> getFolderFiles(File folder, List<StorageDefinitions.MediaType> type, Integer limit, Long minDate, Long maxDate, MediaStoreUtil.DateType dateType)
    {
        List<File> files = new ArrayList<>();
        Long date;
        boolean isValid;
        if (folder.exists())
        {
            if (type == null)
            {
                for (File f : folder.listFiles())
                {
                    if (dateType != null)
                    {
                        if (dateType == MediaStoreUtil.DateType.Created || dateType == MediaStoreUtil.DateType.Added)
                            isValid = true; // we can't check this!
                        else
                        {
                            date = f.lastModified();
                            isValid = date >= minDate && date <= maxDate;
                        }
                        if (isValid)
                            files.add(f);
                    }
                    else
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
                    if (dateType != null)
                    {
                        ArrayList<File> filtered = new ArrayList<>();
                        for (File f : files)
                        {
                            if (dateType == MediaStoreUtil.DateType.Created || dateType == MediaStoreUtil.DateType.Added)
                                isValid = true; // we can't check this!
                            else
                            {
                                date = f.lastModified();
                                isValid = date >= minDate && date <= maxDate;
                            }

                            if (isValid)
                            {
                                filtered.add(f);
                                if (limit != null && filtered.size() == limit)
                                    break;
                            }
                        }
                        files = filtered;
                    }
                }
            }
        }
        return files;
    }

    // --------------------------------
    // Functions
    // --------------------------------
}
