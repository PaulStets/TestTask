package com.testapp.testtask.data;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
    private RecyclerView.LayoutManager mLayoutManager;

    private Territory mTerritory;

    public CountriesFragment() {}


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (getArguments() != null) {
            mTerritory = (Territory) getArguments().getSerializable("Continent");
        }
        else {
            mTerritory = (Territory) getActivity().getIntent().getSerializableExtra("Continent");
        }

        View rootView = inflater.inflate(R.layout.countries_fragment, container, false);

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.my_country_recycler_view);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(rootView.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);



        mAdapter = new CountryAdapter(mTerritory.getRegions(), getActivity());
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        mAdapter = new CountryAdapter(mTerritory.getRegions(), getActivity());
        mRecyclerView.setAdapter(mAdapter);
    }
}
