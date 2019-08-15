package com.young.dynamiclayoutsample.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import com.young.dynamiclayoutsample.R;

public class DynamicLayout extends ViewGroup {
    private boolean mChildrenRemovable;

    public DynamicLayout(Context context) {
        super(context);
    }

    public DynamicLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DynamicLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray typed = context.obtainStyledAttributes(
                attrs, R.styleable.DynamicLayout);

        if (typed.getBoolean(R.styleable.DynamicLayout_childrenRemovable, false)) {
            setChildrenRemovable(true);
        }

        typed.recycle();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DynamicLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        final TypedArray typed = context.obtainStyledAttributes(
                attrs, R.styleable.DynamicLayout, defStyleAttr, defStyleRes);

        if (typed.getBoolean(R.styleable.DynamicLayout_childrenRemovable, false)) {
            setChildrenRemovable(true);
        }

        typed.recycle();
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new LayoutParams(getContext(), attrs);
    }

    @Override
    protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        if (p instanceof LayoutParams) {
            return new LayoutParams((LayoutParams) p);
        } else if (p instanceof MarginLayoutParams) {
            return new LayoutParams((MarginLayoutParams) p);
        }
        return new LayoutParams(p);
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int maxWidth = 0;
        int maxHeight = 0;

//        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
//        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
//        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
//        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                maxWidth = Math.max(maxWidth, child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                maxHeight = Math.max(maxHeight, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
            }
        }
        setMeasuredDimension(resolveSize(maxWidth, widthMeasureSpec), resolveSize(maxHeight, heightMeasureSpec));

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

            final int childWidthMeasureSpec;
            if (lp.width == LayoutParams.MATCH_PARENT) {
                final int width = Math.max(0, getMeasuredWidth() - lp.leftMargin - lp.rightMargin);
                childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
            } else {
                childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, lp.leftMargin + lp.rightMargin, lp.width);
            }

            final int childHeightMeasureSpec;
            if (lp.height == LayoutParams.MATCH_PARENT) {
                final int height = Math.max(0, getMeasuredHeight() - lp.topMargin - lp.bottomMargin);
                childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
            } else {
                childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, lp.topMargin + lp.bottomMargin, lp.height);
            }
            child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        final int count = getChildCount();

        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                final int width = child.getMeasuredWidth();
                final int height = child.getMeasuredHeight();

                int childLeft = lp.leftMargin;
                int childTop = lp.topMargin;

                child.layout(childLeft, childTop, childLeft + width, childTop + height);
            }
        }
    }

    public void setChildrenRemovable(boolean childrenRemovable) {
        mChildrenRemovable = childrenRemovable;
    }

    public boolean getChildrenRemovable() {
        return mChildrenRemovable;
    }


    public static class LayoutParams extends MarginLayoutParams {

        public static final int UNSPECIFIED_GRAVITY = -1;

        public int drawSort;

        public LayoutParams(Context context, AttributeSet attrs) {
            super(context, attrs);

            final TypedArray typed = context.obtainStyledAttributes(attrs, R.styleable.DynamicLayout_Layout);
            drawSort = typed.getInt(R.styleable.DynamicLayout_Layout_layout_drawSort, 0);
            typed.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(LayoutParams source) {
            super(source);
            drawSort = source.drawSort;
        }
    }
}
