package com.michaelflisar.storagemanager.files;

import android.content.ContentProviderResult;
import android.location.Location;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.michaelflisar.storagemanager.MediaStoreUpdateManager;
import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.StorageManager;
import com.michaelflisar.storagemanager.StorageUtil;
import com.michaelflisar.storagemanager.data.MediaStoreFileData;
import com.michaelflisar.storagemanager.interfaces.IFile;
import com.michaelflisar.storagemanager.interfaces.IMediaStoreFile;
import com.michaelflisar.storagemanager.utils.DocumentUtil;
import com.michaelflisar.storagemanager.utils.ExifFileUtil;
import com.michaelflisar.storagemanager.utils.ExtensionUtil;
import com.michaelflisar.storagemanager.utils.FileUtil;
import com.michaelflisar.storagemanager.utils.MediaStoreUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Created by flisar on 03.02.2016.
 */
public class StorageDocument extends BaseFile<DocumentFile> implements IFile<DocumentFile>, IMediaStoreFile<DocumentFile>
{
    // File
    protected DocumentFile file;
    protected String path;

    /**
     * this creates an empty StorageDocument, call initLazyly afterwards!
     */
    public StorageDocument(Boolean isHidden)
    {
        super(isHidden);
        // this lazily creates the document!!!
        // call
        this.path = null;
        this.file = null;
    }

    public StorageDocument(DocumentFile file, Boolean isHidden, boolean loadMediaStoreData)
    {
        super(isHidden);
        setWrapped(file);
        getMediaStoreFileData(loadMediaStoreData);
    }

    public StorageDocument(DocumentFile file, Boolean isHidden, MediaStoreFileData mediaStoreFileData)
    {
        super(isHidden);
        setWrapped(file);
        setMediaStoreFileData(mediaStoreFileData);
    }

    public void initLazyly(String path, MediaStoreFileData mediaStoreFileData)
    {
        this.path = path;
        setMediaStoreFileData(mediaStoreFileData);
    }

    private void initFile()
    {
        if (file == null && path != null)
        {
            Log.d(StorageDocument.class.getSimpleName(), "initFile: " + path);
            if (!path.contains("."))
                file = (DocumentFile) StorageUtil.getFileByPath(path, null).getWrapped();
            else
                file = DocumentUtil.createDocumentFile(path, null, isHidden(false), false);
        }
    }

    @Override
    public DocumentFile getWrapped()
    {
        initFile();
        return file;
    }

    @Override
    public void setWrapped(DocumentFile file)
    {
        this.file = file;
        this.path = DocumentUtil.getPath(file);
    }

    @Override
    public IFile<DocumentFile> getParent()
    {
        DocumentFile parent = getWrapped().getParentFile();
        if (parent == null)
            return null;
        return new StorageDocument(parent, null, false);
    }

    // --------------------------------
    // Media Store data
    // --------------------------------

    @Override
    protected void internalLoadMediaStoreDataForFile()
    {
        if (getWrapped().isFile())
            setMediaStoreFileData(MediaStoreUtil.loadMediaStoreData(getMediaType(), path));
    }

    // --------------------------------
    // Properties
    // --------------------------------

    @Override
    public final boolean isFile()
    {
        return getWrapped().isFile();
    }

    @Override
    public final StorageDefinitions.FileType getType()
    {
        return StorageDefinitions.FileType.Document;
    }

    @Override
    public final StorageDefinitions.MediaType getMediaType()
    {
        return ExtensionUtil.getMediaType(path);
    }

    @Override
    public final String getMimeType()
    {
        return ExtensionUtil.getMimeType(path);
    }

    @Override
    public final String getExtension()
    {
        return ExtensionUtil.getExtension(path);
    }

    @Override
    public String getName()
    {
        if (mediaStoreFileData != null)
            return mediaStoreFileData.getName();
        if (file == null && path != null)
            return new File(path).getName();
        return getWrapped().getName();
    }

    @Override
    public String getPath()
    {
        return path;
    }

