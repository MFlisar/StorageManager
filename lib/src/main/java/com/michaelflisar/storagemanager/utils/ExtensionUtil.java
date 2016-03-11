package com.michaelflisar.storagemanager.utils;

import android.webkit.MimeTypeMap;

import com.michaelflisar.storagemanager.StorageDefinitions;

import java.io.File;

/**
 * Created by flisar on 03.02.2016.
 */
public class ExtensionUtil
{
    public static String getMimeType(File file)
    {
        return getMimeType(file.getAbsolutePath());
    }

    public static String getMimeType(String path)
    {
        String type = null;
        String extension = getExtension(path).toLowerCase();
        if (extension != null)
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return type;
    }

    public static String getExtension(File file) {
        String name = file.getName();
        try {
            return name.substring(name.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }

    public static String getExtension(String path) {
        try {
            return path.substring(path.lastIndexOf(".") + 1);
        } catch (Exception e) {
            return "";
        }
    }

    public static StorageDefinitions.MediaType getMediaType(File file)
    {
        return getMediaType(file.getAbsolutePath());
    }

    public static StorageDefinitions.MediaType getMediaType(String path)
    {
        String mimeType = getMimeType(path);
        return getMediaTypeFromMimeType(mimeType);
    }

    private static StorageDefinitions.MediaType getMediaTypeFromMimeType(String mimeType)
    {
        if (mimeType != null)
        {
            if (mimeType.contains("image"))
                return StorageDefinitions.MediaType.Image;
            else if (mimeType.contains("video"))
                return StorageDefinitions.MediaType.Video;
        }
        return null;
    }
}
