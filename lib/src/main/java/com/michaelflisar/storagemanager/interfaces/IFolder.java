package com.michaelflisar.storagemanager.interfaces;

import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.utils.MediaStoreUtil;

import java.util.List;

/**
 * Created by flisar on 03.02.2016.
 */
public interface IFolder<T> extends IMediaStoreFolder<T>
{
    StorageDefinitions.FolderType getType();
    String getName();
    Integer getCount();
    IFolderData getFolderData();

    List<StorageDefinitions.MediaType> getFileTypes();

    StorageDefinitions.FolderStatus getStatus();
    void reset();
    IFile<T> getFolder();
    void loadFiles(List<StorageDefinitions.MediaType> typeToContain, boolean loadFromMediaStore, StorageDefinitions.FileSortingType limitSortingType, StorageDefinitions.FileSortingOrder limitSortingOrder, Integer limit,  Long minDate, Long maxDate, MediaStoreUtil.DateType dateType);
    List<IFile<T>> loadFilesInternally(boolean loadFromMediaStore, StorageDefinitions.FileSortingType limitSortingType, StorageDefinitions.FileSortingOrder limitSortingOrder, Integer limit,  Long minDate, Long maxDate, MediaStoreUtil.DateType dateType);
    void initFiles(List<StorageDefinitions.MediaType> typeToContain, List<IFile<T>> files);
    List<IFile<T>> getFiles();

    boolean addFile(IFile file);
    boolean removeFile(IFile file);
    boolean removeFile(String path);
}
