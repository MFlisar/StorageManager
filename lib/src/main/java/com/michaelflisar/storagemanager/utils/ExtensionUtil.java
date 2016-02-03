package com.michaelflisar.storagemanager.utils;

import android.webkit.MimeTypeMap;

import java.io.File;

/**
 * Created by flisar on 03.02.2016.
 */
public class ExtensionUtil
{
    public static String getMimeType(File file)
    {
        String type = null;
        String extension = getExtension(file);
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
}
