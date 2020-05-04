package dev.alox.snackbanner;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.os.Looper;

import androidx.annotation.ColorRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewPropertyAnimatorListener;
import androidx.core.view.ViewPropertyAnimatorListenerAdapter;

import com.google.android.material.behavior.SwipeDismissBehavior;
import com.google.android.material.snackbar.BaseTransientBottomBar;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;



public final class SnackBanner {

    public static abstract class Callback {

        public static final int DISMISS_EVENT_SWIPE = 0;

        public static final int DISMISS_EVENT_ACTION = 1;

        public static final int DISMISS_EVENT_TIMEOUT = 2;

        public static final int DISMISS_EVENT_MANUAL = 3;

        public static final int DISMISS_EVENT_CONSECUTIVE = 4;

        @IntDef({
                DISMISS_EVENT_SWIPE, DISMISS_EVENT_ACTION, DISMISS_EVENT_TIMEOUT,
                DISMISS_EVENT_MANUAL, DISMISS_EVENT_CONSECUTIVE
        })

        @Retention(RetentionPolicy.SOURCE)
        public @interface DismissEvent{

        }

        public void onDismissed(SnackBanner snackBanner, @BaseTransientBottomBar.BaseCallback.DismissEvent int event){

        }

        public void onShow(SnackBanner snackBanner){

        }

    }

    /*
    *  @hide
    */
    @IntDef({LENGTH_INDEFINITE,LENGTH_SHORT,LENGTH_LONG})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Duration {

    }

    public static final int LENGTH_INDEFINITE = -2;

    public static final int LENGTH_SHORT = -1;

    public static final int LENGTH_LONG = 0;

    private static final int ANIMATION_DURATION = 250;
    private static final int ANIMATION_FADE_DURATION = 180;

    private static final Handler sHandler;
    private static final int MSG_SHOW = 0;
    private static final int MSG_DISMISS = 1;

    static {
        sHandler = new Handler(Looper.getMainLooper(), new Handler.Callback(){

            @Override
            public boolean handleMessage(@NonNull Message msg) {
                switch (msg.what){
                    case MSG_SHOW:
                        ((SnackBanner) msg.obj).showView();
                        return true;
                    case MSG_DISMISS:
                        ((SnackBanner) msg.obj).hideView(msg.arg1);
                        return true;
                }
                return false;
            }
        });
    }

    private final ViewGroup mParent;
    private final Context mContext;
    private final SnackbarLayout mView;
    private int mDuration;
    private Callback mCallback;

    private SnackBanner(ViewGroup parent){
        mParent = parent;
        mContext = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mView = (SnackbarLayout) inflater.inflate(R.layout.layout_snackbanner,mParent,false);

    }

    @NonNull
    public static SnackBanner make(@NonNull View view, @NonNull CharSequence text, @Duration int duration){
        SnackBanner snackBanner =  new SnackBanner(findSuitableParent(view));
        snackBanner.setText(text);
        snackBanner.setDuration(duration);
        return snackBanner;
    }

    @NonNull
    public static SnackBanner make(@NonNull View view, @StringRes int resId, @Duration int duration){
        return make(view,view.getResources().getText(resId),duration);
    }

    private static ViewGroup findSuitableParent(View view) {
        ViewGroup fallback = null;
        do {
            if (view instanceof CoordinatorLayout){
                return (ViewGroup)view;
            }
            else if (view instanceof FrameLayout){
                if (view.getId() == android.R.id.content) {
                    return (ViewGroup) view;
                }
                else {
                    fallback = (ViewGroup) view;
                }
            }
            else if (view instanceof  androidx.appcompat.widget.Toolbar || view instanceof Toolbar){

                if (view.getParent() instanceof ViewGroup){
                    ViewGroup parent = (ViewGroup) view.getParent();

                    //Check if something else is behind the toolbar
                    if (parent.getChildCount() >1){
                        int childrenCount = parent.getChildCount();
                        int toolbarIndex = 0;
                        for (int i =0; i<childrenCount; i++){
                            //find the index of toolbar
                            if (parent.getChildAt(i) == view){
                                toolbarIndex = i;
                                //check if there is something else after the toolbar
                                if (toolbarIndex<childrenCount-1){
                                    while (i < childrenCount){
                                        i++;
                                        View v = parent.getChildAt(i);
                                        if (v instanceof ViewGroup) return (ViewGroup) v;
                                    }
                                }
                                break;
                            }
                        }
                    }

                }

            }

            if (view != null){
                final ViewParent parent = view.getParent();
                view = parent instanceof  View ? (View) parent : null;
            }
        } while (view != null);

        return fallback;
    }

