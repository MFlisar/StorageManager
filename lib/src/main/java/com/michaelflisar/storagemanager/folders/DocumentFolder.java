package com.michaelflisar.storagemanager.folders;

import android.support.v4.provider.DocumentFile;

import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.data.DocumentFolderData;
import com.michaelflisar.storagemanager.data.FileFolderData;
import com.michaelflisar.storagemanager.data.MediaStoreFolderData;
import com.michaelflisar.storagemanager.interfaces.IFile;
import com.michaelflisar.storagemanager.interfaces.IFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by flisar on 03.02.2016.
 */
public class DocumentFolder extends BaseFolder
{
    // File
    protected DocumentFile folder;

    // Document Folder Data
    protected DocumentFolderData documentFolderData;

    public DocumentFolder(DocumentFile file)
    {
        folder = file;
        documentFolderData = null;
        status = StorageDefinitions.FolderStatus.NotLoaded;
        files = new ArrayList<>();
    }

    // --------------------------------
    // Properties
    // --------------------------------

    public DocumentFile getFolder()
    {
        return folder;
    }

    @Override
    public String getName()
    {
        return folder.getName();
    }

    @Override
    public Integer getCount()
    {
        if (status == StorageDefinitions.FolderStatus.Loaded)
            return files.size();

        if (documentFolderData != null)
            return documentFolderData.getCount();
        return null;
    }

    // --------------------------------
    // Document Folder data
    // --------------------------------

    public DocumentFolderData getDocumentFolderData()
    {
        return documentFolderData;
    }

    public void setDocumentFolderData(DocumentFolderData data)
    {
        documentFolderData = data;
    }
}