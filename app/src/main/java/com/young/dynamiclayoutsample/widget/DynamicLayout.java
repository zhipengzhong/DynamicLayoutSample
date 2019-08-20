package com.young.dynamiclayoutsample.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.young.dynamiclayoutsample.R;

import java.util.ArrayList;

public class DynamicLayout extends ViewGroup {

    private static final String TAG = "DynamicLayout";
    /**
     * 选中条目添加的Z轴高度
     */
    private final float mTranslationZ = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources().getDisplayMetrics());

    private boolean mChildrenRemovable;
    private View mSelectedChild;
    private PointF mTouchOffset1;
    private PointF mTouchOffset2;
    private PointF mTouchPointer1;
    private PointF mTouchPointer2;
    private boolean mChildrenAutoSort;
    private ArrayList<View> mChildrenSort;
    private Rect mScaleBefore;
    private PointF mPointerScaleBefore1;
    private PointF mPointerScaleBefore2;

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
        initTypedArray(typed);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public DynamicLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        final TypedArray typed = context.obtainStyledAttributes(
                attrs, R.styleable.DynamicLayout, defStyleAttr, defStyleRes);
        initTypedArray(typed);
    }

    private void initTypedArray(TypedArray typed) {
        if (typed.getBoolean(R.styleable.DynamicLayout_childrenRemovable, false)) {
            setChildrenRemovable(true);
        }
        mChildrenAutoSort = typed.getBoolean(R.styleable.DynamicLayout_childrenAutoSort, false);
        setChildrenDrawingOrderEnabled(true);
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
        childrenSort();

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

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        if (mChildrenSort != null && mChildrenSort.size() == childCount) {
            return indexOfChild(mChildrenSort.get(i));
        }
        return super.getChildDrawingOrder(childCount, i);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
//        return super.onInterceptTouchEvent(ev);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
//        Log.d(TAG, "onTouchEvent: " + event.getAction());

        int pointerId = event.getPointerId(event.getActionIndex());
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                handleTouchFirstDown(event, pointerId);
                break;
            case MotionEvent.ACTION_UP:
                mTouchPointer1 = null;
                mTouchPointer2 = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                handleTouchDown(event, pointerId);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                handleTouchUp(pointerId);
                break;
            case MotionEvent.ACTION_CANCEL:
                handleTouchUp(pointerId);
                break;
            case MotionEvent.ACTION_MOVE:
                handleTouchMove(event);
                break;
        }

        requestLayout();
        return true;
    }

    private void handleTouchMove(MotionEvent event) {
        if (mSelectedChild == null) {
            return;
        }

        MarginLayoutParams lp = (MarginLayoutParams) mSelectedChild.getLayoutParams();

        int pointerId;
        int count = event.getPointerCount();
        for (int i = 0; i < (count < 2 ? count : 2); i++) {
            pointerId = event.getPointerId(i);
            switch (pointerId) {
                case 0:
                    if (mTouchPointer1 != null) {
                        mTouchPointer1.set(event.getX(i), event.getY(i));
                    }
                    break;
                case 1:
                    if (mTouchPointer2 != null) {
                        mTouchPointer2.set(event.getX(i), event.getY(i));
                    }
                    break;
            }
        }

        if ((mTouchPointer1 != null && mTouchPointer2 == null)) {
            int childX = (int) (mTouchPointer1.x - mTouchOffset1.x);
            if (childX < 0) {
                lp.leftMargin = 0;
            } else {
                int maxX = getMeasuredWidth() - mSelectedChild.getMeasuredWidth();
                lp.leftMargin = childX > maxX ? maxX : childX;
            }

            int childY = (int) (mTouchPointer1.y - mTouchOffset1.y);
            if (childY < 0) {
                lp.topMargin = 0;
            } else {
                int maxY = getMeasuredHeight() - mSelectedChild.getMeasuredHeight();
                lp.topMargin = childY > maxY ? maxY : childY;
            }
        } else if (mTouchPointer1 == null && mTouchPointer2 != null) {
            int childX = (int) (mTouchPointer2.x - mTouchOffset2.x);
            if (childX < 0) {
                lp.leftMargin = 0;
            } else {
                int maxX = getMeasuredWidth() - mSelectedChild.getMeasuredWidth();
                lp.leftMargin = childX > maxX ? maxX : childX;
            }

            int childY = (int) (mTouchPointer2.y - mTouchOffset2.y);
            if (childY < 0) {
                lp.topMargin = 0;
            } else {
                int maxY = getMeasuredHeight() - mSelectedChild.getMeasuredHeight();
                lp.topMargin = childY > maxY ? maxY : childY;
            }
        } else if (mTouchPointer1 != null && mTouchPointer2 != null) {
//            int viewWidth = mScaleBefore.right - mScaleBefore.left;
//            int viewHeight = mScaleBefore.bottom - mScaleBefore.top;
            lp.width = (int) (mScaleBefore.right - mPointerScaleBefore2.x + mTouchPointer2.x - lp.leftMargin);
            lp.height = (int) (mScaleBefore.bottom - mPointerScaleBefore2.y + mTouchPointer2.y - lp.topMargin);

            Log.d(TAG, "handleTouchMove: " + mScaleBefore.right);
            Log.d(TAG, "handleTouchMove: " + mPointerScaleBefore2.x);
            Log.d(TAG, "handleTouchMove: " + mTouchPointer2.x);
            Log.d(TAG, "handleTouchMove: " + lp.leftMargin);
            Log.d(TAG, "handleTouchMove: " + lp.width);

        }


    }

    private void handleTouchUp(int pointerId) {
        switch (pointerId) {
            case 0:
                mTouchPointer1 = null;
                break;
            case 1:
                mTouchPointer2 = null;
                break;
        }
    }

    private void handleTouchFirstDown(MotionEvent event, int pointerId) {
        if (pointerId != 0 && pointerId != 1) {
            return;
        }

        setViewTranslationZ(0);
        mSelectedChild = null;

        int pointerIndex = event.findPointerIndex(pointerId);
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            View child = getChildAt(getChildDrawingOrder(count, i));
            Rect rect = getChildRect(child);
            if (x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom) {
                mSelectedChild = child;
                if (mChildrenAutoSort) {
                    resetChildrenDrawLast(mSelectedChild);
                }
                setViewTranslationZ(mTranslationZ);
                switch (pointerId) {
                    case 0:
                        mTouchPointer1 = new PointF(x, y);
                        mTouchOffset1 = new PointF(x - rect.left, y - rect.top);
                        break;
                    case 1:
                        mTouchPointer2 = new PointF(x, y);
                        mTouchOffset2 = new PointF(x - rect.left, y - rect.top);
                        break;
                }
                break;
            }
        }
    }

    private void handleTouchDown(MotionEvent event, int pointerId) {
        if (pointerId != 0 && pointerId != 1) {
            return;
        }

        int pointerIndex = event.findPointerIndex(pointerId);
        float x = event.getX(pointerIndex);
        float y = event.getY(pointerIndex);

        int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            View child = getChildAt(getChildDrawingOrder(count, i));
            Rect rect = getChildRect(child);
            if (x >= rect.left && x <= rect.right && y >= rect.top && y <= rect.bottom) {
                if (mSelectedChild == child) {
                    switch (pointerId) {
                        case 0:
                            mTouchPointer1 = new PointF(x, y);
                            mTouchOffset1 = new PointF(x - rect.left, y - rect.top);
                            break;
                        case 1:
                            mTouchPointer2 = new PointF(x, y);
                            mTouchOffset2 = new PointF(x - rect.left, y - rect.top);
                            break;
                    }
                }
                mScaleBefore = rect;
                mPointerScaleBefore1 = new PointF(mTouchPointer1.x, mTouchPointer1.y);
                mPointerScaleBefore2 = new PointF(mTouchPointer2.x, mTouchPointer2.y);
                break;
            }
        }
    }

    private void resetChildrenDrawLast(View child) {
        LayoutParams params = (LayoutParams) child.getLayoutParams();
        params.drawSort = Integer.MAX_VALUE;
    }

    private void childrenSort() {
        mChildrenSort = new ArrayList<>();
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(getChildDrawingOrder(count, i));
            LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
            int sort = layoutParams.drawSort;
            boolean isSort = false;
            for (int j = 0; j < mChildrenSort.size(); j++) {
                View view = mChildrenSort.get(j);
                LayoutParams lp = (LayoutParams) view.getLayoutParams();
                if (lp.drawSort > sort) {
                    mChildrenSort.add(j, child);
                    isSort = true;
                    break;
                }
            }
            if (!isSort) {
                mChildrenSort.add(child);
            }
        }
        if (mChildrenAutoSort) {
            for (int i = 0; i < mChildrenSort.size(); i++) {
                View view = mChildrenSort.get(i);
                LayoutParams params = (LayoutParams) view.getLayoutParams();
                params.drawSort = i;
            }
        }
    }

    private void setViewTranslationZ(float translationZ) {
        if (mSelectedChild != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mSelectedChild.setTranslationZ(translationZ);
            } else {

            }
        }
    }

    private Rect getChildRect(View child) {
        MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
        return new Rect(lp.leftMargin, lp.topMargin, lp.leftMargin + child.getMeasuredWidth(), lp.topMargin + child.getMeasuredHeight());
    }

    public void setChildrenRemovable(boolean childrenRemovable) {
        mChildrenRemovable = childrenRemovable;
    }

    public boolean getChildrenRemovable() {
        return mChildrenRemovable;
    }


    public static class LayoutParams extends MarginLayoutParams {

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
