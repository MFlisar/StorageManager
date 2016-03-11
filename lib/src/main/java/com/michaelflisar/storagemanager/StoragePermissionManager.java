package com.michaelflisar.storagemanager;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.provider.DocumentsContract;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

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

    public static boolean hasPermissions()
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;
        return ((DocumentFolder)StorageManager.get().getSDCardRoot()).getFolder().getWrapped().canRead() &&  ((DocumentFolder)StorageManager.get().getSDCardRoot()).getFolder().getWrapped().canWrite();
    }

    public static boolean hasPermissionsToEdit(List<IFile> files)
    {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;

        boolean hasPermissions = hasPermissions();
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && sdCardRoot != null)
            return true;
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

                // Persist access permissions.
                if (manager.takePersistableUri())
                    updatePersistUriPermissions(activity, treeUri);

                DocumentFile doc = DocumentFile.fromTreeUri(activity, treeUri);
                Log.d(TAG, "SD Card Root AFTER asking user: " + treeUri.toString() + " | " + doc.canRead() + " | " + doc.canWrite());

                if (manager.updateSDCardDocument())
                {
                    DocumentFolder sdCardRoot = (DocumentFolder)StorageManager.get().getSDCardRoot();
                    sdCardRoot.updateDocument(doc);
                }

                manager.onUriSelected(treeUri);
            }
            return true;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static boolean checkPersistUriPermissionsAndUpdate(Activity activity, DocumentFolder documentFolder)
    {
        Uri uri = documentFolder.getFolder().getUri();
        List<UriPermission> permissions = activity.getContentResolver().getPersistedUriPermissions();
        for (UriPermission permission : permissions)
        {
            String permissionTreeId = DocumentsContract.getTreeDocumentId(permission.getUri());
            String uriTreeId = DocumentsContract.getTreeDocumentId(uri);
            if (uriTreeId.startsWith(permissionTreeId))
            {
                // update permissions - after a restart this is necessary...
                updatePersistUriPermissions(activity, uri);
                documentFolder.updateDocument(DocumentFile.fromTreeUri(activity, permission.getUri()));
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
    private static void updatePersistUriPermissions(Activity activity, Uri uri)
    {
//        int takeFlags = flags;
//        takeFlags &= (Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        activity.getContentResolver().takePersistableUriPermission(uri,  Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        activity.grantUriPermission(activity.getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    public interface IStorageSelectionManager
    {
        boolean takePersistableUri();
        boolean updateSDCardDocument();
        void onUriSelected(Uri uri);
    }
}
