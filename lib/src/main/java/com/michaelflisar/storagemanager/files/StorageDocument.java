package com.michaelflisar.storagemanager.files;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.provider.DocumentsContract;
import android.provider.DocumentsProvider;
import android.support.v4.provider.DocumentFile;

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
import com.michaelflisar.storagemanager.utils.InternalStorageUtil;
import com.michaelflisar.storagemanager.utils.MediaStoreUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Created by flisar on 03.02.2016.
 */
public class StorageDocument extends BaseMediaStoreFile<DocumentFile> implements IFile<DocumentFile>, IMediaStoreFile<DocumentFile>
{
    // File
    protected DocumentFile file;
    protected String path;

    public StorageDocument(DocumentFile file, boolean loadMediaStoreData)
    {
        setWrapped(file);
        getMediaStoreFileData(loadMediaStoreData);
    }

    public StorageDocument(DocumentFile file, MediaStoreFileData mediaStoreFileData)
    {
        setWrapped(file);
        setMediaStoreFileData(mediaStoreFileData);
    }

    @Override
    public DocumentFile getWrapped()
    {
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
        DocumentFile parent = file.getParentFile();
        if (parent == null)
            return null;
        return new StorageDocument(parent, false);
    }

    // --------------------------------
    // Media Store data
    // --------------------------------

    @Override
    protected void internalLoadMediaStoreDataForFile()
    {
        if (file.isFile())
            setMediaStoreFileData(MediaStoreUtil.loadMediaStoreData(getMediaType(), path));
    }

    // --------------------------------
    // Properties
    // --------------------------------

    @Override
    public final boolean isFile()
    {
        return file.isFile();
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
        return file.getName();
    }

    @Override
    public String getPath()
    {
        return path;
    }

    @Override
    public Uri getUri()
    {
        return file.getUri();
    }

    @Override
    public long created()
    {
        MediaStoreFileData mediaStoreFileData = getMediaStoreFileData(true);
        if (mediaStoreFileData != null)
            return mediaStoreFileData.getDateTaken();

        // TODO: cache this value and reset it when editing file via a clear function to clear internal values?
        HashMap<String, String> exifInformations = StorageManager.get().getMetaDataHandler().getExifInformations(this);
        return ExifFileUtil.getDate(lastModified(), exifInformations);
    }

    @Override
    public long lastModified()
    {
        return DocumentUtil.getLastModified(file);
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
        return DocumentUtil.getSize(file);
    }

    @Override
    public Location location()
    {
        MediaStoreFileData mediaStoreFileData = getMediaStoreFileData(true);
        if (mediaStoreFileData != null)
            return mediaStoreFileData.getLocation();

        // TODO: cache this value and reset it when editing file via a clear function to clear internal values?
        HashMap<String, String> exifInformations = StorageManager.get().getMetaDataHandler().getExifInformations(this);
        return ExifFileUtil.getLocation(exifInformations);
    }

    @Override
    public Integer rotation()
    {
        MediaStoreFileData mediaStoreFileData = getMediaStoreFileData(true);
        if (mediaStoreFileData != null)
            return mediaStoreFileData.getRotation();

        // TODO: cache this value and reset it when editing file via a clear function to clear internal values?
        HashMap<String, String> exifInformations = StorageManager.get().getMetaDataHandler().getExifInformations(this);
        return ExifFileUtil.getRotation(exifInformations);
    }

    // --------------------------------
    // File operations
    // --------------------------------

    @Override
    public boolean exists()
    {
        return file.exists();
    }

    @Override
    public boolean delete(StorageDefinitions.MediaStoreUpdateType mediaStoreUpdateType, Boolean isHidden)
    {
        boolean success = false;

        if (isHidden == null && mediaStoreUpdateType != StorageDefinitions.MediaStoreUpdateType.None)
            isHidden = StorageUtil.hasNoMediaFile(getParent(), true);

        String path = getPath();
        success = file.delete();

        if (success && isHidden != null && !isHidden)
        {
            switch (mediaStoreUpdateType)
            {
                case Schedule:
                    MediaStoreUpdateManager.get().addDeletion(path);
                    break;
                case Immediately:
                    if (!isHidden)
                    {
                        MediaStoreFileData mediaStoreFileData = getMediaStoreFileData(true);
                        if (mediaStoreFileData != null)
                            success = MediaStoreUtil.delete(mediaStoreFileData.getUri());
                    }
                    break;
            }
        }

        return success;
    }

