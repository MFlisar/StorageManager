package com.michaelflisar.storagemanager.interfaces;

import android.location.Location;
import android.net.Uri;

import com.michaelflisar.storagemanager.StorageDefinitions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by flisar on 03.02.2016.
 */
public interface IFile<T> extends IMediaStoreFile<T>
{
    boolean isFile();
    StorageDefinitions.FileType getType();
    StorageDefinitions.MediaType getMediaType();
    String getMimeType();
    String getExtension();

    T getWrapped();
    void setWrapped(T file);
    IFile<T> getParent();

    // Properties
    String getName();
    String getPath();
    Uri getUri();
    long created();
    long lastModified();
    boolean setLastModified(long time);
    long size();
    Location location();
    Integer rotation();

    // File operations
    boolean exists();
    boolean delete(StorageDefinitions.MediaStoreUpdateType mediaStoreUpdateType, Boolean isHidden);
    boolean renameName(String newName, StorageDefinitions.MediaStoreUpdateType mediaStoreUpdateType);
    boolean move(IFile target, StorageDefinitions.MediaStoreUpdateType mediaStoreUpdateType, Boolean isHidden, Boolean isTargetHidden);
    boolean copy(IFile target, StorageDefinitions.MediaStoreUpdateType mediaStoreUpdateType, Boolean isTargetHidden);

    // Streams
    InputStream getInputStream() throws IOException;
    OutputStream getOutputStream() throws IOException;

}
