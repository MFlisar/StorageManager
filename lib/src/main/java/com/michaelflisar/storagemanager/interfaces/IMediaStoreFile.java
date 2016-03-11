package com.michaelflisar.storagemanager.interfaces;

import android.location.Location;
import android.net.Uri;

import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.data.MediaStoreFileData;
import com.michaelflisar.storagemanager.utils.MediaStoreUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by flisar on 03.02.2016.
 */
public interface IMediaStoreFile<T>
{
    MediaStoreFileData getMediaStoreFileData(boolean loadIfMissing);
    MediaStoreFileData getCopyOrCreateBestPossibleMediaStoreFileData();
    void setMediaStoreFileData(MediaStoreFileData data);
    void resetMediaStoreData();
}
