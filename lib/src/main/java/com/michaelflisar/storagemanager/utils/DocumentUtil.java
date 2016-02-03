package com.michaelflisar.storagemanager.utils;

import android.support.v4.provider.DocumentFile;

import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.data.DocumentFolderData;
import com.michaelflisar.storagemanager.folders.DocumentFolder;
import com.michaelflisar.storagemanager.interfaces.IFolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Prometheus on 29.01.2016.
 */
public class DocumentUtil
{
    public static List<IFolder> getAllFoldersWithoutContent(DocumentFile root, List<StorageDefinitions.MediaType> type, Boolean visible)
    {
        List<IFolder> folders = new ArrayList<>();
        checkFolderForMedia(type, folders, root, visible);
        return folders;
    }

    private static void checkFolderForMedia(List<StorageDefinitions.MediaType> type, List<IFolder> folders, DocumentFile folder, Boolean visible)
    {
        if (!folder.isDirectory())
            return;

        DocumentFile[] files = folder.listFiles();

        // 1) recall this function recursively for all sub folders
        for (int i = 0; i < files.length; i++)
            checkFolderForMedia(type, folders, files[i], visible);

        // 2) check if folder is hidden/visible if necessary
        if (visible != null)
        {
            String noMediaFile = ".nomedia";
            boolean hasNoMediaFile = false;
            for (int i = 0; i < files.length; i++)
            {
                if (files[i].getName().equals(noMediaFile))
                {
                    hasNoMediaFile = true;
                    break;
                }
            }
            if ((visible && hasNoMediaFile) || (!visible && !hasNoMediaFile))
                return;
        }

        // 3) count relevant files in folder
        int count = 0;
        if (type == null)
        {
            for (DocumentFile f : folder.listFiles())
            {
                if (!f.isDirectory())
                    count++;
            }
        }
        else
        {
            for (int i = 0; i < type.size(); i++)
            {
                ArrayList<String> validFormats = null;
                switch (type.get(i))
                {
                    case Image:
                        validFormats = StorageDefinitions.IMG_FORMATS;
                        break;
                    case Video:
                        validFormats = StorageDefinitions.VID_FORMATS;
                        break;
                    default:
                        throw new RuntimeException("Type not handled!");
                }

                for (DocumentFile f : folder.listFiles())
                {
                    boolean isValid = false;
                    for (int j = 0; j < validFormats.size(); j++)
                    {
                        if (f.getName().toLowerCase().endsWith("." + validFormats.get(i)))
                        {
                            isValid = true;
                            break;
                        }
                    }

                    if (isValid)
                        count++;
                }
            }
        }

        DocumentFolder f = new DocumentFolder(folder);
        f.setDocumentFolderData(new DocumentFolderData(count));
        folders.add(f);
    }
}
