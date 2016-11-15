package com.example.xyzreader.ui;

import android.support.design.widget.AppBarLayout;
import android.util.Log;

/**
 * Created by edm on 10/29/2016.
 */

public class ScrollingHelper implements AppBarLayout.OnOffsetChangedListener {
    public static final String TAG = ScrollingHelper.class.getSimpleName();
    private boolean isShow = false;
    private int mScrollRange = -1;
    private OnAppBarLayoutCollapseListener mListener;
    public ScrollingHelper(int scrollRange, OnAppBarLayoutCollapseListener listener) {
        mScrollRange = scrollRange;
        mListener = listener;
    }
    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        int height = appBarLayout.getHeight();
        int bottom = appBarLayout.getBottom();
        int totalRange = appBarLayout.getTotalScrollRange();
        Log.i(TAG, "Vertical Offset: " + verticalOffset + " : Height: " + height +  "  :  " +  totalRange);
        if(verticalOffset == 0) {
            if(mListener != null) {
                mListener.onAppBarLayoutCollapse(true);
            }
        } else if((totalRange - Math.abs(verticalOffset)) == 0) {
            if(mListener != null) {
                mListener.onAppBarLayoutCollapse(false);
                appBarLayout.setElevation(8);
            }
        }
    }
}
