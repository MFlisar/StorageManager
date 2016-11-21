package com.michaelflisar.storagemanager.interfaces;

import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.data.MediaStoreFileData;

/**
 * Created by flisar on 03.02.2016.
 */
public interface IHideableFile<T>
{
    void setHidden(boolean hidden);
    Boolean isHidden(boolean loadIfMissing);
}
