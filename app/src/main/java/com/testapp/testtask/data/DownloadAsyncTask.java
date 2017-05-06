package com.testapp.testtask.data;

import android.content.Context;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
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

import static com.testapp.testtask.CountriesActivity.mConstraintLayout;
import static com.testapp.testtask.CountriesActivity.mProgressDialog;

/**
 * Created by paul on 05.05.17.
 */


