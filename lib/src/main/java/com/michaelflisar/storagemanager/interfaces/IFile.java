package com.michaelflisar.storagemanager.interfaces;

import android.content.Context;

/**
 * Created by flisar on 03.02.2016.
 */
public interface IFile
{
    String getName();
    boolean delete(Context context);
}
