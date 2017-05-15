package com.testapp.testtask.data;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by paul on 12.05.17.
 */

public class DownloadingService extends IntentService {

    public static final int UPDATE_PROGRESS = 8344;
    public static final int START_DOWNLOAD = 1;
    public static final int CANCEL_DOWNLOAD = -1;
    private static final long  MEGABYTE = 1024L * 1024L;
    public static boolean cancelled = false;

    private InputStream input;
    private OutputStream output;
    private String regionName;


    public DownloadingService() {
        super("DownloadingService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String urlToDownload = intent.getStringExtra("url");
        regionName = intent.getStringExtra("RegionName");
        Log.e("DownloadingService", urlToDownload);
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);

        Intent intentToSend = new Intent("download.initialize");
        intentToSend.putExtra("RegionName", regionName);
        manager.sendBroadcast(intentToSend);
        Log.e("DownloadService", "Broadcast sent");
        try {
            URL url = new URL(urlToDownload);
            URLConnection connection = url.openConnection();
            connection.connect();

            int fileLength = connection.getContentLength();

            // download the file
            input = new BufferedInputStream(connection.getInputStream());
            File file = new File(Environment.getExternalStorageDirectory() + "/Maps/");
            if (!file.exists()) {
                file.mkdirs();
            }
            output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Maps/" +
                    regionName.replace(" ", "") + ".zip");

            byte data[] = new byte[1024];
            long total = 0;
            int count;
            while ((count = input.read(data)) != -1 && !cancelled) {
                total += count;
                // publishing the progress....
                Bundle resultData = new Bundle();
                resultData.putInt("fileLengthMb", (int)(fileLength/MEGABYTE));
                resultData.putLong("downloadedInMb", (total/MEGABYTE));
                resultData.putInt("progress" ,(int) (total * 100 / fileLength));
                resultData.putString("RegionName", regionName);
                Intent intentResults = new Intent("update.data");
                intentResults.putExtras(resultData);
                manager.sendBroadcast(intentResults);
                output.write(data, 0, count);
            }
            output.flush();
            output.close();
            input.close();
            if (cancelled) {
                sendDestroy();
                onDestroy();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (!cancelled) {
            Intent intentFinish = new Intent("update.data");
            intentFinish.putExtra("progress", 100);
            manager.sendBroadcast(intentFinish);
        }


    }


    public void sendDestroy() {
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Maps/" + regionName.replace(" ", "") + ".zip");
        if (file.exists()) {
            file.delete();
        }
        Intent intent = new Intent("download.cancel");
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }
}
