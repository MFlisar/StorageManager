package com.michaelflisar.storagemanager.data;

import com.michaelflisar.storagemanager.interfaces.IFolderData;

/**
 * Created by flisar on 03.02.2016.
 */
public class FileFolderData implements IFolderData
{
    private int mCount;

    public FileFolderData(int count)
    {
        mCount = count;
    }

    // --------------------------------
    // Getter
    // --------------------------------

    @Override
    public int getCount()
    {
        return mCount;
    }
}