    @Override
    public boolean renameName(String newName, StorageDefinitions.MediaStoreUpdateType mediaStoreUpdateType)
    {
        // 1) remember old file and old media store data
        String pathOld = getPath();
        MediaStoreFileData mediaStoreFileData = mediaStoreUpdateType != StorageDefinitions.MediaStoreUpdateType.None ? getCopyOrCreateBestPossibleMediaStoreFileData() : null;

        // 2) rename file
        boolean success = file.renameTo(newName);

        // 3) update media store
        if (success)
        {
            switch (mediaStoreUpdateType)
            {
                case None:
                    break;
                case Immediately:
                    // TODO: could be optimised both could be called in 1 transaction!
                    MediaStoreUtil.updateAfterDeletion(pathOld);
                    MediaStoreUtil.updateAfterCreation(this, mediaStoreFileData);
                    break;
                case Schedule:
                    MediaStoreUpdateManager.get().addDeletion(pathOld);
                    MediaStoreUpdateManager.get().addCreation(this, mediaStoreFileData);
                    break;
            }
        }
        return success;
    }

    @Override
    public boolean move(IFile target, StorageDefinitions.MediaStoreUpdateType mediaStoreUpdateType, Boolean isHidden, Boolean isTargetHidden)
    {
        // 1) remember old file and old media store data
        String oldPath = getPath();
        MediaStoreFileData mediaStoreFileData = mediaStoreUpdateType != StorageDefinitions.MediaStoreUpdateType.None ? getCopyOrCreateBestPossibleMediaStoreFileData() : null;
        if (isHidden == null && mediaStoreUpdateType != StorageDefinitions.MediaStoreUpdateType.None)
            isHidden = StorageUtil.hasNoMediaFile(getParent(), true);

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
                    // TODO: could be optimised both could be called in 1 transaction!
                    if (!isHidden)
                        MediaStoreUtil.updateAfterDeletion(oldPath);
                    if (!isTargetHidden)
                        MediaStoreUtil.updateAfterCreation(target, mediaStoreFileData);
                    break;
                case Schedule:
                    if (!isHidden)
                        MediaStoreUpdateManager.get().addDeletion(oldPath);
                    if (!isTargetHidden)
                        MediaStoreUpdateManager.get().addCreation(this, mediaStoreFileData);
                    break;
            }
        }
        return moved;
    }

    @Override
    public boolean copy(IFile target, StorageDefinitions.MediaStoreUpdateType mediaStoreUpdateType, Boolean isTargetHidden)
    {
        // 1) copy file
        boolean copied = StorageManager.get().getCopyHandler().copy(this, target);

        // 2) get hidden state if necessary
        if (copied && isTargetHidden == null && mediaStoreUpdateType != StorageDefinitions.MediaStoreUpdateType.None)
            isTargetHidden = StorageUtil.hasNoMediaFile(target.getParent(), true);

        // 3) update media store
        if (copied)
        {
            switch (mediaStoreUpdateType)
            {
                case None:
                    break;
                case Immediately:
                    if (!isTargetHidden)
                        MediaStoreUtil.updateAfterCreation(target, getCopyOrCreateBestPossibleMediaStoreFileData());
                    break;
                case Schedule:
                    if (!isTargetHidden)
                        MediaStoreUpdateManager.get().addCreation(target, getCopyOrCreateBestPossibleMediaStoreFileData());
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
        return StorageManager.get().getContext().getContentResolver().openInputStream(file.getUri());
    }

    @Override
    public OutputStream getOutputStream() throws IOException
    {
        return StorageManager.get().getContext().getContentResolver().openOutputStream(file.getUri());
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