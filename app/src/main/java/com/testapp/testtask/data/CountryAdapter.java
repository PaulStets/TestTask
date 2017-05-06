package com.testapp.testtask.data;

import android.app.Activity;
import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
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
 * Created by paul on 05.05.17.
 */

public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.ViewHolder> {

    private static List<Territory> mData;

    private Activity mActivity;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        public TextView mTextView;
        public ImageView mDownloadImage;
        public ImageView mMapIcon;
        public ConstraintLayout mConstraintLayout;
        public ViewHolder(ConstraintLayout v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.textView_country_name);
            mDownloadImage = (ImageView) v.findViewById(R.id.imageView_download_icon);
            mMapIcon = (ImageView) v.findViewById(R.id.imageView_map_icon);
            mDownloadImage.setColorFilter(R.color.colorIcons);
            mMapIcon.setColorFilter(R.color.colorIcons);
            mConstraintLayout = v;

            mConstraintLayout.setOnClickListener(this);
            mDownloadImage.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if(view.getId() == mConstraintLayout.getId()) {
                Territory ter = mData.get(this.getAdapterPosition());
                if(ter.hasChilden()) {
                    Intent intent = new Intent("start.regions.action");
                    intent.putExtra("Continent", ter);
                    mConstraintLayout.getContext().sendBroadcast(intent);
                }

            }
            else if (view.getId() == mDownloadImage.getId() && mTextView.getText().toString().equals("Berlin ")) {

            }

        }
    }

    public CountryAdapter(List<Territory> data, Activity activity) {
        mData = data;
        mActivity = activity;

    }

    @Override
    public CountryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ConstraintLayout layout = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.country_viewholder, parent, false);

        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(final CountryAdapter.ViewHolder holder, int position) {
        holder.mTextView.setText(mData.get(position).getName());
        if (mData.get(position).hasChilden()) {
            holder.mDownloadImage.setVisibility(View.INVISIBLE);
        }
        if(holder.mTextView.getText().toString().equals("Berlin ")) {
            holder.mDownloadImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.mConstraintLayout.getContext().sendBroadcast(new Intent("start.download.action"));


                }
            });
        }


    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
