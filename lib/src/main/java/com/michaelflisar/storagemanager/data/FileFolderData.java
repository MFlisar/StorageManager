package com.michaelflisar.storagemanager.data;

import com.michaelflisar.storagemanager.interfaces.IFile;
import com.michaelflisar.storagemanager.interfaces.IFolderData;

/**
 * Created by flisar on 03.02.2016.
 */
public class FileFolderData implements IFolderData
{
    private Integer mCount;
    private IFile mMainFile;

    public FileFolderData(IFile file, Integer count)
    {
        mMainFile = file;
        mCount = count;
    }

    // --------------------------------
    // Getter
    // --------------------------------

    @Override
    public boolean knowsCount()
    {
        return mCount != null;
    }

    @Override
    public int getCount()
    {
        return mCount;
    }

    @Override
    public IFile getMainFile()
    {
        return mMainFile;
    }
}