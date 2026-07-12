package com.example.aioutfit;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.ImageView;

class ZoomImageView extends ImageView {
    private final ScaleGestureDetector scaleDetector;
    private float currentScale = 1f;
    private float lastX;
    private float lastY;
    private float downX;
    private float downY;
    private SwipeListener swipeListener;

    ZoomImageView(Context context) {
        super(context);
        setScaleType(ImageView.ScaleType.FIT_CENTER);
        setClickable(true);
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                currentScale *= detector.getScaleFactor();
                currentScale = Math.max(1f, Math.min(currentScale, 4f));
                setPivotX(detector.getFocusX());
                setPivotY(detector.getFocusY());
                setScaleX(currentScale);
                setScaleY(currentScale);
                if (currentScale == 1f) {
                    resetTranslation();
                }
                return true;
            }
        });
    }

    void setSwipeListener(SwipeListener listener) {
        swipeListener = listener;
    }

    void resetZoom() {
        currentScale = 1f;
        setScaleX(1f);
        setScaleY(1f);
        resetTranslation();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleDetector.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                downY = event.getY();
                lastX = event.getX();
                lastY = event.getY();
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            case MotionEvent.ACTION_MOVE:
                if (!scaleDetector.isInProgress() && currentScale > 1f) {
                    float dx = event.getX() - lastX;
                    float dy = event.getY() - lastY;
                    setTranslationX(getTranslationX() + dx);
                    setTranslationY(getTranslationY() + dy);
                    lastX = event.getX();
                    lastY = event.getY();
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
                if (currentScale <= 1.05f && swipeListener != null) {
                    float deltaX = event.getX() - downX;
                    float deltaY = event.getY() - downY;
                    if (Math.abs(deltaX) > dp(54) && Math.abs(deltaX) > Math.abs(deltaY) * 1.35f) {
                        swipeListener.onSwipe(deltaX < 0 ? 1 : -1);
                        return true;
                    }
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
                return true;
            default:
                return true;
        }
    }

    private void resetTranslation() {
        setTranslationX(0f);
        setTranslationY(0f);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
