package com.michaelflisar.storagemanager.utils;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.location.Location;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.util.Log;

import com.michaelflisar.storagemanager.StorageDefinitions;
import com.michaelflisar.storagemanager.StorageManager;
import com.michaelflisar.storagemanager.StoragePermissionManager;
import com.michaelflisar.storagemanager.StorageUtil;
import com.michaelflisar.storagemanager.data.DocumentFolderData;
import com.michaelflisar.storagemanager.data.FileFolderData;
import com.michaelflisar.storagemanager.data.MediaStoreFileData;
import com.michaelflisar.storagemanager.data.MediaStoreFolderData;
import com.michaelflisar.storagemanager.exceptions.StorageException;
import com.michaelflisar.storagemanager.files.StorageDocument;
import com.michaelflisar.storagemanager.files.StorageFile;
import com.michaelflisar.storagemanager.folders.BaseFolder;
import com.michaelflisar.storagemanager.folders.DocumentFolder;
import com.michaelflisar.storagemanager.folders.FileFolder;
import com.michaelflisar.storagemanager.interfaces.IFile;
import com.michaelflisar.storagemanager.interfaces.IFolder;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by flisar on 03.02.2016.
 */
public class MediaStoreUtil
{
    // --------------------------------
    // Definitions
    // --------------------------------

    public enum DateType
    {
        Created,
        Modified,
        Added
    }

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

    public static final String[] MEDIA_STORE_COLUMNS_VIDEO_QUERY = {
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

    public static final String[] MEDIA_STORE_COLUMNS_VIDEO_COLS = {
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
            MediaStore.Images.ImageColumns.ORIENTATION,
    };

    public static final String[] MEDIA_STORE_FOLDER_COLUMNS_IMAGE = new String[]{
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.DATA,
            "COUNT(" + MediaStore.Images.Media.DATA + ")",
            "MIN(" + MediaStore.Images.Media.DATE_TAKEN + ")",
            "MAX(" + MediaStore.Images.Media.DATE_TAKEN + ")",
            "MIN(" + MediaStore.Images.Media.DATE_MODIFIED + ")",
            "MAX(" + MediaStore.Images.Media.DATE_MODIFIED + ")"
    };

    public static final String[] MEDIA_STORE_FOLDER_COLUMNS_VIDEO = new String[]{
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.BUCKET_ID,
            MediaStore.Video.Media.DATA,
            "COUNT(" + MediaStore.Video.Media.DATA + ")",
            "MIN(" + MediaStore.Video.Media.DATE_TAKEN + ")",
            "MAX(" + MediaStore.Video.Media.DATE_TAKEN + ")",
            "MIN(" + MediaStore.Video.Media.DATE_MODIFIED + ")",
            "MAX(" + MediaStore.Video.Media.DATE_MODIFIED + ")"
    };

    public static final String[] MEDIA_STORE_FOLDER_COLUMNS_FILE = new String[]{
            MediaStore.Files.FileColumns.DATA,
            MediaStore.Files.FileColumns.PARENT,
    };


    private static final String FILE_TYPE_NO_MEDIA = ".nomedia";

    // --------------------------------
    // Functions
    // --------------------------------

    public static List<IFolder> getAllFoldersWithoutContent(StorageDefinitions.MediaType mediaType, boolean external)
    {
        HashSet<String> foldersSet = new HashSet<>();
        ArrayList<IFolder> folders = new ArrayList<>();

        String[] projection = null;
        Uri baseUri = null;
        switch (mediaType)
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

        // nur das Bild mit dem größtem Datum holen
        String groupBy = "1) GROUP BY 1,(2";

        Cursor cursor = StorageManager.get().getContext().getContentResolver().query(baseUri,
                projection, // Which columns to return
                groupBy,    // Which rows to return
                null,       // Selection arguments (none)
                null     // Ordering
        );

        if (cursor.moveToFirst())
        {
            int maxDateModifiedColumn = projection.length - 1;
            int minDateModifiedColumn = projection.length - 2;
            int maxDateTakenColumn = projection.length - 3;
            int minDateTakenColumn = projection.length - 4;
            int countColumn = projection.length - 5;
            int idColumn = cursor.getColumnIndex(MediaStore.Images.Media._ID);
            int bucketColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);
            int bucketIdColumn = cursor.getColumnIndex(MediaStore.Images.Media.BUCKET_ID);
            int dataColumn = cursor.getColumnIndex(MediaStore.Images.Media.DATA);

            do
            {
                long maxDateModified = cursor.getLong(maxDateModifiedColumn);
                long minDateModified = cursor.getLong(minDateModifiedColumn);
                long maxDateTaken = cursor.getLong(maxDateTakenColumn);
                long minDateTaken = cursor.getLong(minDateTakenColumn);
                int count = cursor.getInt(countColumn);
                long id = cursor.getLong(idColumn);
                long bucketId = cursor.getLong(bucketIdColumn);
                String bucket = cursor.getString(bucketColumn);
                String data = cursor.getString(dataColumn);

                // foldersSet check is necessary, because more images with the same date could exist!!! Only use 1 and don't add folders twice!!!
//                BaseFolder folder = null;
//                IFile file = StorageUtil.getFileByPath(data, false);

                // get folder path manually
                String pathFolder = null;

                if (data != null)
                {
                    try
                    {
                        pathFolder = new File(data).getParentFile().getAbsolutePath();
                    }
                    catch (Exception e) {}
                }
//                        (data.substring(0, data.lastIndexOf("/"))) : null;

                if (pathFolder != null) // ignore orphaned paths! + ignore if file may be not YET createable + ignore files we don't have permissions for!
                {
                    if (!foldersSet.contains(pathFolder))
                    {
                        BaseFolder folder = null;
                        boolean isFile = StorageUtil.isPathAFile(pathFolder);
                        if (isFile)//file.getType() == StorageDefinitions.FileType.File)
                        {
                            folder = new FileFolder(new File(pathFolder));
                            // TODO
//                            ((FileFolder)folder).setFileFolderData(new FileFolderData(new StorageFile(data, true, false), count));
                        }
                        else
                        {
                            if (StoragePermissionManager.hasSDCardPermissions())
                            {
                                StorageDocument doc = new StorageDocument(null);
                                doc.initLazyly(pathFolder, null); //ohne MediaStoreData geht nur path und name eigentlich!!
//                                StorageDocument doc = (StorageDocument) StorageUtil.getFileByPath(pathFolder, null);
//                                if (doc != null)
                                    folder = new DocumentFolder(doc);
                            }

                            // TODO
//                            ((DocumentFolder)folder).setDocumentFolderData(new DocumentFolderData());
                        }

                        if (folder != null)
                        {
                            folder.setMediaStoreFolderData(new MediaStoreFolderData(id, data, bucketId, bucket, count, minDateTaken, maxDateTaken, minDateModified, maxDateModified));
                            folders.add(folder);
                            foldersSet.add(pathFolder);
                        }
                    }
                }
            }
            while (cursor.moveToNext());
        }

        cursor.close();

        return folders;
    }

