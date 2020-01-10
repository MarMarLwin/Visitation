package com.onthego.onthegovisitation.CustomRecyclerView;

/**
 * Created by frantic on 2/2/18.
 */

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;

import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.onthego.onthegovisitation.R;

public class IndexBarRecyclerView extends RecyclerView {

    public int setIndexTextSize = 12;
    public float mIndexbarWidth = 20;
    public float mIndexbarMargin = 5;
    public float mIndexbarMarginTop = 5;
    public float mIndexbarMarginBottom = 5;
    public int mPreviewPadding = 5;
    public int mIndexBarCornerRadius = 5;
    public float mIndexBarTransparentValue = (float) 0.6;
    public @ColorInt
    int mIndexbarBackgroudColor = Color.BLACK;
    public @ColorInt
    int mIndexbarTextColor = Color.WHITE;
    public @ColorInt
    int mIndexbarHighLateTextColor = Color.BLACK;
    private IndexBarRecyclerViewSection mScroller = null;
    private GestureDetector mGestureDetector = null;
    private boolean mEnabled = true;

    public IndexBarRecyclerView(Context context) {
        super(context);
    }

    public IndexBarRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public IndexBarRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }


    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.IndexBarRecyclerView, 0, 0);

            if (typedArray != null) {
                try {
                    setIndexTextSize = typedArray.getInt(R.styleable.IndexBarRecyclerView_setIndexTextSize, setIndexTextSize);
                    mIndexbarWidth = typedArray.getFloat(R.styleable.IndexBarRecyclerView_setIndexbarWidth, mIndexbarWidth);
                    mIndexbarMargin = typedArray.getFloat(R.styleable.IndexBarRecyclerView_setIndexbarMargin, mIndexbarMargin);
                    mIndexbarMarginTop = typedArray.getFloat(R.styleable.IndexBarRecyclerView_setIndexbarMarginTop, mIndexbarMarginTop);
                    mIndexbarMarginBottom = typedArray.getFloat(R.styleable.IndexBarRecyclerView_setIndexbarMarginBottom, mIndexbarMarginBottom);
                    mPreviewPadding = typedArray.getInt(R.styleable.IndexBarRecyclerView_setPreviewPadding, mPreviewPadding);
                    mIndexBarCornerRadius = typedArray.getInt(R.styleable.IndexBarRecyclerView_setIndexBarCornerRadius, mIndexBarCornerRadius);
                    mIndexBarTransparentValue = typedArray.getFloat(R.styleable.IndexBarRecyclerView_setIndexBarTransparentValue, mIndexBarTransparentValue);

                    if (typedArray.hasValue(R.styleable.IndexBarRecyclerView_setIndexBarColor)) {
                        mIndexbarBackgroudColor = Color.parseColor(typedArray.getString(R.styleable.IndexBarRecyclerView_setIndexBarColor));
                    }

                    if (typedArray.hasValue(R.styleable.IndexBarRecyclerView_setIndexBarTextColor)) {
                        mIndexbarTextColor = Color.parseColor(typedArray.getString(R.styleable.IndexBarRecyclerView_setIndexBarTextColor));
                    }

                    if (typedArray.hasValue(R.styleable.IndexBarRecyclerView_setIndexBarHighlightTextColor)) {
                        mIndexbarHighLateTextColor = Color.parseColor(typedArray.getString(R.styleable.IndexBarRecyclerView_setIndexBarHighlightTextColor));
                    }

                    if (typedArray.hasValue(R.styleable.IndexBarRecyclerView_setIndexBarColorRes)) {
                        mIndexbarBackgroudColor = typedArray.getColor(R.styleable.IndexBarRecyclerView_setIndexBarColorRes, mIndexbarBackgroudColor);
                    }

                    if (typedArray.hasValue(R.styleable.IndexBarRecyclerView_setIndexBarTextColorRes)) {
                        mIndexbarTextColor = typedArray.getColor(R.styleable.IndexBarRecyclerView_setIndexBarTextColorRes, mIndexbarTextColor);
                    }

                    if (typedArray.hasValue(R.styleable.IndexBarRecyclerView_setIndexBarHighlightTextColorRes)) {
                        mIndexbarHighLateTextColor = typedArray.getColor(R.styleable.IndexBarRecyclerView_setIndexBarHighlightTextColor, mIndexbarHighLateTextColor);
                    }

                } finally {
                    typedArray.recycle();
                }
            }
        }
        mScroller = new IndexBarRecyclerViewSection(context, this);
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);

        // Overlay index bar
        if (mScroller != null)
            mScroller.draw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mEnabled) {
            // Intercept ListView's touch event
            if (mScroller != null && mScroller.onTouchEvent(ev))
                return true;

            if (mGestureDetector == null) {
                mGestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onFling(MotionEvent e1, MotionEvent e2,
                                           float velocityX, float velocityY) {
                        return super.onFling(e1, e2, velocityX, velocityY);
                    }

                });
            }
            mGestureDetector.onTouchEvent(ev);
        }

        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (mEnabled && mScroller != null && mScroller.contains(ev.getX(), ev.getY()))
            return true;

        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void setAdapter(Adapter adapter) {
        super.setAdapter(adapter);
        if (mScroller != null)
            mScroller.setAdapter(adapter);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (mScroller != null)
            mScroller.onSizeChanged(w, h, oldw, oldh);
    }

    /**
     * @param value int to set the text size of the index bar
     */
    public void setIndexTextSize(int value) {
        mScroller.setIndexTextSize(value);
    }

    /**
     * @param value float to set the width of the index bar
     */
    public void setIndexbarWidth(float value) {
        mScroller.setIndexbarWidth(value);
    }

    /**
     * @param value float to set the margin of the index bar
     */
    public void setIndexbarMargin(float value) {
        mScroller.setIndexbarMargin(value);
    }

    /**
     * @param value float to set the margin of the index bar top
     */
    public void setIndexbarMarginTop(float value) {
        mScroller.setIndexbarMarginTop(value);
    }

    /**
     * @param value float to set the margin of the index bar bottom
     */
    public void setIndexbarMarginBottom(float value) {
        mScroller.setIndexbarMarginBottom(value);
    }

    /**
     * @param value int to set the preview padding
     */
    public void setPreviewPadding(int value) {
        mScroller.setPreviewPadding(value);
    }

    /**
     * @param value int to set the corner radius of the index bar
     */
    public void setIndexBarCornerRadius(int value) {
        mScroller.setIndexBarCornerRadius(value);
    }

    /**
     * @param value float to set the transparency value of the index bar
     */
    public void setIndexBarTransparentValue(float value) {
        mScroller.setIndexBarTransparentValue(value);
    }

    /**
     * @param typeface Typeface to set the typeface of the preview & the index bar
     */
    public void setTypeface(Typeface typeface) {
        mScroller.setTypeface(typeface);
    }

    /**
     * @param shown boolean to show or hide the index bar
     */
    public void setIndexBarVisibility(boolean shown) {
        mScroller.setIndexBarVisibility(shown);
        mEnabled = shown;
    }

    /**
     * @param shown boolean to show or hide the preview
     */
    public void setPreviewVisibility(boolean shown) {
        mScroller.setPreviewVisibility(shown);
    }

    /**
     * @param color The color for the index bar
     */
    public void setIndexBarColor(String color) {
        mScroller.setIndexBarColor(Color.parseColor(color));
    }

    /**
     * @param color The color for the index bar
     */
    public void setIndexBarColor(@ColorRes int color) {
        int colorValue = ContextCompat.getColor(getContext(), color)/*getContext().getResources().getColor(color)*/;
        mScroller.setIndexBarColor(colorValue);
    }


    /**
     * @param color The text color for the index bar
     */
    public void setIndexBarTextColor(String color) {
        mScroller.setIndexBarTextColor(Color.parseColor(color));
    }

    /**
     * @param color The text color for the index bar
     */
    public void setIndexBarTextColor(@ColorRes int color) {
        int colorValue = ContextCompat.getColor(getContext(), color)/*getContext().getResources().getColor(color)*/;
        mScroller.setIndexBarTextColor(colorValue);
    }

    /**
     * @param color The text color for the index bar
     */
    public void setIndexbarHighLateTextColor(String color) {
        mScroller.setIndexBarHighLateTextColor(Color.parseColor(color));
    }

    /**
     * @param color The text color for the index bar
     */
    public void setIndexbarHighLateTextColor(@ColorRes int color) {
        int colorValue = ContextCompat.getColor(getContext(), color);
        mScroller.setIndexBarHighLateTextColor(colorValue);
    }

    /**
     * @param shown boolean to show or hide the index bar
     */
    public void setIndexBarHighLateTextVisibility(boolean shown) {
        mScroller.setIndexBarHighLateTextVisibility(shown);
    }
}