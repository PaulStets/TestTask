package com.testapp.testtask;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.testapp.testtask.data.CountriesFragment;
import com.testapp.testtask.data.DownloadFragment;
import com.testapp.testtask.data.DownloadingTask;
import com.testapp.testtask.data.Territory;

import java.util.Stack;


public class CountriesActivity extends AppCompatActivity {




    private Territory mTerritory;
    private BroadcastReceiver mBroadcastReceiver;
    private Stack<String> activityName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countries);
        mTerritory = (Territory) getIntent().getSerializableExtra("Continent");
        activityName = new Stack<>();
        activityName.add(mTerritory.getName());
        setTitle(activityName.peek());

        CountriesFragment countriesFragment = new CountriesFragment();

        final FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction().add(R.id.countries_list, countriesFragment)
                .addToBackStack(null)
                .commit();

        mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //This piece of code will be executed when you click on your item
                if(intent.getAction().equals("start.download.action")) {
                    DownloadFragment downloadFragment = new DownloadFragment();
                    fragmentManager.beginTransaction()
                            .add(R.id.downloading_screen, downloadFragment)
                            .commitAllowingStateLoss();

                    fragmentManager.executePendingTransactions();


                    CountriesFragment countriesFrag = (CountriesFragment)
                            fragmentManager.findFragmentById(R.id.countries_list);
                    LinearLayoutManager layoutManager = countriesFrag.mLayoutManager;
                    ConstraintLayout constraintLayout = (ConstraintLayout) layoutManager.findViewByPosition(
                            intent.getIntExtra("position", 0));
                    ProgressBar mProgressBar = (ProgressBar) constraintLayout.findViewById(R.id.download_progressbar);
                    ImageView mCancelButton = (ImageView) constraintLayout.findViewById(R.id.imageView_cancel_icon);

                    DownloadFragment currDownloadFragment = (DownloadFragment) getSupportFragmentManager()
                            .findFragmentById(R.id.downloading_screen);

                    TextView mPercentage = currDownloadFragment.percentage;
                    ImageView mDownloaded = currDownloadFragment.downloaded;
                    ImageView mLeftToLoad = currDownloadFragment.leftToLoad;
                    ConstraintLayout mDownloadLayout = currDownloadFragment.mLayout;

                    final DownloadingTask mDownloadingTask = new DownloadingTask(getApplicationContext(),
                            mPercentage,
                            mDownloaded,
                            mLeftToLoad,
                            mDownloadLayout,
                            mProgressBar,
                            mCancelButton);

                    mDownloadingTask.execute("http://download.osmand.net/download.php?standard=yes&file=Denmark_europe_2.obf.zip");

                    mCancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mDownloadingTask.cancel(true);
                        }
                    });


                }
                else {
                    Territory terr = (Territory) intent.getSerializableExtra("Continent");
                    activityName.add(terr.getName());
                    setTitle(activityName.peek());
                    CountriesFragment countriesFragment1 = new CountriesFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("Continent", intent.getSerializableExtra("Continent"));
                    countriesFragment1.setArguments(bundle);
                    fragmentManager.beginTransaction()
                            .replace(R.id.countries_list, countriesFragment1)
                            .addToBackStack(null).commitAllowingStateLoss();

                    fragmentManager.executePendingTransactions();

                }

            }
        };


        registerBroadcastReceiver();

//        mConstraintLayout = (ConstraintLayout) findViewById(R.id.download_screen);
//
//
//
//        });


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }

    private void registerBroadcastReceiver() {
        this.registerReceiver(mBroadcastReceiver, new IntentFilter("start.download.action"));
        this.registerReceiver(mBroadcastReceiver, new IntentFilter("start.regions.action"));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                onBackPressed();

                return true;

        }
        return super.onOptionsItemSelected(item);
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
        if (mBroadcastReceiver != null) {
            unregisterReceiver(mBroadcastReceiver);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        registerBroadcastReceiver();
    }





}
