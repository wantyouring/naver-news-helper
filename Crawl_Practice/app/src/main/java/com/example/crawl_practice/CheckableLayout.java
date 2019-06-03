package com.example.crawl_practice;

import android.content.Context;
import android.graphics.Color;
import android.support.constraint.ConstraintLayout;
import android.util.AttributeSet;
import android.widget.Checkable;

public class CheckableLayout extends ConstraintLayout implements Checkable {

    private boolean mChecked;

    public CheckableLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        mChecked = false;
    }

    @Override
    public void setChecked(boolean checked) {
        if(checked) {
            this.setBackgroundColor(Color.rgb(233,233,233));
        } else {
            this.setBackgroundColor(Color.rgb(255,255,255));
        }
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public void toggle() {
        setChecked(mChecked ? false : true);
    }
}