    public static List<IFolder> getAllHiddenFoldersWithoutContent(StorageDefinitions.MediaType mediaType)
    {
        ArrayList<IFolder> folders = new ArrayList<>();

        String[] projection = MEDIA_STORE_FOLDER_COLUMNS_FILE;
        Uri baseUri = MediaStore.Files.getContentUri("external");

        String nonMediaCondition = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;
        String where = nonMediaCondition + " AND " + MediaStore.Files.FileColumns.TITLE + " LIKE ?";

        String[] params = new String[] { "%" + FILE_TYPE_NO_MEDIA + "%" };

        //String groupBy = "1) GROUP BY 1,(2";

        Cursor cursor = StorageManager.get().getContext().getContentResolver().query(baseUri,
                projection, // Which columns to return
                where,    // Which rows to return
                params,       // Selection arguments (none)
                null     // Ordering
        );

        if (cursor.moveToFirst())
        {
            Log.d(MediaStoreUtil.class.getSimpleName(), "Cursor count: " + cursor.getCount());
            int dataColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA);
            int parentColumn = cursor.getColumnIndex(MediaStore.Files.FileColumns.PARENT);

            do
            {
                String data = cursor.getString(dataColumn);
                String parent = cursor.getString(parentColumn);
                // foldersSet check is necessary, because more images with the same date could exist!!! Only use 1 and don't add folders twice!!!
//                BaseFolder folder = null;
//                IFile file = StorageUtil.getFileByPath(data, false);

                // get folder path manually
                String pathFolder = null;

                if (data != null)
                {
                    try
                    {
                        pathFolder = new File(data).getParentFile().getAbsolutePath();
                    }
                    catch (Exception e) {}
                }
//                        (data.substring(0, data.lastIndexOf("/"))) : null;

                if (pathFolder != null) // ignore orphaned paths! + ignore if file may be not YET createable + ignore files we don't have permissions for!
                {
                    BaseFolder folder = null;
                    boolean isFile = StorageUtil.isPathAFile(pathFolder);
                    if (isFile)//file.getType() == StorageDefinitions.FileType.File)
                    {
                        folder = new FileFolder(new File(pathFolder));
                        // TODO
//                            ((FileFolder)folder).setFileFolderData(new FileFolderData(new StorageFile(data, true, false), count));
                    }
                    else
                    {
                        if (StoragePermissionManager.hasSDCardPermissions())
                        {
                            StorageDocument doc = new StorageDocument(null);
                            doc.initLazyly(pathFolder, null); //ohne MediaStoreData geht nur path und name eigentlich!!
//                                StorageDocument doc = (StorageDocument) StorageUtil.getFileByPath(pathFolder, null);
//                                if (doc != null)
                            folder = new DocumentFolder(doc);
                        }

                        // TODO
//                            ((DocumentFolder)folder).setDocumentFolderData(new DocumentFolderData());
                    }

                    if (folder != null)
                    {
                        ArrayList<String> filesInParent = getAllHiddenFilesInFileParent(parent);
                        filterPathsByMediaType(filesInParent, mediaType);
                        int count = filesInParent.size();
                        if (count > 0)
                        {
                            if (folder instanceof FileFolder)
                                ((FileFolder) folder).setFileFolderData(new FileFolderData(new StorageFile(data, true, false), count));
                            else
                                ((DocumentFolder) folder).setDocumentFolderData(new DocumentFolderData(new StorageFile(data, true, false), count));
                            folders.add(folder);
                        }
                    }
                }
            }
            while (cursor.moveToNext());
        }

