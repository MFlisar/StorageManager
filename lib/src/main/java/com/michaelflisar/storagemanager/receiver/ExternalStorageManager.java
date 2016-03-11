package com.michaelflisar.storagemanager.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;

/**
 * Created by flisar on 04.03.2016.
 */
public class ExternalStorageManager
{
    private BroadcastReceiver mExternalStorageReceiver;
    private boolean mExternalStorageAvailable = false;
    private boolean mExternalStorageWriteable = false;

    public ExternalStorageManager()
    {

    }

    public void startWatchingExternalStorage(Context context)
    {
        stopWatchingExternalStorage(context);

        mExternalStorageReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateExternalStorageState();
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        context.registerReceiver(mExternalStorageReceiver, filter);
        updateExternalStorageState();
    }

    public void stopWatchingExternalStorage(Context context)
    {
        if (mExternalStorageReceiver != null)
            context.unregisterReceiver(mExternalStorageReceiver);
        mExternalStorageReceiver = null;
    }

    private void updateExternalStorageState()
    {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state))
            setState(true, true);
        else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state))
            setState(true, false);
        else
            setState(false, false);
    }

    private void setState(boolean available, boolean writeable)
    {
        mExternalStorageAvailable = available;
        mExternalStorageWriteable = writeable;
    }
}