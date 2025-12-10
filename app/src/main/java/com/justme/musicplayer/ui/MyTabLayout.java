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

        if (getChildCount() == 0) return;
        View child0 = getChildAt(0);
        if (!(child0 instanceof ViewGroup)) return;
        final ViewGroup container = (ViewGroup) child0;

        if (container.getChildCount() == 0) return;
        final View firstTab = container.getChildAt(0);
        final View lastTab = container.getChildAt(container.getChildCount() - 1);
        if (firstTab == null || lastTab == null) return;

        Runnable applyPadding = new Runnable() {
            @Override
            public void run() {
                int width = getWidth();
                int left = (width / 2) - (firstTab.getWidth() / 2);
                int right = (width / 2) - (lastTab.getWidth() / 2);
                ViewCompat.setPaddingRelative(container, Math.max(left, 0), 0, Math.max(right, 0), 0);
            }
        };

        // If tabs haven't been measured yet, defer adjusting padding
        if (firstTab.getWidth() == 0 || lastTab.getWidth() == 0) {
            post(applyPadding);
        } else {
            applyPadding.run();
        }
    }
}
