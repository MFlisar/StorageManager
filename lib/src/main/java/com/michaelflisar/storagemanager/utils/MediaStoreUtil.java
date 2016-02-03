package com.michaelflisar.storagemanager.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.data.MediaStoreFileData;
import com.michaelflisar.storagemanager.data.MediaStoreFolderData;
import com.michaelflisar.storagemanager.folders.FileFolder;
import com.michaelflisar.storagemanager.interfaces.IFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by flisar on 03.02.2016.
 */
public class MediaStoreUtil
{
    // --------------------------------
    // Definitions
    // --------------------------------

    public static final String[] MEDIA_STORE_COLUMNS_IMAGE = {
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DISPLAY_NAME,
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
            MediaStore.Images.ImageColumns.DATE_MODIFIED,
            MediaStore.Images.ImageColumns.MIME_TYPE,
            MediaStore.Images.ImageColumns.WIDTH,
            MediaStore.Images.ImageColumns.HEIGHT,
            MediaStore.Images.ImageColumns.LATITUDE,
            MediaStore.Images.ImageColumns.LONGITUDE,
            MediaStore.Images.ImageColumns.ORIENTATION
    };

    public static final String[] MEDIA_STORE_COLUMNS_VIDEO = {
            MediaStore.Video.VideoColumns._ID,
            MediaStore.Video.VideoColumns.DISPLAY_NAME,
            MediaStore.Video.VideoColumns.DATA,
            MediaStore.Video.VideoColumns.DATE_TAKEN,
            MediaStore.Video.VideoColumns.DATE_MODIFIED,
            MediaStore.Video.VideoColumns.MIME_TYPE,
            MediaStore.Video.VideoColumns.WIDTH,
            MediaStore.Video.VideoColumns.HEIGHT,
            MediaStore.Video.VideoColumns.LATITUDE,
            MediaStore.Video.VideoColumns.LONGITUDE,
            "0 AS " + MediaStore.Images.ImageColumns.ORIENTATION,
    };

    public static final String[] MEDIA_STORE_FOLDER_COLUMNS_IMAGE = new String[]{
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN,
            "COUNT(" + MediaStore.Images.Media.DATA + ")"
    };

    public static final String[] MEDIA_STORE_FOLDER_COLUMNS_VIDEO = new String[]{
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DATE_TAKEN,
            "COUNT(" + MediaStore.Video.Media.DATA + ")"
    };

    // --------------------------------
    // Listings
    // --------------------------------

    public static List<IFolder> getAllFoldersWithoutContent(Context context, StorageDefinitions.MediaType type, boolean external)
    {
        HashMap<Long, IFolder> foldersMap = new HashMap<>();

        String[] projection = null;
        Uri baseUri = null;
        switch (type)
        {
            case Image:
                projection = MEDIA_STORE_FOLDER_COLUMNS_IMAGE;
                if (external)
                    baseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                else
                    baseUri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
                break;
            case Video:
                projection = MEDIA_STORE_FOLDER_COLUMNS_VIDEO;
                if (external)
                    baseUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                else
                    baseUri = MediaStore.Video.Media.INTERNAL_CONTENT_URI;
                break;
            default:
                throw new RuntimeException("Type not handled");
        }

        String dateTakenColumn = projection[3];

        // only get folder with highest date
        String groupBy = "1) GROUP BY 1,(2";
        String orderBy = "MAX(" + dateTakenColumn + ") DESC";

        // query
        Cursor cursor = context.getContentResolver().query(baseUri,
                projection, // Which columns to return
                groupBy,    // Which rows to return
                null,       // Selection arguments (none)
                orderBy     // Ordering
        );

        if (cursor.moveToFirst())
        {
            int bucketColumn = cursor.getColumnIndex(projection[0]);
            int bucketIdColumn = cursor.getColumnIndex(projection[1]);
            int dataColumn = cursor.getColumnIndex(projection[2]);
            int countColumn = projection.length - 1;

            do
            {
                int count = cursor.getInt(countColumn);
                long bucketId = cursor.getLong(bucketIdColumn);
                String bucket = cursor.getString(bucketColumn);
                String data = cursor.getString(dataColumn);

                // following is necessary, because it's possible that multiple files with the highest date exist!

                // prüfen, weil es möglich ist, dass mehrere Bilder mit gleichem Datum existieren!
                boolean found = false;
                IFolder folder = foldersMap.get(bucketId);
                if (folder == null)
                {
                    folder = new FileFolder(new File(data).getParentFile().getAbsolutePath());
                    ((FileFolder)folder).setMediaStoreFolderData(new MediaStoreFolderData(bucketId, bucket, count));
                }
                else
                    found = true;

                if (!found)
                    foldersMap.put(bucketId, folder);

            }
            while (cursor.moveToNext());
        }

        cursor.close();

        return new ArrayList<>(foldersMap.values());
    }

