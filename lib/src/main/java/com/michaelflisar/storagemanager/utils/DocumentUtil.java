package com.michaelflisar.storagemanager.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;

import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.StorageManager;
import com.michaelflisar.storagemanager.data.DocumentFolderData;
import com.michaelflisar.storagemanager.files.StorageDocument;
import com.michaelflisar.storagemanager.folders.DocumentFolder;
import com.michaelflisar.storagemanager.interfaces.IFile;
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
        List<DocumentFile> filesInFolder = getFolderFiles(folder, type, null);
        count = filesInFolder.size();

        DocumentFolder f = new DocumentFolder(new StorageDocument(folder, null));
        f.setDocumentFolderData(new DocumentFolderData(count));
        folders.add(f);
    }

    public static List<DocumentFile> getFolderFiles(DocumentFile folder, List<StorageDefinitions.MediaType> type, Integer limit)
    {
        List<DocumentFile> files = new ArrayList<>();
        if (type == null)
        {
            for (DocumentFile f : folder.listFiles())
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
                    {
                        files.add(f);
                        if (limit != null && files.size() == limit)
                            break;
                    }
                }
            }
        }
        return files;
    }

    public static String getPath(DocumentFile file)
    {
        String sep = "/";
        String path = sep + file.getName();
        DocumentFile temp = file;
        while ((temp = temp.getParentFile()) != null)
            path = sep + temp.getName() + path;
        // remove this part again, as the media store will NOT have this in it... so that paths stay compareable
        if (!path.startsWith("/storage"))
            path = "/storage" + path;//.substring(8);
        return path;
    }

    public static long getLastModified(DocumentFile file)
    {
        long lastModified = 0;
        final Cursor cursor = StorageManager.get().getContext().getContentResolver().query(file.getUri(), null, null, null, null);
        try
        {
            if (cursor.moveToFirst())
                lastModified = cursor.getLong(cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_LAST_MODIFIED));
        }
        finally
        {
            cursor.close();
        }
        return lastModified;
    }

    public static long getSize(DocumentFile file)
    {
        long size = 0;
        final Cursor cursor = StorageManager.get().getContext().getContentResolver().query(file.getUri(), null, null, null, null);
        try
        {
            if (cursor.moveToFirst())
                size = cursor.getLong(cursor.getColumnIndexOrThrow(DocumentsContract.Document.COLUMN_SIZE));
        }
        finally
        {
            cursor.close();
        }
        return size;
    }

    public static StorageDocument createFromPath(List<String> pathParts, String mimeType, boolean createEmptyFile)
    {
        // path can look like following:
        // * /storage/18F2-1104/...
        // * /18F2-1104/...
        Integer startIndex = null;
        if (pathParts.size() > 1 && pathParts.get(1).equals(StorageManager.get().getSDCardID()))
            startIndex = 2;
        else if (pathParts.size() > 2 && pathParts.get(1).equals("storage") && pathParts.get(2).equals(StorageManager.get().getSDCardID()))
            startIndex = 3;
        if (startIndex != null)
                //pathParts.size() >= 3 && pathParts.get(1).equals("storage") && pathParts.get(2).equals(StorageManager.get().getSDCardID()))
        {
            // file is on sd card
            DocumentFolder sdCardRoot = (DocumentFolder)StorageManager.get().getSDCardRoot();
            DocumentFile doc = sdCardRoot.getFolder().getWrapped();
            for (int i = startIndex; i < pathParts.size(); i++)
            {
                DocumentFile nextDoc = doc.findFile(pathParts.get(i));
                if (nextDoc != null)
                    doc = nextDoc;
                else if (createEmptyFile)
                {
                    if (i == pathParts.size() - 1)
                        doc = doc.createFile(mimeType, pathParts.get(i));
                    else
                        doc = doc.createDirectory(pathParts.get(i));
                }
                else
                {
                    doc = null;
                    break;
                }
            }
            return doc != null ? new StorageDocument(doc, false) : null;
        }
        return null;
    }
}
