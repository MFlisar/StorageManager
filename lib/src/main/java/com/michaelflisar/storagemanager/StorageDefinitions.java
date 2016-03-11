package com.michaelflisar.storagemanager;

import android.provider.MediaStore;

import java.util.ArrayList;

/**
 * Created by flisar on 03.02.2016.
 */
public class StorageDefinitions
{
    public static final String AUTHORITY = "content://com.android.externalstorage.documents/tree";
    public static final String AUTHORITY_COLON = "%3A";

    public enum MediaType
    {
        Image,
        Video
    }

    public enum FolderStatus
    {
        NotLoaded,
        Loaded
    }

    public enum FileType
    {
        File,
        Document
    }

    public enum FolderType
    {
        FileFolder,
        DocumentFolder
    }

    public enum MediaStoreUpdateType
    {
        None,
        Immediately,
        Schedule
    }

    public enum FileSortingType
    {
        Name,
        CreationDate,
        ModificationDate
    }

    public enum FileSortingOrder
    {
        Asc,
        Desc
    }

    public static final ArrayList<String> IMG_FORMATS = new ArrayList<String>()
    {
        {
            add("jpg");
            add("jpeg");
            add("gif");
            add("png");
            add("bmp");
        }
    };

    public static final ArrayList<String> VID_FORMATS = new ArrayList<String>()
    {
        {
            add("3gp");
            add("mp4");
            add("ts");
            add("mkv");
        }
    };
}