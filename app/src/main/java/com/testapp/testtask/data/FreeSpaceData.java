package com.testapp.testtask.data;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.testapp.testtask.R;
import com.testapp.testtask.utils.DiskUtils;


/**
 * Created by paul on 03.05.17.
 */

public class FreeSpaceData extends Fragment {

    private static final String TAG = "FreeSpaceData";

    public FreeSpaceData() {

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.free_space_fragment, container, false);
        TextView freeSpaceText = (TextView) rootView.findViewById(R.id.textView_free_mem);

        ImageView used = (ImageView) rootView.findViewById(R.id.used_space);
        ImageView free = (ImageView) rootView.findViewById(R.id.free_spce);
        float freeSpaceNum = DiskUtils.freeSpace(true);
        float allSpace = DiskUtils.totalSpace(true);

        // Display free space
        String freeSpace = "Free " + freeSpaceNum + " GB";
        final SpannableStringBuilder sb = new SpannableStringBuilder(freeSpace);
        final StyleSpan bss = new StyleSpan(android.graphics.Typeface.BOLD);
        sb.setSpan(bss, 5, freeSpace.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        freeSpaceText.setText(sb);

        // Calculate and display free space graph.
        float percentage = freeSpaceNum / allSpace;
        Context context = rootView.getContext();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        used.getLayoutParams().width = (int) (width * percentage);

        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = 24 * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        Log.e(TAG, String.valueOf(px));
        free.getLayoutParams().width = (int) (width - (width * percentage) - px);

        return rootView;
    }
}
