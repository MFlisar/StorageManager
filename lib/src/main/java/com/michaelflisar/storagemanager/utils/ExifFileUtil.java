package com.michaelflisar.storagemanager.utils;

import android.location.Location;
import android.media.ExifInterface;

import com.michaelflisar.storagemanager.interfaces.IFile;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Prometheus on 04.03.2016.
 */
public class ExifFileUtil {

    public static final String[] ATTRIBUTES = new String[]
            {
                    ExifInterface.TAG_APERTURE,
                    ExifInterface.TAG_DATETIME,
                    ExifInterface.TAG_DATETIME_DIGITIZED,
                    ExifInterface.TAG_EXPOSURE_TIME,
                    ExifInterface.TAG_FLASH,
                    ExifInterface.TAG_FOCAL_LENGTH,
                    ExifInterface.TAG_GPS_ALTITUDE,
                    ExifInterface.TAG_GPS_ALTITUDE_REF,
                    ExifInterface.TAG_GPS_DATESTAMP,
                    ExifInterface.TAG_GPS_LATITUDE,
                    ExifInterface.TAG_GPS_LATITUDE_REF,
                    ExifInterface.TAG_GPS_LONGITUDE,
                    ExifInterface.TAG_GPS_LONGITUDE_REF,
                    ExifInterface.TAG_GPS_PROCESSING_METHOD,
                    ExifInterface.TAG_GPS_TIMESTAMP,
                    ExifInterface.TAG_IMAGE_LENGTH,
                    ExifInterface.TAG_IMAGE_WIDTH,
                    ExifInterface.TAG_ISO,
                    ExifInterface.TAG_MAKE,
                    ExifInterface.TAG_MODEL,
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.TAG_SUBSEC_TIME,
                    ExifInterface.TAG_SUBSEC_TIME_DIG,
                    ExifInterface.TAG_SUBSEC_TIME_ORIG,
                    ExifInterface.TAG_WHITE_BALANCE
            };

    private static SimpleDateFormat sFormatter = null;

    public static SimpleDateFormat getDateFormat()
    {
        if (sFormatter == null)
        {
            sFormatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
            sFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
        }
        return sFormatter;
    }


    public static boolean copyExif(String oldPath, String newPath) throws IOException
    {
        return copyExifAndroid(oldPath, newPath);
    }

    private static boolean copyExifAndroid(String oldPath, String newPath) throws IOException
    {
        ExifInterface oldExif = new ExifInterface(oldPath);

        ExifInterface newExif = new ExifInterface(newPath);
        for (int i = 0; i < ATTRIBUTES.length; i++)
        {
            String value = oldExif.getAttribute(ATTRIBUTES[i]);
            if (value != null)
                newExif.setAttribute(ATTRIBUTES[i], value);
        }
        newExif.saveAttributes();

        return true;
    }

    public static HashMap<String, String> getExifInformations(String path)
    {
        HashMap<String, String> map = new HashMap<>();
        try
        {
            ExifInterface exif = new ExifInterface(path);

            for (int i = 0; i < ATTRIBUTES.length; i++)
            {
                String value = exif.getAttribute(ATTRIBUTES[i]);
                map.put(ATTRIBUTES[i], value);
            }
        }
        catch (IOException e)
        {

        }

        return map;
    }

    // --------------------------------
    // Functions for retrieving single values
    // --------------------------------

    public static long getDate(Long lastModified, Map<String, String> exifValues)
    {
        if (exifValues == null)
            return 0;

        try
        {
            String date = exifValues.get(ExifInterface.TAG_DATETIME);
            return getDateFormat().parse(date).getTime();
        }
        catch (Exception e) {}

        // Fallback...
        return lastModified != null ? lastModified : 0;
    }

    public static Integer getRotation(Map<String, String> exifValues)
    {
        if (exifValues == null)
            return null;

        try
        {
            String orientationAsString = exifValues.get(ExifInterface.TAG_ORIENTATION);
            Integer orientation = orientationAsString != null ? Integer.parseInt(orientationAsString) : null;
            return orientation == null ? null : convertExifOrientationToDegrees(orientation);
        }
        catch (Exception e) {}

        return null;
    }

