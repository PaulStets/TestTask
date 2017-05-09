package com.testapp.testtask.data;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.testapp.testtask.R;

/**
 * Created by paul on 05.05.17.
 */

public class CountriesFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    public LinearLayoutManager mLayoutManager;
    private  static final String TAG = "CountriesFragment";



    public CountriesFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Territory mTerritory;
        if (getArguments() != null) {
            mTerritory = (Territory) getArguments().getSerializable("Continent");
            Log.e(TAG, "Creating from arguments");
        }
        else {
            mTerritory = (Territory) getActivity().getIntent().getSerializableExtra("Continent");
            Log.e(TAG, "Creating from intent");
            Log.e(TAG, "Number of children: " + String.valueOf(mTerritory.getRegions().size()));
        }

        View rootView = inflater.inflate(R.layout.countries_fragment, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_country_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(rootView.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);


        mAdapter = new CountryAdapter(mTerritory.getRegions());
        mRecyclerView.setAdapter(mAdapter);
        Log.e(TAG, "Adapter is set");


        return rootView;
    }



}
