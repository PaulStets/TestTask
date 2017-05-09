package com.testapp.testtask.data;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.constraint.ConstraintLayout;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.testapp.testtask.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import static android.R.attr.width;
import static android.os.Build.VERSION_CODES.M;
import static java.security.AccessController.getContext;


/**
 * Created by paul on 08.05.17.
 */

public class DownloadingTask extends AsyncTask<String, Integer, Void> {
    private PowerManager.WakeLock mWakeLock;

    private Context mContext;
    private Dialog mProgressDialog;
    private TextView percentage;
    private ImageView downloaded;
    private ImageView leftToLoad;
    private ConstraintLayout mLayout;
    private ProgressBar mProgressBar;
    private ImageView mCancelButton;
    private ImageView mDownloadButton;
    private int width;

    private static final long  MEGABYTE = 1024L * 1024L;
    private int fileSizeInMb;
    private int downloadedInMb;

    private DownloadingTask thisTask;

    private TextView mProgressInMb;
    private ProgressBar mDialogProgressBar;
    private Button dismissButton;
    private ImageView mDialogCancel;

    public DownloadingTask(Context context, TextView percentage, ImageView downloaded,
                    ImageView leftToLoad,
                    ConstraintLayout downloadLayout,
                    ProgressBar progressBar, ImageView cancelButton, ImageView downloadButton) {
        mContext = context;
        this.percentage = percentage;
        this.downloaded = downloaded;
        this.leftToLoad = leftToLoad;
        mLayout = downloadLayout;
        mProgressBar = progressBar;
        mCancelButton = cancelButton;
        mDownloadButton = downloadButton;
        thisTask = this;
    }



    @Override
    protected Void doInBackground(String... urls) {
        Log.d("Downloading", "Started Downloading of the map");
        String root = Environment.getExternalStorageDirectory().toString();
        URL url = null;
        URLConnection connection = null;
        try {
            url = new URL(urls[0]);
            connection = url.openConnection();
            connection.connect();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(url != null && connection != null) {
            int lengthOfFile = connection.getContentLength();
            fileSizeInMb = (int) (connection.getContentLength() / MEGABYTE);
            int count;

            // input stream to read file - with 8k buffer
            try {
                InputStream input = new BufferedInputStream(url.openStream(), 8192);
                OutputStream output = new FileOutputStream(root+"/Berlin.zip");
                byte data[] = new byte[1024];

                long total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    downloadedInMb += count;
                    downloadedInMb = (int) (total / MEGABYTE);

                    // writing data to file
                    output.write(data, 0, count);

                    // publishing the progress....
                    if (lengthOfFile > 0) // only if total length is known
                        publishProgress((int) (total * 100 / lengthOfFile));


                }

                // flushing output
                output.flush();

                // closing streams
                output.close();
                input.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        Resources resources = mContext.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = 48 * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);

        width = (int) (getDisplayWidth() - px);

        mDownloadButton.setVisibility(View.GONE);

        mCancelButton.setVisibility(View.VISIBLE);
        mCancelButton.setColorFilter(R.color.colorIcons);



        mProgressDialog = new Dialog(mLayout.getContext());
        mProgressDialog.setContentView(R.layout.my_progressdialog);

        mProgressInMb = (TextView) mProgressDialog.findViewById(R.id.textView_progress_in_mb);
        mDialogCancel = (ImageView) mProgressDialog.findViewById(R.id.imageView_remove_download);
        mDialogProgressBar = (ProgressBar) mProgressDialog.findViewById(R.id.progressBar);
        dismissButton = (Button) mProgressDialog.findViewById(R.id.button_cancel);

        mDialogCancel.setColorFilter(R.color.colorIcons);

        mProgressDialog.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);

        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialog.dismiss();
            }
        });

        mDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thisTask.cancel(true);
            }
        });

        mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialog.show();
            }
        });

        mProgressBar.setVisibility(View.VISIBLE);

        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) mContext
                .getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();
    }



    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);

        String update = String.valueOf(progress[0]) +"%";
        String dialogMagabytes = String.valueOf(downloadedInMb)
                + " Mb of " + String.valueOf(fileSizeInMb) + "Mb";

        mProgressInMb.setText(dialogMagabytes);

        mProgressBar.setProgress(progress[0]);
        mDialogProgressBar.setProgress(progress[0]);
        percentage.setText(update);
        downloaded.getLayoutParams().width = (int) (width*((float)progress[0]/(float)100));
        leftToLoad.getLayoutParams().width = (int) (width - (width*(float)progress[0]/(float)100));

    }

    @Override
    protected void onPostExecute(Void v) {
        mWakeLock.release();
        Toast.makeText(mContext, "Download finished", Toast.LENGTH_SHORT).show();
        mLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mCancelButton.setVisibility(View.GONE);
        mProgressDialog.dismiss();

        mDownloadButton.setVisibility(View.VISIBLE);
        mDownloadButton.getDrawable()
                .setColorFilter(mContext.getResources()
                        .getColor(R.color.colorIconDownloaded),
                        PorterDuff.Mode.SRC_IN);
        mDownloadButton.setClickable(false);
    }

    @Override
    protected void onCancelled() {
        mWakeLock.release();
        mLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mCancelButton.setVisibility(View.GONE);
        mProgressDialog.dismiss();
        mDownloadButton.setVisibility(View.VISIBLE);
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Berlin.zip");
        if (file.exists()) {
            file.delete();
        }


        super.onCancelled();


    }




    public int getDisplayWidth() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }
}
