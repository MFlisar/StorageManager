package com.michaelflisar.storagemanager;

import android.content.Context;
import android.location.Location;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.michaelflisar.storagemanager.files.StorageDocument;
import com.michaelflisar.storagemanager.folders.DocumentFolder;
import com.michaelflisar.storagemanager.folders.FileFolder;
import com.michaelflisar.storagemanager.interfaces.IFile;
import com.michaelflisar.storagemanager.interfaces.IFolder;
import com.michaelflisar.storagemanager.utils.ExifFileUtil;
import com.michaelflisar.storagemanager.utils.ExtensionUtil;
import com.michaelflisar.storagemanager.utils.InternalStorageUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by flisar on 01.03.2016.
 */
public class StorageManager
{
    private static final String TAG = StorageManager.class.getName();

    private static StorageManager INSTANCE = null;

    public static StorageManager get()
    {
        if (INSTANCE == null)
            INSTANCE = new StorageManager();
        return INSTANCE;
    }

    private StorageManager()
    {
    }

    // -------------------------
    // main functions
    // -------------------------

    private ICopyHandler mDefaultCopyHandler = null;
    private IMoveHandler mDefaultMoveHandler = null;
    private IMetaDataHandler mDefaultMetaDataHandler = null;
    private ICopyHandler mCopyHandler = null;
    private IMoveHandler mMoveHandler = null;
    private IMetaDataHandler mMetaDataHandler = null;
    private Context mContext;
    private FileFolder mRoot = null;
    private IFolder mSDCardRoot = null;
    private String mSDCardID = null;
    private int mTimeToWaitToRecheckMediaScanner;

    public void init(Context appContext, int timeToWaitToRecheckMediaScanner)
    {
        initDefaultHandlers();
        mContext = appContext;
        mTimeToWaitToRecheckMediaScanner = timeToWaitToRecheckMediaScanner;
        mRoot = StorageFolderUtil.getMainRootFolderWithoutFolderData();
        Log.d(TAG, "Main root: " + mRoot.getFolder().getPath());

        mSDCardRoot = StorageFolderUtil.getSDCardRootFolder(mRoot);
        if (mSDCardRoot == null)
            Log.d(TAG, "SD Card root: " + "NULL");
        else
        {
            if (mSDCardRoot instanceof FileFolder)
                Log.d(TAG, "SD Card root: " + ((FileFolder) mSDCardRoot).getFolder().getPath());
            else if (mSDCardRoot instanceof DocumentFolder)
            {
                Log.d(TAG, "SD Card root: " + ((DocumentFolder) mSDCardRoot).getFolder().getUri().toString() + " | " + ((DocumentFolder) mSDCardRoot).getFolder().getWrapped().canRead() + " | " + ((DocumentFolder) mSDCardRoot).getFolder().getWrapped().canWrite());

                // Example String: content://com.android.externalstorage.documents/tree/0FEC-3001%3A
                String[] parts = ((DocumentFolder) mSDCardRoot).getFolder().getUri().toString().split("/");
                if (parts.length >= 5)
                    mSDCardID = parts[4].replace(StorageDefinitions.AUTHORITY_COLON, "");
            }
        }
    }

    private void initDefaultHandlers()
    {
        mDefaultCopyHandler = new ICopyHandler() {
            @Override
            public boolean copy(IFile source, IFile target) {
                try
                {
                    if (source == null || target == null || !source.exists())
                        return false;

                    if (target instanceof StorageDocument && !target.exists())
                    {
                        String mimeType = ExtensionUtil.getExtension(target.getName());
                        DocumentFile df = ((StorageDocument)target).getWrapped().getParentFile().createFile(mimeType, target.getName());
                        if (df == null)
                            return false;
                        target.setWrapped(df);
                    }
                    return InternalStorageUtil.copyStream(source.getInputStream(), target.getOutputStream());
                }
                catch (IOException e)
                {
                    return false;
                }
            }
        };
        mDefaultMoveHandler = new IMoveHandler() {
            @Override
            public boolean move(IFile source, IFile target) {
                try
                {
                    return InternalStorageUtil.move(source, target);
                }
                catch (IOException e)
                {
                    return false;
                }
            }
        };
        mDefaultMetaDataHandler = new IMetaDataHandler() {
            @Override
            public HashMap<String, String> getExifInformations(IFile file) {
                if (file.getType() == StorageDefinitions.FileType.File)
                    return ExifFileUtil.getExifInformations(file.getPath());
                return null;
            }
        };
    }

    public void setCopyHandler(ICopyHandler copyHandler)
    {
        mCopyHandler = copyHandler;
    }

    public void setMoveHandler(IMoveHandler moveHandler)
    {
        mMoveHandler = moveHandler;
    }

    public void setMetaDataHandler(IMetaDataHandler metaDataHandler)
    {
        mMetaDataHandler = metaDataHandler;
    }

    private void checkInitialised()
    {
        if (mRoot == null)
            throw new RuntimeException(getClass().getSimpleName() +  " has not been initialised!");
    }

    public FileFolder getRoot()
    {
        checkInitialised();
        return mRoot;
    }

    public IFolder getSDCardRoot()
    {
        checkInitialised();
        return mSDCardRoot;
    }

    public String getSDCardID()
    {
        checkInitialised();
        return mSDCardID;
    }
    public Context getContext()
    {
        checkInitialised();
        return mContext;
    }


    public ICopyHandler getDefaultCopyHandler()
    {
        checkInitialised();
        return mDefaultCopyHandler;
    }

    public IMoveHandler getDefaultMoveHandler()
    {
        checkInitialised();
        return mDefaultMoveHandler;
    }

    public IMetaDataHandler getDefaultMetaDataHandler()
    {
        checkInitialised();
        return mDefaultMetaDataHandler;
    }

    public ICopyHandler getCopyHandler()
    {
        checkInitialised();
        if (mCopyHandler != null)
            return mCopyHandler;
        return mDefaultCopyHandler;
    }

    public IMoveHandler getMoveHandler()
    {
        checkInitialised();
        if (mMoveHandler != null)
            return mMoveHandler;
        return mDefaultMoveHandler;
    }

    public IMetaDataHandler getMetaDataHandler()
    {
        checkInitialised();
        if (mMetaDataHandler != null)
            return mMetaDataHandler;
        return mDefaultMetaDataHandler;
    }

    public int getTimeToWaitToRecheckMediaScanner()
    {
        checkInitialised();
        return mTimeToWaitToRecheckMediaScanner;
    }

    public interface ICopyHandler
    {
        boolean copy(IFile source, IFile target);
    }

    public interface IMoveHandler
    {
        boolean move(IFile source, IFile target);
    }

    public interface IMetaDataHandler
    {
        HashMap<String, String> getExifInformations(IFile file);
    }
}
