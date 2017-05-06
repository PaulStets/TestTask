package com.testapp.testtask.data;

import android.content.Context;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.testapp.testtask.R;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;



/**
 * Created by paul on 05.05.17.
 */

public class DownloadFragment extends Fragment {

    private int width;
    private TextView percentage;
    private ImageView downloaded;
    private ImageView leftToLoad;
    private ConstraintLayout mLayout;

    public DownloadFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.downloading_fragment, container, false);

        rootView.setVisibility(View.VISIBLE);

        percentage = (TextView) rootView.findViewById(R.id.textView_percentage_complete);
        downloaded = (ImageView) rootView.findViewById(R.id.downloaded);
        leftToLoad = (ImageView) rootView.findViewById(R.id.left_to_download);
        mLayout = (ConstraintLayout) rootView;

        leftToLoad.getLayoutParams().width = width;
        downloaded.getLayoutParams().width = 0;

        mDownloadingTask.execute("http://download.osmand.net/download.php?standard=yes&file=Denmark_europe_2.obf.zip");

        return rootView;


    }

    AsyncTask<String, Integer, Void> mDownloadingTask = new AsyncTask<String, Integer, Void>() {
        private PowerManager.WakeLock mWakeLock;



        @Override
        protected Void doInBackground(String... urls) {
            Log.e("Downloading", "Started Downloading of the map");
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
                int count;

                // input stream to read file - with 8k buffer
                try {
                    InputStream input = new BufferedInputStream(url.openStream(), 8192);
                    OutputStream output = new FileOutputStream(root+"/Berlin.zip");
                    byte data[] = new byte[1024];

                    long total = 0;
                    while ((count = input.read(data)) != -1) {
                        total += count;

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
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) getActivity().getApplicationContext().getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();

            WindowManager wm = (WindowManager) getActivity().getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            Point size = new Point();
            display.getSize(size);
            width = size.x;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);

            percentage.setText(String.valueOf(progress[0]));
            downloaded.getLayoutParams().width = (int) (width*((float)progress[0]/(float)100));
            leftToLoad.getLayoutParams().width = (int) (width - (width*(float)progress[0]/(float)100));

        }

        @Override
        protected void onPostExecute(Void v) {
            mWakeLock.release();
            Toast.makeText(getActivity().getApplicationContext(), "Download finished", Toast.LENGTH_SHORT).show();
            mLayout.setVisibility(View.GONE);
        }
    };

}