    @Override
    public Uri getUri()
    {
        if (mediaStoreFileData != null)
            return mediaStoreFileData.getUri();
        return getWrapped().getUri();
    }

    @Override
    public long created()
    {
        MediaStoreFileData mediaStoreFileData = getMediaStoreFileData(true);
        if (mediaStoreFileData != null)
            return mediaStoreFileData.getDateTaken();

        return ExifFileUtil.getDate(lastModified(), getExifData(true));
    }

    @Override
    public long lastModified()
    {
        if (mediaStoreFileData != null)
            return mediaStoreFileData.getDateModified();
        return DocumentUtil.getLastModified(getWrapped());
    }

    @Override
    public boolean setLastModified(long time)
    {
        // TODO: find workaround....
        return false;
//        ContentValues updateValues = new ContentValues();
//        updateValues.put(DocumentsContract.Document.COLUMN_LAST_MODIFIED, time);
//        int updated = context.getContentResolver().update(file.getUri(), updateValues, null, null);
//        return updated == 1;
    }

    @Override
    public long size()
    {
        return DocumentUtil.getSize(getWrapped());
    }

    @Override
    public Location location()
    {
        MediaStoreFileData mediaStoreFileData = getMediaStoreFileData(true);
        if (mediaStoreFileData != null)
            return mediaStoreFileData.getLocation();

        return ExifFileUtil.getLocation(getExifData(true));
    }

    @Override
    public Integer rotation()
    {
        MediaStoreFileData mediaStoreFileData = getMediaStoreFileData(true);
        if (mediaStoreFileData != null)
            return mediaStoreFileData.getRotation();

        return ExifFileUtil.getOrientation(getExifData(true), true);
    }

    // --------------------------------
    // File operations
    // --------------------------------

    @Override
    public boolean exists()
    {
        return getWrapped().exists();
    }

    @Override
    public boolean delete(StorageDefinitions.MediaStoreUpdateType mediaStoreUpdateType)
    {
        // 1) prepare
        // get hidden state (if necessary)
        Boolean hiddenState = getHiddenState(mediaStoreUpdateType);

        String path = getPath();
        boolean success = getWrapped().delete();

        if (success && hiddenState != null && !hiddenState)
        {
            switch (mediaStoreUpdateType)
            {
                case Schedule:
                    MediaStoreUpdateManager.get().addDeletion(path);
                    break;
                case Immediately:
                    if (!hiddenState)
                        MediaStoreUtil.delete(path, true);
                    break;
            }
        }

        return success;
    }

    @Override
    public boolean renameName(String newName, StorageDefinitions.MediaStoreUpdateType mediaStoreUpdateType)
    {
        // 1) prepare
        // get hidden state (if necessary) and remember old file and old media store data
        String pathOld = getPath();
        Boolean hiddenState = getHiddenState(mediaStoreUpdateType);

        // 2) rename file
        boolean success = getWrapped().renameTo(newName);

        // 3) update dependencies (currently this is the path only)
        if (success)
            setWrapped(getWrapped());

        // 4) update media store
        if (success && hiddenState != null && !hiddenState)
        {
            switch (mediaStoreUpdateType)
            {
                case None:
                    break;
                case Immediately:
                    if (getMediaStoreFileData(false) != null)
                        MediaStoreUtil.renameMedia(getMediaType(), getMediaStoreFileData(false).getId(), getPath(), getName());
                    else
                        MediaStoreUtil.renameMedia(getMediaType(), pathOld, getPath(), getName());
                    if (getMediaStoreFileData(false) != null)
                        getMediaStoreFileData(false).updateName(getPath(), getName());
                    break;
                case Schedule:
                    MediaStoreUpdateManager.get().addRename(pathOld, getPath());
                    if (getMediaStoreFileData(false) != null)
                        getMediaStoreFileData(false).updateName(getPath(), getName());
                    break;
            }
        }
        return success;
    }

