package com.michaelflisar.storagemanager.interfaces;

import com.michaelflisar.storagemanager.data.MediaStoreFileData;
import com.michaelflisar.storagemanager.data.MediaStoreFolderData;

/**
 * Created by flisar on 03.02.2016.
 */
public interface IMediaStoreFolder<T>
{
    void setMediaStoreFolderData(MediaStoreFolderData data);
    MediaStoreFolderData getMediaStoreFolderData();
}
