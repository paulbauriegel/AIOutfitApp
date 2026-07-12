package com.example.aioutfit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

class OutfitShareImageRenderer {
    static final int WIDTH = 1080;
    static final int HEIGHT = 1350;

    private final Context context;
    private final AppPalette palette;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.SUBPIXEL_TEXT_FLAG);
    private final Map<String, Integer> colorMap = new LinkedHashMap<>();

    OutfitShareImageRenderer(Context context, AppPalette palette) {
        this.context = context;
        this.palette = palette;
        seedColorMap();
    }

    Bitmap render(Outfit outfit, List<ClothingItem> items) {
        Bitmap output = Bitmap.createBitmap(WIDTH, HEIGHT, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        canvas.drawColor(Color.rgb(252, 248, 240));
        drawHeader(canvas, outfit);
        drawItems(canvas, items);
        drawMetadata(canvas, outfit, items);
        drawFooter(canvas, outfit, items.size());
        return output;
    }

    private void drawHeader(Canvas canvas, Outfit outfit) {
        textPaint.setColor(Color.rgb(16, 55, 48));
        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setLetterSpacing(0.18f);
        textPaint.setTextSize(64);
        canvas.drawText("MY STYLE", WIDTH / 2f, 92, textPaint);

        textPaint.setColor(Color.rgb(30, 30, 30));
        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        textPaint.setLetterSpacing(0.12f);
        textPaint.setTextSize(22);
        canvas.drawText(styleLine(outfit), WIDTH / 2f, 148, textPaint);
        textPaint.setLetterSpacing(0f);

        drawLine(canvas, 48, 208, 402, 208, Color.rgb(190, 184, 174));
        drawLine(canvas, 678, 208, WIDTH - 48, 208, Color.rgb(190, 184, 174));
        drawLabel(canvas, "THE OUTFIT", WIDTH / 2f, 208);
    }

    private void drawItems(Canvas canvas, List<ClothingItem> items) {
        int visible = Math.min(6, items.size());
        int columns = visible <= 2 ? visible : 3;
        if (columns <= 0) columns = 1;
        int rows = visible <= 3 ? 1 : 2;
        float startX = 48;
        float startY = 250;
        float cellW = (WIDTH - 96) / 3f;
        float cellH = rows == 1 ? 410 : 320;
        for (int i = 0; i < visible; i++) {
            int col = rows == 1 ? i : i % 3;
            int row = rows == 1 ? 0 : i / 3;
            float x = startX + col * cellW;
            float y = startY + row * cellH;
            drawItemCell(canvas, items.get(i), x, y, cellW, cellH - 10);
        }
        if (rows > 1) {
            drawLine(canvas, 48, 890, WIDTH - 48, 890, Color.rgb(214, 207, 195));
        }
    }

    private void drawItemCell(Canvas canvas, ClothingItem item, float x, float y, float w, float h) {
        float imageTop = y + 6;
        float imageBottom = y + h - 92;
        RectF imageBox = new RectF(x + 18, imageTop, x + w - 18, imageBottom);
        Bitmap bitmap = decodeBitmap(item.imageUri);
        if (bitmap != null) {
            drawBitmapFitCenter(canvas, bitmap, imageBox);
        } else {
            drawPlaceholder(canvas, item, imageBox);
        }

        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setColor(Color.rgb(28, 28, 28));
        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        textPaint.setTextSize(22);
        textPaint.setLetterSpacing(0.08f);
        canvas.drawText(clean(value(item.category, "ITEM")).toUpperCase(Locale.ROOT), x + w / 2f, y + h - 46, textPaint);
        textPaint.setLetterSpacing(0f);

        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        textPaint.setTextSize(20);
        textPaint.setColor(Color.rgb(28, 28, 28));
        drawCenteredSingleLine(canvas, value(item.name, item.category), x + 18, x + w - 18, y + h - 16, 20);

        if (x > 60) {
            drawLine(canvas, x, y + 10, x, y + h - 28, Color.rgb(224, 216, 204));
        }
    }

    private void drawMetadata(Canvas canvas, Outfit outfit, List<ClothingItem> items) {
        float top = 930;
        drawLine(canvas, 48, top - 28, WIDTH - 48, top - 28, Color.rgb(214, 207, 195));
        float colW = (WIDTH - 96) / 3f;
        drawMetadataColumn(canvas, "SEASON", seasonLines(outfit, items), 48, top, colW);
        drawMetadataColumn(canvas, "OCCASION", occasionLines(outfit), 48 + colW, top, colW);
        drawColorColumn(canvas, items, 48 + colW * 2, top, colW);
    }

    private void drawMetadataColumn(Canvas canvas, String title, List<String> lines, float x, float y, float w) {
        drawLabel(canvas, title, x + w / 2f, y);
        textPaint.setTextAlign(Paint.Align.LEFT);
        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        textPaint.setTextSize(25);
        textPaint.setColor(Color.rgb(28, 28, 28));
        float lineY = y + 76;
        for (String line : lines) {
            if (lineY > y + 195) break;
            canvas.drawText(line, x + 44, lineY, textPaint);
            lineY += 52;
        }
        if (x > 60) {
            drawLine(canvas, x, y - 10, x, y + 220, Color.rgb(224, 216, 204));
        }
    }

    private void drawColorColumn(Canvas canvas, List<ClothingItem> items, float x, float y, float w) {
        drawLabel(canvas, "COLOR PALETTE", x + w / 2f, y);
        List<Integer> colors = paletteColors(items);
        float dot = 50;
        for (int i = 0; i < colors.size(); i++) {
            int row = i / 3;
            int col = i % 3;
            float cx = x + 64 + col * 86;
            float cy = y + 74 + row * 82;
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(colors.get(i));
            canvas.drawCircle(cx, cy, dot / 2f, paint);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            paint.setColor(Color.argb(90, 0, 0, 0));
            canvas.drawCircle(cx, cy, dot / 2f, paint);
            paint.setStyle(Paint.Style.FILL);
        }
        drawLine(canvas, x, y - 10, x, y + 220, Color.rgb(224, 216, 204));
    }

    private void drawFooter(Canvas canvas, Outfit outfit, int itemCount) {
        drawLine(canvas, 48, 1236, WIDTH - 48, 1236, Color.rgb(214, 207, 195));
        String footer = value(outfit.notes, "Created with AI Outfit.");
        if (itemCount > 6) {
            footer = joinNonEmpty(footer, "+" + (itemCount - 6) + " more items");
        }
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        textPaint.setColor(Color.rgb(32, 32, 32));
        textPaint.setTextSize(24);
        drawCenteredWrappedText(canvas, footer, 170, WIDTH - 170, 1282, 32, 2);
        textPaint.setColor(palette.primary);
        textPaint.setTextSize(26);
        canvas.drawText("✦", WIDTH / 2f, 1330, textPaint);
    }

    private void drawLabel(Canvas canvas, String label, float centerX, float centerY) {
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD));
        textPaint.setLetterSpacing(0.1f);
        textPaint.setTextSize(24);
        textPaint.setColor(Color.WHITE);
        Rect textBounds = new Rect();
        textPaint.getTextBounds(label, 0, label.length(), textBounds);
        float width = textBounds.width() + 54;
        RectF box = new RectF(centerX - width / 2f, centerY - 27, centerX + width / 2f, centerY + 27);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(palette.primary);
        canvas.drawRoundRect(box, 0, 0, paint);
        canvas.drawText(label, centerX, centerY + 9, textPaint);
        textPaint.setLetterSpacing(0f);
    }

    private void drawPlaceholder(Canvas canvas, ClothingItem item, RectF box) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.rgb(245, 242, 236));
        canvas.drawRoundRect(box, 18, 18, paint);
        Drawable drawable = context.getDrawable(categoryIcon(item.category));
        if (drawable == null) return;
        int size = Math.round(Math.min(box.width(), box.height()) * 0.38f);
        int left = Math.round(box.centerX() - size / 2f);
        int top = Math.round(box.centerY() - size / 2f);
        drawable.setTint(palette.muted);
        drawable.setBounds(left, top, left + size, top + size);
        drawable.draw(canvas);
    }

    private void drawBitmapFitCenter(Canvas canvas, Bitmap bitmap, RectF box) {
        float scale = Math.min(box.width() / bitmap.getWidth(), box.height() / bitmap.getHeight());
        float width = bitmap.getWidth() * scale;
        float height = bitmap.getHeight() * scale;
        RectF dest = new RectF(box.centerX() - width / 2f, box.centerY() - height / 2f,
                box.centerX() + width / 2f, box.centerY() + height / 2f);
        canvas.drawBitmap(bitmap, null, dest, paint);
    }

    private Bitmap decodeBitmap(String uri) {
        if (uri == null || uri.trim().isEmpty()) return null;
        try (InputStream stream = context.getContentResolver().openInputStream(Uri.parse(uri))) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            return BitmapFactory.decodeStream(stream, null, options);
        } catch (Exception ignored) {
            return null;
        }
    }

    private List<String> seasonLines(Outfit outfit, List<ClothingItem> items) {
        List<String> lines = splitValues(outfit.season);
        if (lines.isEmpty()) {
            for (ClothingItem item : items) {
                for (String season : splitValues(item.season)) {
                    if (!lines.contains(season)) lines.add(season);
                    if (lines.size() >= 3) break;
                }
                if (lines.size() >= 3) break;
            }
        }
        if (lines.isEmpty()) lines.add("Any season");
        return lines;
    }

    private List<String> occasionLines(Outfit outfit) {
        List<String> lines = new ArrayList<>();
        String occasion = value(outfit.occasion, "Casual");
        lines.add(occasion);
        if (!occasion.toLowerCase(Locale.ROOT).contains("casual")) lines.add("Everyday");
        if (!occasion.toLowerCase(Locale.ROOT).contains("travel")) lines.add("Travel");
        return lines;
    }

    private List<Integer> paletteColors(List<ClothingItem> items) {
        List<Integer> colors = new ArrayList<>();
        for (ClothingItem item : items) {
            for (String name : splitValues(item.color)) {
                Integer color = colorMap.get(clean(name).toLowerCase(Locale.ROOT));
                if (color != null && !colors.contains(color)) colors.add(color);
                if (colors.size() >= 6) return colors;
            }
        }
        int[] fallback = {Color.rgb(18, 18, 18), Color.rgb(118, 120, 126), Color.rgb(139, 45, 52),
                palette.primary, Color.rgb(112, 70, 40), Color.rgb(238, 229, 216)};
        for (int color : fallback) {
            if (!colors.contains(color)) colors.add(color);
            if (colors.size() >= 6) break;
        }
        return colors;
    }

    private List<String> splitValues(String value) {
        List<String> values = new ArrayList<>();
        if (value == null) return values;
        for (String part : value.split("[,·]")) {
            String cleaned = clean(part);
            if (!cleaned.isEmpty() && !values.contains(cleaned)) values.add(cleaned);
        }
        return values;
    }

    private void drawCenteredWrappedText(Canvas canvas, String value, float left, float right, float y, float lineHeight, int maxLines) {
        List<String> lines = wrap(value, right - left, maxLines, 24);
        for (int i = 0; i < lines.size(); i++) {
            canvas.drawText(lines.get(i), WIDTH / 2f, y + i * lineHeight, textPaint);
        }
    }

    private void drawCenteredSingleLine(Canvas canvas, String value, float left, float right, float y, float textSize) {
        String text = ellipsize(value, right - left, textSize);
        canvas.drawText(text, (left + right) / 2f, y, textPaint);
    }

    private List<String> wrap(String value, float maxWidth, int maxLines, float textSize) {
        List<String> lines = new ArrayList<>();
        String[] words = value(value, "").split("\\s+");
        StringBuilder line = new StringBuilder();
        for (String word : words) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (measure(candidate, textSize) <= maxWidth) {
                line = new StringBuilder(candidate);
            } else {
                if (line.length() > 0) lines.add(line.toString());
                line = new StringBuilder(word);
                if (lines.size() == maxLines - 1) break;
            }
        }
        if (line.length() > 0 && lines.size() < maxLines) lines.add(line.toString());
        if (lines.size() == maxLines && words.length > 0) {
            int last = lines.size() - 1;
            lines.set(last, ellipsize(lines.get(last), maxWidth, textSize));
        }
        return lines;
    }

    private String ellipsize(String value, float maxWidth, float textSize) {
        String text = value(value, "");
        if (measure(text, textSize) <= maxWidth) return text;
        while (text.length() > 1 && measure(text + "…", textSize) > maxWidth) {
            text = text.substring(0, text.length() - 1).trim();
        }
        return text + "…";
    }

    private float measure(String value, float textSize) {
        textPaint.setTextSize(textSize);
        return textPaint.measureText(value);
    }

    private String styleLine(Outfit outfit) {
        return joinNonEmpty(value(outfit.occasion, "SMART CASUAL"), value(outfit.season, "COMFORTABLE"), noteKeyword(outfit.notes)).toUpperCase(Locale.ROOT);
    }

    private String noteKeyword(String notes) {
        if (notes == null || notes.trim().isEmpty()) return "MODERN";
        String[] words = clean(notes).split("\\s+");
        return words.length == 0 ? "MODERN" : words[0];
    }

    private void drawLine(Canvas canvas, float startX, float startY, float stopX, float stopY, int color) {
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(1.4f);
        paint.setColor(color);
        canvas.drawLine(startX, startY, stopX, stopY, paint);
        paint.setStyle(Paint.Style.FILL);
    }

    private int categoryIcon(String category) {
        String group = PromptBuilder.categoryGroup(category).toLowerCase(Locale.ROOT);
        if (group.contains("bottom")) return R.drawable.ic_cat_pants;
        if (group.contains("dress")) return R.drawable.ic_cat_dress;
        if (group.contains("outer")) return R.drawable.ic_cat_coat;
        if (group.contains("shoe")) return R.drawable.ic_cat_shoe;
        if (group.contains("access")) return R.drawable.ic_cat_bag;
        return R.drawable.ic_clothes;
    }

    private void seedColorMap() {
        colorMap.put("black", Color.BLACK);
        colorMap.put("white", Color.WHITE);
        colorMap.put("cream", Color.rgb(245, 240, 218));
        colorMap.put("beige", Color.rgb(216, 193, 140));
        colorMap.put("brown", Color.rgb(128, 77, 42));
        colorMap.put("gray", Color.rgb(143, 143, 143));
        colorMap.put("grey", Color.rgb(143, 143, 143));
        colorMap.put("light gray", Color.rgb(205, 205, 205));
        colorMap.put("light grey", Color.rgb(205, 205, 205));
        colorMap.put("blue", Color.rgb(36, 87, 166));
        colorMap.put("light blue", Color.rgb(174, 220, 238));
        colorMap.put("navy", Color.rgb(7, 61, 111));
        colorMap.put("green", Color.rgb(46, 138, 79));
        colorMap.put("olive", Color.rgb(112, 128, 0));
        colorMap.put("yellow", Color.rgb(255, 219, 28));
        colorMap.put("orange", Color.rgb(255, 160, 20));
        colorMap.put("red", Color.rgb(240, 30, 42));
        colorMap.put("burgundy", Color.rgb(139, 30, 48));
        colorMap.put("pink", Color.rgb(255, 166, 172));
        colorMap.put("purple", Color.rgb(196, 146, 221));
        colorMap.put("teal", Color.rgb(31, 160, 146));
    }

    private String joinNonEmpty(String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) continue;
            if (builder.length() > 0) builder.append(" • ");
            builder.append(clean(value));
        }
        return builder.toString();
    }

    private String clean(String value) {
        return value == null ? "" : value.replaceAll("[🌱☀️🍂❄️\\u2713]", "").trim();
    }

    private String value(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : clean(value);
    }
}
