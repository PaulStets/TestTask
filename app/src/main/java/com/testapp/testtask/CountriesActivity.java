package com.testapp.testtask;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.testapp.testtask.data.ContinentAdapter;
import com.testapp.testtask.data.CountriesFragment;
import com.testapp.testtask.data.CountryAdapter;
import com.testapp.testtask.data.DownloadFragment;
import com.testapp.testtask.data.Territory;

import java.util.ArrayList;
import java.util.List;


public class CountriesActivity extends AppCompatActivity {




    private Territory mTerritory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countries);
        mTerritory = (Territory) getIntent().getSerializableExtra("Continent");
        setTitle(mTerritory.getName());

        CountriesFragment countriesFragment = new CountriesFragment();

        final FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction().add(R.id.countries_list, countriesFragment)
                .addToBackStack(null)
                .commit();

        BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //This piece of code will be executed when you click on your item
                if(intent.getAction().equals("start.download.action")) {
                    DownloadFragment downloadFragment = new DownloadFragment();
                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.downloading_screen, downloadFragment)
                            .commitAllowingStateLoss();
                }
                else {
                    Territory terr = (Territory) intent.getSerializableExtra("Continent");
                    setTitle(terr.getName());
                    CountriesFragment countriesFragment1 = new CountriesFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("Continent", intent.getSerializableExtra("Continent"));
                    countriesFragment1.setArguments(bundle);
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.countries_list, countriesFragment1)
                            .addToBackStack(null).commitAllowingStateLoss();
                }

            }
        };

        this.registerReceiver(mBroadcastReceiver, new IntentFilter("start.download.action"));
        this.registerReceiver(mBroadcastReceiver, new IntentFilter("start.regions.action"));



//        mConstraintLayout = (ConstraintLayout) findViewById(R.id.download_screen);
//
//
//        mProgressDialog = new ProgressDialog(this);
//        mProgressDialog.setMessage("Download Started");
//        mProgressDialog.setIndeterminate(true);
//        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
//        mProgressDialog.setCancelable(true);
//        mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                dialog.dismiss();
//
//            }
//        });


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                getFragmentManager().popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
