package com.testapp.testtask;

import android.app.ActivityManager;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.testapp.testtask.data.CountriesFragment;
import com.testapp.testtask.data.DownloadFragment;
import com.testapp.testtask.data.DownloadingService;
import com.testapp.testtask.data.DownloadingTask;
import com.testapp.testtask.data.Territory;

import java.util.Stack;

import static android.R.attr.width;
import static com.testapp.testtask.R.id.downloaded;
import static com.testapp.testtask.data.DownloadingService.CANCEL_DOWNLOAD;
import static com.testapp.testtask.data.DownloadingService.START_DOWNLOAD;
import static com.testapp.testtask.data.DownloadingService.cancelled;


public class CountriesActivity extends AppCompatActivity {


    private static final String TAG = "CountriesActivity";
    private Territory mTerritory;
    private Stack<String> activityName;
    private Context mContext;

    private Dialog mProgressDialog;
    private TextView mPercentage;
    private ImageView mDownloaded;
    private ImageView mLeftToLoad;
    private ConstraintLayout mDownloadLayout;
    private ProgressBar mProgressBar;
    private ImageView mCancelButton;
    private ImageView mDownloadButton;
    private TextView mProgressInMb;
    private ProgressBar mDialogProgressBar;
    private Button dismissButton;
    private ImageView mDialogCancel;
    private ImageView mMapIcon;

    private int holderPosition;
    private BroadcastReceiver mResultsReceiver;

    private CountriesFragment countriesFrag;

    private int width;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countries);
        mTerritory = (Territory) getIntent().getSerializableExtra("Continent");
        mContext = this;
        activityName = new Stack<>();
        activityName.add(mTerritory.getName());
        setTitle(activityName.peek());

        CountriesFragment countriesFragment = new CountriesFragment();

        final FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction().add(R.id.countries_list, countriesFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
        countriesFrag = (CountriesFragment) fragmentManager.findFragmentById(R.id.countries_list);

        mResultsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("download.initialize")) {
                    showDownloadingScreen(intent);
                }
                else if (intent.getAction().equals("update.data")) {
                    int progress = intent.getIntExtra("progress", -1);
                    // Percentage of completion.
                    String update = String.valueOf(progress) +"%";
                    // Downloaded megabytes.
                    String dialogMagabytes = String.valueOf(intent.getLongExtra("downloadedInMb", -1))
                            + " Mb of " + String.valueOf(intent.getIntExtra("fileLengthMb", -1)) + "Mb";

                    // Update the views.
                    if (mProgressInMb == null) {
                        showDownloadingScreen(intent);
                    }
                    mProgressInMb.setText(dialogMagabytes);
                    mProgressBar.setProgress(progress);
                    mDialogProgressBar.setProgress(progress);
                    mPercentage.setText(update);
                    mDownloaded.getLayoutParams().width = (int) (width*((float)progress/(float)100));
                    mLeftToLoad.getLayoutParams().width = (int) (width - (width*(float)progress/(float)100));

                    if (intent.getIntExtra("progress", -1) == 100) {
                        Toast.makeText(context, "Download finished", Toast.LENGTH_SHORT).show();
                        mDownloadLayout.setVisibility(View.GONE);
                        mProgressBar.setVisibility(View.GONE);
                        mCancelButton.setVisibility(View.GONE);
                        mProgressDialog.dismiss();
                        // Make the icon green.
                        mMapIcon.getDrawable()
                                .setColorFilter(context.getResources()
                                                .getColor(R.color.colorIconDownloaded),
                                        PorterDuff.Mode.SRC_IN);
                        mDownloadButton.setVisibility(View.GONE);
                    }

                }
                else if (intent.getAction().equals("download.cancel")) {
                    mDownloadLayout.setVisibility(View.GONE);
                    mProgressBar.setVisibility(View.GONE);
                    mCancelButton.setVisibility(View.GONE);
                    mProgressDialog.dismiss();
                    mDownloadButton.setVisibility(View.VISIBLE);
                    Log.e("DownloadingTask", "Cancelling download in onCancelled");
                    cancelled = false;
                }
            }
        };
//        if (isMyServiceRunning(DownloadingService.class)) {
//            Intent intent = new Intent("download.initialize");
//            intent.putExtra("holderPos", holderPosition);
//            showDownloadingScreen(intent);
//        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().getBackStackEntryCount() == 1) {
            finish();

        }
        else {
            super.onBackPressed();
            activityName.pop();
            setTitle(activityName.peek());
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        Log.e(TAG, "OnPause");

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mResultsReceiver);
    }

    @Override
    public void onResume() {
        super.onResume();
        registerReceivers();
        Log.e(TAG, "OnResume");

    }

    private void registerReceivers() {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mResultsReceiver, new IntentFilter("download.initialize"));
        manager.registerReceiver(mResultsReceiver, new IntentFilter("download.cancel"));
        manager.registerReceiver(mResultsReceiver, new IntentFilter("update.data"));
    }


    public void addActivityName(String newName) {
        activityName.add(newName);
    }

    public void initUiComponents(int holderPos) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DownloadFragment downloadinFragment = new DownloadFragment();
        getSupportFragmentManager().beginTransaction().add(R.id.downloading_screen, downloadinFragment)
                .commitAllowingStateLoss();
        getSupportFragmentManager().executePendingTransactions();


        countriesFrag = (CountriesFragment)
                fragmentManager.findFragmentById(R.id.countries_list);

        LinearLayoutManager layoutManager = countriesFrag.mLayoutManager;

        ConstraintLayout constraintLayout = (ConstraintLayout) layoutManager.findViewByPosition(
                holderPos);
        mProgressBar = (ProgressBar) constraintLayout.findViewById(R.id.download_progressbar);
        mCancelButton = (ImageView) constraintLayout.findViewById(R.id.imageView_cancel_icon);
        mDownloadButton = (ImageView) constraintLayout.findViewById(R.id.imageView_download_icon);
        mMapIcon = (ImageView) constraintLayout.findViewById(R.id.imageView_map_icon);

        DownloadFragment currDownloadFragment = (DownloadFragment) getSupportFragmentManager()
                .findFragmentById(R.id.downloading_screen);

        mPercentage = currDownloadFragment.percentage;
        mDownloaded = currDownloadFragment.downloaded;
        mLeftToLoad = currDownloadFragment.leftToLoad;
        mDownloadLayout = currDownloadFragment.mLayout;
    }

    public void showDownloadingScreen(Intent intent) {
        holderPosition = intent.getIntExtra("holderPos", -1);
        Log.e(TAG, "Holder Position: " + holderPosition);
        initUiComponents(holderPosition);

        mDownloadButton.setVisibility(View.GONE);

        mCancelButton.setVisibility(View.VISIBLE);
        mCancelButton.setColorFilter(R.color.colorIcons);


        // Creates custom dialog window with the info about the download.
        mProgressDialog = new Dialog(mDownloadLayout.getContext());
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
                cancelled = true;
            }
        });
        // Set listener on the whole downloading layout to show the dialog.
        mDownloadLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialog.show();
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancelled = true;
            }
        });
        // Show progress bar underneath the region title.
        mProgressBar.setVisibility(View.VISIBLE);

        // Convert margin into dp
        Resources resources = this.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = 48 * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        // width - margin
        width = (int) (getDisplayWidth() - px);
    }


    /**
     * Determines the width of a given display.
     * @return the width of the display in px.
     */
    public int getDisplayWidth() {
        WindowManager wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }


}
