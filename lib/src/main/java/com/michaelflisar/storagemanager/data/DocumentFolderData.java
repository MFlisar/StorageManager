package com.michaelflisar.storagemanager.data;

import com.michaelflisar.storagemanager.interfaces.IFolderData;

/**
 * Created by flisar on 03.02.2016.
 */
public class DocumentFolderData implements IFolderData
{
    private int mCount;

    public DocumentFolderData(int count)
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