package com.testapp.testtask.data;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.testapp.testtask.CountriesActivity;
import com.testapp.testtask.R;

import java.util.List;

/**
 * Created by paul on 04.05.17.
 */

public class ContinentAdapter extends RecyclerView.Adapter<ContinentAdapter.ViewHolder> {

    private List<Territory> mData;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public ConstraintLayout mConstraintLayout;
        public ImageView mGlobeIcon;
        public ViewHolder(ConstraintLayout v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.textView_continent_name);
            mConstraintLayout = v;
            mGlobeIcon = (ImageView) v.findViewById(R.id.imageView_globe);
            mGlobeIcon.setColorFilter(R.color.colorIcons);

        }


    }

    public ContinentAdapter(List<Territory> data) {
        mData = data;
    }

    @Override
    public ContinentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ConstraintLayout layout = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.continent_viewholder, parent, false);

        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(final ContinentAdapter.ViewHolder holder, final int position) {
        holder.mTextView.setText(mData.get(position).getName());

        holder.mConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Territory ter = mData.get(position);
                Intent intent = new Intent(holder.mConstraintLayout.getContext(), CountriesActivity.class);
                intent.putExtra("Continent", ter);
                holder.mConstraintLayout.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
