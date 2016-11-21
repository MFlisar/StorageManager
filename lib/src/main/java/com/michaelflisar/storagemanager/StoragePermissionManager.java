package com.michaelflisar.storagemanager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.michaelflisar.storagemanager.files.StorageDocument;
import com.michaelflisar.storagemanager.folders.DocumentFolder;
import com.michaelflisar.storagemanager.interfaces.IFile;
import com.michaelflisar.storagemanager.interfaces.IFolder;

import java.util.List;

/**
 * Created by flisar on 01.03.2016.
 */
public class StoragePermissionManager
{
    private static final String TAG = StoragePermissionManager.class.getName();

    public static boolean hasSDCardPermissions()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return true;
        if (StorageManager.get().getSDCardRoot() == null)
            return false;
        return ((DocumentFolder)StorageManager.get().getSDCardRoot()).getFolder().getWrapped().canRead() &&  ((DocumentFolder)StorageManager.get().getSDCardRoot()).getFolder().getWrapped().canWrite();
    }

    public static boolean hasPermissionsToEdit(List<IFile> files)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return true;

        Boolean hasPermissions = hasSDCardPermissions();
        if (hasPermissions)
            return true;

        for (int i = 0; i < files.size(); i++)
        {
            if (files.get(i).getType() == StorageDefinitions.FileType.Document)
                return false;
        }
        return true;
    }

    public static boolean needsToGetPermissionsForSDCardIfNecessary(IFolder sdCardRoot)
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && sdCardRoot != null)
            return sdCardRoot.getFolder().getUri().equals(StorageManager.get().getSDCardUri()); // default is treeUri, afterwards it will be exchanged with the document uri based on the tree!
        return false;
    }

    public static void letUserSelectSDCard(Activity activity, int requestCode)
    {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        activity.startActivityForResult(intent, requestCode);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean handleOnActivityResult(Activity activity, int requestCode, int resultCode, Intent data, int resultCodeToCheck, IStorageSelectionManager manager)
    {
        if (requestCode == resultCodeToCheck)
        {
            if (resultCode == Activity.RESULT_OK)
            {
                // Get Uri from Storage Access Framework.
                Uri treeUri = data.getData();

                activity.grantUriPermission(activity.getPackageName(), treeUri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                DocumentFile doc = DocumentFile.fromTreeUri(activity, treeUri);
                Log.d(TAG, "SD Card Root AFTER asking user: " + treeUri.toString() + " | " + doc.canRead() + " | " + doc.canWrite());

                if (manager.isSelectionOk(doc))
                {
                    // Persist access permissions.
                    if (manager.takePersistableUri())
                        updatePersistUriPermissions(activity, treeUri);

                    if (manager.updateSDCardDocument())
                    {
                        StorageManager.get().updateSDCard(treeUri, doc);
//                        DocumentFolder sdCardRoot = (DocumentFolder) StorageManager.get().getSDCardRoot();
//                        if (sdCardRoot != null)
//                            sdCardRoot.updateDocument(doc);
                    }

                    manager.onAcceptableUriSelected(treeUri);
                }
            }
            return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean checkPersistUriPermissionsAndUpdate(Context context, DocumentFolder documentFolder)
    {
        Uri uri = documentFolder.getFolder().getUri();
        List<UriPermission> permissions = context.getContentResolver().getPersistedUriPermissions();
        for (UriPermission permission : permissions)
        {
            String permissionTreeId = DocumentsContract.getTreeDocumentId(permission.getUri());
            String uriTreeId = DocumentsContract.getTreeDocumentId(uri);
            if (uriTreeId.startsWith(permissionTreeId))
            {
                // update permissions - after a restart this is necessary...
                updatePersistUriPermissions(context, uri);
                documentFolder.updateDocument(DocumentFile.fromTreeUri(context, permission.getUri()));
                return true;
            }
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean checkPersistUriPermissionsAndUpdate(Context context, StorageDocument storageDocument)
    {
        Uri uri = storageDocument.getUri();
        List<UriPermission> permissions = context.getContentResolver().getPersistedUriPermissions();
        for (UriPermission permission : permissions)
        {
            String permissionTreeId = DocumentsContract.getTreeDocumentId(permission.getUri());
            String uriTreeId = DocumentsContract.getTreeDocumentId(uri);
            if (uriTreeId.startsWith(permissionTreeId))
            {
                // update permissions - after a restart this is necessary...
                updatePersistUriPermissions(context, uri);
                storageDocument.setWrapped(DocumentFile.fromTreeUri(context, permission.getUri()));
                return true;
            }
        }
        return false;
    }


    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean releasePersistUriPermissions(Activity activity, Uri uri)
    {
        try
        {
//        int takeFlags = flags;
//        takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            activity.getContentResolver().releasePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            return true;
        }
        catch (SecurityException e)
        {
            return false;
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void updatePersistUriPermissions(Context context, Uri uri)
    {
//        int takeFlags = flags;
//        takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        context.getContentResolver().takePersistableUriPermission(uri,  Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        context.grantUriPermission(context.getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    public interface IStorageSelectionManager
    {
        boolean takePersistableUri();
        boolean updateSDCardDocument();
        boolean isSelectionOk(DocumentFile selectedFile);
        void onAcceptableUriSelected(Uri uri);
    }
}