        cursor.close();

        return folders;
    }

    public static ArrayList<String> getAllHiddenFilesInFileParent(String parent)
    {
        String[] projection = {
                MediaStore.Files.FileColumns.DATA,
                MediaStore.Files.FileColumns.MEDIA_TYPE,
                MediaStore.Files.FileColumns.PARENT,
                MediaStore.Files.FileColumns.TITLE
        };
        Uri baseUri = MediaStore.Files.getContentUri("external");

        String condition = MediaStore.Files.FileColumns.MEDIA_TYPE + "=" + MediaStore.Files.FileColumns.MEDIA_TYPE_NONE;

        String where = condition + " AND " + MediaStore.Files.FileColumns.PARENT + "=?";

        String[] params = new String[] { parent };

        Cursor cursor = StorageManager.get().getContext().getContentResolver().query(baseUri,
                projection, // Which columns to return
                where,    // Which rows to return
                params,       // Selection arguments (none)
                null     // Ordering
        );

       ArrayList<String> files = new ArrayList<>();
        if (cursor.moveToFirst())
        {
            do {
                files.add(cursor.getString(0));
            }
            while (cursor.moveToNext());
        }

        cursor.close();

        return files;
    }

    public static void filterPathsByMediaType(List<String> paths, StorageDefinitions.MediaType type)
    {
        switch (type)
        {
            case Image:
            {
                for (Iterator<String> it = paths.iterator(); it.hasNext();)
                {
                    String p = it.next();
                    boolean accepted = false;
                    for (final String ext : StorageDefinitions.IMG_FORMATS)
                    {
                        if (p.toLowerCase().endsWith("." + ext))
                        {
                            accepted = true;
                            break;
                        }
                    }
                    if (!accepted)
                        it.remove();
                }
                break;
            }
            case Video:
            {
                for (Iterator<String> it = paths.iterator(); it.hasNext();)
                {
                    String p = it.next();
                    boolean accepted = false;
                    for (final String ext : StorageDefinitions.VID_FORMATS)
                    {
                        if (p.toLowerCase().endsWith("." + ext))
                        {
                            accepted = true;
                            break;
                        }
                    }
                    if (!accepted)
                        it.remove();
                }
                break;
            }
            default:
                throw new RuntimeException("Type not handled");
        }
    }

    public static List<MediaStoreFileData> loadFilesInFolder(Long folderBucketId, List<StorageDefinitions.MediaType> fileTypesToList, StorageDefinitions.FileSortingType limitSortingType, StorageDefinitions.FileSortingOrder limitSortingOrder, Integer limit, Long minDate, Long maxDate, DateType dateType)
    {
        if (fileTypesToList == null)
        {
            fileTypesToList = new ArrayList<>();
            fileTypesToList.addAll(Arrays.asList(StorageDefinitions.MediaType.values()));
        }

        List<MediaStoreFileData> files = new ArrayList<>();
        for (int i = 0; i < fileTypesToList.size(); i++)
            files.addAll(doLoadFilesInFolder(folderBucketId, fileTypesToList.get(i), limitSortingType, limitSortingOrder, limit, minDate, maxDate, dateType));
        return files;
    }

    private static List<MediaStoreFileData> doLoadFilesInFolder(Long folderBucketId, StorageDefinitions.MediaType mediaType, StorageDefinitions.FileSortingType limitSortingType, StorageDefinitions.FileSortingOrder limitSortingOrder, Integer limit,  Long minDate, Long maxDate, DateType dateType)
    {
        if (limitSortingOrder == null)
            limitSortingOrder = StorageDefinitions.FileSortingOrder.Asc;
        if (limitSortingType == null)
            limitSortingType = StorageDefinitions.FileSortingType.Name;
        
        List<MediaStoreFileData> mediaStoreFileDatas = new ArrayList<>();

        String[] projection = null;
        switch (mediaType)
        {
            case Image:
                projection = MEDIA_STORE_COLUMNS_IMAGE;
                break;
            case Video:
                projection = MEDIA_STORE_COLUMNS_VIDEO_QUERY;
                break;
            default:
                throw new RuntimeException("Type not handled");
        }
        Uri uri = getMediaStoreUri(mediaType, true);
        String bucketIdCol = getContentBucketIdColumn(mediaType);

        String orderBy = null;
        switch (limitSortingType)
        {
            case Name:
                orderBy = getContentNameColumn(mediaType);
                break;
            case CreationDate:
                orderBy = getContentCreationDateColumn(mediaType);
                break;
            case ModificationDate:
                orderBy = getContentModifiedDateColumn(mediaType);
                break;
            default:
                throw new RuntimeException("Type not handled!");
        }
        switch (limitSortingOrder)
        {
            case Asc:
                orderBy += " ASC";
                break;
            case Desc:
                orderBy += " DESC";
                break;
        }

        if (limit != null)
            orderBy += " LIMIT " + limit;

        String selection = bucketIdCol + " LIKE ?";
        List<String> selectionArgsList = new ArrayList<>();
        selectionArgsList.add(String.valueOf(folderBucketId));
        String dateCol = null;
        if (dateType != null)
        {
            switch (dateType)
            {
                case Created:
                    dateCol = getContentCreationDateColumn(mediaType);
                    break;
                case Modified:
                    dateCol = getContentModifiedDateColumn(mediaType);
                    break;
                case Added:
                    dateCol = getContentAddedDateColumn(mediaType);
                    break;
            }
        }
        if (minDate != null)
        {
            selection += " AND " + dateCol + ">=?";
            if (dateType == DateType.Modified) // modified ist in seconds!
                minDate /= 1000;
            selectionArgsList.add(String.valueOf(minDate));
        }
        if (maxDate != null)
        {
            selection += " AND " + dateCol + "<=?";
            if (dateType == DateType.Modified) // modified ist in seconds!
                maxDate /= 1000;
            selectionArgsList.add(String.valueOf(maxDate));
        }

        // Make the query.
        Cursor cursor = StorageManager.get().getContext().getContentResolver().query(uri,
                projection, // Which columns to return
                selection,    // Which rows to return
                selectionArgsList.toArray(new String[selectionArgsList.size()]),     // Which rows to return (all rows)
                orderBy     // Ordering
        );

        if (cursor.moveToFirst())
        {
            int idColumn = 0;
            int nameColumn = 1;
            int dataColum = 2;
            int dateTakenColumn = 3;
            int dateModifiedColumn = 4;
            int mimeTypeColumn = 5;
            int widthColumn = 6;
            int heightColumn = 7;
            int latitudeColumn = 8;
            int longitudeColumn = 9;
            int orientationColumn = 10;

            do
            {
                String data = cursor.getString(dataColum);
                String dateToken  = cursor.getString(dateTakenColumn);
                String dateModified = cursor.getString(dateModifiedColumn);
                long id = cursor.getLong(idColumn);
                String name = cursor.getString(nameColumn);
                String mimeType = cursor.getString(mimeTypeColumn);
                int orientation = cursor.getInt(orientationColumn);
                int width = cursor.getInt(widthColumn);
                int height = cursor.getInt(heightColumn);
                double latitude = cursor.getDouble(latitudeColumn);
                double longitude = cursor.getDouble(longitudeColumn);

                long lDateToken = dateToken != null ? Long.parseLong(dateToken) : 0;
                long lDateModified = dateModified != null ? (Long.parseLong(dateModified) * 1000) : 0;

                Uri fileUri = Uri.withAppendedPath(uri, String.valueOf(id));

                MediaStoreFileData mediaStoreFileData = new MediaStoreFileData(mediaType, fileUri, id, name, data, lDateToken, lDateModified, mimeType, width, height, latitude, longitude, orientation);
                mediaStoreFileDatas.add(mediaStoreFileData);
            }
            while (cursor.moveToNext());
        }
        cursor.close();
        return mediaStoreFileDatas;
    }

    public static MediaStoreFileData loadMediaStoreData(StorageDefinitions.MediaType mediaType, File f)
    {
        return loadMediaStoreData(mediaType, f.getPath());
    }

    public static MediaStoreFileData loadMediaStoreData(StorageDefinitions.MediaType mediaType, String path)
    {
        Uri baseUri = getMediaStoreUri(mediaType, true);
        String dataCol = getContentDataColumn(mediaType);
        String[] projection = null;
        switch (mediaType)
        {
            case Image:
                projection = MEDIA_STORE_COLUMNS_IMAGE;
                break;
            case Video:
                projection = MEDIA_STORE_COLUMNS_VIDEO_QUERY;
                break;
            default:
                throw new RuntimeException("Type not handled");
        }

        Cursor cursor = StorageManager.get().getContext().getContentResolver().query(baseUri,
                projection, // Which columns to return
                dataCol + " LIKE ?",    // Which rows to return
                new String[] { path },    // Selection arguments (none)
                null     // Ordering
        );

        MediaStoreFileData data = null;
        if (cursor.moveToFirst())
            data = calcMediaStoreDataFromCursor(cursor, true, mediaType);
        cursor.close();
        return data;
    }

    public static MediaStoreFileData calcMediaStoreDataFromCursor(Cursor cursor, boolean external, StorageDefinitions.MediaType type)
    {
        String[] projection = null;
        Uri baseUri = null;
        switch (type)
        {
            case Image:
                projection = MEDIA_STORE_COLUMNS_IMAGE;
                baseUri = getMediaStoreUri(StorageDefinitions.MediaType.Image, external);
                break;
            case Video:
                projection = MEDIA_STORE_COLUMNS_VIDEO_COLS;
                baseUri = getMediaStoreUri(StorageDefinitions.MediaType.Video, external);
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
        int width = cursor.getInt(cursor.getColumnIndex(projection[6]));
        int height = cursor.getInt(cursor.getColumnIndex(projection[7]));
        double latitude = cursor.getDouble(cursor.getColumnIndex(projection[8]));
        double longitude = cursor.getDouble(cursor.getColumnIndex(projection[9]));
        int orientation = cursor.getInt(cursor.getColumnIndex(projection[10]));

        // parse values if necessary
        long lDateToken = dateToken != null ? Long.parseLong(dateToken) : 0;
        long lDateModified = dateModified != null ? (Long.parseLong(dateModified) * 1000) : 0;

        Uri uri = Uri.withAppendedPath(baseUri, String.valueOf(id));

        return new MediaStoreFileData(type, uri, id, name, data, lDateToken, lDateModified, mimeType, width, height, latitude, longitude, orientation);
    }

    public static boolean delete(Uri uri)
    {
        return StorageManager.get().getContext().getContentResolver().delete(uri, null, null) == 1;
    }

    public static boolean tryDelete(Uri uri)
    {
        try
        {
            return StorageManager.get().getContext().getContentResolver().delete(uri, null, null) == 1;
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
    }

    public static ContentProviderOperation deleteOperation(StorageDefinitions.MediaType mediaType, String path)
    {
        String columnData = getContentDataColumn(mediaType);
        Uri mainUri = getMediaStoreUri(mediaType, true);
        return ContentProviderOperation.newDelete(mainUri)
                .withSelection(columnData + "=?", new String[]{path})
                .build();
    }

    public static boolean delete(File file, Boolean external)
    {
        return delete(file.getAbsoluteFile(), external);
    }

    public static boolean delete(String path, Boolean external)
    {
        List<Uri> baseUris = new ArrayList<>();
        String dataColumn = null;

        boolean deleted = false;
        String mimeType = ExtensionUtil.getMimeType(path);
        if (mimeType != null)
        {
            if (mimeType.contains("image"))
            {
                if (external == null || external)
                    baseUris.add(getMediaStoreUri(StorageDefinitions.MediaType.Image, true));
                if (external == null || !external)
                    baseUris.add(getMediaStoreUri(StorageDefinitions.MediaType.Image, false));
                dataColumn = MediaStore.Images.Media.DATA;
            }
            else if (mimeType.contains("video"))
            {
                if (external == null || external)
                    baseUris.add(getMediaStoreUri(StorageDefinitions.MediaType.Video, true));
                if (external == null || !external)
                    baseUris.add(getMediaStoreUri(StorageDefinitions.MediaType.Video, false));
                dataColumn = MediaStore.Video.Media.DATA;
            }
            else
            {
                // handle other data types as audio/folder
//                throw new RuntimeException("Type not handled!");
            }
        }

        for (int i = 0; i < baseUris.size(); i++)
            deleted |= StorageManager.get().getContext().getContentResolver().delete(baseUris.get(i), dataColumn + "=?", new String[]{path}) > 0;

        return deleted;
    }

    public static boolean updateAfterDeletion(String path)
    {
        return updateAfterDeletion(new File(path));
    }

    public static boolean updateAfterDeletion(File file)
    {
        return updateAfterDeletion(Uri.fromFile(file));
    }

    public static boolean updateAfterDeletion(Uri uri)
    {
        return delete(uri);
    }

    public static ContentProviderOperation createOperation(StorageDefinitions.MediaType mediaType, String path, long dateTaken, long dateModified, Double latitude, Double longitude, Integer rotation)
    {
        Uri uri = getMediaStoreUri(mediaType, true);
        File f = new File(path);
        ContentValues values = createMediaContentValues(mediaType, f.getAbsolutePath(), f.getName(), dateTaken, dateModified, latitude, longitude, rotation);
        return ContentProviderOperation.newInsert(uri)
                .withValues(values)
                .build();
    }

    public static Uri updateAfterCreation(IFile file, MediaStoreFileData mediaStoreFileData)
    {
        StorageDefinitions.MediaType mediaType = ExtensionUtil.getMediaType(file.getPath());
        return addMedia(mediaType, true, file.getPath(), file.getName(), mediaStoreFileData.getDateTaken(), mediaStoreFileData.getDateModified(), mediaStoreFileData.getLatitude(), mediaStoreFileData.getLongitude(), mediaStoreFileData.getRotation());
//        addMedia(mediaType, true, file.getWrapped(), file.created(), file.lastModified(), file.location(), file.rotation());
    }

    public static boolean updateAfterChangeBlocking(String path, int timeToWaitToRecheckMediaScanner)
    {
        final AtomicBoolean changed = new AtomicBoolean(false);
        MediaScannerConnection.scanFile(StorageManager.get().getContext(),
                new String[]{path}, null,
                new MediaScannerConnection.OnScanCompletedListener()
                {
                    public void onScanCompleted(String path, Uri uri)
                    {
                        changed.set(true);
                    }
                });

        while (!changed.get()) {
            try {
                Thread.sleep(timeToWaitToRecheckMediaScanner);
            } catch (InterruptedException e) {
                return false;
            }
        }

        return true;
    }

    public static boolean updateAfterChangeBlocking(String[] paths, int timeToWaitToRecheckMediaScanner)
    {
        final AtomicInteger counter = new AtomicInteger(0);
        MediaScannerConnection.scanFile(StorageManager.get().getContext(), paths, null,
                new MediaScannerConnection.OnScanCompletedListener()
                {
                    public void onScanCompleted(String path, Uri uri)
                    {
                        counter.incrementAndGet();
                    }
                });

        int count = paths.length;
        while (counter.get() != count) {
            try {
                Thread.sleep(timeToWaitToRecheckMediaScanner);
            } catch (InterruptedException e) {
                return false;
            }
        }

        return true;
    }

    public static Uri addMedia(StorageDefinitions.MediaType mediaType, boolean external, String filePath, String fileName, long dateTaken, long dateModified, Double latitude, Double longitude, int rotation)
    {
        ContentValues cv = createMediaContentValues(mediaType, filePath, fileName, dateTaken, dateModified, latitude, longitude, rotation);
        return StorageManager.get().getContext().getContentResolver().insert(getMediaStoreUri(mediaType, external), cv);
    }

    public static int addMedias(StorageDefinitions.MediaType mediaType, boolean external, ContentResolver cr, ContentValues[] values)
    {
        return cr.bulkInsert(getMediaStoreUri(mediaType, external), values);
    }

    public static boolean renameMedia(StorageDefinitions.MediaType mediaType, long id, String newFilePath, String newName)
    {
        ContentResolver resolver = StorageManager.get().getContext().getContentResolver();
        ContentValues values = createRenameMediaContentValues(mediaType, newFilePath, newName);
        Uri uri = getMediaStoreUri(mediaType, true);
        String columnId = getContentIDColumn(mediaType);
        return resolver.update(uri, values, columnId + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    public static int renameMedia(StorageDefinitions.MediaType mediaType, String oldFilePath, String newFilePath, String newName)
    {
        ContentResolver resolver = StorageManager.get().getContext().getContentResolver();
        ContentValues values = createRenameMediaContentValues(mediaType, newFilePath, newName);
        Uri uri = getMediaStoreUri(mediaType, true);
        String columnData = getContentDataColumn(mediaType);
        return StorageManager.get().getContext().getContentResolver().update(uri, values, columnData + "=?", new String[]{oldFilePath});
    }

    public static ContentProviderOperation renameMediaOperation(StorageDefinitions.MediaType mediaType, long id, String newFilePath, String newName)
    {
        String columnId = getContentIDColumn(mediaType);
        Uri uri = getMediaStoreUri(mediaType, true);
        ContentValues values = createRenameMediaContentValues(mediaType, newFilePath, newName);
        return ContentProviderOperation.newUpdate(uri)
                .withSelection(columnId + "=?", new String[]{String.valueOf(id)})
                .withValues(values)
                .build();
    }

    public static ContentProviderOperation renameMediaOperation(StorageDefinitions.MediaType mediaType, String oldPath, String newPath, String newName)
    {
        String columnData = getContentDataColumn(mediaType);
        Uri uri = getMediaStoreUri(mediaType, true);
        ContentValues values = createRenameMediaContentValues(mediaType, newPath, newName);
        return ContentProviderOperation.newUpdate(uri)
                .withSelection(columnData + "=?", new String[]{oldPath})
                .withValues(values)
                .build();
    }

    public static String getContentIDColumn(StorageDefinitions.MediaType mediaType)
    {
        String column;
        switch (mediaType)
        {
            case Image:
                column = MediaStore.Images.Media._ID;
                break;
            case Video:
                column = MediaStore.Video.Media._ID;
                break;
            default:
                throw new RuntimeException("Type not handled");
        }
        return column;
    }

    public static String getContentDataColumn(StorageDefinitions.MediaType mediaType)
    {
        String column;
        switch (mediaType)
        {
            case Image:
                column = MediaStore.Images.Media.DATA;
                break;
            case Video:
                column = MediaStore.Video.Media.DATA;
                break;
            default:
                throw new RuntimeException("Type not handled");
        }
        return column;
    }

    public static String getContentNameColumn(StorageDefinitions.MediaType mediaType)
    {
        String column;
        switch (mediaType)
        {
            case Image:
                column = MediaStore.Images.Media.DISPLAY_NAME;
                break;
            case Video:
                column = MediaStore.Video.Media.DISPLAY_NAME;
                break;
            default:
                throw new RuntimeException("Type not handled");
        }
        return column;
    }

    public static String getContentBucketIdColumn(StorageDefinitions.MediaType mediaType)
    {
        String column;
        switch (mediaType)
        {
            case Image:
                column = MediaStore.Images.Media.BUCKET_ID;
                break;
            case Video:
                column = MediaStore.Video.Media.BUCKET_ID;
                break;
            default:
                throw new RuntimeException("Type not handled");
        }
        return column;
    }

    public static String getContentAddedDateColumn(StorageDefinitions.MediaType mediaType)
    {
        String column;
        switch (mediaType)
        {
            case Image:
                column = MediaStore.Images.Media.DATE_ADDED;
                break;
            case Video:
                column = MediaStore.Video.Media.DATE_ADDED;
                break;
            default:
                throw new RuntimeException("Type not handled");
        }
        return column;
    }

    public static String getContentCreationDateColumn(StorageDefinitions.MediaType mediaType)
    {
        String column;
        switch (mediaType)
        {
            case Image:
                column = MediaStore.Images.Media.DATE_TAKEN;
                break;
            case Video:
                column = MediaStore.Video.Media.DATE_TAKEN;
                break;
            default:
                throw new RuntimeException("Type not handled");
        }
        return column;
    }

    public static String getContentModifiedDateColumn(StorageDefinitions.MediaType mediaType)
    {
        String column;
        switch (mediaType)
        {
            case Image:
                column = MediaStore.Images.Media.DATE_MODIFIED;
                break;
            case Video:
                column = MediaStore.Video.Media.DATE_MODIFIED;
                break;
            default:
                throw new RuntimeException("Type not handled");
        }
        return column;
    }

    private static ContentValues createRenameMediaContentValues(StorageDefinitions.MediaType mediaType, String newFilePath, String newName)
    {
        ContentValues values = new ContentValues(3);
        String keyTitle;
        String keyDisplayName;
        String keyData;
        switch (mediaType)
        {
            case Image:
                keyTitle = MediaStore.Images.Media.TITLE;
                keyDisplayName = MediaStore.Images.Media.DISPLAY_NAME;
                keyData = MediaStore.Images.Media.DATA;
                break;
            case Video:
                keyTitle = MediaStore.Video.Media.TITLE;
                keyDisplayName = MediaStore.Video.Media.DISPLAY_NAME;
                keyData = MediaStore.Video.Media.DATA;
                break;
            default:
                throw new RuntimeException("Type not handled");
        }
        values.put(keyTitle, newName);
        values.put(keyDisplayName, newName);
        values.put(keyData, newFilePath);
        return values;
    }

    public static Uri getMediaStoreUri(StorageDefinitions.MediaType mediaType, boolean external)
    {
        Uri uri = null;
        switch (mediaType)
        {
            case Image:
                uri = external ? MediaStore.Images.Media.EXTERNAL_CONTENT_URI : MediaStore.Images.Media.INTERNAL_CONTENT_URI;
                break;
            case Video:
                uri = external ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI : MediaStore.Video.Media.INTERNAL_CONTENT_URI;
                break;
            default:
                throw new RuntimeException("Type not handled!");
        }
        return uri;
    }

    public static ContentValues deleteMediaContentValues(StorageDefinitions.MediaType mediaType, String filePath)
    {
        ContentValues values = new ContentValues(1);
        switch (mediaType)
        {
            case Image:
                values.put(MediaStore.Images.Media.DATA, filePath);
                break;
            case Video:
                values.put(MediaStore.Video.Media.DATA, filePath);
                break;
            default:
                throw new RuntimeException("Type not handled!");
        }

        return values;
    }

    public static ContentValues createMediaContentValues(StorageDefinitions.MediaType mediaType, String filePath, String fileName, long dateTaken, long dateModified, Double latitude, Double longitude, Integer rotation)
    {
        ContentValues values = new ContentValues(7);
        switch (mediaType)
        {
            case Image:
                values.put(MediaStore.Images.Media.TITLE, fileName);
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.DATE_TAKEN, dateTaken);
                values.put(MediaStore.Images.Media.DATE_MODIFIED, dateModified / 1000L);
                values.put(MediaStore.Images.Media.MIME_TYPE, ExtensionUtil.getMimeType(fileName));
                values.put(MediaStore.Images.Media.ORIENTATION, rotation);
                values.put(MediaStore.Images.Media.DATA, filePath);
                if (latitude != null || longitude != null)
                {
                    values.put(MediaStore.Images.Media.LATITUDE, latitude);
                    values.put(MediaStore.Images.Media.LONGITUDE, longitude);
                }
                break;
            case Video:
                values.put(MediaStore.Video.Media.TITLE, fileName);
                values.put(MediaStore.Video.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Video.Media.DATE_TAKEN, dateTaken);
                values.put(MediaStore.Video.Media.DATE_MODIFIED, dateModified / 1000L);
                values.put(MediaStore.Video.Media.MIME_TYPE, ExtensionUtil.getMimeType(fileName));
                values.put(MediaStore.Video.Media.DATA, filePath);
                if (latitude != null || longitude != null)
                {
                    values.put(MediaStore.Video.Media.LATITUDE, latitude);
                    values.put(MediaStore.Video.Media.LONGITUDE, longitude);
                }
                break;
            default:
                throw new RuntimeException("Type not handled!");
        }

        return values;
    }

    public static String getRealPathFromURI(Uri contentUri)
    {
        String path = null;
        Cursor cursor = null;
        try
        {
            for (int i = 0; i < StorageDefinitions.MediaType.values().length; i++)
            {
                StorageDefinitions.MediaType mediaType = StorageDefinitions.MediaType.values()[i];
                String dataColumn = getContentDataColumn(mediaType);
                cursor = StorageManager.get().getContext().getContentResolver().query(contentUri, new String[]{dataColumn}, null, null, null);
                if (cursor.moveToFirst())
                {
                    int column_index = cursor.getColumnIndexOrThrow(dataColumn);
                    path = cursor.getString(column_index);
                    break;
                }
            }
        }
        catch (Exception e)
        {

        }
        finally
        {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    public static String getRealPathFromURI(StorageDefinitions.MediaType mediaType, Uri contentUri)
    {
        String path = null;
        Cursor cursor = null;
        try {
            String dataColumn = getContentDataColumn(mediaType);
            cursor = StorageManager.get().getContext().getContentResolver().query(contentUri, new String[]{dataColumn}, null, null, null);
            if (cursor.moveToFirst())
            {
                int column_index = cursor.getColumnIndexOrThrow(dataColumn);
                path = cursor.getString(column_index);
            }
        }
        catch (Exception e)
        {

        }
        finally
        {
            if (cursor != null) {
                cursor.close();
            }
        }
        return path;
    }

    public static String getLastImagePath(StorageDefinitions.MediaType mediaType)
    {
        String path = null;
        final String[] imageColumns = { getContentIDColumn(mediaType), getContentDataColumn(mediaType)};
        final String imageOrderBy = getContentIDColumn(mediaType) + " DESC";
        Cursor cursor = StorageManager.get().getContext().getContentResolver().query(
                getMediaStoreUri(mediaType, true),
                imageColumns,
                null,
                null,
                imageOrderBy);

        if(cursor.moveToFirst())
            path = cursor.getString(cursor.getColumnIndex(getContentDataColumn(mediaType)));
        cursor.close();

        return path;
    }

    public static ContentProviderResult[] applyBatch(ArrayList<ContentProviderOperation> operations) throws StorageException
    {
        if (operations == null && operations.size() == 0)
            return null;

        try
        {
            ContentProviderResult[] result = StorageManager.get().getContext().getContentResolver().applyBatch(MediaStore.AUTHORITY, operations);
            return result;
        }
        catch (RemoteException e)
        {
            throw new StorageException(StorageException.Type.BatchError, e);
        }
        catch (OperationApplicationException e)
        {
            throw new StorageException(StorageException.Type.BatchError, e);
        }
    }

}
