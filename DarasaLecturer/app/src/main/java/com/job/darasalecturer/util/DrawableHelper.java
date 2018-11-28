package com.job.darasalecturer.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Job on Friday : 5/4/2018.
 * This class will be used to change vector drawable colors to conserve space
 */
public class DrawableHelper {
    @NonNull
    private Context mContext;
    @ColorRes
    private int mColor;
    private Drawable mDrawable;
    private Drawable mWrappedDrawable;

    public DrawableHelper(@NonNull Context context) {
        mContext = context;
    }

    public static DrawableHelper withContext(@NonNull Context context) {
        return new DrawableHelper(context);
    }

    public DrawableHelper withDrawable(@DrawableRes int drawableRes) {
        mDrawable = ContextCompat.getDrawable(mContext, drawableRes);
        return this;
    }

    public DrawableHelper withDrawable(@NonNull Drawable drawable) {
        mDrawable = drawable;
        return this;
    }


    @SuppressLint("ResourceAsColor")
    public DrawableHelper withColor(@ColorRes int colorRes) {
        mColor = ContextCompat.getColor(mContext, colorRes);
        return this;
    }

    @SuppressLint("ResourceAsColor")
    public DrawableHelper tint() {
        if (mDrawable == null) {
            throw new NullPointerException("You must report the drawable resource using the withDrawable () method");
        }

        if (mColor == 0) {
            throw new IllegalStateException("It is necessary to inform the color to be defined by the method withColor()");
        }

        mWrappedDrawable = mDrawable.mutate();
        mWrappedDrawable = DrawableCompat.wrap(mWrappedDrawable);
        DrawableCompat.setTint(mWrappedDrawable, mColor);
        DrawableCompat.setTintMode(mWrappedDrawable, PorterDuff.Mode.SRC_IN);

        return this;
    }

    @SuppressLint("ObsoleteSdkInt")
    @SuppressWarnings("deprecation")
    public void applyToBackground(@NonNull View view) {
        if (mWrappedDrawable == null) {
            throw new NullPointerException("You must call the method tint()");
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setBackground(mWrappedDrawable);
        } else {
            view.setBackgroundDrawable(mWrappedDrawable);
        }
    }

    public void applyTo(@NonNull ImageView imageView) {
        if (mWrappedDrawable == null) {
            throw new NullPointerException("You must call the method tint()");
        }

        imageView.setImageDrawable(mWrappedDrawable);
    }

    public void applyTo(@NonNull MenuItem menuItem) {
        if (mWrappedDrawable == null) {
            throw new NullPointerException("You must call the method tint()");
        }

        menuItem.setIcon(mWrappedDrawable);
    }

    public Drawable get() {
        if (mWrappedDrawable == null) {
            throw new NullPointerException("You must call the method tint()");
        }

        return mWrappedDrawable;
    }
}
/*
* DrawableHelper
            .withContext(this)
            .withColor(R.color.white)
            .withDrawable(R.drawable.ic_search_24dp)
            .tint()
            .applyTo(mSearchItem);

            OR

  final Drawable drawable = DrawableHelper
            .withContext(this)
            .withColor(R.color.white)
            .withDrawable(R.drawable.ic_search_24dp)
            .tint()
            .get();

    actionBar.setHomeAsUpIndicator(drawable);
*/