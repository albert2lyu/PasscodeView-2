package com.kevalpatel.passcodeview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.Dimension;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;

/**
 * Created by Keval on 06-Apr-17.
 *
 * @author 'https://github.com/kevalpatel2106'
 */

public class PinView extends View {
    private Context mContext;
    private float mDownKeyX;                                    //X coordinate of the ACTION_DOWN point
    private float mDownKeyY;                                    //Y coordinate of the ACTION_DOWN point
    private int mPinCodeLength;                                 //PIN code length

    private AuthenticationListener mAuthenticationListener;     //Callback listener for application to get notify when authentication successful.
    private String mPinToCheck;                                 //Current PIN with witch entered PIN will check.
    @NonNull
    private String mPinTyped = "";                              //PIN typed.

    //Rectangle bounds
    private Rect mRootViewBound = new Rect();
    private Rect mDividerBound = new Rect();        //Divider bound

    //Theme attributes
    @ColorInt
    private int mDividerColor;                      //Horizontal divider color

    //Paints
    private Paint mDividerPaint;                    //Horizontal divider paint color

    private BoxKeypad mBoxKeypad;
    private BoxFingerprint mBoxFingerprint;
    private BoxTitleIndicator mBoxIndicator;

    ///////////////////////////////////////////////////////////////
    //                  CONSTRUCTORS
    ///////////////////////////////////////////////////////////////

    public PinView(Context context) {
        super(context);
        init(context, null);
    }

    public PinView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PinView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    ///////////////////////////////////////////////////////////////
    //                  SET THEME PARAMS
    ///////////////////////////////////////////////////////////////

    /**
     * Initialize view.
     *
     * @param context instance of the caller.
     * @param attrs   Typed attributes or null.
     */
    @SuppressWarnings("deprecation")
    private void init(@NonNull Context context, @Nullable AttributeSet attrs) {
        mContext = context;
        mBoxKeypad = new BoxKeypad(this);
        mBoxFingerprint = new BoxFingerprint(this);
        mBoxIndicator = new BoxTitleIndicator(this);

        if (attrs != null) {
            parseTypeArr(attrs);
        } else {
            mPinCodeLength = Constants.DEF_PIN_LENGTH;

            mDividerColor = getResources().getColor(R.color.divider_color);

            mBoxIndicator.setDefaults();
            mBoxKeypad.setDefaults();
            mBoxFingerprint.setDefaults();
        }

        prepareDividerPaint();
        mBoxKeypad.preparePaint();
        mBoxFingerprint.preparePaint();
        mBoxIndicator.preparePaint();
    }

