package com.michaelflisar.storagemanager.folders;

import android.support.v4.provider.DocumentFile;

import com.michaelflisar.storagemanager.data.DocumentFolderData;
import com.michaelflisar.storagemanager.data.FileFolderData;
import com.michaelflisar.storagemanager.data.MediaStoreFolderData;
import com.michaelflisar.storagemanager.interfaces.IFolder;

import java.io.File;

/**
 * Created by flisar on 03.02.2016.
 */
public class DocumentFolder implements IFolder
{
    // File
    protected DocumentFile file;

    // Document Folder Data
    protected DocumentFolderData documentFolderData;

    public DocumentFolder(DocumentFile file)
    {
        this.file = file;
        documentFolderData = null;
    }

    // --------------------------------
    // Properties
    // --------------------------------

    public DocumentFile getFile()
    {
        return file;
    }

    @Override
    public String getName()
    {
        return file.getName();
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