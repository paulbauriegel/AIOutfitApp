package com.example.aioutfit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.View;

class CropImageView extends View {
    private static final int NONE = 0;
    private static final int MOVE = 1;
    private static final int LEFT = 2;
    private static final int TOP = 4;
    private static final int RIGHT = 8;
    private static final int BOTTOM = 16;

    private final Paint imagePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint dimPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF imageRect = new RectF();
    private final RectF cropRect = new RectF();
    private Bitmap bitmap;
    private float scale = 1f;
    private float lastX;
    private float lastY;
    private int dragMode = NONE;
    private boolean cropInitialized;

    CropImageView(Context context, int surface, int primary) {
        super(context);
        setBackgroundColor(surface);
        dimPaint.setColor(0x88000000);
        borderPaint.setColor(primary);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(dp(2));
        handlePaint.setColor(primary);
        handlePaint.setStyle(Paint.Style.FILL);
    }

    void setBitmap(Bitmap bitmap) {
        this.bitmap = bitmap;
        cropInitialized = false;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        layoutImage();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap == null) return;
        layoutImage();
        canvas.drawBitmap(bitmap, null, imageRect, imagePaint);
        drawCropOverlay(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (bitmap == null || imageRect.isEmpty()) return true;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastX = event.getX();
                lastY = event.getY();
                dragMode = hitMode(lastX, lastY);
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            case MotionEvent.ACTION_MOVE:
                float dx = event.getX() - lastX;
                float dy = event.getY() - lastY;
                updateCropRect(dx, dy);
                lastX = event.getX();
                lastY = event.getY();
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                dragMode = NONE;
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
                return true;
            default:
                return true;
        }
    }

    Bitmap createCroppedBitmap() {
        if (bitmap == null || cropRect.isEmpty() || imageRect.isEmpty()) return null;
        float left = (cropRect.left - imageRect.left) / scale;
        float top = (cropRect.top - imageRect.top) / scale;
        float right = (cropRect.right - imageRect.left) / scale;
        float bottom = (cropRect.bottom - imageRect.top) / scale;
        int x = Math.max(0, Math.min(bitmap.getWidth() - 1, Math.round(left)));
        int y = Math.max(0, Math.min(bitmap.getHeight() - 1, Math.round(top)));
        int cropRight = Math.max(x + 1, Math.min(bitmap.getWidth(), Math.round(right)));
        int cropBottom = Math.max(y + 1, Math.min(bitmap.getHeight(), Math.round(bottom)));
        int width = cropRight - x;
        int height = cropBottom - y;
        if (width <= 0 || height <= 0) return null;
        return Bitmap.createBitmap(bitmap, x, y, width, height);
    }

    private void layoutImage() {
        if (bitmap == null || getWidth() == 0 || getHeight() == 0) return;
        float availableWidth = getWidth() - dp(24);
        float availableHeight = getHeight() - dp(24);
        scale = Math.min(availableWidth / bitmap.getWidth(), availableHeight / bitmap.getHeight());
        float drawWidth = bitmap.getWidth() * scale;
        float drawHeight = bitmap.getHeight() * scale;
        float left = (getWidth() - drawWidth) / 2f;
        float top = (getHeight() - drawHeight) / 2f;
        imageRect.set(left, top, left + drawWidth, top + drawHeight);
        if (!cropInitialized) {
            float insetX = imageRect.width() * 0.1f;
            float insetY = imageRect.height() * 0.1f;
            cropRect.set(imageRect.left + insetX, imageRect.top + insetY, imageRect.right - insetX, imageRect.bottom - insetY);
            cropInitialized = true;
        }
        clampCropRect();
    }

    private void drawCropOverlay(Canvas canvas) {
        canvas.drawRect(imageRect.left, imageRect.top, imageRect.right, cropRect.top, dimPaint);
        canvas.drawRect(imageRect.left, cropRect.bottom, imageRect.right, imageRect.bottom, dimPaint);
        canvas.drawRect(imageRect.left, cropRect.top, cropRect.left, cropRect.bottom, dimPaint);
        canvas.drawRect(cropRect.right, cropRect.top, imageRect.right, cropRect.bottom, dimPaint);
        canvas.drawRect(cropRect, borderPaint);

        float thirdWidth = cropRect.width() / 3f;
        float thirdHeight = cropRect.height() / 3f;
        canvas.drawLine(cropRect.left + thirdWidth, cropRect.top, cropRect.left + thirdWidth, cropRect.bottom, borderPaint);
        canvas.drawLine(cropRect.left + thirdWidth * 2, cropRect.top, cropRect.left + thirdWidth * 2, cropRect.bottom, borderPaint);
        canvas.drawLine(cropRect.left, cropRect.top + thirdHeight, cropRect.right, cropRect.top + thirdHeight, borderPaint);
        canvas.drawLine(cropRect.left, cropRect.top + thirdHeight * 2, cropRect.right, cropRect.top + thirdHeight * 2, borderPaint);

        float handle = dp(6);
        canvas.drawCircle(cropRect.left, cropRect.top, handle, handlePaint);
        canvas.drawCircle(cropRect.right, cropRect.top, handle, handlePaint);
        canvas.drawCircle(cropRect.left, cropRect.bottom, handle, handlePaint);
        canvas.drawCircle(cropRect.right, cropRect.bottom, handle, handlePaint);
    }

    private int hitMode(float x, float y) {
        float edge = dp(30);
        int mode = NONE;
        if (Math.abs(x - cropRect.left) <= edge) mode |= LEFT;
        if (Math.abs(x - cropRect.right) <= edge) mode |= RIGHT;
        if (Math.abs(y - cropRect.top) <= edge) mode |= TOP;
        if (Math.abs(y - cropRect.bottom) <= edge) mode |= BOTTOM;
        if (mode != NONE && x >= cropRect.left - edge && x <= cropRect.right + edge
                && y >= cropRect.top - edge && y <= cropRect.bottom + edge) {
            return mode;
        }
        return cropRect.contains(x, y) ? MOVE : NONE;
    }

    private void updateCropRect(float dx, float dy) {
        if (dragMode == NONE) return;
        if (dragMode == MOVE) {
            cropRect.offset(dx, dy);
            clampCropRect();
            return;
        }
        float minSize = dp(56);
        if ((dragMode & LEFT) != 0) cropRect.left = Math.min(cropRect.left + dx, cropRect.right - minSize);
        if ((dragMode & RIGHT) != 0) cropRect.right = Math.max(cropRect.right + dx, cropRect.left + minSize);
        if ((dragMode & TOP) != 0) cropRect.top = Math.min(cropRect.top + dy, cropRect.bottom - minSize);
        if ((dragMode & BOTTOM) != 0) cropRect.bottom = Math.max(cropRect.bottom + dy, cropRect.top + minSize);
        clampCropRect();
    }

    private void clampCropRect() {
        if (imageRect.isEmpty() || cropRect.isEmpty()) return;
        if (cropRect.width() > imageRect.width()) {
            cropRect.left = imageRect.left;
            cropRect.right = imageRect.right;
        }
        if (cropRect.height() > imageRect.height()) {
            cropRect.top = imageRect.top;
            cropRect.bottom = imageRect.bottom;
        }
        if (cropRect.left < imageRect.left) cropRect.offset(imageRect.left - cropRect.left, 0);
        if (cropRect.right > imageRect.right) cropRect.offset(imageRect.right - cropRect.right, 0);
        if (cropRect.top < imageRect.top) cropRect.offset(0, imageRect.top - cropRect.top);
        if (cropRect.bottom > imageRect.bottom) cropRect.offset(0, imageRect.bottom - cropRect.bottom);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