    @SuppressWarnings("deprecation")
    private void parseTypeArr(@Nullable AttributeSet attrs) {
        TypedArray a = mContext.getTheme().obtainStyledAttributes(attrs, R.styleable.PinView, 0, 0);
        try {
            mPinCodeLength = (a.getInteger(R.styleable.PinView_pinLength,
                    Constants.DEF_PIN_LENGTH));

            //Parse title params
            mBoxIndicator.setTitle(a.hasValue(R.styleable.PinView_titleText) ?
                    a.getString(R.styleable.PinView_titleText) : Constants.DEF_TITLE_TEXT);
            mBoxIndicator.setTitleColor(a.getColor(R.styleable.PinView_titleTextColor,
                    mContext.getResources().getColor(R.color.key_default_color)));

            //Parse divider params
            mDividerColor = a.getColor(R.styleable.PinView_dividerColor,
                    mContext.getResources().getColor(R.color.divider_color));

            //Parse indicator params
            mBoxIndicator.setIndicatorFilledColor(a.getColor(R.styleable.PinView_indicatorSolidColor,
                    getResources().getColor(R.color.indicator_filled_color)));
            mBoxIndicator.setIndicatorStrokeColor(a.getColor(R.styleable.PinView_indicatorStrokeColor,
                    getResources().getColor(R.color.indicator_stroke_color)));

            //Set the key box params
            mBoxKeypad.setKeyTextColor(a.getColor(R.styleable.PinView_keyTextColor,
                    mContext.getResources().getColor(R.color.key_default_color)));
            mBoxKeypad.setKeyBackgroundColor(a.getColor(R.styleable.PinView_keyStrokeColor,
                    mContext.getResources().getColor(R.color.key_background_color)));
            mBoxKeypad.setKeyTextSize(a.getDimensionPixelSize(R.styleable.PinView_keyTextSize,
                    (int) mContext.getResources().getDimension(R.dimen.key_text_size)));
            mBoxKeypad.setKeyStrokeWidth(a.getDimension(R.styleable.PinView_keyStrokeWidth,
                    mContext.getResources().getDimension(R.dimen.key_stroke_width)));
            //noinspection WrongConstant
            mBoxKeypad.setKeyShape(a.getInt(R.styleable.PinView_keyShape, BoxKeypad.KEY_TYPE_CIRCLE));
            mBoxKeypad.setFingerPrintEnable(a.getBoolean(R.styleable.PinView_fingerprintEnable, true));

            //Fet fingerprint params
            //noinspection ConstantConditions
            mBoxFingerprint.setStatusText(a.hasValue(R.styleable.PinView_titleText) ?
                    a.getString(R.styleable.PinView_fingerprintDefaultText) : BoxFingerprint.DEF_FINGERPRINT_STATUS);
            mBoxFingerprint.setStatusTextColor(a.getColor(R.styleable.PinView_fingerprintTextColor,
                    mContext.getResources().getColor(R.color.key_default_color)));
            mBoxFingerprint.setStatusTextSize(a.getDimension(R.styleable.PinView_fingerprintTextSize,
                    (int) mContext.getResources().getDimension(R.dimen.fingerprint_status_text_size)));
            mBoxFingerprint.setFingerPrintEnable(a.getBoolean(R.styleable.PinView_fingerprintEnable, true));
        } finally {
            a.recycle();
        }
    }

    private void prepareDividerPaint() {
        mDividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDividerPaint.setColor(mDividerColor);
    }


