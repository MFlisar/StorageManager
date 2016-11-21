package com.michaelflisar.storagemanager.interfaces;

import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.data.MediaStoreFileData;

import java.util.HashMap;

/**
 * Created by flisar on 20.06.2016.
 */
public interface IExifFile<T>
{
    HashMap<String, String> getExifData(boolean loadIfMissing);
    void setExifData(HashMap<String, String> data);
    void resetExifData();
}
