package com.michaelflisar.storagemanager.interfaces;

/**
 * Created by flisar on 09.03.2016.
 */
public interface IFolderData
{
    boolean knowsCount();
    int getCount();
    IFile getMainFile();
}