    public static Integer getWidth(Map<String, String> exifValues)
    {
        if (exifValues == null)
            return null;

        try
        {
            String widthAsString = exifValues.get(ExifInterface.TAG_IMAGE_WIDTH);
            Integer width = widthAsString != null ? Integer.parseInt(widthAsString) : null;
            return  width;
        }
        catch (Exception e) {}

        return null;
    }

    public static Integer getHeight(Map<String, String> exifValues)
    {
        if (exifValues == null)
            return null;

        try
        {
            String heightAsString = exifValues.get(ExifInterface.TAG_IMAGE_LENGTH);
            Integer height = heightAsString != null ? Integer.parseInt(heightAsString) : null;
            return  height;
        }
        catch (Exception e) {}

        return null;
    }

    public static Location getLocation(Map<String, String> exifValues)
    {
        if (exifValues == null)
            return null;

        String sLatitude = exifValues.get(ExifInterface.TAG_GPS_LATITUDE);
        String sLatitudeRef = exifValues.get(ExifInterface.TAG_GPS_LATITUDE_REF);
        String sLongitude = exifValues.get(ExifInterface.TAG_GPS_LONGITUDE);
        String sLongitudeRef = exifValues.get(ExifInterface.TAG_GPS_LONGITUDE_REF);

        Float latitude = 0f, longitude = 0f;

        if((sLatitude !=null) && (sLatitudeRef !=null) && (sLongitude != null) && (sLongitudeRef !=null))
        {
            if(sLatitudeRef.equals("N"))
                latitude = convertToDegree(sLatitude);
            else
                latitude = 0 - convertToDegree(sLatitude);

            if(sLongitudeRef.equals("E"))
                longitude = convertToDegree(sLongitude);
            else
                longitude = 0 - convertToDegree(sLongitude);
        }

        if (latitude == 0.0 && longitude == 0.0)
            return null;

        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        return location;
    }

    // --------------------------
    // helper functions
    // --------------------------

    public static Float convertToDegree(String stringDMS)
    {
        Float result = null;
        String[] DMS = stringDMS.split(",", 3);

        String[] stringD = DMS[0].split("/", 2);
        Double D0 = new Double(stringD[0]);
        Double D1 = new Double(stringD[1]);
        Double FloatD = D0/D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = new Double(stringM[0]);
        Double M1 = new Double(stringM[1]);
        Double FloatM = M0/M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = new Double(stringS[0]);
        Double S1 = new Double(stringS[1]);
        Double FloatS = S0/S1;

        result = new Float(FloatD + (FloatM/60) + (FloatS/3600));

        return result;
    }

    public static int normaliseRotation(int rotation)
    {
        int rot = rotation % 360;
        if (rot == 0)
            rot = 0;
        else if (rot == -90 || rot == 270)
            rot = 270;
        else if (rot == -180 || rot == 180)
            rot = 180;
        else if (rot == -270 || rot == 90)
            rot = 90;

        return rot;
    }

    public static int convertExifOrientationToDegrees(int orientation)
    {
        if (orientation == ExifInterface.ORIENTATION_NORMAL)
            return 0;
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_90)
            return 90;
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_180)
            return 180;
        else if (orientation == ExifInterface.ORIENTATION_ROTATE_270)
            return 270;
        return ExifInterface.ORIENTATION_UNDEFINED;
    }

    public static int convertNormalisedDegreesToExif(int degrees)
    {
        int rot = ExifInterface.ORIENTATION_UNDEFINED;
        if (degrees == 0)
            rot = ExifInterface.ORIENTATION_NORMAL;
        else if (degrees == 90)
            rot = ExifInterface.ORIENTATION_ROTATE_90;
        else if (degrees == 180)
            rot = ExifInterface.ORIENTATION_ROTATE_180;
        else if (degrees == 270)
            rot = ExifInterface.ORIENTATION_ROTATE_270;
        return rot;
    }
}