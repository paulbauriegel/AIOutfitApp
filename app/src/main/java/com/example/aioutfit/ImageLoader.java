package com.example.aioutfit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.InputStream;
import java.util.concurrent.ExecutorService;

class ImageLoader {
    private final Context context;
    private final ExecutorService executor;
    private final Handler mainHandler;
    private final LruCache<String, Bitmap> imageCache;

    ImageLoader(Context context, ExecutorService executor, Handler mainHandler) {
        this.context = context.getApplicationContext();
        this.executor = executor;
        this.mainHandler = mainHandler;
        int maxKb = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheKb = Math.max(8 * 1024, maxKb / 8);
        imageCache = new LruCache<String, Bitmap>(cacheKb) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    void load(ImageView target, String uri, int fallbackRes, int fallbackColor, int targetPx) {
        String cleanUri = uri == null ? "" : uri.trim();
        if (cleanUri.isEmpty()) {
            target.setTag(null);
            target.setImageResource(fallbackRes);
            target.setColorFilter(fallbackColor);
            return;
        }

        String key = cleanUri + "@" + targetPx;
        target.setTag(key);
        Bitmap cached = imageCache.get(key);
        if (cached != null) {
            target.clearColorFilter();
            target.setImageBitmap(cached);
            return;
        }

        target.setImageResource(fallbackRes);
        target.setColorFilter(fallbackColor);
        executor.execute(() -> {
            Bitmap bitmap = decodeScaledBitmap(cleanUri, targetPx, targetPx);
            if (bitmap != null) {
                imageCache.put(key, bitmap);
            }
            mainHandler.post(() -> {
                Object tag = target.getTag();
                if (!key.equals(tag)) return;
                if (bitmap != null) {
                    target.clearColorFilter();
                    target.setImageBitmap(bitmap);
                }
            });
        });
    }

    private Bitmap decodeScaledBitmap(String uri, int reqWidth, int reqHeight) {
        try {
            BitmapFactory.Options bounds = new BitmapFactory.Options();
            bounds.inJustDecodeBounds = true;
            try (InputStream stream = context.getContentResolver().openInputStream(Uri.parse(uri))) {
                BitmapFactory.decodeStream(stream, null, bounds);
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize(bounds, reqWidth, reqHeight);
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            try (InputStream stream = context.getContentResolver().openInputStream(Uri.parse(uri))) {
                return BitmapFactory.decodeStream(stream, null, options);
            }
        } catch (Exception ignored) {
            return null;
        }
    }

    private int sampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int height = options.outHeight;
        int width = options.outWidth;
        int sample = 1;
        if (height > reqHeight || width > reqWidth) {
            int halfHeight = height / 2;
            int halfWidth = width / 2;
            while ((halfHeight / sample) >= reqHeight && (halfWidth / sample) >= reqWidth) {
                sample *= 2;
            }
        }
        return Math.max(1, sample);
    }
}
