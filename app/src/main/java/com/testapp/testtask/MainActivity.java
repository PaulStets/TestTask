package com.testapp.testtask;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

import com.testapp.testtask.data.ContinentAdapter;
import com.testapp.testtask.data.FreeSpaceData;
import com.testapp.testtask.data.Territory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import static java.util.Collections.sort;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int REQUEST_CODE = 300;
    private List<Territory> allRegions;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();

        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new ContinentAdapter(new ArrayList<Territory>());
        mRecyclerView.setAdapter(mAdapter);

        allRegions = new ArrayList<>();
        // Initialize the free space fragment.
        FreeSpaceData freeSpaceData = new FreeSpaceData();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(R.id.free_space_container, freeSpaceData)
                .commit();
        fragmentManager.executePendingTransactions();
        // Fetch the xml countries data asynchronously.
        loadCountries.execute();
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


            mAdapter = new ContinentAdapter(result);
            mAdapter.notifyItemRangeChanged(0, mAdapter.getItemCount());

            mRecyclerView.setAdapter(mAdapter);



        }


    };

    private List<Territory> parseNodes(NodeList regions) {
        int length = regions.getLength();
        List<Territory> allNodes = new ArrayList<>();
        if(length > 0) {
            for (int i = 0; i < length; i++) {
                Node node = regions.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String name = element.getAttribute("name");
                    if(!element.hasChildNodes() && !name.isEmpty()) {
                        Territory ter = new Territory(name, new ArrayList<Territory>());
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


}
