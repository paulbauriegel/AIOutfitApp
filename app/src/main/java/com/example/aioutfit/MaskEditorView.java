package com.example.aioutfit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

class MaskEditorView extends View {
    private final Paint imagePaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final Paint checkerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint brushPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint cursorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF imageRect = new RectF();
    private final List<Bitmap> undoStack = new ArrayList<>();
    private final ScaleGestureDetector scaleDetector;
    private final int primary;
    private final int error;
    private Bitmap source;
    private Bitmap mask;
    private Bitmap originalMask;
    private Canvas maskCanvas;
    private boolean restoreMode;
    private boolean touching;
    private boolean panning;
    private boolean transparentSource;
    private float baseScale = 1f;
    private float zoomScale = 1f;
    private float panX;
    private float panY;
    private float lastBitmapX;
    private float lastBitmapY;
    private float lastPanX;
    private float lastPanY;
    private float cursorX;
    private float cursorY;
    int brushSizeDp = 32;

    MaskEditorView(Context context, int surface, int primary, int error) {
        super(context);
        this.primary = primary;
        this.error = error;
        setBackgroundColor(surface);
        maskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        cursorPaint.setStyle(Paint.Style.STROKE);
        cursorPaint.setStrokeWidth(dp(2));
        scaleDetector = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                float before = zoomScale;
                zoomScale *= detector.getScaleFactor();
                zoomScale = Math.max(1f, Math.min(zoomScale, 6f));
                float factor = zoomScale / before;
                panX = detector.getFocusX() - (detector.getFocusX() - panX) * factor;
                panY = detector.getFocusY() - (detector.getFocusY() - panY) * factor;
                constrainPan();
                layoutImage();
                invalidate();
                return true;
            }
        });
    }

    void setBitmap(Bitmap bitmap) {
        setBitmap(bitmap, hasTransparentPixels(bitmap));
    }

    void setBitmap(Bitmap bitmap, boolean hasTransparency) {
        source = bitmap.getConfig() == Bitmap.Config.ARGB_8888 ? bitmap : bitmap.copy(Bitmap.Config.ARGB_8888, false);
        mask = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        originalMask = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        maskCanvas = new Canvas(mask);
        transparentSource = hasTransparency;
        if (transparentSource) {
            fillMaskFromAlpha(source, mask);
        } else {
            maskCanvas.drawColor(Color.WHITE);
        }
        new Canvas(originalMask).drawBitmap(mask, 0, 0, null);
        undoStack.clear();
        zoomScale = 1f;
        panX = 0f;
        panY = 0f;
        layoutImage();
        invalidate();
    }

    boolean sourceHasTransparency() {
        return transparentSource;
    }

    boolean isRestoreMode() {
        return restoreMode;
    }

    void setRestoreMode(boolean restoreMode) {
        this.restoreMode = restoreMode;
        invalidate();
    }

    void setBrushSizeDp(int brushSizeDp) {
        this.brushSizeDp = brushSizeDp;
        invalidate();
    }

    void setMaskFromAlpha(Bitmap foreground) {
        if (source == null || mask == null || foreground == null) return;
        Bitmap scaled = foreground;
        if (foreground.getWidth() != source.getWidth() || foreground.getHeight() != source.getHeight()) {
            scaled = Bitmap.createScaledBitmap(foreground, source.getWidth(), source.getHeight(), true);
        }
        fillMaskFromAlpha(scaled, mask);
        new Canvas(originalMask).drawBitmap(mask, 0, 0, null);
        undoStack.clear();
        invalidate();
    }

    void undo() {
        if (undoStack.isEmpty() || mask == null) {
            Toast.makeText(getContext(), getContext().getString(R.string.nothing_to_undo), Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap previous = undoStack.remove(undoStack.size() - 1);
        Canvas canvas = new Canvas(mask);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(previous, 0, 0, null);
        maskCanvas = new Canvas(mask);
        invalidate();
    }

    void resetMask() {
        if (mask == null || originalMask == null) return;
        pushUndo();
        Canvas canvas = new Canvas(mask);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(originalMask, 0, 0, null);
        maskCanvas = new Canvas(mask);
        invalidate();
    }

    Bitmap createMaskedBitmap() {
        if (source == null || mask == null) return null;
        Bitmap output = Bitmap.createBitmap(source.getWidth(), source.getHeight(), Bitmap.Config.ARGB_8888);
        int width = source.getWidth();
        int height = source.getHeight();
        int[] sourcePixels = new int[width];
        int[] maskPixels = new int[width];
        int[] outputPixels = new int[width];
        for (int y = 0; y < height; y++) {
            source.getPixels(sourcePixels, 0, width, 0, y, width, 1);
            mask.getPixels(maskPixels, 0, width, 0, y, width, 1);
            for (int x = 0; x < width; x++) {
                int sourceColor = sourcePixels[x];
                int alpha = Color.alpha(sourceColor) * Color.alpha(maskPixels[x]) / 255;
                outputPixels[x] = (sourceColor & 0x00FFFFFF) | (alpha << 24);
            }
            output.setPixels(outputPixels, 0, width, 0, y, width, 1);
        }
        return output;
    }

    @Override
    protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
        super.onSizeChanged(width, height, oldWidth, oldHeight);
        layoutImage();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (source == null || mask == null) return;
        drawCheckerboard(canvas);
        int layer = canvas.saveLayer(imageRect, null);
        canvas.drawBitmap(source, null, imageRect, imagePaint);
        canvas.drawBitmap(mask, null, imageRect, maskPaint);
        canvas.restoreToCount(layer);
        if (touching) {
            cursorPaint.setColor(restoreMode ? primary : error);
            canvas.drawCircle(cursorX, cursorY, dp(brushSizeDp) / 2f, cursorPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (source == null || mask == null || imageRect.isEmpty()) return true;
        scaleDetector.onTouchEvent(event);
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (!imageRect.contains(event.getX(), event.getY())) return true;
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                lastPanX = event.getX();
                lastPanY = event.getY();
                panning = false;
                pushUndo();
                touching = true;
                cursorX = event.getX();
                cursorY = event.getY();
                float[] start = toBitmapPoint(event.getX(), event.getY());
                lastBitmapX = start[0];
                lastBitmapY = start[1];
                paintMaskPoint(lastBitmapX, lastBitmapY);
                invalidate();
                return true;
            case MotionEvent.ACTION_POINTER_DOWN:
                panning = true;
                touching = false;
                undoIfLastStrokeWasOnlyStart();
                lastPanX = averageX(event);
                lastPanY = averageY(event);
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(true);
                invalidate();
                return true;
            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() > 1 || scaleDetector.isInProgress()) {
                    panning = true;
                    touching = false;
                    float centerX = averageX(event);
                    float centerY = averageY(event);
                    if (!scaleDetector.isInProgress() && zoomScale > 1f) {
                        panX += centerX - lastPanX;
                        panY += centerY - lastPanY;
                        constrainPan();
                        layoutImage();
                    }
                    lastPanX = centerX;
                    lastPanY = centerY;
                    invalidate();
                    return true;
                }
                if (panning) return true;
                cursorX = event.getX();
                cursorY = event.getY();
                float[] current = toBitmapPoint(event.getX(), event.getY());
                paintMaskLine(lastBitmapX, lastBitmapY, current[0], current[1]);
                lastBitmapX = current[0];
                lastBitmapY = current[1];
                invalidate();
                return true;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                touching = false;
                panning = false;
                if (getParent() != null) getParent().requestDisallowInterceptTouchEvent(false);
                invalidate();
                return true;
            default:
                return true;
        }
    }

    private void layoutImage() {
        if (source == null || getWidth() == 0 || getHeight() == 0) return;
        float availableWidth = getWidth() - dp(24);
        float availableHeight = getHeight() - dp(24);
        baseScale = Math.min(availableWidth / source.getWidth(), availableHeight / source.getHeight());
        float scale = drawingScale();
        float drawWidth = source.getWidth() * scale;
        float drawHeight = source.getHeight() * scale;
        float left = (getWidth() - drawWidth) / 2f + panX;
        float top = (getHeight() - drawHeight) / 2f + panY;
        imageRect.set(left, top, left + drawWidth, top + drawHeight);
    }

    private void drawCheckerboard(Canvas canvas) {
        int save = canvas.save();
        canvas.clipRect(imageRect);
        int size = dp(14);
        for (float y = imageRect.top; y < imageRect.bottom; y += size) {
            for (float x = imageRect.left; x < imageRect.right; x += size) {
                int index = (((int) ((x - imageRect.left) / size)) + ((int) ((y - imageRect.top) / size))) % 2;
                checkerPaint.setColor(index == 0 ? 0xFFEFEFEF : 0xFFDCDCDC);
                canvas.drawRect(x, y, x + size, y + size, checkerPaint);
            }
        }
        canvas.restoreToCount(save);
    }

    private float[] toBitmapPoint(float viewX, float viewY) {
        float scale = drawingScale();
        float x = (viewX - imageRect.left) / scale;
        float y = (viewY - imageRect.top) / scale;
        x = Math.max(0, Math.min(source.getWidth() - 1, x));
        y = Math.max(0, Math.min(source.getHeight() - 1, y));
        return new float[]{x, y};
    }

    private void paintMaskLine(float startX, float startY, float endX, float endY) {
        float distance = (float) Math.hypot(endX - startX, endY - startY);
        float step = Math.max(1f, dp(brushSizeDp) / drawingScale() / 4f);
        int count = Math.max(1, (int) (distance / step));
        for (int i = 0; i <= count; i++) {
            float t = i / (float) count;
            paintMaskPoint(startX + (endX - startX) * t, startY + (endY - startY) * t);
        }
    }

    private void paintMaskPoint(float bitmapX, float bitmapY) {
        if (maskCanvas == null) return;
        brushPaint.setColor(restoreMode ? Color.WHITE : Color.TRANSPARENT);
        brushPaint.setStyle(Paint.Style.FILL);
        brushPaint.setXfermode(restoreMode ? null : new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        float radius = dp(brushSizeDp) / drawingScale() / 2f;
        maskCanvas.drawCircle(bitmapX, bitmapY, radius, brushPaint);
        brushPaint.setXfermode(null);
    }

    private float drawingScale() {
        return baseScale * zoomScale;
    }

    private void constrainPan() {
        if (source == null || getWidth() == 0 || getHeight() == 0) return;
        if (zoomScale <= 1f) {
            panX = 0f;
            panY = 0f;
            return;
        }
        float drawWidth = source.getWidth() * drawingScale();
        float drawHeight = source.getHeight() * drawingScale();
        float maxX = Math.max(0f, (drawWidth - getWidth()) / 2f + dp(24));
        float maxY = Math.max(0f, (drawHeight - getHeight()) / 2f + dp(24));
        panX = Math.max(-maxX, Math.min(maxX, panX));
        panY = Math.max(-maxY, Math.min(maxY, panY));
    }

    private float averageX(MotionEvent event) {
        float sum = 0f;
        for (int i = 0; i < event.getPointerCount(); i++) {
            sum += event.getX(i);
        }
        return sum / event.getPointerCount();
    }

    private float averageY(MotionEvent event) {
        float sum = 0f;
        for (int i = 0; i < event.getPointerCount(); i++) {
            sum += event.getY(i);
        }
        return sum / event.getPointerCount();
    }

    private void undoIfLastStrokeWasOnlyStart() {
        if (undoStack.isEmpty() || mask == null) return;
        Bitmap previous = undoStack.remove(undoStack.size() - 1);
        Canvas canvas = new Canvas(mask);
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        canvas.drawBitmap(previous, 0, 0, null);
        maskCanvas = new Canvas(mask);
    }

    private void pushUndo() {
        if (mask == null) return;
        if (undoStack.size() >= 10) {
            undoStack.remove(0);
        }
        undoStack.add(mask.copy(Bitmap.Config.ARGB_8888, false));
    }

    private boolean hasTransparentPixels(Bitmap bitmap) {
        return bitmapHasTransparentPixels(bitmap);
    }

    static boolean bitmapHasTransparentPixels(Bitmap bitmap) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int stepX = Math.max(1, width / 80);
        int stepY = Math.max(1, height / 80);
        for (int y = 0; y < height; y += stepY) {
            for (int x = 0; x < width; x += stepX) {
                if (Color.alpha(bitmap.getPixel(x, y)) < 250) return true;
            }
        }
        return false;
    }

    private void fillMaskFromAlpha(Bitmap sourceAlpha, Bitmap targetMask) {
        int width = targetMask.getWidth();
        int height = targetMask.getHeight();
        int[] row = new int[width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int alpha = Color.alpha(sourceAlpha.getPixel(x, y));
                row[x] = Color.argb(alpha, 255, 255, 255);
            }
            targetMask.setPixels(row, 0, width, 0, y, width, 1);
        }
        maskCanvas = new Canvas(targetMask);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
