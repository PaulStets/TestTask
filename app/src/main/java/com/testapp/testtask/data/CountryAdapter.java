package com.testapp.testtask.data;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.testapp.testtask.CountriesActivity;
import com.testapp.testtask.R;

import java.io.File;
import java.util.List;

import static com.testapp.testtask.data.DownloadingService.cancelled;


/**
 * Custom adapter for Countries recyclerView.
 */

public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.ViewHolder> {

    private List<Territory> mData;
    private Context mContext;
    CountriesActivity currentActivity;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // Views inside the viewHolder.
        public TextView mTextView;
        public ImageView mDownloadImage;
        public ImageView mMapIcon;
        public ImageView mCancelButton;
        public ProgressBar mProgressBar;
        public ConstraintLayout mConstraintLayout;
        public ViewHolder(ConstraintLayout v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.textView_country_name);
            mDownloadImage = (ImageView) v.findViewById(R.id.imageView_download_icon);
            mMapIcon = (ImageView) v.findViewById(R.id.imageView_map_icon);
            mConstraintLayout = v;
            mProgressBar = (ProgressBar) v.findViewById(R.id.download_progressbar);
            mCancelButton = (ImageView) v.findViewById(R.id.imageView_cancel_icon);

        }

    }

    public CountryAdapter(List<Territory> data, Context context) {
        mData = data;
        mContext = context;

    }

    @Override
    public CountryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ConstraintLayout layout = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.country_viewholder, parent, false);

        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(final CountryAdapter.ViewHolder holder, final int position) {
        currentActivity = (CountriesActivity) mContext;
        holder.mTextView.setText(mData.get(position).getName());
        holder.mDownloadImage.setColorFilter(R.color.colorIcons);
        holder.mMapIcon.setColorFilter(R.color.colorIcons);
        holder.mProgressBar.setVisibility(View.GONE);
        holder.mCancelButton.setVisibility(View.GONE);

        if (mData.get(position).hasChildren()) {
            holder.mDownloadImage.setVisibility(View.GONE);
            holder.mConstraintLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Territory ter = mData.get(position);
                    Log.e("CountryAdapter", "OnClick mConstraintLayout");
                    FragmentManager fragmentManager = ((CountriesActivity) mContext)
                            .getSupportFragmentManager();
                    CountriesFragment countriesFragment1 = new CountriesFragment();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("Continent", ter);
                    currentActivity.addActivityName(ter.getName());
                    currentActivity.setTitle(ter.getName());
                    countriesFragment1.setArguments(bundle);
                    fragmentManager.beginTransaction()
                            .replace(R.id.countries_list, countriesFragment1)
                            .addToBackStack(null)
                            .commitAllowingStateLoss();
                    fragmentManager.executePendingTransactions();
                }
            });
        }
        else {
            holder.mDownloadImage.setVisibility(View.VISIBLE);
        }

        holder.mDownloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext,
                        DownloadingService.class);
                intent.putExtra("url", "http://download.osmand.net/download.php?standard=yes&file=Denmark_europe_2.obf.zip");
                intent.putExtra("holderPosition", position);
                Log.w("CountryAdapter", "Passing Receiver to the Service");
                cancelled = false;
                intent.putExtra("RegionName", mData.get(position).getName());
                mContext.startService(intent);
            }
        });




    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
