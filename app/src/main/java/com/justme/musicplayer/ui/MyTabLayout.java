package com.justme.musicplayer.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.core.view.ViewCompat;

import com.google.android.material.tabs.TabLayout;

public class MyTabLayout extends TabLayout {
    public MyTabLayout(Context context) {
        super(context);
    }

    public MyTabLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        View firstTab = ((ViewGroup)getChildAt(0)).getChildAt(0);
        View lastTab = ((ViewGroup)getChildAt(0)).getChildAt(((ViewGroup)getChildAt(0)).getChildCount()-1);
        ViewCompat.setPaddingRelative(getChildAt(0), (getWidth()/2) - (firstTab.getWidth()/2),0,(getWidth()/2) - (lastTab.getWidth()/2),0);
    }
}
