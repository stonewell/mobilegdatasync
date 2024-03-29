package com.angelstone.android.easydialer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

public class ButtonGridLayout extends ViewGroup {
    static private final int COLUMNS = 3;
    static private final int ROWS = 4;
    static private final int NUM_CHILDREN = ROWS * COLUMNS;

    private View[] mButtons = new View[NUM_CHILDREN];
    
    private int mPaddingLeft = 1;
    private int mPaddingRight = 1;
    private int mPaddingTop = 1;
    private int mPaddingBottom = 1;

    // This what the fields represent (height is similar):
    // PL: mPaddingLeft
    // BW: mButtonWidth
    // PR: mPaddingRight
    //
    //        mWidthInc
    // <-------------------->
    //   PL      BW      PR
    // <----><--------><---->
    //        --------
    //       |        |
    //       | button |
    //       |        |
    //        --------
    //
    // We assume mPaddingLeft == mPaddingRight == 1/2 padding between
    // buttons.
    //
    // mWidth == COLUMNS x mWidthInc

    // Width and height of a button
    private int mButtonWidth;
    private int mButtonHeight;

    // Width and height of a button + padding.
    private int mWidthInc;
    private int mHeightInc;

    // Height of the dialpad. Used to align it at the bottom of the
    // view.
    private int mWidth;
    private int mHeight;


    public ButtonGridLayout(Context context) {
        super(context);
    }

    public ButtonGridLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ButtonGridLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Cache the buttons in a member array for faster access.  Compute
     * the measurements for the width/height of buttons.  The inflate
     * sequence is called right after the constructor and before the
     * measure/layout phase.
     */
    @Override
    protected void onFinishInflate () {
        super.onFinishInflate();
        final View[] buttons = mButtons;
        for (int i = 0; i < NUM_CHILDREN; i++) {
            buttons[i] = getChildAt(i);
            // Measure the button to get initialized.
            buttons[i].measure(MeasureSpec.UNSPECIFIED , MeasureSpec.UNSPECIFIED);
        }

        // Cache the measurements.
        final View child = buttons[0];
        mButtonWidth = child.getMeasuredWidth();
        mButtonHeight = child.getMeasuredHeight();
        mWidthInc = mButtonWidth + mPaddingLeft + mPaddingRight;
        mHeightInc = mButtonHeight + mPaddingTop + mPaddingBottom;
        mWidth = COLUMNS * mWidthInc;
        mHeight = ROWS * mHeightInc;
    }

    /**
     * Set the background of all the children. Typically a selector to
     * change the background based on some combination of the button's
     * attributes (e.g pressed, enabled...)
     * @param resid Is a resource id to be used for each button's background.
     */
    public void setChildrenBackgroundResource(int resid) {
        final View[] buttons = mButtons;
        for (int i = 0; i < NUM_CHILDREN; i++) {
            buttons[i].setBackgroundResource(resid);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        final View[] buttons = mButtons;
        final int paddingLeft = mPaddingLeft;
        final int buttonWidth = mButtonWidth;
        final int buttonHeight = mButtonHeight;
        final int widthInc = mWidthInc;
        final int heightInc = mHeightInc;

        int i = 0;
        // The last row is bottom aligned.
        int y = (bottom - top) - mHeight + mPaddingTop;
        for (int row = 0; row < ROWS; row++) {
            int x = paddingLeft;
            for (int col = 0; col < COLUMNS; col++) {
                buttons[i].layout(x, y, x + buttonWidth, y + buttonHeight);
                x += widthInc;
                i++;
            }
            y += heightInc;
        }
    }

    /**
     * This method is called twice in practice. The first time both
     * with and height are constraint by AT_MOST. The second time, the
     * width is still AT_MOST and the height is EXACTLY. Either way
     * the full width/height should be in mWidth and mHeight and we
     * use 'resolveSize' to do the right thing.
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        final int width = resolveSize(mWidth, widthMeasureSpec);
        final int height = resolveSize(mHeight, heightMeasureSpec);
        setMeasuredDimension(width, height);
    }
}
