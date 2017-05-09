package com.testapp.testtask.data;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Environment;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.testapp.testtask.R;

import java.io.File;
import java.util.List;


/**
 * Created by paul on 05.05.17.
 */

public class CountryAdapter extends RecyclerView.Adapter<CountryAdapter.ViewHolder> {

    private List<Territory> mData;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
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

    public CountryAdapter(List<Territory> data) {
        mData = data;

    }

    @Override
    public CountryAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ConstraintLayout layout = (ConstraintLayout) LayoutInflater.from(parent.getContext())
                .inflate(R.layout.country_viewholder, parent, false);

        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(final CountryAdapter.ViewHolder holder, final int position) {
        holder.mTextView.setText(mData.get(position).getName());
        holder.mDownloadImage.setColorFilter(R.color.colorIcons);
        holder.mMapIcon.setColorFilter(R.color.colorIcons);
        holder.mProgressBar.setVisibility(View.GONE);
        holder.mCancelButton.setVisibility(View.GONE);
        if (mData.get(position).hasChildren()) {
            holder.mDownloadImage.setVisibility(View.GONE);
        }
        else {
            holder.mDownloadImage.setVisibility(View.VISIBLE);
        }
        if(holder.mTextView.getText().toString().equals("Berlin ")) {

            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),
                    "Berlin.zip");
            if (file.exists()) {
                holder.mDownloadImage.getDrawable()
                        .setColorFilter(holder
                                .mDownloadImage.
                                        getContext().
                                        getResources().
                                        getColor(R.color.colorIconDownloaded),
                                                PorterDuff.Mode.SRC_IN);
            }
            else {
                holder.mDownloadImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent("start.download.action");
                        intent.putExtra("position", position);
                        holder.mConstraintLayout.getContext().sendBroadcast(intent);
                        holder.mDownloadImage.setVisibility(View.INVISIBLE);

                    }
                });
            }

        }
        holder.mConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Territory ter = mData.get(position);
                if (ter.hasChildren()) {
                    Intent intent = new Intent("start.regions.action");
                    intent.putExtra("Continent", ter);
                    holder.mConstraintLayout.getContext().sendBroadcast(intent);
                }
            }
        });


    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}
