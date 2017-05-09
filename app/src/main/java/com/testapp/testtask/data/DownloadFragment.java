package com.testapp.testtask.data;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.testapp.testtask.R;



/**
 * Created by paul on 05.05.17.
 */

public class DownloadFragment extends Fragment {

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