    private static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    @NonNull
    public SnackBanner setText(@NonNull CharSequence message){
        final TextView textV = mView.getMessageView();
        textV.setText(message);
        return this;
    }


    @NonNull
    public SnackBanner setText(@StringRes int resId){
        return setText(mContext.getText(resId));
    }

    @NonNull
    public SnackBanner setDuration(@Duration int duration){
        mDuration = duration;
        return this;
    }

    public SnackBanner setMaxWidth(int maxWidth){
        mView.mMaxWidth = maxWidth;
        return this;
    }

    @Duration
    public int getDuration(){
        return mDuration;
    }

    @NonNull
    public View getView(){
        return mView;
    }

    public void show(){
        SnackbarManager.getInstance().show(mDuration, mManagerCallback);
    }

    public void dismiss(){
        dispatchDismiss(Callback.DISMISS_EVENT_MANUAL);
    }

    private void dispatchDismiss(@Callback.DismissEvent int event){
        SnackbarManager.getInstance().dismiss(mManagerCallback, event);
    }

    @NonNull
    public SnackBanner setCallback(Callback callback) {
        mCallback = callback;
        return this;
    }

    public boolean isShown() {
        return SnackbarManager.getInstance().isCurrent(mManagerCallback);
    }

    public boolean isShownOrQueued(){
        return SnackbarManager.getInstance().isCurrentOrNext(mManagerCallback);
    }

