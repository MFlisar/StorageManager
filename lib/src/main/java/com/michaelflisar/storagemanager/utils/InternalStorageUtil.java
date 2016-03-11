package com.michaelflisar.storagemanager.utils;

import android.content.Context;
import android.support.v4.provider.DocumentFile;

import com.michaelflisar.storagemanager.MediaStoreUpdateManager;
import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.StorageManager;
import com.michaelflisar.storagemanager.StorageUtil;
import com.michaelflisar.storagemanager.files.StorageDocument;
import com.michaelflisar.storagemanager.files.StorageFile;
import com.michaelflisar.storagemanager.interfaces.IFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by flisar on 04.03.2016.
 */
public class InternalStorageUtil
{
    public static boolean copyStream(InputStream in, OutputStream out)
    {
        boolean success = false;
        try
        {
            // Transfer bytes from in to out
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            success = true;
        }
        catch (IOException e)
        {
        }
        finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                }
                catch (IOException e)
                {

                }
            }
            if (out != null)
            {
                try
                {
                    out.close();
                }
                catch (IOException e)
                {

                }
            }
        }
        return success;
    }

    public static boolean move(IFile src, IFile tgt) throws IOException
    {
        if (src.getPath().equals(tgt.getPath()))
            return false;

        boolean success;
        // if possible, we rename the file
        if (src.getType() == StorageDefinitions.FileType.File && tgt.getType() == StorageDefinitions.FileType.File)
        {
            File f1 = ((StorageFile)src).getWrapped();
            File f2 = ((StorageFile)tgt).getWrapped();
            success = f1.renameTo(f2);
        }
        // else we copy the source and delete it afterwards
        else
        {
            success = StorageManager.get().getCopyHandler().copy(src, tgt);
            success &= src.delete(StorageDefinitions.MediaStoreUpdateType.None, null);
        }
        return success;
    }
}
