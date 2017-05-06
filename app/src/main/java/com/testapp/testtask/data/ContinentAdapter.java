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

    private static List<Territory> mData;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
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

            mConstraintLayout.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(view.getId() == mConstraintLayout.getId()) {
                Territory ter = mData.get(this.getAdapterPosition());
                Intent intent = new Intent(mConstraintLayout.getContext(), CountriesActivity.class);
                intent.putExtra("Continent", ter);
                mConstraintLayout.getContext().startActivity(intent);
            }

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
    public void onBindViewHolder(ContinentAdapter.ViewHolder holder, int position) {
        holder.mTextView.setText(mData.get(position).getName());
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
