package com.testapp.testtask;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
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

import com.testapp.testtask.data.ContinentAdapter;
import com.testapp.testtask.data.CountriesFragment;
import com.testapp.testtask.data.DownloadFragment;
import com.testapp.testtask.data.FreeSpaceData;
import com.testapp.testtask.data.Territory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static android.R.attr.width;

public class MainActivity extends AppCompatActivity {

    private static final long  MEGABYTE = 1024L * 1024L;

    private static final String TAG = "MainActivity";
    private static final String BASE_URL = "http://download.osmand.net/download.php?standard=yes&file=";
    private static final  String URL_END = "_2.obf.zip";
    private static final int REQUEST_CODE = 300;
    private List<Territory> allRegions;
    private Stack<String> activityName;

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

    private BroadcastReceiver mResultsReceiver;

    private CountriesFragment countriesFrag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();

        allRegions = new ArrayList<>();
        activityName = new Stack<>();
        // Initialize the free space fragment.
        FreeSpaceData freeSpaceData = new FreeSpaceData();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.top_container, freeSpaceData)
                .commit();
        fragmentManager.executePendingTransactions();
        // Fetch the xml countries data asynchronously.
        loadCountries.execute();

        mResultsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("download.initialize")) {
                    showDownloadingScreen(intent.getStringExtra("regionName"));
                }
                else if (intent.getAction().equals("update.data")) {
                    int progress = intent.getIntExtra("progress", -1);
                    int total = intent.getIntExtra("total", -1);
                    int downloaded = intent.getIntExtra("downloaded", -1);
                    String regionName = intent.getStringExtra("regionName");
                    updateProgress(progress, downloaded, total, regionName);
                }
            }
        };

        registerReceivers();
    }

    private void registerReceivers() {
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.registerReceiver(mResultsReceiver, new IntentFilter("download.initialize"));
        manager.registerReceiver(mResultsReceiver, new IntentFilter("update.data"));
    }

    /**
     * Asks for storage permission.
     */
    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission_group.STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        }

    }

    // asyncTask to fetch countries' names and load them into Territory.
    public AsyncTask<Void, Void, List<Territory>> loadCountries = new AsyncTask<Void, Void, List<Territory>>() {
        @Override
        protected List<Territory> doInBackground(Void... voids) {
            DocumentBuilderFactory factory =
                    DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = null;
            try {
                builder = factory.newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        "Unable to create xml-parser", Toast.LENGTH_SHORT).show();
            }
            InputStream is = getApplicationContext().getResources().openRawResource(R.raw.regions);
            if (builder != null) {

                try {
                    Document document = builder.parse(is);
                    Element root = document.getDocumentElement();
                    Log.d(TAG, "root: " + root.getTagName());
                    NodeList regions = root.getChildNodes();
                    allRegions = parseNodes(regions);
                    Log.e(TAG, String.valueOf(allRegions.size()));



                } catch (SAXException | IOException e) {
                    Toast.makeText(getApplicationContext(), "Could not parse XML", Toast.LENGTH_SHORT)
                            .show();
                    e.printStackTrace();
                }

        }

            return allRegions;
        }

        protected void onPostExecute(List<Territory> result) {

            CountriesFragment countriesFragment = new CountriesFragment();
            Bundle bundle = new Bundle();
            Territory ter = new Territory("World Regions", result);
            bundle.putSerializable("Continent", ter);
            countriesFragment.setArguments(bundle);

            activityName.add("World Regions");
            setTitle("World Regions");

            getSupportFragmentManager().beginTransaction().add(R.id.bottom_container, countriesFragment)
                    .addToBackStack(null).commitAllowingStateLoss();
            getSupportFragmentManager().executePendingTransactions();
        }


    };

    String innerDownloadSuffix = "";
    String innerDownloadPrefix = "";

    private List<Territory> parseNodes(NodeList regions) {
        int length = regions.getLength();
        List<Territory> allNodes = new ArrayList<>();
        if(length > 0) {

            for (int i = 0; i < length; i++) {
                Node node = regions.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String name = element.getAttribute("name");
                    if (!element.getAttribute("inner_download_suffix").isEmpty()) {
                        innerDownloadSuffix = element.getAttribute("inner_download_suffix");
                    }
                    if (!element.getAttribute("inner_download_prefix").isEmpty()) {
                        innerDownloadPrefix = element.getAttribute("inner_download_prefix");
                    }

                    if(!element.hasChildNodes() && !name.isEmpty()) {
                        Territory ter = new Territory(name, new ArrayList<Territory>());
                        String nameVar = element.getAttribute("name");
                        String cap = nameVar.substring(0, 1).toUpperCase() + nameVar.substring(1);
                        if (!innerDownloadPrefix.isEmpty()) {

                            if (innerDownloadPrefix.equals("$name")) {
                                Element parent = (Element) element.getParentNode();
                                if (!parent.getAttribute("inner_download_prefix").isEmpty()) {
                                    innerDownloadPrefix = parent.getAttribute("name");
                                }
                                else {
                                    Element parentParent = (Element) parent.getParentNode();
                                    innerDownloadPrefix = parent.getAttribute("inner_download_prefix");
                                }

                            }


                            String resultUrl = BASE_URL+innerDownloadPrefix+"_"+cap+
                                    "_"+innerDownloadSuffix+URL_END;
                            ter.setUrl(resultUrl);
                            Log.d(TAG, resultUrl);
                        }
                        else {
                            String result = BASE_URL+cap+"_"+innerDownloadSuffix+URL_END;
                            Log.d(TAG, result);
                            ter.setUrl(result);
                        }

                        allNodes.add(ter);
                    }
                    else if (!name.isEmpty()) {
                        Territory ter = new Territory(name, parseNodes(element.getChildNodes()));
                        allNodes.add(ter);
                    }
                }
            }
        }
        Collections.sort(allNodes);
        return allNodes;
    }

    public void initDownloadScreen(String regionName) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        DownloadFragment downloadinFragment = new DownloadFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.top_container, downloadinFragment)
                .commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();


        DownloadFragment currDownloadFragment = (DownloadFragment) fragmentManager
                .findFragmentById(R.id.downloading_screen);

        mPercentage = currDownloadFragment.percentage;
        mDownloaded = currDownloadFragment.downloaded;
        mLeftToLoad = currDownloadFragment.leftToLoad;
        mDownloadLayout = currDownloadFragment.mLayout;
        TextView label = currDownloadFragment.label;
        String labelText = "Downloading " + regionName;
        label.setText(labelText);
    }

    public void showDownloadingScreen(String regionName) {

        Log.e(TAG, regionName);
        initDownloadScreen(regionName);
        populateRecyclerView(regionName);

        // Creates custom dialog window with the info about the download.
        mProgressDialog = new Dialog(mDownloadLayout.getContext());
        mProgressDialog.setContentView(R.layout.my_progressdialog);
        // Get the references to Views in progress dialog
        mProgressInMb = (TextView) mProgressDialog.findViewById(R.id.textView_progress_in_mb);
        mDialogCancel = (ImageView) mProgressDialog.findViewById(R.id.imageView_remove_download);
        mDialogProgressBar = (ProgressBar) mProgressDialog.findViewById(R.id.progressBar);
        dismissButton = (Button) mProgressDialog.findViewById(R.id.button_cancel);
        mDialogCancel.setColorFilter(R.color.colorIcons);

        TextView cityLabel = (TextView) mProgressDialog.findViewById(R.id.city_label);
        cityLabel.setText(regionName);
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

            }
        });
        // Set listener on the whole downloading layout to show the dialog.
        mDownloadLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressDialog.show();
            }
        });

        // Convert margin into dp
        Resources resources = this.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = 48 * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        // width - margin
        int width = (int) (getDisplayWidth() - px);
    }

    public void populateRecyclerView(String name) {
        countriesFrag = (CountriesFragment)
                getSupportFragmentManager().findFragmentById(R.id.countries_list);

        RecyclerView layoutManager = countriesFrag.mRecyclerView;

        for (int i = 0; i < layoutManager.getChildCount(); i++) {
            ConstraintLayout constraintLayout = (ConstraintLayout) layoutManager.getChildAt(i);
            TextView countryName;
            if (constraintLayout.findViewById(R.id.textView_country_name) != null) {
                countryName = (TextView) constraintLayout.findViewById(R.id.textView_country_name);
                if (countryName.getText().toString().equals(name)) {
                    mProgressBar = (ProgressBar) constraintLayout.findViewById(R.id.download_progressbar);
                    mCancelButton = (ImageView) constraintLayout.findViewById(R.id.imageView_cancel_icon);
                    mDownloadButton = (ImageView) constraintLayout.findViewById(R.id.imageView_download_icon);
                    mMapIcon = (ImageView) constraintLayout.findViewById(R.id.imageView_map_icon);

                    mDownloadButton.setVisibility(View.GONE);

                    mCancelButton.setVisibility(View.VISIBLE);
                    mCancelButton.setColorFilter(R.color.colorIcons);

                    mCancelButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });
                    // Show progress bar underneath the region title.
                    mProgressBar.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public void updateProgress(int progress, int downloaded, int total, String regionName) {
        String update = String.valueOf(progress) +"%";
                    // Downloaded megabytes.
                    String dialogMagabytes = String.valueOf(downloaded / MEGABYTE)
                            + " Mb of " + String.valueOf(total / MEGABYTE) + "Mb";
                    populateRecyclerView(regionName);
                    // Update the views.
                    if (mProgressInMb == null) {
                        showDownloadingScreen(regionName);
                    }
                    mProgressInMb.setText(dialogMagabytes);
                    if (mProgressBar != null) {
                        mProgressBar.setProgress(progress);
                    }

                    mDialogProgressBar.setProgress(progress);
                    mPercentage.setText(update);
                    mDownloaded.getLayoutParams().width = (int) (width*((float)progress/(float)100));
                    mLeftToLoad.getLayoutParams().width = (int) (width - (width*(float)progress/(float)100));

                    if (progress == 100) {
                        Toast.makeText(this, "Download finished", Toast.LENGTH_SHORT).show();
                        mDownloadLayout.setVisibility(View.GONE);

                        mProgressDialog.dismiss();
                        if (mProgressBar != null && mMapIcon != null && mDownloadButton != null
                                && mCancelButton != null) {
                            mCancelButton.setVisibility(View.GONE);
                            mProgressBar.setVisibility(View.GONE);
                            // Make the icon green.
                            mMapIcon.getDrawable()
                                    .setColorFilter(this.getResources()
                                                    .getColor(R.color.colorIconDownloaded),
                                            PorterDuff.Mode.SRC_IN);
                            mDownloadButton.setVisibility(View.GONE);
                        }
                    }
    }

    public void addActivityName(String newName) {
        activityName.add(newName);
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
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        }
        else {
            super.onBackPressed();
            activityName.pop();
            setTitle(activityName.peek());
            if(getSupportFragmentManager().getBackStackEntryCount() == 1) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(false);

            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mResultsReceiver);
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


}
