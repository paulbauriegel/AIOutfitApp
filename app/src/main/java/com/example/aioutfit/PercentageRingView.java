package com.example.aioutfit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.view.View;

class PercentageRingView extends View {
    private final Paint trackPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint progressPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final int percent;

    PercentageRingView(Context context, int percent, int trackColor, int progressColor, int textColor) {
        super(context);
        this.percent = Math.max(0, Math.min(100, percent));
        trackPaint.setColor(trackColor);
        trackPaint.setStyle(Paint.Style.STROKE);
        trackPaint.setStrokeCap(Paint.Cap.ROUND);
        trackPaint.setStrokeWidth(dp(7));
        progressPaint.setColor(progressColor);
        progressPaint.setStyle(Paint.Style.STROKE);
        progressPaint.setStrokeCap(Paint.Cap.ROUND);
        progressPaint.setStrokeWidth(dp(7));
        textPaint.setColor(textColor);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.DEFAULT_BOLD);
        textPaint.setTextSize(dp(18));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float inset = dp(10);
        RectF rect = new RectF(inset, inset, getWidth() - inset, getHeight() - inset);
        canvas.drawArc(rect, -90, 360, false, trackPaint);
        canvas.drawArc(rect, -90, 360f * percent / 100f, false, progressPaint);
        Paint.FontMetrics metrics = textPaint.getFontMetrics();
        float y = getHeight() / 2f - (metrics.ascent + metrics.descent) / 2f;
        canvas.drawText(percent + "%", getWidth() / 2f, y, textPaint);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
