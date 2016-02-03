package com.michaelflisar.storagemanager;

import android.provider.MediaStore;

import java.util.ArrayList;

/**
 * Created by flisar on 03.02.2016.
 */
public class StorageDefinitions
{
    public enum MediaType
    {
        Image,
        Video
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