    // --------------------------------
    // Functions
    // --------------------------------

    public static MediaStoreFileData calcMediaStoreDataFromCursor(Cursor cursor, boolean external, StorageDefinitions.MediaType type)
    {
        String[] projection = null;
        Uri baseUri = null;
        switch (type)
        {
            case Image:
                projection = MEDIA_STORE_COLUMNS_IMAGE;
                if (external)
                    baseUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                else
                    baseUri = MediaStore.Images.Media.INTERNAL_CONTENT_URI;
                break;
            case Video:
                projection = MEDIA_STORE_COLUMNS_VIDEO;
                if (external)
                    baseUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                else
                    baseUri = MediaStore.Video.Media.INTERNAL_CONTENT_URI;
                break;
            default:
                throw new RuntimeException("Type not handled");
        }

        // read values
        long id = cursor.getLong(cursor.getColumnIndex(projection[0]));
        String name = cursor.getString(cursor.getColumnIndex(projection[1]));
        String data = cursor.getString(cursor.getColumnIndex(projection[2]));
        String dateToken = cursor.getString(cursor.getColumnIndex(projection[3]));
        String dateModified = cursor.getString(cursor.getColumnIndex(projection[4]));
        String mimeType = cursor.getString(cursor.getColumnIndex(projection[5]));
        int width= cursor.getInt(cursor.getColumnIndex(projection[6]));
        int height = cursor.getInt(cursor.getColumnIndex(projection[7]));
        double latitude= cursor.getDouble(cursor.getColumnIndex(projection[8]));
        double longitude = cursor.getDouble(cursor.getColumnIndex(projection[9]));
        int orientation = cursor.getInt(cursor.getColumnIndex(projection[10]));

        // parse values if necessary
        long lDateToken = dateToken != null ? Long.parseLong(dateToken) : 0;
        long lDateModified = dateModified != null ? (Long.parseLong(dateModified) * 1000) : 0;

        Uri uri = Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));

        return new MediaStoreFileData(type, uri, id, name, data, lDateToken, lDateModified, mimeType, width, height, latitude, longitude, orientation);
    }

    public static boolean delete(Context context, MediaStoreFileData data)
    {
        return context.getContentResolver().delete(data.getUri(), null, null) == 1;
    }

    public static boolean delete(Context context, File file, Boolean external)
    {
        List<Uri> baseUris = new ArrayList<>();
        String dataColumn = null;

        boolean deleted = false;
        String mimeType = ExtensionUtil.getMimeType(file);
        if (mimeType != null)
        {
            if (mimeType.contains("image"))
            {
                if (external == null || external)
                    baseUris.add(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                if (external == null || !external)
                    baseUris.add(MediaStore.Images.Media.INTERNAL_CONTENT_URI);
                dataColumn = MediaStore.Images.Media.DATA;
            }
            else if (mimeType.contains("video"))
            {
                if (external == null || external)
                    baseUris.add(MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                if (external == null || !external)
                    baseUris.add(MediaStore.Video.Media.INTERNAL_CONTENT_URI);
                dataColumn = MediaStore.Video.Media.DATA;
            }
            else
            {
                // handle other data types as audio/folder
            }
        }

        for (int i = 0; i < baseUris.size(); i++)
            deleted |= context.getContentResolver().delete(baseUris.get(i), dataColumn + "=?", new String[]{file.getAbsolutePath()}) > 0;

        return deleted;
    }
}