    private final SnackbarManager.Callback mManagerCallback = new SnackbarManager.Callback() {
        @Override
        public void show() {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_SHOW, SnackBanner.this));
        }

        @Override
        public void dismiss(int event) {
            sHandler.sendMessage(sHandler.obtainMessage(MSG_DISMISS, event, 0 ,SnackBanner.this));
        }
    };



    final void showView() {
        if (mView.getParent() == null){
            final ViewGroup.LayoutParams lp = mView.getLayoutParams();

            if (lp instanceof CoordinatorLayout.LayoutParams){
                final Behavior behaviour  = new Behavior();
                behaviour.setStartAlphaSwipeDistance(0.1f);
                behaviour.setEndAlphaSwipeDistance(0.6f);
                behaviour.setSwipeDirection(SwipeDismissBehavior.SWIPE_DIRECTION_START_TO_END);
                behaviour.setListener(new SwipeDismissBehavior.OnDismissListener() {
                    @Override
                    public void onDismiss(View view) {
                        dispatchDismiss(Callback.DISMISS_EVENT_SWIPE);
                    }

                    @Override
                    public void onDragStateChanged(int state) {
                        switch (state){
                            case SwipeDismissBehavior.STATE_DRAGGING:
                            case SwipeDismissBehavior.STATE_SETTLING:
                                SnackbarManager.getInstance()
                                        .cancelTimeout(mManagerCallback);
                                break;
                            case SwipeDismissBehavior.STATE_IDLE:
                                SnackbarManager.getInstance()
                                        .restoreTimeout(mManagerCallback);
                                break;
                        }
                    }
                });
                ((CoordinatorLayout.LayoutParams)lp).setBehavior(behaviour);
            }
            mParent.addView(mView);
        }

        mView.setOnAttachStateChangeListener(new SnackbarLayout.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {

            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                if (isShownOrQueued()){
                    sHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            onViewHidden(Callback.DISMISS_EVENT_MANUAL);
                        }
                    });
                }
            }
        });

        if (ViewCompat.isLaidOut(mView)){
            animateViewIn();
        }
        else{
            mView.setOnLayoutChangeListener(new SnackbarLayout.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View view, int left, int top, int right, int bottom) {
                    animateViewIn();
                    mView.setOnLayoutChangeListener(null);
                }
            });
        }
    }

    private void animateViewIn(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            ViewCompat.setTranslationY(mView, -mView.getHeight());
            ViewCompat.animate(mView)
                    .translationY(0f)
                    .setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(View view) {
                            mView.animateChildrenIn(ANIMATION_DURATION - ANIMATION_DURATION,ANIMATION_FADE_DURATION);
                        }

                        @Override
                        public void onAnimationEnd(View view) {
                            if (mCallback != null){
                                mCallback.onShow(SnackBanner.this);
                            }
                            SnackbarManager.getInstance()
                                    .onShown(mManagerCallback);
                        }
                    })
                    .start();
        }
        else{

        }
    }

    private void animateViewOut(final int event){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ViewCompat.animate(mView)
                    .translationY(-mView.getHeight())
                    .setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new ViewPropertyAnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(View view) {
                            mView.animateChildrenOut(0, ANIMATION_FADE_DURATION);
                        }

                        @Override
                        public void onAnimationEnd(View view) {
                            onViewHidden(event);
                        }

                    })
                    .start();
        }
        else{

        }
    }

    public SnackBanner setBackgroundColor(@ColorRes int color){
        mView.setBackgroundColor(mContext.getResources().getColor(color));
        return this;
    }


    final void hideView(int event){
        if (mView.getVisibility() != View.VISIBLE || isBeingDragged()){
            onViewHidden(event);
        }
        else {
            animateViewOut(event);
        }
    }

    private void onViewHidden(int event) {
        SnackbarManager.getInstance()
                .onDismissed(mManagerCallback);
        if (mCallback != null){
            mCallback.onDismissed(this, event);
        }
        final ViewParent parent = mView.getParent();
        if (parent instanceof ViewGroup){
            ((ViewGroup)parent).removeView(mView);
        }
    }

    private boolean isBeingDragged(){
        final ViewGroup.LayoutParams lp = mView.getLayoutParams();

        if (lp instanceof CoordinatorLayout.LayoutParams){
            final CoordinatorLayout.LayoutParams cllp = (CoordinatorLayout.LayoutParams) lp;
            final CoordinatorLayout.Behavior behavior = cllp.getBehavior();

            if (behavior instanceof SwipeDismissBehavior){
                return ((SwipeDismissBehavior)behavior).getDragState() != SwipeDismissBehavior.STATE_IDLE;
            }
        }
        return false;
    }

    public static class SnackbarLayout extends LinearLayout {
        private TextView mMessageView;

        private int mMaxWidth;
        private int mMaxInlineActionWidth;

        interface OnLayoutChangeListener {
            void onLayoutChange(View view, int left, int top, int right, int bottom);
        }

        interface OnAttachStateChangeListener {
            void onViewAttachedToWindow(View v);

            void onViewDetachedFromWindow(View v);
        }

        private OnLayoutChangeListener mOnLayoutChangeListener;
        private OnAttachStateChangeListener mOnAttachStateChangeListener;

        public SnackbarLayout(Context context) {
            this(context, null);
        }

        public SnackbarLayout(Context context, AttributeSet attrs) {
            super(context, attrs);
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SnackbarLayout);
            mMaxWidth = a.getDimensionPixelSize(R.styleable.SnackbarLayout_android_maxWidth, -1);
            mMaxInlineActionWidth = a.getDimensionPixelSize(
                    R.styleable.SnackbarLayout_maxActionInlineWidth, -1);
            /*if (a.hasValue(R.styleable.SnackbarLayout_elevation)) {
                ViewCompat.setElevation(this, a.getDimensionPixelSize(
                        R.styleable.SnackbarLayout_elevation, 0));
            }*/
            a.recycle();
            setTextAlignment(TEXT_ALIGNMENT_CENTER);
            setClickable(true);


            LayoutInflater.from(context)
                    .inflate(R.layout.layout_snackbanner_include, this);

            ViewCompat.setAccessibilityLiveRegion(this,
                    ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);
        }

        @Override
        protected void onFinishInflate() {
            super.onFinishInflate();
            mMessageView = (TextView) findViewById(R.id.snackbar_text);
        }

        TextView getMessageView() {
            return mMessageView;
        }


        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

            if (mMaxWidth > 0 && getMeasuredWidth() > mMaxWidth) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(mMaxWidth, MeasureSpec.EXACTLY);
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }

            final int multiLineVPadding = getResources().getDimensionPixelSize(
                    R.dimen.design_snackbar_padding_vertical_2lines);
            final int singleLineVPadding = getResources().getDimensionPixelSize(
                    R.dimen.design_snackbar_padding_vertical);
            final boolean isMultiLine = mMessageView.getLayout()
                    .getLineCount() > 1;

            boolean remeasure = false;
            /*if (isMultiLine && mMaxInlineActionWidth > 0) {
                if (updateViewsWithinLayout(VERTICAL, multiLineVPadding,
                        multiLineVPadding - singleLineVPadding)) {
                    remeasure = true;
                }
            } else {
                final int messagePadding = isMultiLine ? multiLineVPadding : singleLineVPadding;
                if (updateViewsWithinLayout(HORIZONTAL, messagePadding, messagePadding)) {
                    remeasure = true;
                }
            }*/

            if (remeasure) {
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
        }

        void animateChildrenIn(int delay, int duration) {
            ViewCompat.setAlpha(mMessageView, 0f);
            ViewCompat.animate(mMessageView)
                    .alpha(1f)
                    .setDuration(duration)
                    .setStartDelay(delay)
                    .start();

        }

        void animateChildrenOut(int delay, int duration) {
            ViewCompat.setAlpha(mMessageView, 1f);
            ViewCompat.animate(mMessageView)
                    .alpha(0f)
                    .setDuration(duration)
                    .setStartDelay(delay)
                    .start();
        }

        @Override
        protected void onLayout(boolean changed, int l, int t, int r, int b) {
            super.onLayout(changed, l, t, r, b);
            if (mOnLayoutChangeListener != null) {
                mOnLayoutChangeListener.onLayoutChange(this, l, t, r, b);
            }
        }

        @Override
        protected void onAttachedToWindow() {
            super.onAttachedToWindow();
            if (mOnAttachStateChangeListener != null) {
                mOnAttachStateChangeListener.onViewAttachedToWindow(this);
            }
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            if (mOnAttachStateChangeListener != null) {
                mOnAttachStateChangeListener.onViewDetachedFromWindow(this);
            }
        }

        void setOnLayoutChangeListener(OnLayoutChangeListener onLayoutChangeListener) {
            mOnLayoutChangeListener = onLayoutChangeListener;
        }

        void setOnAttachStateChangeListener(OnAttachStateChangeListener listener) {
            mOnAttachStateChangeListener = listener;
        }

        private boolean updateViewsWithinLayout(final int orientation,
                                                final int messagePadTop, final int messagePadBottom) {
            boolean changed = false;
            if (orientation != getOrientation()) {
                setOrientation(orientation);
                changed = true;
            }
            if (mMessageView.getPaddingTop() != messagePadTop
                    || mMessageView.getPaddingBottom() != messagePadBottom) {
                updateTopBottomPadding(mMessageView, messagePadTop, messagePadBottom);
                changed = true;
            }
            return changed;
        }

        private static void updateTopBottomPadding(View view, int topPadding, int bottomPadding) {
            if (ViewCompat.isPaddingRelative(view)) {
                ViewCompat.setPaddingRelative(view,
                        ViewCompat.getPaddingStart(view), topPadding,
                        ViewCompat.getPaddingEnd(view), bottomPadding);
            } else {
                view.setPadding(view.getPaddingLeft(), topPadding,
                        view.getPaddingRight(), bottomPadding);
            }
        }
    }


    final class Behavior extends SwipeDismissBehavior<SnackbarLayout>{

        @Override
        public boolean canSwipeDismissView(@NonNull View view) {
            return view instanceof SnackbarLayout;
        }

        @Override
        public boolean onInterceptTouchEvent(@NonNull CoordinatorLayout parent, @NonNull SnackbarLayout child, @NonNull MotionEvent event) {
            if (parent.isPointInChildBounds(child, (int) event.getX(), (int) event.getY())){
                switch (event.getActionMasked()){
                    case MotionEvent.ACTION_DOWN:
                        SnackbarManager.getInstance()
                                .cancelTimeout(mManagerCallback);
                        break;
                    case MotionEvent.ACTION_UP:

                    case MotionEvent.ACTION_CANCEL:
                        SnackbarManager.getInstance()
                                .restoreTimeout(mManagerCallback);
                        break;
                }
            }
            return super.onInterceptTouchEvent(parent, child, event);
        }
    }

}
