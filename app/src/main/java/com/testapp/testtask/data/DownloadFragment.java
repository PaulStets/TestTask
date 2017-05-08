package com.testapp.testtask.data;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
    public TextView percentage;
    private TextView label;
    public ImageView downloaded;
    public ImageView leftToLoad;
    public ConstraintLayout mLayout;



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
        label = (TextView) rootView.findViewById(R.id.textView_downloading_label);

        mLayout = (ConstraintLayout) rootView;



        label.setText("Downloading Berlin");

        percentage.setText("0%");




        return rootView;


    }





}
