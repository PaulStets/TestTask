package com.testapp.testtask.data;

import android.app.DownloadManager;
import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.testapp.testtask.MainActivity;

import java.io.BufferedInputStream;
import java.io.File;


/**
 * Created by paul on 12.05.17.
 */

public class DownloadingService extends IntentService {


    public static boolean cancelled = false;

    private String regionName;


    public DownloadingService() {
        super("DownloadingService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        String urlToDownload = intent.getStringExtra("url");

        regionName = intent.getStringExtra("RegionName");
        Log.e("DownloadingService", urlToDownload);

        final DownloadManager downloadManager = (DownloadManager)
                getSystemService(Context.DOWNLOAD_SERVICE);

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(urlToDownload));
        File file = new File(Environment.getExternalStorageDirectory() + "/Maps/");
        if (!file.exists()) {
            file.mkdirs();
        }

        request.setDescription("TestTask");
        request.setTitle("Downloading " + regionName);
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir("/Maps/",
                    regionName.replace(" ", "") + ".zip");

        File fileMap = new File(Environment.getExternalStorageDirectory() + "/Maps/",
                regionName.replace(" ", "") + ".zip");

        if (fileMap.exists()) {
            fileMap.delete();
        }

        final long downloadId = downloadManager.enqueue(request);

        boolean downloading = true;

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);

        Intent intentToSend = new Intent("download.initialize");
        intentToSend.putExtra("regionName", regionName);


        manager.sendBroadcast(intentToSend);


        while (downloading) {

            DownloadManager.Query q = new DownloadManager.Query();
            q.setFilterById(downloadId);

            Cursor cursor = downloadManager.query(q);
            cursor.moveToFirst();
            int downloaded = cursor.getInt(cursor
                    .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            int total = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));

            if (cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)) == DownloadManager.STATUS_SUCCESSFUL) {
                downloading = false;
            }

            final int progress = (int) ((downloaded * 100l) / total);

            Intent intentUpdate = new Intent("update.data");
            intentUpdate.putExtra("downloaded", downloaded);
            intentUpdate.putExtra("total", total);
            intentUpdate.putExtra("progress", progress);
            intentUpdate.putExtra("regionName", regionName);

            manager.sendBroadcast(intentUpdate);

            cursor.close();
        }

    }





//        Log.e("DownloadService", "Broadcast sent");
//        try {
//            URL url = new URL(urlToDownload);
//            URLConnection connection = url.openConnection();
//            connection.connect();
//
//            int fileLength = connection.getContentLength();
//
//            // download the file
//            input = new BufferedInputStream(connection.getInputStream());
//            File file = new File(Environment.getExternalStorageDirectory() + "/Maps/");
//            if (!file.exists()) {
//                file.mkdirs();
//            }
//            output = new FileOutputStream(Environment.getExternalStorageDirectory() + "/Maps/" +
//                    regionName.replace(" ", "") + ".zip");
//
//            byte data[] = new byte[1024];
//            long total = 0;
//            int count;
//            while ((count = input.read(data)) != -1 && !cancelled) {
//                total += count;
//                // publishing the progress....
//                Bundle resultData = new Bundle();
//                resultData.putInt("fileLengthMb", (int)(fileLength/MEGABYTE));
//                resultData.putLong("downloadedInMb", (total/MEGABYTE));
//                resultData.putInt("progress" ,(int) (total * 100 / fileLength));
//                resultData.putString("RegionName", regionName);
//                Intent intentResults = new Intent("update.data");
//                intentResults.putExtras(resultData);
//                manager.sendBroadcast(intentResults);
//                output.write(data, 0, count);
//            }
//            output.flush();
//            output.close();
//            input.close();
//            if (cancelled) {
//                sendDestroy();
//                onDestroy();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        if (!cancelled) {
//            Intent intentFinish = new Intent("update.data");
//            intentFinish.putExtra("progress", 100);
//            manager.sendBroadcast(intentFinish);
//        }



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