    @Override
    public boolean move(IFile target, StorageDefinitions.MediaStoreUpdateType mediaStoreUpdateType, Boolean isTargetHidden)
    {
        // 1) remember old file and old media store data
        String oldPath = getPath();
        MediaStoreFileData mediaStoreFileData = getCopyOrCreateBestPossibleMediaStoreFileData(mediaStoreUpdateType);
        Boolean hiddenState = getHiddenState(null);

        // 2) move file
        boolean moved = StorageManager.get().getMoveHandler().move(this, target);

        // 3) get hidden state if necessary
        if (moved && isTargetHidden == null && mediaStoreUpdateType != StorageDefinitions.MediaStoreUpdateType.None)
            isTargetHidden = StorageUtil.hasNoMediaFile(target.getParent(), true);

        // 4) update media store
        if (moved)
        {
            switch (mediaStoreUpdateType)
            {
                case None:
                    break;
                case Immediately:
                    if (!hiddenState && isTargetHidden)
                        MediaStoreUtil.delete(oldPath, true);
                    else if (hiddenState && !isTargetHidden)
                        MediaStoreUtil.updateAfterCreation(target, mediaStoreFileData);
                    else if (!hiddenState && !isTargetHidden)
                        MediaStoreUtil.renameMedia(getMediaType(), oldPath, target.getPath(), target.getName());
                    // no need to update this IFile media store data as the moved file is stored in the target and this file becomes invalid
                    break;
                case Schedule:
                    if (!hiddenState && isTargetHidden)
                        MediaStoreUpdateManager.get().addDeletion(oldPath);
                    else if (hiddenState && !isTargetHidden)
                        MediaStoreUpdateManager.get().addCreation(target.getPath(), mediaStoreFileData);
                    else if (!hiddenState && !isTargetHidden)
                        MediaStoreUpdateManager.get().addRename(oldPath, target.getPath());
                    // no need to update this IFile media store data as the moved file is stored in the target and this file becomes invalid
                    break;
            }
        }
        return moved;
    }

    @Override
    public boolean copy(IFile target, StorageDefinitions.MediaStoreUpdateType mediaStoreUpdateType, Boolean isTargetHidden)
    {
        // 1) remember old media store data
        MediaStoreFileData mediaStoreFileData = getCopyOrCreateBestPossibleMediaStoreFileData(mediaStoreUpdateType);

        // 2) copy file
        boolean copied = StorageManager.get().getCopyHandler().copy(this, target);

        // 3) get hidden state if necessary
        if (copied && isTargetHidden == null && mediaStoreUpdateType != StorageDefinitions.MediaStoreUpdateType.None)
            isTargetHidden = StorageUtil.hasNoMediaFile(target.getParent(), true);

        // 4) update media store
        if (copied)
        {
            switch (mediaStoreUpdateType)
            {
                case None:
                    break;
                case Immediately:
                    if (!isTargetHidden)
                        MediaStoreUtil.updateAfterCreation(target, mediaStoreFileData);
                    break;
                case Schedule:
                    if (!isTargetHidden)
                        MediaStoreUpdateManager.get().addCreation(target, mediaStoreFileData);
                    break;
            }
        }
        return copied;
    }

    // --------------------------------
    // Streams
    // --------------------------------

    @Override
    public InputStream getInputStream() throws IOException
    {
        return StorageManager.get().getContext().getContentResolver().openInputStream(getUri());
    }

    @Override
    public OutputStream getOutputStream() throws IOException
    {
        return StorageManager.get().getContext().getContentResolver().openOutputStream(getUri());
    }

    // --------------------------------
    // Equals/Hash
    // --------------------------------

//    @Override
//    public int hashCode()
//    {
//        final int prime = 31;
//        int result = 1;
//        result = prime * result + path.hashCode();
//        return result;
//    }
//
//    @Override
//    public boolean equals(Object obj)
//    {
//        if (this == obj)
//            return true;
//        if (obj == null)
//            return false;
//        if (getClass() != obj.getClass())
//            return false;
//        StorageDocument other = (StorageDocument) obj;
//        if (!((Integer)hashCode()).equals(other.hashCode()))
//            return false;
//        return true;
//    }
}