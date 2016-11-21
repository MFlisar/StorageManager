package com.michaelflisar.storagemanager.folders;

import android.net.Uri;
import android.support.v4.provider.DocumentFile;

import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.StorageManager;
import com.michaelflisar.storagemanager.StorageUtil;
import com.michaelflisar.storagemanager.data.DocumentFolderData;
import com.michaelflisar.storagemanager.data.MediaStoreFileData;
import com.michaelflisar.storagemanager.files.StorageDocument;
import com.michaelflisar.storagemanager.interfaces.IFile;
import com.michaelflisar.storagemanager.interfaces.IFolderData;
import com.michaelflisar.storagemanager.utils.DocumentUtil;
import com.michaelflisar.storagemanager.utils.MediaStoreUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by flisar on 03.02.2016.
 */
public class DocumentFolder extends BaseFolder<DocumentFile>
{
    // Document Folder Data
    protected DocumentFolderData documentFolderData;

    public DocumentFolder(StorageDocument file)
    {
        super(file);
        documentFolderData = null;
        status = StorageDefinitions.FolderStatus.NotLoaded;
        files = new ArrayList<>();
    }

    public DocumentFolder(Uri treeUri)
    {
        // this folder is not read/writeable! but if I use fromTreeUri, it does not work here!
        super(new StorageDocument(DocumentFile.fromSingleUri(StorageManager.get().getContext(), treeUri), null, null));
        documentFolderData = null;
        status = StorageDefinitions.FolderStatus.NotLoaded;
        files = new ArrayList<>();
    }

    public void updateDocument(DocumentFile doc)
    {
        folder = new StorageDocument(doc, null, null);
    }

    @Override
    public List<IFile<DocumentFile>> loadFilesInternally(boolean loadFromMediaStore, StorageDefinitions.FileSortingType limitSortingType, StorageDefinitions.FileSortingOrder limitSortingOrder, Integer limit, Long minDate, Long maxDate, MediaStoreUtil.DateType dateType)
    {
//        List<IFile<DocumentFile>> files = new ArrayList<>();

        boolean isHidden = StorageUtil.hasNoMediaFile(getFolder().getParent(), true);
        if (loadFromMediaStore)
        {
            List<MediaStoreFileData> folderFiles = MediaStoreUtil.loadFilesInFolder(mediaStoreFolderData.getBucketId(), fileTypesToList, limitSortingType, limitSortingOrder, limit, minDate, maxDate, dateType);
            for (int i = 0; i < folderFiles.size(); i++)
            {
//                StorageDocument file = (StorageDocument)StorageUtil.getFileByPath(folderFiles.get(i).getData(), isHidden);
//                file.setMediaStoreFileData(folderFiles.get(i));
                // lazy creation!
                StorageDocument file = new StorageDocument(isHidden);
                file.initLazyly(folderFiles.get(i).getData(), folderFiles.get(i));
                files.add(file);
            }
        }
        else
        {
            // TODO: use sorting and order? then you would have to load all files even if limit is set!
            // TODO: minCreationDate not yet supported!
            List<DocumentFile> folderFiles = DocumentUtil.getFolderFiles(folder.getWrapped(), fileTypesToList, limit, minDate, maxDate, dateType);
            for (int i = 0; i < folderFiles.size(); i++)
                files.add(new StorageDocument(folderFiles.get(i), isHidden, null)); // MediaStoreData is set to null, not using media store does only make sense when loading data in a hidden folder!
        }

        return files;
    }

    // --------------------------------
    // Properties
    // --------------------------------

    @Override
    public final StorageDefinitions.FolderType getType()
    {
        return StorageDefinitions.FolderType.DocumentFolder;
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

        if (documentFolderData != null && documentFolderData.knowsCount())
            return documentFolderData.getCount();

        if (mediaStoreFolderData != null)
            return mediaStoreFolderData.getCount();

        return null;
    }

    @Override
    public IFolderData getFolderData()
    {
        return documentFolderData;
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