    ///////////////////////////////////////////////////////////////
    //                  VIEW DRAW
    ///////////////////////////////////////////////////////////////

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mBoxKeypad.draw(canvas);
        drawDivider(canvas);
        mBoxIndicator.draw(canvas);
        mBoxFingerprint.draw(canvas);
    }

    private void drawDivider(Canvas canvas) {
        canvas.drawLine(mDividerBound.left,
                mDividerBound.top,
                mDividerBound.right,
                mDividerBound.bottom,
                mDividerPaint);
    }

    ///////////////////////////////////////////////////////////////
    //                  VIEW MEASUREMENT
    ///////////////////////////////////////////////////////////////

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        measureMainView();
        mBoxKeypad.measure(mRootViewBound);
        measureDivider();
        mBoxIndicator.measure(mRootViewBound);
        mBoxFingerprint.measure(mRootViewBound);

        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Measure the root view and get bounds.
     */
    private Rect measureMainView() {
        getLocalVisibleRect(mRootViewBound);

        //Get the height of the actionbar if we have any actionbar and add it to the top
        TypedValue tv = new TypedValue();
        if (mContext.getTheme().resolveAttribute(android.R.attr.actionBarSize, tv, true)) {
            mRootViewBound.top = mRootViewBound.top
                    + TypedValue.complexToDimensionPixelSize(tv.data, getResources().getDisplayMetrics());
        }

        return mRootViewBound;
    }

    /**
     * Measure horizontal divider bounds.
     * Don't change untill you know what you are doing. :-)
     */
    private void measureDivider() {
        mDividerBound.left = (int) (mRootViewBound.left + mContext.getResources().getDimension(R.dimen.divider_horizontal_margin));
        mDividerBound.right = (int) (mRootViewBound.right - mContext.getResources().getDimension(R.dimen.divider_horizontal_margin));
        mDividerBound.top = (int) (mBoxKeypad.getBounds().top - mContext.getResources().getDimension(R.dimen.divider_vertical_margin));
        mDividerBound.bottom = (int) (mBoxKeypad.getBounds().top - mContext.getResources().getDimension(R.dimen.divider_vertical_margin));
    }

    ///////////////////////////////////////////////////////////////
    //                  TOUCH HANDLER
    ///////////////////////////////////////////////////////////////


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mDownKeyX = event.getX();
                mDownKeyY = event.getY();
                break;
            case MotionEvent.ACTION_UP:
                onKeyPressed(mBoxKeypad.findKeyPressed(mDownKeyX,
                        mDownKeyY,
                        event.getX(),
                        event.getY()));
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * Handle the newly added key digit. Append the digit to {@link #mPinTyped}.
     * If the new digit is {@link Constants#BACKSPACE_TITLE}, remove the last digit of the {@link #mPinTyped}.
     * If the {@link #mPinTyped} has length of {@link #mPinCodeLength} and equals to {@link #mPinToCheck}
     * notify application as authenticated.
     *
     * @param newDigit newly pressed digit
     */
    private void onKeyPressed(@Nullable String newDigit) {
        if (newDigit == null) return;

        //Check for the state
        if (mAuthenticationListener == null) {
            throw new IllegalStateException("Set AuthenticationListener to receive callbacks.");
        } else if (mPinToCheck.isEmpty() || mPinToCheck.length() != mPinCodeLength) {
            throw new IllegalStateException("Please set current PIN to check with the entered value.");
        }

        if (newDigit.equals(Constants.BACKSPACE_TITLE)) {
            if (!mPinTyped.isEmpty()) mPinTyped = mPinTyped.substring(0, mPinTyped.length() - 1);
        } else {
            mPinTyped = mPinTyped + newDigit;
        }

        mBoxIndicator.onValueEntered(mPinTyped);
        invalidate();

        if (mPinTyped.length() == mPinCodeLength) {

            if (mPinToCheck.equals(mPinTyped)) {
                mAuthenticationListener.onAuthenticationSuccessful();
                mBoxKeypad.onAuthenticationSuccess();
                mBoxIndicator.onAuthenticationSuccess();
                mBoxFingerprint.onAuthenticationSuccess();
            } else {
                mAuthenticationListener.onAuthenticationFailed();
                mBoxFingerprint.onAuthenticationFail();
                mBoxKeypad.onAuthenticationFail();
                mBoxIndicator.onAuthenticationFail();
            }

            //Reset the view.
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    reset();
                }
            }, 350);
        }
    }

    /**
     * Reset the pin code and view state.
     */
    public void reset() {
        mPinTyped = "";
        mBoxIndicator.onValueEntered(mPinTyped);
        invalidate();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mBoxFingerprint.stopFingerprintScanner();
    }

    ///////////////////////////////////////////////////////////////
    //                  GETTERS/SETTERS
    ///////////////////////////////////////////////////////////////

    public float getKeyPadding() {
        return mBoxKeypad.getKeyPadding();
    }

    public void setKeyPadding(@Dimension float keyPadding) {
        mBoxKeypad.setKeyPadding(keyPadding);
        requestLayout();
        invalidate();
    }

    public boolean isOneHandOperationEnabled() {
        return mBoxKeypad.isOneHandOperation();
    }

    public void enableOneHandOperation(boolean isEnable) {
        mBoxKeypad.setOneHandOperation(isEnable);
        requestLayout();
        invalidate();
    }

    @Nullable
    public String getPinToCheck() {
        return mPinToCheck;
    }

    public void setPinToCheck(@NonNull String pinToCheck) {
        mPinToCheck = pinToCheck;
        mPinCodeLength = mPinToCheck.length();
        mBoxIndicator.setPintCodeLength(mPinCodeLength);
        invalidate();
    }

    @Nullable
    public AuthenticationListener getAuthenticationListener() {
        return mAuthenticationListener;
    }

    public void setAuthenticationListener(@NonNull AuthenticationListener authenticationListener) {
        mAuthenticationListener = authenticationListener;
    }

    public int getKeyBackgroundColor() {
        return mBoxKeypad.getKeyBackgroundColor();
    }

    public void setKeyBackgroundColor(@ColorInt int keyBackgroundColor) {
        mBoxKeypad.setKeyBackgroundColor(keyBackgroundColor);
        invalidate();
    }

    public int getKeyTextColor() {
        return mBoxKeypad.getKeyTextColor();
    }

    public void setKeyTextColor(@ColorInt int keyTextColor) {
        mBoxKeypad.setKeyTextColor(keyTextColor);
        invalidate();
    }

    public int getIndicatorStrokeColor() {
        return mBoxIndicator.getIndicatorStrokeColor();
    }

    public void setIndicatorStrokeColor(@ColorInt int indicatorStrokeColor) {
        mBoxIndicator.setIndicatorStrokeColor(indicatorStrokeColor);
        invalidate();
    }

    public int getIndicatorFilledColor() {
        return mBoxIndicator.getIndicatorFilledColor();
    }

    public void setIndicatorFilledColor(@ColorInt int indicatorFilledColor) {
        mBoxIndicator.setIndicatorFilledColor(indicatorFilledColor);
        invalidate();
    }

    public int getDividerColor() {
        return mDividerColor;
    }

    public void setDividerColor(@ColorInt int dividerColor) {
        mDividerColor = dividerColor;
        prepareDividerPaint();
        invalidate();
    }

    public int getTitleColor() {
        return mBoxIndicator.getTitleColor();
    }

    public void setTitleColor(@ColorInt int titleColor) {
        mBoxIndicator.setTitleColor(titleColor);
        invalidate();
    }

    /**
     * @return Current title of the view.
     */
    public String getTitle() {
        return mBoxIndicator.getTitle();
    }

    /**
     * Set the title at the top of view.
     *
     * @param title title string
     */
    public void setTitle(@NonNull String title) {
        mBoxIndicator.setTitle(title);
        invalidate();
    }

    @NonNull
    String getFingerPrintStatusText() {
        return mBoxFingerprint.getStatusText();
    }

    void setFingerPrintStatusText(@NonNull String statusText) {
        mBoxFingerprint.setStatusText(statusText);
        invalidate();
    }

    int getFingerPrintStatusTextColor() {
        return mBoxFingerprint.getStatusTextColor();
    }

    void setFingerPrintStatusTextColor(@ColorInt int statusTextColor) {
        mBoxFingerprint.setStatusTextColor(statusTextColor);
        invalidate();
    }

    void setFingerPrintStatusTextColorRes(@ColorRes int statusTextColor) {
        mBoxFingerprint.setStatusTextColor(mContext.getResources().getColor(statusTextColor));
        invalidate();
    }

    float getFingerPrintStatusTextSize() {
        return mBoxFingerprint.getStatusTextSize();
    }

    void setFingerPrintStatusTextSize(@Dimension float statusTextSize) {
        mBoxFingerprint.setStatusTextSize(statusTextSize);
        invalidate();
    }

    void setFingerPrintStatusTextSize(@DimenRes int statusTextSize) {
        mBoxFingerprint.setStatusTextSize(getResources().getDimension(statusTextSize));
        invalidate();
    }

    Boolean isFingerPrintEnable() {
        return mBoxFingerprint.setFingerPrintEnable();
    }

    void isFingerPrintEnable(boolean isEnable) {
        mBoxFingerprint.setFingerPrintEnable(isEnable);
        mBoxKeypad.setFingerPrintEnable(isEnable);
        invalidate();
    }
}
