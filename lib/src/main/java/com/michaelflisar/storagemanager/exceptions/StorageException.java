package com.michaelflisar.storagemanager.exceptions;

/**
 * Created by Prometheus on 06.03.2016.
 */
public class StorageException extends Exception {

    public enum Type
    {
        Unknown,
        BatchError
    }

    public StorageException(Type type)
    {
        super(getMessage(type));
    }

    public StorageException(Type type, Exception e)
    {
        super(getMessage(type) + " | e=" + (e != null ? e.getMessage() : "NULL"), e);
    }

    private static String getMessage(Type type)
    {
        switch (type)
        {
            case Unknown:
                return "Unknown storage exception";
            default:
                throw new RuntimeException("Type not handled");
        }
    }
}
