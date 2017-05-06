package com.testapp.testtask;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.Toast;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import com.testapp.testtask.data.ContinentAdapter;
import com.testapp.testtask.data.FreeSpaceData;
import com.testapp.testtask.data.Territory;

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

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);



        allRegions = new ArrayList<>();

        FreeSpaceData freeSpaceData = new FreeSpaceData();
        FragmentManager fragmentManager = getSupportFragmentManager();

        fragmentManager.beginTransaction()
                .add(R.id.free_space_container, freeSpaceData)
                .commit();
    loadCountries.execute();

    }

    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission_group.STORAGE) !=
                    PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        }

    }

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
                        Territory ter = new Territory(name, new ArrayList<Territory>(),
                                false);
                        allNodes.add(ter);
                    }
                    else if (!name.isEmpty()) {
                        Territory ter = new Territory(name, parseNodes(element.getChildNodes()),
                                true);
                        allNodes.add(ter);
                    }
                }
            }
        }
        return allNodes;
    }


}
