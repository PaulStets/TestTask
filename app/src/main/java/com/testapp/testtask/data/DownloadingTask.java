package com.testapp.testtask.data;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
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


/**
 * Async task that deals with downloading of the file and publishing
 * progress in UI.
 */

public class DownloadingTask extends AsyncTask<String, Integer, Void> {
    private PowerManager.WakeLock mWakeLock;
    // Variables to update the UI.
    private Context mContext;
    private Dialog mProgressDialog;
    private TextView percentage;
    private ImageView downloaded;
    private ImageView leftToLoad;
    private ConstraintLayout mLayout;
    private ProgressBar mProgressBar;
    private ImageView mCancelButton;
    private ImageView mDownloadButton;
    private TextView mProgressInMb;
    private ProgressBar mDialogProgressBar;
    private Button dismissButton;
    private ImageView mDialogCancel;
    private ImageView mMapIcon;

    private int width;
    private int fileSizeInMb;
    private int downloadedInMb;

    private DownloadingTask thisTask;



    private static final long  MEGABYTE = 1024L * 1024L;

    public DownloadingTask(Context context, TextView percentage, ImageView downloaded,
                    ImageView leftToLoad,
                    ConstraintLayout downloadLayout,
                    ProgressBar progressBar, ImageView cancelButton,
                           ImageView downloadButton, ImageView mapIcon) {
        mContext = context;
        this.percentage = percentage;
        this.downloaded = downloaded;
        this.leftToLoad = leftToLoad;
        mLayout = downloadLayout;
        mProgressBar = progressBar;
        mCancelButton = cancelButton;
        mDownloadButton = downloadButton;
        mMapIcon = mapIcon;
        thisTask = this;
    }



    @Override
    protected Void doInBackground(String... urls) {

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
                thisTask.cancel(true);
                Toast.makeText(mContext, "Please check your Internet connection.", Toast.LENGTH_SHORT)
                        .show();
            }

        }
        return null;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // Convert margin into dp
        Resources resources = mContext.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = 48 * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        // width - margin
        width = (int) (getDisplayWidth() - px);

        mDownloadButton.setVisibility(View.GONE);

        mCancelButton.setVisibility(View.VISIBLE);
        mCancelButton.setColorFilter(R.color.colorIcons);


        // Creates custom dialog window with the info about the download.
        mProgressDialog = new Dialog(mLayout.getContext());
        mProgressDialog.setContentView(R.layout.my_progressdialog);
        // Get the references to Views in progress dialog
        mProgressInMb = (TextView) mProgressDialog.findViewById(R.id.textView_progress_in_mb);
        mDialogCancel = (ImageView) mProgressDialog.findViewById(R.id.imageView_remove_download);
        mDialogProgressBar = (ProgressBar) mProgressDialog.findViewById(R.id.progressBar);
        dismissButton = (Button) mProgressDialog.findViewById(R.id.button_cancel);
        mDialogCancel.setColorFilter(R.color.colorIcons);
        // Set the width of progress dialog to fill the screen.
        mProgressDialog.getWindow().setLayout(ConstraintLayout.LayoutParams.MATCH_PARENT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT);
        // Set listener for "CANCEL" button.
        dismissButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialog.dismiss();
            }
        });
        // Set listener for X button.
        mDialogCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thisTask.onCancelled();
            }
        });
        // Set listener on the whole downloading layout to show the dialog.
        mLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialog.show();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                thisTask.onCancelled();
            }
        });
        // Show progress bar underneath the region title.
        mProgressBar.setVisibility(View.VISIBLE);

        // CPU lock prevents CPU from going off if the user
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


    }

    @Override
    protected void onPostExecute(Void v) {
        mWakeLock.release();
        Toast.makeText(mContext, "Download finished", Toast.LENGTH_SHORT).show();
        mLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mCancelButton.setVisibility(View.GONE);
        mProgressDialog.dismiss();
        // Make the icon green.
        mMapIcon.getDrawable()
                .setColorFilter(mContext.getResources()
                        .getColor(R.color.colorIconDownloaded),
                        PorterDuff.Mode.SRC_IN);
        mDownloadButton.setVisibility(View.GONE);
    }

    @Override
    protected void onCancelled() {
        if (mWakeLock.isHeld()) {
            mWakeLock.release();
        }

        mLayout.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mCancelButton.setVisibility(View.GONE);
        mProgressDialog.dismiss();
        mDownloadButton.setVisibility(View.VISIBLE);
        Log.e("DownloadingTask", "Cancelling download in onCancelled");
        // Delete downloaded info, if exists.
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "Berlin.zip");
        if (file.exists()) {
            file.delete();
        }
        thisTask.cancel(true);
    }




    /**
     * Determines the width of a given display.
     * @return the width of the display in px.
     */
    public int getDisplayWidth() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }
}
