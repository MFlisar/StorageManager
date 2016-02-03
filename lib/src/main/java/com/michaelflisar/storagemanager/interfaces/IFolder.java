package com.michaelflisar.storagemanager.interfaces;

import com.michaelflisar.storagemanager.StorageDefinitions;

import java.util.List;

/**
 * Created by flisar on 03.02.2016.
 */
public interface IFolder
{
    String getName();
    Integer getCount();

    StorageDefinitions.FolderStatus getStatus();
    void initFiles(List<IFile> files);
    List<IFile> getFiles();
}
