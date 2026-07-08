package com.example.aioutfit;

import android.graphics.Color;

public class AppPalette {
    public final int primary;
    public final int onPrimary;
    public final int primaryContainer;
    public final int onPrimaryContainer;
    public final int ink;
    public final int muted;
    public final int surface;
    public final int surfaceContainer;
    public final int surfaceDialog;
    public final int outline;
    public final int outlineVariant;
    public final int placeholder;
    public final int iconMuted;
    public final int rowText;
    public final int arrow;
    public final int neutralButtonContainer;
    public final int neutralButtonText;
    public final int inactiveChipText;
    public final int dangerOutline;
    public final int overlayScrim;
    public final int error;

    public AppPalette(int primary) {
        this.primary = primary;
        this.onPrimary = Color.WHITE;
        this.primaryContainer = blend(primary, Color.WHITE, 0.78f);
        this.onPrimaryContainer = blend(primary, Color.BLACK, 0.45f);
        this.ink = Color.rgb(26, 28, 27);
        this.muted = Color.rgb(89, 99, 94);
        this.surface = Color.rgb(252, 248, 253);
        this.surfaceDialog = Color.rgb(255, 251, 255);
        this.surfaceContainer = blend(primary, Color.WHITE, 0.88f);
        this.outline = Color.rgb(203, 199, 190);
        this.outlineVariant = Color.rgb(234, 229, 236);
        this.placeholder = Color.rgb(210, 210, 210);
        this.iconMuted = Color.rgb(70, 70, 70);
        this.rowText = Color.rgb(78, 78, 78);
        this.arrow = Color.rgb(65, 65, 65);
        this.neutralButtonContainer = Color.rgb(232, 232, 232);
        this.neutralButtonText = Color.rgb(32, 32, 32);
        this.inactiveChipText = Color.rgb(154, 154, 154);
        this.dangerOutline = Color.rgb(221, 185, 187);
        this.overlayScrim = Color.argb(140, 30, 30, 30);
        this.error = Color.rgb(150, 36, 45);
    }

    public int tonalSurface() {
        return blend(primary, Color.WHITE, 0.92f);
    }

    public int softSurface() {
        return blend(primary, Color.WHITE, 0.95f);
    }

    public int outlineVariant() {
        return outlineVariant;
    }

    private static int blend(int color, int target, float targetAmount) {
        float sourceAmount = 1f - targetAmount;
        return Color.rgb(
                Math.round(Color.red(color) * sourceAmount + Color.red(target) * targetAmount),
                Math.round(Color.green(color) * sourceAmount + Color.green(target) * targetAmount),
                Math.round(Color.blue(color) * sourceAmount + Color.blue(target) * targetAmount)
        );
    }
}
