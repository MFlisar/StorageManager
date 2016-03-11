package com.michaelflisar.storagemanager.files;

import android.content.Context;
import android.location.Location;
import android.net.Uri;
import android.provider.MediaStore;

import com.michaelflisar.storagemanager.MediaStoreUpdateManager;
import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.StorageManager;
import com.michaelflisar.storagemanager.StorageUtil;
import com.michaelflisar.storagemanager.data.MediaStoreFileData;
import com.michaelflisar.storagemanager.interfaces.IMediaStoreFile;
import com.michaelflisar.storagemanager.utils.ExifFileUtil;
import com.michaelflisar.storagemanager.utils.ExtensionUtil;
import com.michaelflisar.storagemanager.utils.FileUtil;
import com.michaelflisar.storagemanager.utils.InternalStorageUtil;
import com.michaelflisar.storagemanager.utils.MediaStoreUtil;
import com.michaelflisar.storagemanager.interfaces.IFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * Created by flisar on 03.02.2016.
 */
public class StorageFile extends BaseMediaStoreFile<File> implements IFile<File>, IMediaStoreFile<File>
{
    // File
    protected File file;

    public StorageFile(String path, boolean loadMediaStoreData)
    {
       this(new File(path), loadMediaStoreData);
    }

    public StorageFile(File file, boolean loadMediaStoreData)
    {
        this.file = file;
        getMediaStoreFileData(loadMediaStoreData);
    }

    public StorageFile(String path, MediaStoreFileData mediaStoreFileData)
    {
        this(new File(path), mediaStoreFileData);
    }

    public StorageFile(File file, MediaStoreFileData mediaStoreFileData)
    {
        this.file = file;
        setMediaStoreFileData(mediaStoreFileData);
    }

    @Override
    public File getWrapped()
    {
        return file;
    }

    @Override
    public void setWrapped(File file)
    {
        this.file = file;
    }

    @Override
    public IFile<File> getParent()
    {
        File parent = file.getParentFile();
        if (parent == null)
            return null;
        return new StorageFile(parent, null);
    }

    // --------------------------------
    // Media Store data
    // --------------------------------

    @Override
    protected void internalLoadMediaStoreDataForFile()
    {
        if (file.isFile())
            setMediaStoreFileData(MediaStoreUtil.loadMediaStoreData(getMediaType(), file));
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
        return StorageDefinitions.FileType.File;
    }

    @Override
    public final StorageDefinitions.MediaType getMediaType()
    {
        return ExtensionUtil.getMediaType(file);
    }

    @Override
    public final String getMimeType()
    {
        return ExtensionUtil.getMimeType(file);
    }

    @Override
    public final String getExtension()
    {
        return ExtensionUtil.getExtension(getPath());
    }

    @Override
    public String getName()
    {
        return file.getName();
    }

    @Override
    public String getPath()
    {
        return file.getAbsolutePath();
    }

    @Override
    public Uri getUri()
    {
        return Uri.fromFile(file);
    }

    @Override
    public long created()
    {
        MediaStoreFileData mediaStoreFileData = getMediaStoreFileData(false);
        if (mediaStoreFileData != null)
            return mediaStoreFileData.getDateTaken();

        // TODO: cache this value and reset it when editing file via a clear function to clear internal values?
        HashMap<String, String> exifInformations = StorageManager.get().getMetaDataHandler().getExifInformations(this);
        return ExifFileUtil.getDate(lastModified(), exifInformations);
    }

    @Override
    public long lastModified()
    {
        return file.lastModified();
    }

    @Override
    public boolean setLastModified(long time)
    {
        return file.setLastModified(time);
    }

    @Override
    public long size()
    {
        if (file == null || !file.exists() || file.isDirectory())
            return 0;
        return file.length();
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

        switch (mediaStoreUpdateType)
        {
            case None:
            case Schedule:
                success = file.delete();
                if (mediaStoreUpdateType == StorageDefinitions.MediaStoreUpdateType.Schedule && !isHidden)
                    MediaStoreUpdateManager.get().addDeletion(file.getAbsolutePath());
                break;
            case Immediately:
                // 1) try to delete via media store if file is not hidden => this will delete the file from the storage as well
                if (!isHidden)
                {
                    MediaStoreFileData mediaStoreFileData = getMediaStoreFileData(false);
                    if (mediaStoreFileData != null)
                        success = MediaStoreUtil.delete(mediaStoreFileData.getUri());
                    else
                        success = MediaStoreUtil.delete(file, true);
                }
                // 2) Fallback if deletion via media store did not work (as it may not be indexed (yet)... or may not exist anymore => then this function will fail as well)
                if (!success)
                    success = file.delete();
                break;
        }

        return success;
    }

    @Override
    public boolean renameName(String newName, StorageDefinitions.MediaStoreUpdateType mediaStoreUpdateType)
    {
        // 1) remember old file and old media store data
        File oldFile = file;
        MediaStoreFileData mediaStoreFileData = mediaStoreUpdateType != StorageDefinitions.MediaStoreUpdateType.None ? getCopyOrCreateBestPossibleMediaStoreFileData() : null;

        // 2) rename file
        File newFile = new File(file.getParentFile().getAbsolutePath(), newName);
        boolean renamed = file.renameTo(newFile);

        // 3) update local file
        if (renamed)
            file = newFile;

        // 4) update media store
        if (renamed)
        {
            file = newFile;
            switch (mediaStoreUpdateType)
            {
                case None:
                    break;
                case Immediately:
                    // TODO: could be optimised both could be called in 1 transaction!
                    if (mediaStoreFileData != null)
                        MediaStoreUtil.delete(mediaStoreFileData.getUri());
                    else
                        MediaStoreUtil.delete(file, true);
                    MediaStoreUtil.updateAfterCreation(this, mediaStoreFileData);
                    break;
                case Schedule:
                    MediaStoreUpdateManager.get().addDeletion(oldFile.getAbsolutePath());
                    MediaStoreUpdateManager.get().addCreation(this, mediaStoreFileData);
                    break;
            }
        }
        return renamed;
    }

    @Override
    public boolean move(IFile target, StorageDefinitions.MediaStoreUpdateType mediaStoreUpdateType, Boolean isHidden, Boolean isTargetHidden)
    {
        // 1) remember old file and old media store data
        File oldFile = file;
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
                        MediaStoreUtil.updateAfterDeletion(oldFile);
                    if (!isTargetHidden)
                        MediaStoreUtil.updateAfterCreation(target, mediaStoreFileData);
                    break;
                case Schedule:
                    if (!isHidden)
                        MediaStoreUpdateManager.get().addDeletion(oldFile.getAbsolutePath());
                    if (!isTargetHidden)
                        MediaStoreUpdateManager.get().addCreation(target, mediaStoreFileData);
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
        return new FileInputStream(file);
    }

    @Override
    public OutputStream getOutputStream() throws IOException
    {
        return new FileOutputStream(file);
    }
}