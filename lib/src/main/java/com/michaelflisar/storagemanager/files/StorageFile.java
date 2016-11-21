package com.michaelflisar.storagemanager.files;

import android.content.ContentProviderResult;
import android.location.Location;
import android.net.Uri;

import com.michaelflisar.storagemanager.MediaStoreUpdateManager;
import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.StorageManager;
import com.michaelflisar.storagemanager.StorageUtil;
import com.michaelflisar.storagemanager.data.MediaStoreFileData;
import com.michaelflisar.storagemanager.interfaces.IMediaStoreFile;
import com.michaelflisar.storagemanager.utils.ExifFileUtil;
import com.michaelflisar.storagemanager.utils.ExtensionUtil;
import com.michaelflisar.storagemanager.utils.MediaStoreUtil;
import com.michaelflisar.storagemanager.interfaces.IFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by flisar on 03.02.2016.
 */
public class StorageFile extends BaseFile<File> implements Serializable, IFile<File>, IMediaStoreFile<File>
{
    // File
    protected File file;

    public StorageFile(String path, Boolean isHidden, boolean loadMediaStoreData)
    {
       this(new File(path), isHidden, loadMediaStoreData);
    }

    public StorageFile(File file, Boolean isHidden, boolean loadMediaStoreData)
    {
        super(isHidden);
        this.file = file;
        getMediaStoreFileData(loadMediaStoreData);
    }

    public StorageFile(String path, Boolean isHidden, MediaStoreFileData mediaStoreFileData)
    {
        this(new File(path), isHidden, mediaStoreFileData);
    }

    public StorageFile(File file, Boolean isHidden, MediaStoreFileData mediaStoreFileData)
    {
        super(isHidden);
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
        return new StorageFile(parent, null, null);
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
        MediaStoreFileData mediaStoreFileData = getMediaStoreFileData(false);
        if (mediaStoreFileData != null)
            return mediaStoreFileData.getUri();
        return Uri.fromFile(file);
    }

    @Override
    public long created()
    {
        MediaStoreFileData mediaStoreFileData = getMediaStoreFileData(false);
        if (mediaStoreFileData != null)
            return mediaStoreFileData.getDateTaken();

        return ExifFileUtil.getDate(lastModified(), getExifData(true));
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
        return file.exists();
    }

    @Override
    public boolean delete(StorageDefinitions.MediaStoreUpdateType mediaStoreUpdateType)
    {
        boolean deleted = false;

        // 1) prepare
        // get hidden state (if necessary)
        Boolean hiddenState = getHiddenState(mediaStoreUpdateType);

        // 2) delete file and update (or schedule) media store update
        switch (mediaStoreUpdateType)
        {
            case None:
            case Schedule:
                deleted = file.delete();
                if (mediaStoreUpdateType == StorageDefinitions.MediaStoreUpdateType.Schedule && !hiddenState)
                    MediaStoreUpdateManager.get().addDeletion(file.getAbsolutePath());
                break;
            case Immediately:
                // 1) try to delete via media store if file is not hidden => this will delete the file from the storage as well
                if (!hiddenState)
                {
                    MediaStoreFileData mediaStoreFileData = getMediaStoreFileData(false);
                    if (mediaStoreFileData != null)
                        deleted = MediaStoreUtil.delete(mediaStoreFileData.getUri());
                    else
                        deleted = MediaStoreUtil.delete(file, true);
                }
                // 2) Fallback if deletion via media store did not work (as it may not be indexed (yet)... or may not exist anymore => then this function will fail as well)
                if (!deleted)
                    deleted = file.delete();
                break;
        }

        return deleted;
    }

    @Override
    public boolean renameName(String newName, StorageDefinitions.MediaStoreUpdateType mediaStoreUpdateType)
    {
        // 1) prepare
        // get hidden state (if necessary) and remember old file and old media store data
        File oldFile = file;
        Boolean hiddenState = getHiddenState(mediaStoreUpdateType);

        // 2) rename file
        File newFile = new File(file.getParentFile().getAbsolutePath(), newName);
        boolean renamed = file.renameTo(newFile);

        // 3) update local file
        if (renamed)
            file = newFile;

        // 4) update media store
        if (renamed && hiddenState != null && !hiddenState)
        {
            switch (mediaStoreUpdateType)
            {
                case None:
                    break;
                case Immediately:
                    if (getMediaStoreFileData(false) != null)
                        MediaStoreUtil.renameMedia(getMediaType(), getMediaStoreFileData(false).getId(), newFile.getAbsolutePath(), newFile.getName());
                    else
                        MediaStoreUtil.renameMedia(getMediaType(), oldFile.getAbsolutePath(), newFile.getAbsolutePath(), newFile.getName());
                    if (getMediaStoreFileData(false) != null)
                        getMediaStoreFileData(false).updateName(newFile.getPath(), newFile.getName());
                    break;
                case Schedule:
                    MediaStoreUpdateManager.get().addRename(oldFile.getAbsolutePath(), newFile.getAbsolutePath());
                    if (getMediaStoreFileData(false) != null)
                        getMediaStoreFileData(false).updateName(newFile.getPath(), newFile.getName());
                    break;
            }
        }

        return renamed;
    }

    @Override
    public boolean move(IFile target, StorageDefinitions.MediaStoreUpdateType mediaStoreUpdateType, Boolean isTargetHidden)
    {
        // 1) remember old file and old media store data
        File oldFile = file;
        MediaStoreFileData mediaStoreFileData = getCopyOrCreateBestPossibleMediaStoreFileData(mediaStoreUpdateType);
        Boolean hiddenState = getHiddenState(null);

        // 2) move file
        boolean moved = StorageManager.get().getMoveHandler().move(this, target);

        // 3) get hidden state if necessary
        if (moved && isTargetHidden == null && mediaStoreUpdateType != StorageDefinitions.MediaStoreUpdateType.None)
            isTargetHidden = StorageUtil.hasNoMediaFile(target.getParent(), true);

        // 4) update media store of TARGET FIle
        if (moved)
        {
            switch (mediaStoreUpdateType)
            {
                case None:
                    break;
                case Immediately:
                    if (!hiddenState && isTargetHidden)
                        MediaStoreUtil.delete(oldFile, true);
                    else if (hiddenState && !isTargetHidden)
                        MediaStoreUtil.updateAfterCreation(target, mediaStoreFileData);
                    else if (!hiddenState && !isTargetHidden)
                        MediaStoreUtil.renameMedia(getMediaType(), oldFile.getAbsolutePath(), target.getPath(), target.getName());
                    // no need to update this IFile media store data as the moved file is stored in the target and this file becomes invalid
                    break;
                case Schedule:
                    if (!hiddenState && isTargetHidden)
                        MediaStoreUpdateManager.get().addDeletion(oldFile.getAbsolutePath());
                    else if (hiddenState && !isTargetHidden)
                        MediaStoreUpdateManager.get().addCreation(target.getPath(), mediaStoreFileData);
                    else if (!hiddenState && !isTargetHidden)
                        MediaStoreUpdateManager.get().addRename(oldFile.getAbsolutePath(), target.getPath());
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
        return new FileInputStream(file);
    }

    @Override
    public OutputStream getOutputStream() throws IOException
    {
        return new FileOutputStream(file);
    }
}