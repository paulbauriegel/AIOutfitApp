package com.example.aioutfit;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.LruCache;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation;
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentationResult;
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenter;
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private static final int PICK_IMAGE_REQUEST = 42;
    private static final int EXPORT_BACKUP_REQUEST = 43;
    private static final int IMPORT_BACKUP_REQUEST = 44;
    private static final int PICK_PROFILE_BODY_IMAGE_REQUEST = 45;
    private static final int PICK_PROFILE_FACE_IMAGE_REQUEST = 46;
    private static final int BACKUP_SCHEMA_VERSION = 1;
    private static final String DEFAULT_OPENAI_MODEL = "gpt-5.4";
    private static final String SEASON_ANY = "Any season";
    private static final String[] SEASON_OPTIONS = {"🌱 Spring", "☀️ Summer", "🍂 Autumn", "❄️ Winter"};
    private static final OccasionOption[] OCCASION_OPTIONS = {
            new OccasionOption("👔", "Work"),
            new OccasionOption("🧢", "Casual"),
            new OccasionOption("🏠", "Home"),
            new OccasionOption("🎒", "School"),
            new OccasionOption("🥰", "Date"),
            new OccasionOption("🥂", "Party"),
            new OccasionOption("🎽", "Sports"),
            new OccasionOption("🎩", "Formal event"),
            new OccasionOption("💍", "Wedding"),
            new OccasionOption("✈️", "Travel"),
            new OccasionOption("🏖️", "Vacation"),
            new OccasionOption("✨", "Other")
    };
    private static final ColorOption[] CLOTHING_COLORS = {
            new ColorOption("Black", 0xFF000000),
            new ColorOption("Brown", 0xFF9A4F14),
            new ColorOption("Beige", 0xFFD9C28A),
            new ColorOption("Gray", 0xFF8F8F8F),
            new ColorOption("Light gray", 0xFFD1D1D1),
            new ColorOption("White", 0xFFFFFFFF),
            new ColorOption("Cream", 0xFFFCFAEA),
            new ColorOption("Light blue", 0xFFAED9EA),
            new ColorOption("Blue", 0xFF2437E8),
            new ColorOption("Navy", 0xFF064478),
            new ColorOption("Teal", 0xFF1EA094),
            new ColorOption("Mint", 0xFF43D6C5),
            new ColorOption("Light green", 0xFF8BE78A),
            new ColorOption("Green", 0xFF08AA4A),
            new ColorOption("Olive", 0xFF8C9200),
            new ColorOption("Yellow", 0xFFFFD91A),
            new ColorOption("Orange", 0xFFFFA20A),
            new ColorOption("Red", 0xFFF51D2A),
            new ColorOption("Burgundy", 0xFF8F1F35),
            new ColorOption("Pink", 0xFFFFA8AD),
            new ColorOption("Lavender", 0xFFC99BE2),
            new ColorOption("Magenta", 0xFFD21DCE)
    };
    private static final CategoryOption[] CATEGORY_OPTIONS = {
            new CategoryOption("Tops", "T-Shirt", R.drawable.ic_cat_tshirt),
            new CategoryOption("Tops", "Polo", R.drawable.ic_cat_polo),
            new CategoryOption("Tops", "Top", R.drawable.ic_cat_tank),
            new CategoryOption("Tops", "Shirt", R.drawable.ic_cat_shirt),
            new CategoryOption("Tops", "Blouse", R.drawable.ic_cat_blouse),
            new CategoryOption("Tops", "Sweater", R.drawable.ic_cat_sweater),
            new CategoryOption("Tops", "Long Sleeve", R.drawable.ic_cat_long_sleeve),
            new CategoryOption("Tops", "Hoodie", R.drawable.ic_cat_hoodie),
            new CategoryOption("Tops", "Cardigan", R.drawable.ic_cat_cardigan),
            new CategoryOption("Tops", "Knitwear", R.drawable.ic_cat_sweater),
            new CategoryOption("Tops", "Tank top", R.drawable.ic_cat_tank),
            new CategoryOption("Tops", "Sportswear", R.drawable.ic_cat_sportswear),
            new CategoryOption("Tops", "Bodysuit", R.drawable.ic_cat_bodysuit),
            new CategoryOption("Bottoms", "Pants", R.drawable.ic_cat_pants),
            new CategoryOption("Bottoms", "Jeans", R.drawable.ic_cat_pants),
            new CategoryOption("Bottoms", "Chinos", R.drawable.ic_cat_pants),
            new CategoryOption("Bottoms", "Shorts", R.drawable.ic_cat_shorts),
            new CategoryOption("Bottoms", "Skirt", R.drawable.ic_cat_skirt),
            new CategoryOption("Dresses", "Dress", R.drawable.ic_cat_dress),
            new CategoryOption("Dresses", "Jumpsuit", R.drawable.ic_cat_jumpsuit),
            new CategoryOption("Outerwear", "Coat", R.drawable.ic_cat_coat),
            new CategoryOption("Outerwear", "Jacket", R.drawable.ic_cat_jacket),
            new CategoryOption("Outerwear", "Blazer", R.drawable.ic_cat_blazer),
            new CategoryOption("Shoes", "Sneakers", R.drawable.ic_cat_shoe),
            new CategoryOption("Shoes", "Boots", R.drawable.ic_cat_boot),
            new CategoryOption("Shoes", "Dress shoes", R.drawable.ic_cat_shoe),
            new CategoryOption("Shoes", "Sandals", R.drawable.ic_cat_sandal),
            new CategoryOption("Accessories", "Bag", R.drawable.ic_cat_bag),
            new CategoryOption("Accessories", "Belt", R.drawable.ic_cat_belt),
            new CategoryOption("Accessories", "Hat", R.drawable.ic_cat_hat),
            new CategoryOption("Accessories", "Scarf", R.drawable.ic_cat_scarf),
            new CategoryOption("Other", "Other", R.drawable.ic_cat_other)
    };
    private static final MaterialOption[] MATERIAL_OPTIONS = {
            new MaterialOption("Cotton", 0xFFE6E2D6),
            new MaterialOption("Polyester", 0xFF3568A5),
            new MaterialOption("Nylon", 0xFF9EA79E),
            new MaterialOption("Denim", 0xFF2D4B63),
            new MaterialOption("Wool", 0xFF8E8E8E),
            new MaterialOption("Silk", 0xFFB01636),
            new MaterialOption("Linen", 0xFFD4C6B1),
            new MaterialOption("Spandex", 0xFF171717),
            new MaterialOption("Acrylic", 0xFF8C6348),
            new MaterialOption("Leather", 0xFF5C3A25),
            new MaterialOption("Viscose", 0xFFB8B0A1),
            new MaterialOption("Rayon", 0xFFC9B8A2),
            new MaterialOption("Cashmere", 0xFFC7B59A),
            new MaterialOption("Suede", 0xFFA77D58),
            new MaterialOption("Other", 0xFFE2E2E2)
    };
    private int primary;
    private int onPrimary;
    private int primaryContainer;
    private int onPrimaryContainer;
    private int ink;
    private int muted;
    private int surface;
    private int surfaceContainer;
    private int surfaceDialog;
    private int outline;
    private int error;
    private AppPalette palette;
    private WardrobeStore store;
    private final List<ClothingItem> clothes = new ArrayList<>();
    private final List<Outfit> outfits = new ArrayList<>();
    private final List<String> categories = new ArrayList<>();
    private final Map<String, ClothingItem> itemById = new LinkedHashMap<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService imageExecutor = Executors.newFixedThreadPool(2);
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private LruCache<String, Bitmap> imageCache;
    private LinearLayout topBar;
    private LinearLayout stickyBar;
    private FrameLayout bodyHost;
    private LinearLayout content;
    private TextView clothesNavLabel;
    private TextView outfitsNavLabel;
    private View clothesNavIndicator;
    private View outfitsNavIndicator;
    private LinearLayout clothesNavItem;
    private LinearLayout outfitsNavItem;
    private String activeTab = "clothes";
    private float swipeStartX;
    private float swipeStartY;
    private String searchQuery = "";
    private String selectedCategory = "All";
    private String currency = "$";
    private String openAiApiKey = "";
    private String openAiBaseUrl = "";
    private String openAiModel = DEFAULT_OPENAI_MODEL;
    private String profileBodyImageUri = "";
    private String profileFaceImageUri = "";
    private String profileEyeColor = "";
    private String profileHairColor = "";
    private ClothingItem imageTarget;
    private ImageView imageTargetView;
    private boolean appendPickedPhoto;
    private int activePhotoIndex;
    private boolean outfitInspirationTab;
    private boolean outfitInspirationLoading;
    private String outfitFilterCategory = "All";
    private Outfit outfitDraft;
    private Outfit outfitEditingTarget;
    private int outfitFormStep = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        store = new WardrobeStore(this);
        applyPalette(store.loadPrimaryColor());
        getWindow().setStatusBarColor(surface);
        getWindow().setNavigationBarColor(surface);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        clothes.addAll(store.loadClothes());
        outfits.addAll(store.loadOutfits());
        categories.addAll(store.loadCategories());
        currency = store.loadCurrency();
        openAiApiKey = store.loadOpenAiApiKey();
        openAiBaseUrl = store.loadOpenAiBaseUrl();
        openAiModel = store.loadOpenAiModel();
        profileBodyImageUri = store.loadProfileBodyImage();
        profileFaceImageUri = store.loadProfileFaceImage();
        profileEyeColor = store.loadProfileEyeColor();
        profileHairColor = store.loadProfileHairColor();
        initImageCache();
        rebuildItemIndex();
        setContentView(buildRoot());
        renderClothes();
    }

    @Override
    protected void onDestroy() {
        imageExecutor.shutdownNow();
        ioExecutor.shutdownNow();
        super.onDestroy();
    }

    private void initImageCache() {
        int maxKb = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheKb = Math.max(8 * 1024, maxKb / 8);
        imageCache = new LruCache<String, Bitmap>(cacheKb) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                return bitmap.getByteCount() / 1024;
            }
        };
    }

    private void rebuildItemIndex() {
        itemById.clear();
        for (ClothingItem item : clothes) {
            itemById.put(item.id, item);
        }
    }

    @Override
    public void onBackPressed() {
        if (navigateBack()) {
            return;
        }
        super.onBackPressed();
    }

    private void applyPalette(int primaryColor) {
        palette = new AppPalette(primaryColor);
        primary = palette.primary;
        onPrimary = palette.onPrimary;
        primaryContainer = palette.primaryContainer;
        onPrimaryContainer = palette.onPrimaryContainer;
        ink = palette.ink;
        muted = palette.muted;
        surface = palette.surface;
        surfaceContainer = palette.surfaceContainer;
        surfaceDialog = palette.surfaceDialog;
        outline = palette.outline;
        error = palette.error;
    }

    private View buildRoot() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(surface);

        topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.HORIZONTAL);
        topBar.setGravity(Gravity.CENTER_VERTICAL);
        topBar.setMinimumHeight(dp(64) + statusBarInset());
        topBar.setPadding(dp(16), statusBarInset(), dp(16), 0);
        topBar.setBackgroundColor(surface);
        root.addView(topBar, new LinearLayout.LayoutParams(-1, dp(64) + statusBarInset()));

        stickyBar = new LinearLayout(this);
        stickyBar.setOrientation(LinearLayout.VERTICAL);
        stickyBar.setBackgroundColor(surface);
        stickyBar.setVisibility(View.GONE);
        root.addView(stickyBar, new LinearLayout.LayoutParams(-1, -2));

        bodyHost = new FrameLayout(this);
        root.addView(bodyHost, new LinearLayout.LayoutParams(-1, 0, 1));
        useScrollContent();
        root.addView(bottomNavigation(), new LinearLayout.LayoutParams(-1, dp(120)));
        return root;
    }

    private void useScrollContent() {
        if (bodyHost == null) return;
        bodyHost.removeAllViews();
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        scroll.setOnTouchListener((view, event) -> handleMainSwipe(event));
        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setPadding(dp(16), 0, dp(16), dp(20));
        scroll.addView(content);
        bodyHost.addView(scroll, new FrameLayout.LayoutParams(-1, -1));
    }

    private void useBodyView(View view) {
        if (bodyHost == null) return;
        bodyHost.removeAllViews();
        bodyHost.addView(view, new FrameLayout.LayoutParams(-1, -1));
    }

    private void renderTopBar(String title, boolean showBack) {
        renderTopBar(title, showBack, null);
    }

    private void renderTopBar(String title, boolean showBack, Runnable saveAction) {
        renderTopBar(title, showBack, saveAction, true);
    }

    private void renderTopBar(String title, boolean showBack, Runnable saveAction, boolean showSettings) {
        clearStickyBar();
        topBar.removeAllViews();
        if (showBack) {
            LinearLayout.LayoutParams backParams = new LinearLayout.LayoutParams(dp(48), dp(48));
            backParams.setMargins(0, 0, dp(4), 0);
            topBar.addView(iconButton(R.drawable.ic_back, v -> navigateBack()), backParams);
        }
        LinearLayout titleGroup = new LinearLayout(this);
        titleGroup.setGravity(Gravity.CENTER_VERTICAL);
        if (!showBack && getString(R.string.app_name).equals(title)) {
            ImageView logo = iconView(R.drawable.ic_clothes);
            LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(dp(34), dp(34));
            logoParams.setMargins(0, 0, dp(10), 0);
            titleGroup.addView(logo, logoParams);
        }
        TextView titleView = text(title, 28, ink, true);
        titleView.setSingleLine(true);
        titleGroup.addView(titleView);
        topBar.addView(titleGroup, new LinearLayout.LayoutParams(0, -2, 1));
        if (!showBack) {
            topBar.addView(iconButton(R.drawable.ic_search, v -> showSearchDialog()), new LinearLayout.LayoutParams(dp(48), dp(48)));
        }
        if (saveAction != null) {
            TextView save = text("Save", 15, primary, true);
            save.setGravity(Gravity.CENTER);
            save.setPadding(dp(12), 0, dp(12), 0);
            save.setOnClickListener(v -> saveAction.run());
            topBar.addView(save, new LinearLayout.LayoutParams(dp(72), dp(48)));
        } else if (showSettings) {
            topBar.addView(iconButton(R.drawable.ic_settings, v -> renderCategories()), new LinearLayout.LayoutParams(dp(48), dp(48)));
        }
    }

    private void clearStickyBar() {
        if (stickyBar == null) return;
        stickyBar.removeAllViews();
        stickyBar.setPadding(0, 0, 0, 0);
        stickyBar.setVisibility(View.GONE);
    }

    private void setStickyBar(View view, int horizontalPaddingDp) {
        if (stickyBar == null) return;
        stickyBar.removeAllViews();
        stickyBar.setPadding(dp(horizontalPaddingDp), 0, dp(horizontalPaddingDp), 0);
        stickyBar.addView(view, new LinearLayout.LayoutParams(-1, -2));
        stickyBar.setVisibility(View.VISIBLE);
    }

    private boolean navigateBack() {
        if (activeTab.equals("outfitForm")) {
            if (outfitFormStep > 1) {
                outfitFormStep--;
                renderOutfitFormStep();
            } else {
                clearOutfitDraft();
                renderOutfits();
            }
            return true;
        }
        if (activeTab.equals("customCategories")) {
            renderCategories();
            return true;
        }
        if (activeTab.equals("aiProfile")) {
            renderCategories();
            return true;
        }
        if (activeTab.equals("detail") || activeTab.equals("clothingForm") || activeTab.equals("categories")) {
            renderClothes();
            return true;
        }
        return false;
    }

    private ImageView iconButton(int iconRes, View.OnClickListener listener) {
        ImageView button = iconView(iconRes);
        button.setPadding(dp(10), dp(10), dp(10), dp(10));
        button.setBackground(rounded(Color.TRANSPARENT, 24, Color.TRANSPARENT, 0));
        button.setOnClickListener(listener);
        return button;
    }

    private ImageView transparentIconButton(int iconRes, int color, View.OnClickListener listener) {
        ImageView button = iconView(iconRes);
        button.setColorFilter(color);
        button.setPadding(dp(10), dp(10), dp(10), dp(10));
        button.setBackground(rounded(Color.TRANSPARENT, 22, Color.TRANSPARENT, 0));
        button.setOnClickListener(listener);
        return button;
    }

    private int statusBarInset() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            WindowInsets insets = getWindow().getDecorView().getRootWindowInsets();
            if (insets != null) {
                return insets.getStableInsetTop();
            }
        }
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return dp(24);
    }

    private ImageView iconView(int iconRes) {
        ImageView image = new ImageView(this);
        image.setImageResource(iconRes);
        image.setScaleType(ImageView.ScaleType.CENTER);
        image.setColorFilter(ink);
        return image;
    }

    private void loadImage(ImageView target, String uri, int fallbackRes, int fallbackColor, int targetDp) {
        String cleanUri = uri == null ? "" : uri.trim();
        if (isBlank(cleanUri)) {
            target.setTag(null);
            target.setImageResource(fallbackRes);
            target.setColorFilter(fallbackColor);
            return;
        }

        String key = cleanUri + "@" + targetDp;
        target.setTag(key);
        Bitmap cached = imageCache == null ? null : imageCache.get(key);
        if (cached != null) {
            target.clearColorFilter();
            target.setImageBitmap(cached);
            return;
        }

        target.setImageResource(fallbackRes);
        target.setColorFilter(fallbackColor);
        imageExecutor.execute(() -> {
            Bitmap bitmap = decodeScaledBitmap(cleanUri, dp(targetDp), dp(targetDp));
            if (bitmap != null && imageCache != null) {
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
            try (InputStream stream = getContentResolver().openInputStream(Uri.parse(uri))) {
                BitmapFactory.decodeStream(stream, null, bounds);
            }
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = sampleSize(bounds, reqWidth, reqHeight);
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            try (InputStream stream = getContentResolver().openInputStream(Uri.parse(uri))) {
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

    private FrameLayout bottomNavigation() {
        FrameLayout container = new FrameLayout(this);
        container.setClipChildren(false);
        container.setClipToPadding(false);

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(18), dp(8), dp(18), dp(8));
        nav.setBackground(rounded(surfaceDialog, 0, palette.outlineVariant(), 1));
        clothesNavItem = navItem(getString(R.string.nav_clothes), R.drawable.ic_clothes, v -> renderClothes(), true);
        outfitsNavItem = navItem(getString(R.string.nav_outfits), R.drawable.ic_outfit, v -> renderOutfits(), false);
        nav.addView(clothesNavItem, new LinearLayout.LayoutParams(0, -1, 1));
        TextView centerGap = new TextView(this);
        nav.addView(centerGap, new LinearLayout.LayoutParams(dp(92), -1));
        nav.addView(outfitsNavItem, new LinearLayout.LayoutParams(0, -1, 1));

        FrameLayout.LayoutParams navParams = new FrameLayout.LayoutParams(-1, dp(84));
        navParams.gravity = Gravity.BOTTOM;
        container.addView(nav, navParams);

        ImageView add = addNavButton();
        FrameLayout.LayoutParams addParams = new FrameLayout.LayoutParams(dp(64), dp(64));
        addParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        container.addView(add, addParams);
        return container;
    }

    private LinearLayout navItem(String label, int iconRes, View.OnClickListener listener, boolean clothes) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(0, dp(4), 0, dp(4));
        item.setOnClickListener(listener);
        ImageView icon = iconView(iconRes);
        item.addView(icon, new LinearLayout.LayoutParams(dp(28), dp(28)));
        TextView text = text(label, 12, muted, true);
        text.setGravity(Gravity.CENTER);
        item.addView(text);
        TextView indicator = new TextView(this);
        LinearLayout.LayoutParams indicatorParams = new LinearLayout.LayoutParams(dp(26), dp(3));
        indicatorParams.setMargins(0, dp(3), 0, 0);
        item.addView(indicator, indicatorParams);
        if (clothes) {
            clothesNavLabel = text;
            clothesNavIndicator = indicator;
        } else {
            outfitsNavLabel = text;
            outfitsNavIndicator = indicator;
        }
        return item;
    }

    private ImageView addNavButton() {
        ImageView button = iconView(R.drawable.ic_add);
        button.setPadding(dp(18), dp(18), dp(18), dp(18));
        button.setBackground(rounded(primaryContainer, 22, Color.TRANSPARENT, 0));
        button.setElevation(dp(8));
        button.setOnClickListener(v -> showAddMenu());
        return button;
    }

    private void updateBottomNavigation() {
        if (clothesNavLabel == null || outfitsNavLabel == null || clothesNavItem == null || outfitsNavItem == null || clothesNavIndicator == null || outfitsNavIndicator == null) return;
        boolean clothesActive = activeTab.equals("clothes");
        boolean outfitsActive = activeTab.equals("outfits") || activeTab.equals("outfitForm");
        clothesNavLabel.setTextColor(clothesActive ? ink : muted);
        outfitsNavLabel.setTextColor(outfitsActive ? ink : muted);
        clothesNavIndicator.setBackground(clothesActive ? rounded(primary, 2, Color.TRANSPARENT, 0) : rounded(Color.TRANSPARENT, 2, Color.TRANSPARENT, 0));
        outfitsNavIndicator.setBackground(outfitsActive ? rounded(primary, 2, Color.TRANSPARENT, 0) : rounded(Color.TRANSPARENT, 2, Color.TRANSPARENT, 0));
        clothesNavItem.setBackgroundColor(Color.TRANSPARENT);
        outfitsNavItem.setBackgroundColor(Color.TRANSPARENT);
    }

    private boolean handleMainSwipe(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                swipeStartX = event.getX();
                swipeStartY = event.getY();
                return false;
            case MotionEvent.ACTION_UP:
                if (!activeTab.equals("clothes") && !activeTab.equals("outfits")) {
                    return false;
                }
                float deltaX = event.getX() - swipeStartX;
                float deltaY = event.getY() - swipeStartY;
                if (Math.abs(deltaX) > dp(72) && Math.abs(deltaX) > Math.abs(deltaY) * 1.4f) {
                    if (deltaX < 0 && activeTab.equals("clothes")) {
                        renderOutfits();
                    } else if (deltaX > 0 && activeTab.equals("outfits")) {
                        renderClothes();
                    }
                    return true;
                }
                return false;
            default:
                return false;
        }
    }

    private void showSearchDialog() {
        EditText input = input("Search clothes");
        input.setText(searchQuery);
        showMaterialDialog("Search", input, "Clear", "Search", () -> {
            searchQuery = "";
            renderClothes();
        }, () -> {
            searchQuery = input.getText().toString().trim();
            renderClothes();
        });
    }

    private void showAddMenu() {
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        Dialog[] holder = new Dialog[1];
        menu.addView(menuRow(R.drawable.ic_add_outfit, "Add outfit", "Combine saved clothes", v -> {
            if (holder[0] != null) holder[0].dismiss();
            renderOutfitForm(null);
        }));
        menu.addView(menuRow(R.drawable.ic_add_cloth, "Add cloth", "Inventorize a wardrobe item", v -> {
            if (holder[0] != null) holder[0].dismiss();
            renderClothingForm(null);
        }));
        holder[0] = showMaterialDialog("Add", menu, null, null, null, null);
    }

    private View menuRow(int iconRes, String title, String subtitle, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(8), dp(10), dp(8), dp(10));
        row.setOnClickListener(listener);

        ImageView icon = iconButton(iconRes, listener);
        icon.setBackground(rounded(surfaceContainer, 18, Color.TRANSPARENT, 0));
        row.addView(icon, new LinearLayout.LayoutParams(dp(48), dp(48)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(14), 0, 0, 0);
        copy.addView(text(title, 17, ink, true));
        copy.addView(text(subtitle, 13, muted, false));
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        return row;
    }

    private Dialog showMaterialDialog(String title, View body, String negative, String positive, Runnable negativeAction, Runnable positiveAction) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        LinearLayout panel = new LinearLayout(this);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setPadding(dp(24), dp(22), dp(24), dp(18));
        panel.setBackground(rounded(surfaceDialog, 28, Color.TRANSPARENT, 0));

        TextView heading = text(title, 22, ink, true);
        heading.setPadding(0, 0, 0, dp(16));
        panel.addView(heading);
        panel.addView(body);

        if (negative != null || positive != null) {
            LinearLayout actions = new LinearLayout(this);
            actions.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
            actions.setPadding(0, dp(18), 0, 0);
            if (negative != null) {
                Button negativeButton = textButton(negative);
                negativeButton.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (negativeAction != null) negativeAction.run();
                });
                actions.addView(negativeButton);
            }
            if (positive != null) {
                Button positiveButton = filledButton(positive);
                positiveButton.setOnClickListener(v -> {
                    dialog.dismiss();
                    if (positiveAction != null) positiveAction.run();
                });
                actions.addView(positiveButton);
            }
            panel.addView(actions);
        }

        dialog.setContentView(panel);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            shownWindow.setLayout(dp(330), -2);
        }
        return dialog;
    }

    private void renderClothes() {
        activeTab = "clothes";
        renderTopBar(getString(R.string.app_name), false);
        setStickyBar(clothesStickyHeader(), 22);
        updateBottomNavigation();
        useBodyView(clothingGridView(filteredClothes()));
    }

    private View clothesStickyHeader() {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        header.addView(wardrobeSummary());
        header.addView(categoryChips());
        return header;
    }

    private List<ClothingItem> filteredClothes() {
        List<ClothingItem> filtered = new ArrayList<>();
        for (ClothingItem item : clothes) {
            if (matchesClothingFilter(item)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private boolean matchesClothingFilter(ClothingItem item) {
        boolean categoryMatches = categoryMatchesFilter(item.category, selectedCategory);
        return categoryMatches && item.matches(searchQuery, "All");
    }

    private boolean categoryMatchesFilter(String itemCategory, String filter) {
        if (isBlank(filter) || "All".equalsIgnoreCase(filter)) return true;
        if (!isBlank(itemCategory) && filter.equalsIgnoreCase(itemCategory)) return true;
        return filter.equalsIgnoreCase(categoryGroup(itemCategory));
    }

    private View clothingGridView(List<ClothingItem> items) {
        FrameLayout frame = new FrameLayout(this);
        GridView grid = new GridView(this);
        grid.setNumColumns(3);
        grid.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
        grid.setHorizontalSpacing(dp(8));
        grid.setVerticalSpacing(dp(8));
        grid.setPadding(dp(22), dp(4), dp(22), dp(28));
        grid.setClipToPadding(false);
        grid.setOnTouchListener((view, event) -> handleMainSwipe(event));
        grid.setAdapter(new ClothingGridAdapter(items));
        grid.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < items.size()) {
                showClothingDetail(items.get(position));
            }
        });

        TextView empty = emptyState("No clothing items match this view.");
        empty.setGravity(Gravity.CENTER);
        frame.addView(empty, new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER));
        empty.setVisibility(items.isEmpty() ? View.VISIBLE : View.GONE);
        frame.addView(grid, new FrameLayout.LayoutParams(-1, -1));
        return frame;
    }

    private void renderClothingList() {
        LinearLayout list = content.findViewWithTag("clothingList");
        if (list == null) return;
        list.removeAllViews();
        int shown = 0;
        LinearLayout row = null;
        for (ClothingItem item : clothes) {
            if (item.matches(searchQuery, selectedCategory)) {
                if (shown % 3 == 0) {
                    row = new LinearLayout(this);
                    row.setOrientation(LinearLayout.HORIZONTAL);
                    list.addView(row, new LinearLayout.LayoutParams(-1, -2));
                }
                LinearLayout.LayoutParams tileParams = new LinearLayout.LayoutParams(0, dp(104), 1);
                tileParams.setMargins(dp(4), dp(4), dp(4), dp(8));
                row.addView(clothingTile(item), tileParams);
                shown++;
            }
        }
        if (shown > 0 && row != null && shown % 3 != 0) {
            for (int i = shown % 3; i < 3; i++) {
                TextView spacer = new TextView(this);
                LinearLayout.LayoutParams tileParams = new LinearLayout.LayoutParams(0, dp(104), 1);
                tileParams.setMargins(dp(4), dp(4), dp(4), dp(8));
                row.addView(spacer, tileParams);
            }
        }
        if (shown == 0) {
            list.addView(emptyState("No clothing items match this view."));
        }
    }

    private View wardrobeSummary() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, dp(12));
        row.addView(text(clothes.size() + " clothes", 15, muted, true), new LinearLayout.LayoutParams(0, -2, 1));
        TextView outfitCount = text(outfits.size() + " outfits", 14, muted, false);
        row.addView(outfitCount);
        return row;
    }

    private View categoryChips() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, dp(12));

        for (String option : basicClothingFilters()) {
            Button chip = chip(option, option.equals(selectedCategory));
            chip.setOnClickListener(v -> {
                selectedCategory = option;
                renderClothes();
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(38), 1);
            params.setMargins(0, 0, dp(6), 0);
            row.addView(chip, params);
        }

        boolean detailedFilterActive = !basicClothingFilters().contains(selectedCategory);
        ImageView filter = iconButton(R.drawable.ic_filter, v -> showClothingFilterMenu());
        filter.setColorFilter(detailedFilterActive ? onPrimaryContainer : ink);
        filter.setBackground(rounded(detailedFilterActive ? primaryContainer : Color.TRANSPARENT, 20,
                detailedFilterActive ? Color.TRANSPARENT : palette.outlineVariant, detailedFilterActive ? 0 : 1));
        row.addView(filter, new LinearLayout.LayoutParams(dp(48), dp(38)));
        return row;
    }

    private List<String> basicClothingFilters() {
        List<String> filters = new ArrayList<>();
        filters.add("All");
        filters.add("Tops");
        filters.add("Bottoms");
        return filters;
    }

    private void showClothingFilterMenu() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(20), dp(14), dp(20), dp(16));
        sheet.setBackground(rounded(surfaceDialog, 32, Color.TRANSPARENT, 0));

        TextView handle = new TextView(this);
        handle.setBackground(rounded(outline, 3, Color.TRANSPARENT, 0));
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(dp(44), dp(5));
        handleParams.gravity = Gravity.CENTER_HORIZONTAL;
        handleParams.setMargins(0, 0, 0, dp(18));
        sheet.addView(handle, handleParams);

        TextView title = text("Filter clothes", 22, ink, true);
        title.setPadding(dp(4), 0, 0, dp(10));
        sheet.addView(title);

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        sheet.addView(scroll, new LinearLayout.LayoutParams(-1, dp(520)));

        addFilterSection(list, dialog, "Basic", basicClothingFilters());
        addFilterSection(list, dialog, "Groups", clothingFilterGroups());
        addFilterSection(list, dialog, "Categories", clothingFilterCategories());

        dialog.setContentView(sheet);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            shownWindow.setGravity(Gravity.BOTTOM);
            shownWindow.setLayout(-1, -2);
        }
    }

    private void addFilterSection(LinearLayout list, Dialog dialog, String title, List<String> options) {
        if (options.isEmpty()) return;
        TextView section = text(title, 13, muted, true);
        section.setPadding(dp(4), dp(14), dp(4), dp(6));
        list.addView(section);
        for (String option : options) {
            list.addView(clothingFilterRow(option, option.equals(selectedCategory), v -> {
                selectedCategory = option;
                dialog.dismiss();
                renderClothes();
            }));
        }
    }

    private View clothingFilterRow(String option, boolean selected, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(6), dp(6), dp(6), dp(6));
        row.setOnClickListener(listener);

        ImageView icon = iconView(filterDrawable(option));
        icon.setColorFilter(selected ? primary : ink);
        icon.setPadding(dp(8), dp(8), dp(8), dp(8));
        row.addView(icon, new LinearLayout.LayoutParams(dp(48), dp(48)));

        TextView label = text(option, 18, selected ? primary : palette.rowText, selected);
        label.setPadding(dp(14), 0, 0, 0);
        row.addView(label, new LinearLayout.LayoutParams(0, -2, 1));

        if (selected) {
            TextView check = text("✓", 18, primary, true);
            check.setGravity(Gravity.CENTER);
            row.addView(check, new LinearLayout.LayoutParams(dp(32), dp(40)));
        }
        return row;
    }

    private List<String> clothingFilterGroups() {
        LinkedHashSet<String> groups = new LinkedHashSet<>();
        for (CategoryOption option : CATEGORY_OPTIONS) {
            groups.add(option.group);
        }
        groups.remove("Tops");
        groups.remove("Bottoms");
        groups.remove("Other");
        groups.add("Other");
        return new ArrayList<>(groups);
    }

    private List<String> clothingFilterCategories() {
        LinkedHashSet<String> options = new LinkedHashSet<>();
        for (CategoryOption option : CATEGORY_OPTIONS) {
            options.add(option.label);
        }
        for (String category : categories) {
            if (!basicClothingFilters().contains(category) && !clothingFilterGroups().contains(category)) {
                options.add(category);
            }
        }
        return new ArrayList<>(options);
    }

    private int filterDrawable(String option) {
        if ("All".equals(option)) return R.drawable.ic_filter;
        if ("Tops".equals(option)) return R.drawable.ic_cat_tshirt;
        if ("Bottoms".equals(option)) return R.drawable.ic_cat_pants;
        if ("Dresses".equals(option)) return R.drawable.ic_cat_dress;
        if ("Outerwear".equals(option)) return R.drawable.ic_cat_coat;
        if ("Shoes".equals(option)) return R.drawable.ic_cat_shoe;
        if ("Accessories".equals(option)) return R.drawable.ic_cat_bag;
        return categoryDrawable(option);
    }

    private View clothingTile(ClothingItem item) {
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(0, 0, 0, 0);
        tile.setBackground(rounded(palette.softSurface(), 7, Color.TRANSPARENT, 0));
        tile.setClipToOutline(true);
        tile.setElevation(0);

        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setBackgroundColor(Color.TRANSPARENT);
        image.setPadding(dp(6), dp(6), dp(6), dp(6));
        tile.setTag(new ClothingTileHolder(image));
        tile.addView(image, new LinearLayout.LayoutParams(-1, 0, 1));
        bindClothingTile(tile, item);
        return tile;
    }

    private void bindClothingTile(LinearLayout tile, ClothingItem item) {
        Object holderObject = tile.getTag();
        if (!(holderObject instanceof ClothingTileHolder)) return;
        ImageView image = ((ClothingTileHolder) holderObject).image;
        if (!isBlank(item.imageUri)) {
            loadImage(image, item.imageUri, android.R.drawable.ic_menu_gallery, palette.placeholder, 140);
        } else {
            image.setTag(null);
            image.setImageResource(android.R.drawable.ic_menu_gallery);
            image.setColorFilter(palette.placeholder);
        }
    }

    private void showClothingDetail(ClothingItem item) {
        activeTab = "detail";
        updateBottomNavigation();
        renderTopBar(value(item.category, value(item.name, "Details")), true);
        useScrollContent();
        content.removeAllViews();
        content.setPadding(dp(18), dp(8), dp(18), dp(32));

        ImageView hero = new ImageView(this);
        hero.setScaleType(ImageView.ScaleType.FIT_CENTER);
        hero.setBackgroundColor(Color.TRANSPARENT);
        hero.setPadding(0, 0, 0, 0);
        String selectedPhoto = selectedPhotoUri(item);
        if (!isBlank(selectedPhoto)) {
            loadImage(hero, selectedPhoto, android.R.drawable.ic_menu_gallery, palette.placeholder, 360);
        } else {
            hero.setImageResource(android.R.drawable.ic_menu_gallery);
            hero.setColorFilter(palette.placeholder);
        }
        attachPhotoSwipe(hero, item, () -> showImagePreview(item));
        content.addView(hero, new LinearLayout.LayoutParams(-1, dp(260)));
        content.addView(photoDots(item));

        content.addView(insightCards(item));

        TextView section = text("Details", 24, ink, true);
        section.setPadding(0, dp(18), 0, dp(8));
        content.addView(section);

        LinearLayout details = groupedRows();
        details.addView(detailRow(categoryDrawable(item.category), "Category", value(item.category, "Add category"), v -> editCategoryField(item)));
        details.addView(detailRow(R.drawable.ic_detail_category, "Name", value(item.name, "Add name"), v -> editTextField("Name", item.name, value -> item.name = value, item)));
        details.addView(detailRow(R.drawable.ic_detail_brand, "Brand", value(item.brand, "Add brand"), v -> editTextField("Brand", item.brand, value -> item.brand = value, item)));
        details.addView(detailColorRow(item));
        details.addView(detailRow(R.drawable.ic_detail_material, "Material", value(item.material, "Add material"), v -> editMaterialField(item)));
        details.addView(detailRow(R.drawable.ic_detail_notes, "Notes", value(item.notes, "Add a note..."), v -> editTextField("Notes", item.notes, value -> item.notes = value, item)));
        details.addView(detailRow(R.drawable.ic_detail_link, "Link", value(item.link, "Add link"), v -> editLinkField(item)));
        details.addView(detailRow(R.drawable.ic_detail_size, "Fit", value(item.size, "Add fit or size"), v -> editTextField("Size", item.size, value -> item.size = value, item)));
        if (isPantsItem(item)) {
            details.addView(detailRow(R.drawable.ic_detail_waist, "Waist", value(item.waist, "Add waist (W)"), v -> editTextField("Waist (W)", item.waist, value -> item.waist = value, item)));
            details.addView(detailRow(R.drawable.ic_detail_length, "Length", value(item.length, "Add length (L)"), v -> editTextField("Length (L)", item.length, value -> item.length = value, item)));
        }
        details.addView(detailRow(R.drawable.ic_detail_date, "Added", DateFormat.getDateInstance().format(item.addedAt), v -> editAddedDate(item)));
        details.addView(detailRow(R.drawable.ic_detail_season, "Season", value(item.season, "Add season"), v -> editSeasonField(item)));
        details.addView(detailRow(R.drawable.ic_detail_price, "Price", priceText(item.price), v -> editTextField("Price", item.price, value -> item.price = value, item)));
        details.addView(detailRow(R.drawable.ic_detail_care, "Care", value(item.care, "Add care instructions"), v -> editTextField("Care instructions", item.care, value -> item.care = value, item)));
        content.addView(details);
    }

    private void chooseDetailPhoto(ClothingItem item, ImageView targetView) {
        appendPickedPhoto = false;
        imageTarget = item;
        imageTargetView = targetView;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void appendDetailPhoto(ClothingItem item, ImageView targetView) {
        appendPickedPhoto = true;
        imageTarget = item;
        imageTargetView = targetView;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private List<String> photoUris(ClothingItem item) {
        List<String> uris = new ArrayList<>();
        if (item != null) {
            for (String uri : item.imageUris) {
                if (!isBlank(uri) && !uris.contains(uri)) {
                    uris.add(uri);
                }
            }
            if (!isBlank(item.imageUri) && !uris.contains(item.imageUri)) {
                uris.add(0, item.imageUri);
            }
        }
        return uris;
    }

    private String selectedPhotoUri(ClothingItem item) {
        List<String> uris = photoUris(item);
        if (uris.isEmpty()) return "";
        if (activePhotoIndex < 0 || activePhotoIndex >= uris.size()) activePhotoIndex = 0;
        return uris.get(activePhotoIndex);
    }

    private void attachPhotoSwipe(View view, ClothingItem item, Runnable tapAction) {
        final float[] downX = new float[1];
        final float[] downY = new float[1];
        view.setOnTouchListener((target, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downX[0] = event.getX();
                    downY[0] = event.getY();
                    target.getParent().requestDisallowInterceptTouchEvent(true);
                    return true;
                case MotionEvent.ACTION_UP:
                    float deltaX = event.getX() - downX[0];
                    float deltaY = event.getY() - downY[0];
                    target.getParent().requestDisallowInterceptTouchEvent(false);
                    if (Math.abs(deltaX) > dp(54) && Math.abs(deltaX) > Math.abs(deltaY) * 1.35f) {
                        if (movePhoto(item, deltaX < 0 ? 1 : -1)) {
                            showClothingDetail(item);
                        }
                        return true;
                    }
                    if (Math.abs(deltaX) < dp(10) && Math.abs(deltaY) < dp(10) && tapAction != null) {
                        tapAction.run();
                        return true;
                    }
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    target.getParent().requestDisallowInterceptTouchEvent(false);
                    return true;
                default:
                    return true;
            }
        });
    }

    private boolean movePhoto(ClothingItem item, int direction) {
        List<String> uris = photoUris(item);
        if (uris.size() <= 1) return false;
        activePhotoIndex += direction;
        if (activePhotoIndex < 0) {
            activePhotoIndex = uris.size() - 1;
        } else if (activePhotoIndex >= uris.size()) {
            activePhotoIndex = 0;
        }
        return true;
    }

    private View photoDots(ClothingItem item) {
        LinearLayout dots = new LinearLayout(this);
        dots.setGravity(Gravity.CENTER);
        dots.setPadding(0, dp(6), 0, 0);
        int count = Math.max(1, photoUris(item).size());
        for (int i = 0; i < count; i++) {
            int index = i;
            TextView dot = new TextView(this);
            dot.setBackground(rounded(i == activePhotoIndex ? primary : outline, 4, Color.TRANSPARENT, 0));
            dot.setOnClickListener(v -> {
                activePhotoIndex = index;
                showClothingDetail(item);
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(7), dp(7));
            params.setMargins(dp(3), 0, dp(3), 0);
            dots.addView(dot, params);
        }
        return dots;
    }

    private void replaceSelectedPhoto(ClothingItem item, String uri) {
        if (item == null || isBlank(uri)) return;
        if (item.imageUris.isEmpty() && !isBlank(item.imageUri)) {
            item.imageUris.add(item.imageUri);
        }
        if (activePhotoIndex < 0 || activePhotoIndex >= item.imageUris.size()) {
            activePhotoIndex = 0;
        }
        if (item.imageUris.isEmpty()) {
            item.imageUris.add(uri);
        } else {
            item.imageUris.set(activePhotoIndex, uri);
        }
        if (activePhotoIndex == 0 || isBlank(item.imageUri)) {
            item.imageUri = uri;
        }
    }

    private void showImagePreview(ClothingItem item) {
        String photoUri = selectedPhotoUri(item);
        if (isBlank(photoUri)) {
            Toast.makeText(this, "Choose a photo first", Toast.LENGTH_SHORT).show();
            return;
        }
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        FrameLayout frame = new FrameLayout(this);
        frame.setBackgroundColor(surface);

        ZoomImageView image = new ZoomImageView(this);
        image.setImageURI(Uri.parse(photoUri));
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setAdjustViewBounds(true);
        image.setSwipeListener(direction -> {
            if (movePhoto(item, direction)) {
                String nextPhoto = selectedPhotoUri(item);
                if (!isBlank(nextPhoto)) {
                    image.resetZoom();
                    image.setImageURI(Uri.parse(nextPhoto));
                }
            }
        });
        FrameLayout.LayoutParams imageParams = new FrameLayout.LayoutParams(-1, -1);
        imageParams.setMargins(0, 0, 0, dp(244));
        frame.addView(image, imageParams);

        ImageView close = iconButton(R.drawable.ic_back, v -> dialog.dismiss());
        close.setColorFilter(ink);
        close.setBackground(rounded(surfaceDialog, 24, palette.outlineVariant, 1));
        FrameLayout.LayoutParams closeParams = new FrameLayout.LayoutParams(dp(48), dp(48));
        closeParams.gravity = Gravity.TOP | Gravity.LEFT;
        closeParams.setMargins(dp(18), dp(18), 0, 0);
        frame.addView(close, closeParams);

        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(24), dp(18), dp(24), dp(18));
        sheet.setBackground(roundedTop(surfaceDialog, 32, palette.outlineVariant, 1));
        sheet.setClipToOutline(true);
        sheet.setElevation(dp(3));
        sheet.addView(imageActionRow(R.drawable.ic_background_remove, "Remove background", v -> {
            dialog.dismiss();
            removeBackground(item, new ImageView(this));
            showClothingDetail(item);
        }));
        sheet.addView(imageActionRow(R.drawable.ic_rotate, "Rotate", v -> {
            dialog.dismiss();
            rotatePhoto(item, new ImageView(this));
            showClothingDetail(item);
        }));
        sheet.addView(imageActionRow(R.drawable.ic_camera, "Add photo", v -> {
            dialog.dismiss();
            appendDetailPhoto(item, null);
        }));
        if (photoUris(item).size() > 1) {
            sheet.addView(imageActionRow(R.drawable.ic_move_left, "Move earlier", v -> {
                dialog.dismiss();
                reorderSelectedPhoto(item, -1);
            }));
            sheet.addView(imageActionRow(R.drawable.ic_move_right, "Move later", v -> {
                dialog.dismiss();
                reorderSelectedPhoto(item, 1);
            }));
        }
        sheet.addView(imageActionRow(R.drawable.ic_detail_tag, "Delete photo", v -> {
            dialog.dismiss();
            deleteSelectedPhoto(item);
        }));
        FrameLayout.LayoutParams sheetParams = new FrameLayout.LayoutParams(-1, -2);
        sheetParams.gravity = Gravity.BOTTOM;
        frame.addView(sheet, sheetParams);

        dialog.setContentView(frame);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(surface));
        }
        dialog.show();
        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setBackgroundDrawable(new ColorDrawable(surface));
            shownWindow.setLayout(-1, -1);
        }
    }

    private View imageActionRow(int iconRes, String label, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));
        row.setOnClickListener(listener);
        ImageView icon = iconView(iconRes);
        icon.setPadding(dp(7), dp(7), dp(7), dp(7));
        row.addView(icon, new LinearLayout.LayoutParams(dp(38), dp(38)));
        TextView text = text(label, 15, ink, false);
        text.setPadding(dp(12), 0, 0, 0);
        row.addView(text, new LinearLayout.LayoutParams(0, -2, 1));
        return row;
    }

    private void reorderSelectedPhoto(ClothingItem item, int direction) {
        List<String> uris = photoUris(item);
        if (uris.size() <= 1) {
            Toast.makeText(this, "Add more photos first", Toast.LENGTH_SHORT).show();
            return;
        }
        if (activePhotoIndex < 0 || activePhotoIndex >= uris.size()) {
            activePhotoIndex = 0;
        }
        int targetIndex = activePhotoIndex + direction;
        if (targetIndex < 0 || targetIndex >= uris.size()) {
            Toast.makeText(this, direction < 0 ? "Already first photo" : "Already last photo", Toast.LENGTH_SHORT).show();
            showClothingDetail(item);
            return;
        }
        String selected = uris.remove(activePhotoIndex);
        uris.add(targetIndex, selected);
        item.imageUris.clear();
        item.imageUris.addAll(uris);
        item.imageUri = item.imageUris.get(0);
        activePhotoIndex = targetIndex;
        item.updatedAt = System.currentTimeMillis();
        store.saveClothes(clothes);
        Toast.makeText(this, "Photo order updated", Toast.LENGTH_SHORT).show();
        showClothingDetail(item);
    }

    private void deleteSelectedPhoto(ClothingItem item) {
        List<String> uris = photoUris(item);
        if (uris.isEmpty()) {
            Toast.makeText(this, "No photo to delete", Toast.LENGTH_SHORT).show();
            return;
        }
        String selected = selectedPhotoUri(item);
        item.imageUris.remove(selected);
        if (item.imageUris.isEmpty()) {
            item.imageUri = "";
            activePhotoIndex = 0;
        } else {
            if (activePhotoIndex >= item.imageUris.size()) activePhotoIndex = item.imageUris.size() - 1;
            item.imageUri = item.imageUris.get(0);
        }
        item.updatedAt = System.currentTimeMillis();
        store.saveClothes(clothes);
        showClothingDetail(item);
    }

    private void removeBackground(ClothingItem item, ImageView hero) {
        String photoUri = selectedPhotoUri(item);
        if (isBlank(photoUri)) {
            Toast.makeText(this, "Choose a photo first", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Removing background…", Toast.LENGTH_SHORT).show();
        try {
            InputImage image = InputImage.fromFilePath(this, Uri.parse(photoUri));
            SubjectSegmenterOptions options = new SubjectSegmenterOptions.Builder()
                    .enableForegroundBitmap()
                    .build();
            SubjectSegmenter segmenter = SubjectSegmentation.getClient(options);
            segmenter.process(image)
                    .addOnSuccessListener(result -> saveSegmentedImage(item, hero, result))
                    .addOnFailureListener(error -> Toast.makeText(this, "Background removal failed: " + error.getMessage(), Toast.LENGTH_LONG).show());
        } catch (IOException exception) {
            Toast.makeText(this, "Could not read photo: " + exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void rotatePhoto(ClothingItem item, ImageView targetView) {
        String photoUri = selectedPhotoUri(item);
        if (isBlank(photoUri)) {
            Toast.makeText(this, "Choose a photo first", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(this, "Rotating photo…", Toast.LENGTH_SHORT).show();
        ioExecutor.execute(() -> {
            try (InputStream stream = getContentResolver().openInputStream(Uri.parse(photoUri))) {
                Bitmap source = BitmapFactory.decodeStream(stream);
                if (source == null) {
                    mainHandler.post(() -> Toast.makeText(this, "Could not read photo", Toast.LENGTH_LONG).show());
                    return;
                }
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap rotated = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
                File directory = new File(getFilesDir(), "rotated-clothes");
                if (!directory.exists() && !directory.mkdirs()) {
                    throw new IOException("Could not create image directory");
                }
                File output = new File(directory, item.id + "-rotated-" + System.currentTimeMillis() + ".png");
                try (FileOutputStream outputStream = new FileOutputStream(output)) {
                    rotated.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
                }
                Uri uri = Uri.fromFile(output);
                mainHandler.post(() -> {
                    replaceSelectedPhoto(item, uri.toString());
                    item.updatedAt = System.currentTimeMillis();
                    store.saveClothes(clothes);
                    if (targetView != null) {
                        loadImage(targetView, uri.toString(), android.R.drawable.ic_menu_gallery, palette.placeholder, 360);
                    }
                    Toast.makeText(this, "Photo rotated", Toast.LENGTH_SHORT).show();
                });
            } catch (IOException exception) {
                mainHandler.post(() -> Toast.makeText(this, "Could not rotate photo: " + exception.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void saveSegmentedImage(ClothingItem item, ImageView hero, SubjectSegmentationResult result) {
        Bitmap foreground = result.getForegroundBitmap();
        if (foreground == null) {
            Toast.makeText(this, "No foreground found", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            File directory = new File(getFilesDir(), "segmented-clothes");
            if (!directory.exists() && !directory.mkdirs()) {
                throw new IOException("Could not create image directory");
            }
            File output = new File(directory, item.id + "-" + System.currentTimeMillis() + ".png");
            try (FileOutputStream stream = new FileOutputStream(output)) {
                foreground.compress(Bitmap.CompressFormat.PNG, 100, stream);
            }
            Uri uri = Uri.fromFile(output);
            replaceSelectedPhoto(item, uri.toString());
            item.updatedAt = System.currentTimeMillis();
            store.saveClothes(clothes);
            loadImage(hero, uri.toString(), android.R.drawable.ic_menu_gallery, palette.placeholder, 360);
            Toast.makeText(this, "Background removed", Toast.LENGTH_SHORT).show();
            if (hero.getParent() == null) {
                showClothingDetail(item);
            }
        } catch (IOException exception) {
            Toast.makeText(this, "Could not save image: " + exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void editTextField(String title, String currentValue, FieldUpdater updater, ClothingItem item) {
        EditText input = input(title);
        input.setText(currentValue);
        showMaterialDialog(title, input, "Cancel", "Save", null, () -> {
            updater.update(input.getText().toString().trim());
            item.updatedAt = System.currentTimeMillis();
            store.saveClothes(clothes);
            showClothingDetail(item);
        });
    }

    private void editLinkField(ClothingItem item) {
        EditText input = input("Link");
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setText(item.link);
        input.setSelection(input.getText().length());
        showMaterialDialog("Link", input, "Cancel", "Save", null, () -> {
            item.link = normalizedLink(input.getText().toString());
            item.updatedAt = System.currentTimeMillis();
            store.saveClothes(clothes);
            showClothingDetail(item);
        });
    }

    private void editCategoryField(ClothingItem item) {
        showCategoryPicker(item.category, value -> {
            item.category = value;
            item.updatedAt = System.currentTimeMillis();
            store.saveClothes(clothes);
            showClothingDetail(item);
        });
    }

    private void editSeasonField(ClothingItem item) {
        showSeasonPicker(item.season, value -> item.season = value, () -> {
            item.updatedAt = System.currentTimeMillis();
            store.saveClothes(clothes);
            showClothingDetail(item);
        });
    }

    private void editMaterialField(ClothingItem item) {
        showMaterialPicker(item.material, value -> item.material = value, () -> {
            item.updatedAt = System.currentTimeMillis();
            store.saveClothes(clothes);
            showClothingDetail(item);
        });
    }

    private void showMaterialPicker(String currentValue, FieldUpdater updater, Runnable afterSave) {
        LinkedHashMap<String, String> selected = selectedMaterials(currentValue);
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(20), dp(14), dp(20), dp(16));
        sheet.setBackground(rounded(surfaceDialog, 32, Color.TRANSPARENT, 0));

        TextView handle = new TextView(this);
        handle.setBackground(rounded(outline, 3, Color.TRANSPARENT, 0));
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(dp(44), dp(5));
        handleParams.gravity = Gravity.CENTER_HORIZONTAL;
        handleParams.setMargins(0, 0, 0, dp(18));
        sheet.addView(handle, handleParams);

        EditText search = input("Search material");
        sheet.addView(search, new LinearLayout.LayoutParams(-1, dp(52)));

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        sheet.addView(scroll, new LinearLayout.LayoutParams(-1, dp(500)));

        Runnable render = () -> renderMaterialOptions(list, search.getText().toString(), selected);
        search.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                render.run();
            }
        });
        render.run();

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setPadding(0, dp(14), 0, 0);
        Button clear = smallButton("Clear selection");
        clear.setOnClickListener(v -> {
            selected.clear();
            render.run();
        });
        actions.addView(clear, new LinearLayout.LayoutParams(0, dp(48), 1));
        Button done = filledButton("Done");
        done.setOnClickListener(v -> {
            dialog.dismiss();
            updater.update(materialValue(selected));
            afterSave.run();
        });
        LinearLayout.LayoutParams doneParams = new LinearLayout.LayoutParams(dp(116), dp(48));
        doneParams.setMargins(dp(16), 0, 0, 0);
        actions.addView(done, doneParams);
        sheet.addView(actions);

        dialog.setContentView(sheet);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            shownWindow.setGravity(Gravity.BOTTOM);
            shownWindow.setLayout(-1, -2);
        }
    }

    private void renderMaterialOptions(LinearLayout list, String query, LinkedHashMap<String, String> selected) {
        list.removeAllViews();
        String needle = query == null ? "" : query.trim().toLowerCase();
        for (MaterialOption option : MATERIAL_OPTIONS) {
            if (!needle.isEmpty() && !option.name.toLowerCase().contains(needle)) {
                continue;
            }
            list.addView(materialOptionRow(option, selected));
        }
    }

    private View materialOptionRow(MaterialOption option, LinkedHashMap<String, String> selected) {
        boolean isSelected = selected.containsKey(option.name);
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(6), dp(8), dp(6), dp(8));

        TextView swatch = new TextView(this);
        swatch.setBackground(rounded(option.color, 10, palette.outlineVariant, 1));
        row.addView(swatch, new LinearLayout.LayoutParams(dp(48), dp(48)));

        TextView label = text(option.name, 19, palette.rowText, false);
        label.setPadding(dp(16), 0, 0, 0);
        row.addView(label, new LinearLayout.LayoutParams(0, -2, 1));

        EditText percent = new EditText(this);
        percent.setText(isSelected ? selected.get(option.name) : "");
        percent.setHint("%");
        percent.setSingleLine(true);
        percent.setTextSize(14);
        percent.setGravity(Gravity.CENTER);
        percent.setInputType(InputType.TYPE_CLASS_NUMBER);
        percent.setTextColor(ink);
        percent.setHintTextColor(muted);
        percent.setBackground(rounded(Color.WHITE, 14, outline, 1));
        percent.setVisibility(isSelected ? View.VISIBLE : View.GONE);
        percent.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                if (selected.containsKey(option.name)) {
                    selected.put(option.name, editable.toString().trim());
                }
            }
        });
        LinearLayout.LayoutParams percentParams = new LinearLayout.LayoutParams(dp(62), dp(42));
        percentParams.setMargins(dp(10), 0, dp(12), 0);
        row.addView(percent, percentParams);

        TextView check = text(isSelected ? "✓" : "", 17, isSelected ? onPrimary : Color.TRANSPARENT, true);
        check.setGravity(Gravity.CENTER);
        check.setBackground(oval(isSelected ? primary : Color.TRANSPARENT, isSelected ? primary : outline, 2));
        row.addView(check, new LinearLayout.LayoutParams(dp(34), dp(34)));

        row.setOnClickListener(v -> {
            if (selected.containsKey(option.name)) {
                selected.remove(option.name);
                percent.setVisibility(View.GONE);
                percent.setText("");
                check.setText("");
                check.setTextColor(Color.TRANSPARENT);
                check.setBackground(oval(Color.TRANSPARENT, outline, 2));
            } else {
                selected.put(option.name, "100");
                percent.setText("100");
                percent.setVisibility(View.VISIBLE);
                check.setText("✓");
                check.setTextColor(onPrimary);
                check.setBackground(oval(primary, primary, 2));
                percent.requestFocus();
                percent.setSelection(percent.getText().length());
            }
        });
        return row;
    }

    private LinkedHashMap<String, String> selectedMaterials(String value) {
        LinkedHashMap<String, String> selected = new LinkedHashMap<>();
        if (isBlank(value)) return selected;
        String lower = value.toLowerCase();
        for (MaterialOption option : MATERIAL_OPTIONS) {
            if (lower.contains(option.name.toLowerCase())) {
                selected.put(option.name, materialPercent(value, option.name));
            }
        }
        return selected;
    }

    private String materialPercent(String value, String materialName) {
        Pattern pattern = Pattern.compile("(?i)" + Pattern.quote(materialName) + "\\s*(\\d{1,3})?\\s*%?");
        Matcher matcher = pattern.matcher(value);
        if (matcher.find() && matcher.group(1) != null) {
            return matcher.group(1);
        }
        Pattern before = Pattern.compile("(\\d{1,3})\\s*%\\s*" + Pattern.quote(materialName), Pattern.CASE_INSENSITIVE);
        Matcher beforeMatcher = before.matcher(value);
        if (beforeMatcher.find()) {
            return beforeMatcher.group(1);
        }
        return "";
    }

    private String materialValue(LinkedHashMap<String, String> selected) {
        StringBuilder value = new StringBuilder();
        for (Map.Entry<String, String> entry : selected.entrySet()) {
            if (value.length() > 0) value.append(" · ");
            value.append(entry.getKey());
            if (!isBlank(entry.getValue())) {
                value.append(" ").append(entry.getValue()).append("%");
            }
        }
        return value.toString();
    }

    private void updateMaterialButton(Button button, String value) {
        button.setText(isBlank(value) ? "Material" : "Material: " + value);
        button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_detail_material, 0, 0, 0);
        button.setCompoundDrawablePadding(dp(10));
    }

    private void showSeasonPicker(String currentValue, FieldUpdater updater, Runnable afterSave) {
        Set<String> selected = selectedSeasons(currentValue);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);

        TextView anyRow = seasonChoiceRow("All seasons", selected.isEmpty());
        body.addView(anyRow);
        List<TextView> rows = new ArrayList<>();
        anyRow.setOnClickListener(v -> {
            selected.clear();
            refreshSeasonRows(anyRow, rows, selected);
        });

        for (String option : SEASON_OPTIONS) {
            TextView row = seasonChoiceRow(option, selected.contains(option));
            row.setOnClickListener(v -> {
                if (selected.contains(option)) {
                    selected.remove(option);
                } else {
                    selected.add(option);
                }
                refreshSeasonRows(anyRow, rows, selected);
            });
            rows.add(row);
            body.addView(row);
        }

        showMaterialDialog("Season", body, "Cancel", "Save", null, () -> {
            updater.update(seasonValue(selected));
            afterSave.run();
        });
    }

    private void showOccasionPicker(String currentValue, FieldUpdater updater, Runnable afterSave) {
        String[] selected = {selectedOccasion(currentValue)};
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(24), dp(14), dp(24), dp(22));
        sheet.setBackground(rounded(surfaceDialog, 32, Color.TRANSPARENT, 0));

        TextView handle = new TextView(this);
        handle.setBackground(rounded(outline, 3, Color.TRANSPARENT, 0));
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(dp(44), dp(5));
        handleParams.gravity = Gravity.CENTER_HORIZONTAL;
        handleParams.setMargins(0, 0, 0, dp(18));
        sheet.addView(handle, handleParams);

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        sheet.addView(scroll, new LinearLayout.LayoutParams(-1, dp(470)));

        Runnable render = () -> renderOccasionOptions(list, selected);
        render.run();

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setPadding(0, dp(14), 0, 0);
        Button clear = smallButton("Clear selection");
        clear.setOnClickListener(v -> {
            selected[0] = "";
            render.run();
        });
        actions.addView(clear, new LinearLayout.LayoutParams(0, dp(48), 1));
        Button done = filledButton("Done");
        done.setOnClickListener(v -> {
            dialog.dismiss();
            updater.update(selected[0]);
            afterSave.run();
        });
        LinearLayout.LayoutParams doneParams = new LinearLayout.LayoutParams(dp(116), dp(48));
        doneParams.setMargins(dp(16), 0, 0, 0);
        actions.addView(done, doneParams);
        sheet.addView(actions);

        dialog.setContentView(sheet);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            shownWindow.setGravity(Gravity.BOTTOM);
            shownWindow.setLayout(-1, -2);
        }
    }

    private void renderOccasionOptions(LinearLayout list, String[] selected) {
        list.removeAllViews();
        for (OccasionOption option : OCCASION_OPTIONS) {
            list.addView(occasionOptionRow(option, option.label.equals(selected[0]), v -> {
                selected[0] = option.label.equals(selected[0]) ? "" : option.label;
                renderOccasionOptions(list, selected);
            }));
        }
    }

    private View occasionOptionRow(OccasionOption option, boolean selected, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(4), dp(8), dp(4), dp(8));
        row.setOnClickListener(listener);

        TextView icon = text(option.icon, 25, ink, false);
        icon.setGravity(Gravity.CENTER);
        row.addView(icon, new LinearLayout.LayoutParams(dp(52), dp(48)));

        TextView label = text(option.label, 19, selected ? ink : palette.rowText, selected);
        label.setPadding(dp(8), 0, 0, 0);
        row.addView(label, new LinearLayout.LayoutParams(0, -2, 1));

        TextView check = text(selected ? "✓" : "", 17, selected ? onPrimary : Color.TRANSPARENT, true);
        check.setGravity(Gravity.CENTER);
        check.setBackground(oval(selected ? primary : Color.TRANSPARENT, selected ? primary : outline, 2));
        row.addView(check, new LinearLayout.LayoutParams(dp(34), dp(34)));
        return row;
    }

    private String selectedOccasion(String value) {
        if (isBlank(value)) return "";
        for (OccasionOption option : OCCASION_OPTIONS) {
            if (option.label.equalsIgnoreCase(value.trim())) {
                return option.label;
            }
        }
        String lower = value.toLowerCase();
        for (OccasionOption option : OCCASION_OPTIONS) {
            if (lower.contains(option.label.toLowerCase())) {
                return option.label;
            }
        }
        return value.trim();
    }

    private TextView seasonChoiceRow(String label, boolean selected) {
        TextView row = text((selected ? "✓  " : "   ") + label, 16, ink, false);
        row.setPadding(dp(8), dp(12), dp(8), dp(12));
        return row;
    }

    private void refreshSeasonRows(TextView anyRow, List<TextView> rows, Set<String> selected) {
        anyRow.setText((selected.isEmpty() ? "✓  " : "   ") + "All seasons");
        for (int i = 0; i < rows.size(); i++) {
            String option = SEASON_OPTIONS[i];
            rows.get(i).setText((selected.contains(option) ? "✓  " : "   ") + option);
        }
    }

    private Set<String> selectedSeasons(String value) {
        Set<String> selected = new LinkedHashSet<>();
        if (isBlank(value) || value.equalsIgnoreCase(SEASON_ANY) || value.toLowerCase().contains("any")) {
            return selected;
        }
        String lower = value.toLowerCase();
        for (String option : SEASON_OPTIONS) {
            String name = option.substring(3).trim().toLowerCase();
            if (lower.contains(name) || lower.contains(option.toLowerCase())) {
                selected.add(option);
            }
        }
        if (lower.contains("fall")) selected.add("🍂 Autumn");
        return selected;
    }

    private String seasonValue(Set<String> selected) {
        if (selected == null || selected.isEmpty() || selected.size() == SEASON_OPTIONS.length) {
            return SEASON_ANY;
        }
        StringBuilder value = new StringBuilder();
        for (String season : selected) {
            if (value.length() > 0) value.append(" · ");
            value.append(season);
        }
        return value.toString();
    }

    private void editAddedDate(ClothingItem item) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(item.addedAt > 0 ? item.addedAt : System.currentTimeMillis());
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.setTimeInMillis(item.addedAt > 0 ? item.addedAt : System.currentTimeMillis());
                    selected.set(Calendar.YEAR, year);
                    selected.set(Calendar.MONTH, month);
                    selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    selected.set(Calendar.HOUR_OF_DAY, 12);
                    selected.set(Calendar.MINUTE, 0);
                    selected.set(Calendar.SECOND, 0);
                    selected.set(Calendar.MILLISECOND, 0);
                    item.addedAt = selected.getTimeInMillis();
                    item.updatedAt = System.currentTimeMillis();
                    store.saveClothes(clothes);
                    showClothingDetail(item);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private View insightCards(ClothingItem item) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, 0, 0, dp(8));
        LinearLayout.LayoutParams left = new LinearLayout.LayoutParams(0, dp(62), 1);
        left.setMargins(0, 0, dp(6), 0);
        LinearLayout.LayoutParams right = new LinearLayout.LayoutParams(0, dp(62), 1);
        right.setMargins(dp(6), 0, 0, 0);
        row.addView(infoCard(categoryDrawable(item.category), "Category", value(item.category, "Uncategorized"), primaryContainer), left);
        row.addView(infoCard(R.drawable.ic_detail_season, "Season", value(item.season, SEASON_ANY), palette.tonalSurface()), right);
        return row;
    }

    private View infoCard(int iconRes, String title, String body, int iconBackground) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(10), dp(8), dp(10), dp(8));
        card.setBackground(rounded(Color.WHITE, 14, palette.outlineVariant, 1));
        ImageView icon = iconView(iconRes);
        icon.setPadding(dp(6), dp(6), dp(6), dp(6));
        icon.setBackground(rounded(iconBackground, 9, Color.TRANSPARENT, 0));
        card.addView(icon, new LinearLayout.LayoutParams(dp(32), dp(32)));
        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(8), 0, 0, 0);
        copy.addView(text(title, 10, muted, true));
        TextView bodyText = text(body, 11, ink, true);
        bodyText.setSingleLine(true);
        copy.addView(bodyText);
        card.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        return card;
    }

    private LinearLayout groupedRows() {
        LinearLayout group = new LinearLayout(this);
        group.setOrientation(LinearLayout.VERTICAL);
        group.setPadding(0, dp(2), 0, dp(2));
        group.setBackground(rounded(Color.WHITE, 22, Color.TRANSPARENT, 0));
        LinearLayout.LayoutParams params = blockParams();
        params.setMargins(0, dp(8), 0, dp(10));
        group.setLayoutParams(params);
        return group;
    }

    private View metadataRow(int iconRes, String value, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(6), dp(14), dp(6));
        row.setOnClickListener(listener);
        ImageView iconView = iconView(iconRes);
        iconView.setColorFilter(palette.iconMuted);
        iconView.setPadding(dp(5), dp(5), dp(5), dp(5));
        row.addView(iconView, new LinearLayout.LayoutParams(dp(34), dp(36)));
        TextView valueView = text(value, 16, palette.rowText, false);
        valueView.setPadding(dp(12), 0, 0, 0);
        row.addView(valueView, new LinearLayout.LayoutParams(0, -2, 1));
        TextView arrow = text("›", 24, palette.arrow, false);
        arrow.setGravity(Gravity.CENTER);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(26), dp(36)));
        return row;
    }

    private View detailRow(int iconRes, String label, String value, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(8), dp(14), dp(8));
        row.setOnClickListener(listener);

        ImageView iconView = iconView(iconRes);
        iconView.setColorFilter(palette.iconMuted);
        iconView.setPadding(dp(5), dp(5), dp(5), dp(5));
        row.addView(iconView, new LinearLayout.LayoutParams(dp(34), dp(40)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(12), 0, 0, 0);
        copy.addView(text(label, 11, muted, true));
        copy.addView(text(value, 14, ink, false));
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));

        TextView arrow = text("›", 24, palette.arrow, false);
        arrow.setGravity(Gravity.CENTER);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(26), dp(40)));
        return row;
    }

    private View detailColorRow(ClothingItem item) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(8), dp(14), dp(8));
        row.setOnClickListener(v -> editColorField(item));

        ImageView iconView = iconView(R.drawable.ic_detail_color);
        iconView.setColorFilter(palette.iconMuted);
        iconView.setPadding(dp(5), dp(5), dp(5), dp(5));
        row.addView(iconView, new LinearLayout.LayoutParams(dp(34), dp(40)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(12), 0, 0, 0);
        copy.addView(text("Color", 11, muted, true));
        if (selectedColorNames(item.color).isEmpty()) {
            copy.addView(text("Add color", 14, ink, false));
        } else {
            copy.addView(colorPreviewRow(item.color, dp(20)));
        }
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));

        TextView arrow = text("›", 24, palette.arrow, false);
        arrow.setGravity(Gravity.CENTER);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(26), dp(40)));
        return row;
    }

    private void editColorField(ClothingItem item) {
        showColorPicker(item.color, value -> item.color = value, () -> {
            item.updatedAt = System.currentTimeMillis();
            store.saveClothes(clothes);
            showClothingDetail(item);
        });
    }

    private void showColorPicker(String currentValue, FieldUpdater updater, Runnable afterSave) {
        Set<String> selected = selectedColorNames(currentValue);
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(24), dp(14), dp(24), dp(22));
        sheet.setBackground(rounded(surfaceDialog, 32, Color.TRANSPARENT, 0));

        TextView handle = new TextView(this);
        handle.setBackground(rounded(outline, 3, Color.TRANSPARENT, 0));
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(dp(44), dp(5));
        handleParams.gravity = Gravity.CENTER_HORIZONTAL;
        handleParams.setMargins(0, 0, 0, dp(18));
        sheet.addView(handle, handleParams);

        List<FrameLayout> swatches = new ArrayList<>();
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        for (int rowIndex = 0; rowIndex < Math.ceil(CLOTHING_COLORS.length / 6f); rowIndex++) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int col = 0; col < 6; col++) {
                int index = rowIndex * 6 + col;
                if (index >= CLOTHING_COLORS.length) {
                    TextView spacer = new TextView(this);
                    row.addView(spacer, new LinearLayout.LayoutParams(0, dp(54), 1));
                    continue;
                }
                ColorOption option = CLOTHING_COLORS[index];
                FrameLayout swatch = colorSwatch(option, selected.contains(option.name));
                swatch.setOnClickListener(v -> {
                    if (selected.contains(option.name)) {
                        selected.remove(option.name);
                    } else {
                        selected.add(option.name);
                    }
                    refreshColorSwatches(swatches, selected);
                });
                swatches.add(swatch);
                FrameLayout cell = new FrameLayout(this);
                FrameLayout.LayoutParams swatchParams = new FrameLayout.LayoutParams(dp(54), dp(54));
                swatchParams.gravity = Gravity.CENTER;
                cell.addView(swatch, swatchParams);
                row.addView(cell, new LinearLayout.LayoutParams(0, dp(54), 1));
            }
            grid.addView(row, new LinearLayout.LayoutParams(-1, dp(54)));
        }
        sheet.addView(grid);

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setPadding(0, dp(22), 0, 0);
        Button clear = smallButton("Clear selection");
        clear.setOnClickListener(v -> {
            selected.clear();
            refreshColorSwatches(swatches, selected);
        });
        actions.addView(clear, new LinearLayout.LayoutParams(0, dp(48), 1));
        Button done = filledButton("Done");
        done.setOnClickListener(v -> {
            dialog.dismiss();
            updater.update(colorValue(selected));
            afterSave.run();
        });
        LinearLayout.LayoutParams doneParams = new LinearLayout.LayoutParams(dp(116), dp(48));
        doneParams.setMargins(dp(16), 0, 0, 0);
        actions.addView(done, doneParams);
        sheet.addView(actions);

        dialog.setContentView(sheet);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            shownWindow.setGravity(Gravity.BOTTOM);
            shownWindow.setLayout(-1, -2);
        }
    }

    private FrameLayout colorSwatch(ColorOption option, boolean selected) {
        FrameLayout outer = new FrameLayout(this);
        outer.setPadding(dp(4), dp(4), dp(4), dp(4));
        outer.setTag(option.name);
        outer.setBackground(oval(Color.TRANSPARENT, selected ? primary : Color.TRANSPARENT, selected ? 2 : 0));

        TextView inner = new TextView(this);
        int strokeColor = option.color == Color.WHITE || option.color == 0xFFFCFAEA ? outline : Color.TRANSPARENT;
        inner.setBackground(oval(option.color, strokeColor, strokeColor == Color.TRANSPARENT ? 0 : 1));
        FrameLayout.LayoutParams innerParams = new FrameLayout.LayoutParams(dp(40), dp(40));
        innerParams.gravity = Gravity.CENTER;
        outer.addView(inner, innerParams);
        return outer;
    }

    private void refreshColorSwatches(List<FrameLayout> swatches, Set<String> selected) {
        for (FrameLayout swatch : swatches) {
            Object tag = swatch.getTag();
            if (tag instanceof String) {
                swatch.setBackground(oval(Color.TRANSPARENT, selected.contains(tag) ? primary : Color.TRANSPARENT, selected.contains(tag) ? 2 : 0));
            }
        }
    }

    private View colorPreviewRow(String value, int size) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        Set<String> selected = selectedColorNames(value);
        int shown = 0;
        for (ColorOption option : CLOTHING_COLORS) {
            if (!selected.contains(option.name)) continue;
            TextView dot = new TextView(this);
            int strokeColor = option.color == Color.WHITE || option.color == 0xFFFCFAEA ? outline : Color.TRANSPARENT;
            dot.setBackground(oval(option.color, strokeColor, strokeColor == Color.TRANSPARENT ? 0 : 1));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(0, dp(2), dp(5), 0);
            row.addView(dot, params);
            shown++;
            if (shown == 6) break;
        }
        if (selected.size() > shown) {
            TextView more = text("+" + (selected.size() - shown), 12, muted, true);
            row.addView(more);
        }
        return row;
    }

    private void updateColorButton(Button button, String value) {
        Set<String> selected = selectedColorNames(value);
        if (selected.isEmpty()) {
            button.setText("Color");
            return;
        }
        button.setText("Color: " + colorValue(selected));
    }

    private Set<String> selectedColorNames(String value) {
        Set<String> selected = new LinkedHashSet<>();
        if (isBlank(value)) return selected;
        String normalized = value.replace(",", "·").replace("/", "·");
        String[] parts = normalized.split("·");
        for (String part : parts) {
            String token = part.trim();
            for (ColorOption option : CLOTHING_COLORS) {
                if (option.name.equalsIgnoreCase(token)) {
                    selected.add(option.name);
                    break;
                }
            }
        }
        return selected;
    }

    private String colorValue(Set<String> selected) {
        StringBuilder value = new StringBuilder();
        for (String color : selected) {
            if (value.length() > 0) value.append(" · ");
            value.append(color);
        }
        return value.toString();
    }

    private interface FieldUpdater {
        void update(String value);
    }

    private Button neutralPill(String label) {
        Button button = baseButton(label);
        button.setTextColor(palette.neutralButtonText);
        button.setTextSize(17);
        button.setTypeface(button.getTypeface(), Typeface.BOLD);
        button.setBackground(rounded(palette.neutralButtonContainer, 18, Color.TRANSPARENT, 0));
        return button;
    }

    private View actionChip(int iconRes, String label, View.OnClickListener listener) {
        LinearLayout chip = new LinearLayout(this);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(6), 0, dp(6), 0);
        chip.setBackground(rounded(Color.WHITE, 18, palette.outlineVariant, 1));
        chip.setOnClickListener(listener);
        ImageView icon = iconView(iconRes);
        icon.setPadding(dp(4), dp(4), dp(4), dp(4));
        chip.addView(icon, new LinearLayout.LayoutParams(dp(24), dp(24)));
        TextView text = text(label, 11, ink, true);
        text.setSingleLine(true);
        text.setPadding(dp(4), 0, 0, 0);
        chip.addView(text);
        return chip;
    }

    private Button chip(String label, boolean active) {
        Button button = baseButton(label);
        button.setTextSize(15);
        button.setTypeface(button.getTypeface(), Typeface.BOLD);
        button.setTextColor(active ? onPrimary : palette.inactiveChipText);
        button.setBackground(rounded(active ? primary : Color.TRANSPARENT, 16, active ? primary : palette.outlineVariant, active ? 0 : 1));
        return button;
    }

    private String colorText(String color) {
        if (isBlank(color)) {
            return "Add color";
        }
        return "●  " + color;
    }

    private String priceText(String price) {
        if (isBlank(price)) {
            return "Add price";
        }
        return currency + " " + price;
    }

    private int seasonIndex(String season) {
        if (isBlank(season)) return 0;
        String lower = season.toLowerCase();
        for (int i = 0; i < SEASON_OPTIONS.length; i++) {
            if (SEASON_OPTIONS[i].toLowerCase().contains(lower) || lower.contains(SEASON_OPTIONS[i].substring(3).trim().toLowerCase())) {
                return i;
            }
        }
        if (lower.contains("spring")) return 0;
        if (lower.contains("summer")) return 1;
        if (lower.contains("autumn") || lower.contains("fall")) return 2;
        if (lower.contains("winter")) return 3;
        return 0;
    }

    private boolean isPantsItem(ClothingItem item) {
        return item != null && (isPantsText(item.category) || isPantsText(item.name));
    }

    private boolean isPantsText(String value) {
        if (value == null) return false;
        String lower = value.toLowerCase();
        return lower.contains("pant")
                || lower.contains("pants")
                || lower.contains("trouser")
                || lower.contains("trousers")
                || lower.contains("jean")
                || lower.contains("jeans")
                || lower.contains("chino")
                || lower.contains("chinos")
                || lower.contains("slack");
    }

    private void updatePantsFieldsFromForm(EditText name, EditText waist, EditText length, String category) {
        int visibility = (isPantsText(category) || isPantsText(name.getText().toString())) ? View.VISIBLE : View.GONE;
        waist.setVisibility(visibility);
        length.setVisibility(visibility);
    }

    private void updateCategoryButton(Button button, String category) {
        button.setText("Category: " + value(category, "Other"));
        button.setCompoundDrawablesWithIntrinsicBounds(categoryDrawable(category), 0, 0, 0);
        button.setCompoundDrawablePadding(dp(10));
    }

    private void showCategoryPicker(String currentValue, FieldUpdater updater) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(20), dp(14), dp(20), dp(16));
        sheet.setBackground(rounded(surfaceDialog, 32, Color.TRANSPARENT, 0));

        TextView handle = new TextView(this);
        handle.setBackground(rounded(outline, 3, Color.TRANSPARENT, 0));
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(dp(44), dp(5));
        handleParams.gravity = Gravity.CENTER_HORIZONTAL;
        handleParams.setMargins(0, 0, 0, dp(18));
        sheet.addView(handle, handleParams);

        EditText search = input("Search category");
        sheet.addView(search, new LinearLayout.LayoutParams(-1, dp(52)));

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        sheet.addView(scroll, new LinearLayout.LayoutParams(-1, dp(520)));

        Runnable render = () -> renderCategoryOptions(list, dialog, currentValue, search.getText().toString(), updater);
        search.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                render.run();
            }
        });
        render.run();

        dialog.setContentView(sheet);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            shownWindow.setGravity(Gravity.BOTTOM);
            shownWindow.setLayout(-1, -2);
        }
    }

    private void renderCategoryOptions(LinearLayout list, Dialog dialog, String currentValue, String query, FieldUpdater updater) {
        list.removeAllViews();
        String activeGroup = "";
        String needle = query == null ? "" : query.trim().toLowerCase();
        for (CategoryOption option : CATEGORY_OPTIONS) {
            if (!needle.isEmpty() && !option.label.toLowerCase().contains(needle) && !option.group.toLowerCase().contains(needle)) {
                continue;
            }
            if (!option.group.equals(activeGroup)) {
                activeGroup = option.group;
                TextView section = text(activeGroup, 16, ink, true);
                section.setPadding(0, dp(18), 0, dp(6));
                list.addView(section);
            }
            list.addView(categoryOptionRow(option, option.label.equalsIgnoreCase(currentValue), v -> {
                dialog.dismiss();
                updater.update(option.label);
            }));
        }
    }

    private View categoryOptionRow(CategoryOption option, boolean selected, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(6), dp(7), dp(6), dp(7));
        row.setOnClickListener(listener);

        ImageView icon = iconView(option.iconRes);
        icon.setColorFilter(selected ? primary : ink);
        icon.setPadding(dp(6), dp(6), dp(6), dp(6));
        row.addView(icon, new LinearLayout.LayoutParams(dp(48), dp(48)));

        TextView label = text(option.label, 19, selected ? primary : palette.rowText, selected);
        label.setPadding(dp(14), 0, 0, 0);
        row.addView(label, new LinearLayout.LayoutParams(0, -2, 1));

        if (selected) {
            TextView check = text("✓", 18, primary, true);
            check.setGravity(Gravity.CENTER);
            row.addView(check, new LinearLayout.LayoutParams(dp(32), dp(40)));
        }
        return row;
    }

    private int categoryDrawable(String category) {
        CategoryOption option = categoryOption(category);
        return option == null ? R.drawable.ic_cat_other : option.iconRes;
    }

    private CategoryOption categoryOption(String category) {
        if (isBlank(category)) return null;
        for (CategoryOption option : CATEGORY_OPTIONS) {
            if (option.label.equalsIgnoreCase(category)) return option;
        }
        String lower = category.toLowerCase();
        for (CategoryOption option : CATEGORY_OPTIONS) {
            if (lower.contains(option.label.toLowerCase()) || option.label.toLowerCase().contains(lower)) {
                return option;
            }
        }
        return null;
    }

    private void renderClothingForm(ClothingItem existing) {
        renderClothingForm(existing == null ? new ClothingItem() : existing, existing == null);
    }

    private void renderClothingForm(ClothingItem draft, boolean isNew) {
        activeTab = "clothingForm";
        updateBottomNavigation();
        renderTopBar(value(draft.category, value(draft.name, isNew ? "Add cloth" : "Edit cloth")), true, () -> saveClothingDraft(draft, isNew));
        useScrollContent();
        content.removeAllViews();
        content.setPadding(dp(18), dp(8), dp(18), dp(32));

        ImageView hero = new ImageView(this);
        hero.setScaleType(ImageView.ScaleType.FIT_CENTER);
        hero.setBackgroundColor(Color.TRANSPARENT);
        hero.setPadding(0, 0, 0, 0);
        String selectedPhoto = selectedPhotoUri(draft);
        if (!isBlank(selectedPhoto)) {
            loadImage(hero, selectedPhoto, android.R.drawable.ic_menu_gallery, palette.placeholder, 360);
        } else {
            hero.setImageResource(android.R.drawable.ic_menu_gallery);
            hero.setColorFilter(palette.placeholder);
        }
        attachPhotoSwipe(hero, draft, () -> appendDetailPhoto(draft, hero));
        content.addView(hero, new LinearLayout.LayoutParams(-1, dp(260)));
        content.addView(photoDots(draft));

        content.addView(insightCards(draft));

        TextView section = text("Details", 24, ink, true);
        section.setPadding(0, dp(18), 0, dp(8));
        content.addView(section);

        LinearLayout details = groupedRows();
        details.addView(detailRow(categoryDrawable(draft.category), "Category", value(draft.category, "Add category"), v -> editDraftCategoryField(draft, isNew)));
        details.addView(detailRow(R.drawable.ic_detail_category, "Name", value(draft.name, "Add name"), v -> editDraftTextField("Name", draft.name, value -> draft.name = value, draft, isNew)));
        details.addView(detailRow(R.drawable.ic_detail_brand, "Brand", value(draft.brand, "Add brand"), v -> editDraftTextField("Brand", draft.brand, value -> draft.brand = value, draft, isNew)));
        details.addView(draftColorRow(draft, isNew));
        details.addView(detailRow(R.drawable.ic_detail_material, "Material", value(draft.material, "Add material"), v -> editDraftMaterialField(draft, isNew)));
        details.addView(detailRow(R.drawable.ic_detail_notes, "Notes", value(draft.notes, "Add a note..."), v -> editDraftTextField("Notes", draft.notes, value -> draft.notes = value, draft, isNew)));
        details.addView(detailRow(R.drawable.ic_detail_link, "Link", value(draft.link, "Add link"), v -> editDraftLinkField(draft, isNew)));
        details.addView(detailRow(R.drawable.ic_detail_size, "Fit", value(draft.size, "Add fit or size"), v -> editDraftTextField("Size", draft.size, value -> draft.size = value, draft, isNew)));
        if (isPantsItem(draft)) {
            details.addView(detailRow(R.drawable.ic_detail_waist, "Waist", value(draft.waist, "Add waist (W)"), v -> editDraftTextField("Waist (W)", draft.waist, value -> draft.waist = value, draft, isNew)));
            details.addView(detailRow(R.drawable.ic_detail_length, "Length", value(draft.length, "Add length (L)"), v -> editDraftTextField("Length (L)", draft.length, value -> draft.length = value, draft, isNew)));
        }
        details.addView(detailRow(R.drawable.ic_detail_date, "Added", DateFormat.getDateInstance().format(draft.addedAt), v -> editDraftAddedDate(draft, isNew)));
        details.addView(detailRow(R.drawable.ic_detail_season, "Season", value(draft.season, "Add season"), v -> editDraftSeasonField(draft, isNew)));
        details.addView(detailRow(R.drawable.ic_detail_price, "Price", priceText(draft.price), v -> editDraftTextField("Price", draft.price, value -> draft.price = value, draft, isNew)));
        details.addView(detailRow(R.drawable.ic_detail_care, "Care", value(draft.care, "Add care instructions"), v -> editDraftTextField("Care instructions", draft.care, value -> draft.care = value, draft, isNew)));
        content.addView(details);
    }

    private void saveClothingDraft(ClothingItem draft, boolean isNew) {
        draft.updatedAt = System.currentTimeMillis();
        if (isNew && !clothes.contains(draft)) {
            clothes.add(0, draft);
            rebuildItemIndex();
        }
        store.saveClothes(clothes);
        renderClothes();
    }

    private void editDraftTextField(String title, String currentValue, FieldUpdater updater, ClothingItem draft, boolean isNew) {
        EditText input = input(title);
        input.setText(currentValue);
        showMaterialDialog(title, input, "Cancel", "Save", null, () -> {
            updater.update(input.getText().toString().trim());
            draft.updatedAt = System.currentTimeMillis();
            renderClothingForm(draft, isNew);
        });
    }

    private void editDraftLinkField(ClothingItem draft, boolean isNew) {
        EditText input = input("Link");
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setText(draft.link);
        input.setSelection(input.getText().length());
        showMaterialDialog("Link", input, "Cancel", "Save", null, () -> {
            draft.link = normalizedLink(input.getText().toString());
            draft.updatedAt = System.currentTimeMillis();
            renderClothingForm(draft, isNew);
        });
    }

    private void editDraftCategoryField(ClothingItem draft, boolean isNew) {
        showCategoryPicker(draft.category, value -> {
            draft.category = value;
            draft.updatedAt = System.currentTimeMillis();
            renderClothingForm(draft, isNew);
        });
    }

    private void editDraftSeasonField(ClothingItem draft, boolean isNew) {
        showSeasonPicker(draft.season, value -> draft.season = value, () -> {
            draft.updatedAt = System.currentTimeMillis();
            renderClothingForm(draft, isNew);
        });
    }

    private void editDraftMaterialField(ClothingItem draft, boolean isNew) {
        showMaterialPicker(draft.material, value -> draft.material = value, () -> {
            draft.updatedAt = System.currentTimeMillis();
            renderClothingForm(draft, isNew);
        });
    }

    private View draftColorRow(ClothingItem draft, boolean isNew) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(8), dp(14), dp(8));
        row.setOnClickListener(v -> showColorPicker(draft.color, value -> draft.color = value, () -> {
            draft.updatedAt = System.currentTimeMillis();
            renderClothingForm(draft, isNew);
        }));

        ImageView iconView = iconView(R.drawable.ic_detail_color);
        iconView.setColorFilter(palette.iconMuted);
        iconView.setPadding(dp(5), dp(5), dp(5), dp(5));
        row.addView(iconView, new LinearLayout.LayoutParams(dp(34), dp(40)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(12), 0, 0, 0);
        copy.addView(text("Color", 11, muted, true));
        if (selectedColorNames(draft.color).isEmpty()) {
            copy.addView(text("Add color", 14, ink, false));
        } else {
            copy.addView(colorPreviewRow(draft.color, dp(20)));
        }
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));

        TextView arrow = text("›", 24, palette.arrow, false);
        arrow.setGravity(Gravity.CENTER);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(26), dp(40)));
        return row;
    }

    private void editDraftAddedDate(ClothingItem draft, boolean isNew) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(draft.addedAt > 0 ? draft.addedAt : System.currentTimeMillis());
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.setTimeInMillis(draft.addedAt > 0 ? draft.addedAt : System.currentTimeMillis());
                    selected.set(Calendar.YEAR, year);
                    selected.set(Calendar.MONTH, month);
                    selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    selected.set(Calendar.HOUR_OF_DAY, 12);
                    selected.set(Calendar.MINUTE, 0);
                    selected.set(Calendar.SECOND, 0);
                    selected.set(Calendar.MILLISECOND, 0);
                    draft.addedAt = selected.getTimeInMillis();
                    draft.updatedAt = System.currentTimeMillis();
                    renderClothingForm(draft, isNew);
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private void renderOutfits() {
        activeTab = "outfits";
        renderTopBar("Outfits", false);
        setStickyBar(outfitTabs(), 22);
        updateBottomNavigation();
        useScrollContent();
        content.removeAllViews();
        content.setPadding(dp(22), dp(2), dp(22), dp(28));

        if (outfitInspirationTab) {
            renderInspirationView();
            return;
        }

        if (clothes.isEmpty()) {
            content.addView(emptyState("Add clothing items before creating outfits."));
            return;
        }
        if (outfits.isEmpty()) {
            content.addView(emptyState("No outfits yet."));
            return;
        }

        for (Outfit outfit : outfits) {
            content.addView(outfitCard(outfit));
        }
    }

    private View outfitTabs() {
        LinearLayout tabs = new LinearLayout(this);
        tabs.setOrientation(LinearLayout.HORIZONTAL);
        tabs.setPadding(0, dp(4), 0, dp(10));
        tabs.addView(outfitTab("My Outfits", !outfitInspirationTab, () -> {
            outfitInspirationTab = false;
            renderOutfits();
        }), new LinearLayout.LayoutParams(0, dp(48), 1));
        tabs.addView(outfitTab("Inspiration", outfitInspirationTab, () -> {
            outfitInspirationTab = true;
            renderOutfits();
        }), new LinearLayout.LayoutParams(0, dp(48), 1));
        return tabs;
    }

    private View outfitTab(String label, boolean active, Runnable action) {
        LinearLayout tab = new LinearLayout(this);
        tab.setOrientation(LinearLayout.VERTICAL);
        tab.setGravity(Gravity.CENTER);
        tab.setOnClickListener(v -> action.run());
        TextView text = text(label, 14, active ? ink : muted, true);
        text.setGravity(Gravity.CENTER);
        tab.addView(text, new LinearLayout.LayoutParams(-1, 0, 1));
        TextView indicator = new TextView(this);
        indicator.setBackground(rounded(active ? primary : palette.outlineVariant, 2, Color.TRANSPARENT, 0));
        tab.addView(indicator, new LinearLayout.LayoutParams(-1, dp(active ? 3 : 1)));
        return tab;
    }

    private void renderInspirationView() {
        LinearLayout card = card();
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        card.addView(text(getString(R.string.inspiration_title), 20, ink, true));

        String message;
        if (clothes.isEmpty()) {
            message = getString(R.string.inspiration_empty_clothes);
        } else if (isBlank(openAiApiKey)) {
            message = getString(R.string.inspiration_empty_key);
        } else {
            message = getString(R.string.inspiration_ready);
        }
        TextView body = text(message, 14, muted, false);
        body.setPadding(0, dp(6), 0, dp(8));
        card.addView(body);

        if (!clothes.isEmpty() && !isBlank(openAiApiKey)) {
            Button create = textButton(outfitInspirationLoading
                    ? getString(R.string.creating_outfit_inspiration)
                    : getString(R.string.create_outfit_inspiration));
            create.setGravity(Gravity.CENTER);
            create.setEnabled(!outfitInspirationLoading);
            create.setOnClickListener(v -> createOutfitInspiration());
            card.addView(create, new LinearLayout.LayoutParams(-1, dp(48)));
        } else if (isBlank(openAiApiKey)) {
            Button settings = textButton(getString(R.string.openai_api_key));
            settings.setGravity(Gravity.CENTER);
            settings.setOnClickListener(v -> renderCategories());
            card.addView(settings, new LinearLayout.LayoutParams(-1, dp(48)));
        }

        content.addView(card);
        content.addView(outfitDedupeCard());
    }

    private View outfitDedupeCard() {
        int issueCount = outfitCleanupIssueCount();
        LinearLayout card = card();
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        card.addView(text(getString(R.string.deduplicate_outfits_title), 20, ink, true));

        TextView body = text(getString(R.string.deduplicate_outfits_body), 14, muted, false);
        body.setPadding(0, dp(6), 0, dp(8));
        card.addView(body);

        TextView count = text(issueCount == 0
                ? getString(R.string.deduplicate_outfits_none)
                : getString(R.string.deduplicate_outfits_count, issueCount),
                13,
                issueCount == 0 ? muted : primary,
                true);
        count.setPadding(0, 0, 0, dp(10));
        card.addView(count);

        Button dedupe = textButton(getString(R.string.deduplicate_outfits_action));
        dedupe.setGravity(Gravity.CENTER);
        dedupe.setEnabled(issueCount > 0);
        dedupe.setOnClickListener(v -> reviewOutfitDeduplication());
        card.addView(dedupe, new LinearLayout.LayoutParams(-1, dp(48)));
        return card;
    }

    private int outfitCleanupIssueCount() {
        return findDuplicateOutfits().size() + findSubsetOutfitOverlaps().size();
    }

    private void reviewOutfitDeduplication() {
        List<Outfit> duplicates = findDuplicateOutfits();
        if (!duplicates.isEmpty()) {
            outfits.removeAll(duplicates);
            store.saveOutfits(outfits);
            Toast.makeText(this, getString(R.string.deduplicate_outfits_done, duplicates.size()), Toast.LENGTH_SHORT).show();
        }

        List<OutfitOverlap> overlaps = findSubsetOutfitOverlaps();
        if (overlaps.isEmpty()) {
            if (duplicates.isEmpty()) {
                Toast.makeText(this, getString(R.string.deduplicate_outfits_none), Toast.LENGTH_SHORT).show();
            }
            renderOutfits();
        } else {
            showSubsetOutfitChoice(overlaps.get(0));
        }
    }

    private void showSubsetOutfitChoice(OutfitOverlap overlap) {
        if (!outfits.contains(overlap.smaller) || !outfits.contains(overlap.larger)) {
            reviewOutfitDeduplication();
            return;
        }

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        TextView explanation = text(getString(R.string.subset_outfits_body), 14, muted, false);
        explanation.setPadding(0, 0, 0, dp(10));
        body.addView(explanation);

        Dialog[] holder = new Dialog[1];
        body.addView(subsetOutfitChoiceRow(overlap.smaller, getString(R.string.subset_outfit_smaller), holder));
        body.addView(subsetOutfitChoiceRow(overlap.larger, getString(R.string.subset_outfit_larger), holder));
        holder[0] = showMaterialDialog(getString(R.string.subset_outfits_title), body, getString(R.string.keep_both), null, () -> renderOutfits(), null);
    }

    private View subsetOutfitChoiceRow(Outfit outfit, String label, Dialog[] holder) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.VERTICAL);
        row.setPadding(dp(14), dp(12), dp(14), dp(12));
        row.setBackground(rounded(Color.WHITE, 18, outline, 1));
        LinearLayout.LayoutParams params = blockParams();
        params.setMargins(0, dp(5), 0, dp(5));
        row.setLayoutParams(params);

        TextView eyebrow = text(label, 12, muted, true);
        row.addView(eyebrow);
        TextView name = text(value(outfit.name, "Untitled outfit"), 17, ink, true);
        name.setPadding(0, dp(2), 0, 0);
        row.addView(name);
        TextView items = text(outfitItemNames(outfit), 13, muted, false);
        items.setPadding(0, dp(4), 0, dp(10));
        row.addView(items);

        Button delete = dangerButton(getString(R.string.delete_this_outfit));
        delete.setOnClickListener(v -> {
            if (holder[0] != null) holder[0].dismiss();
            outfits.remove(outfit);
            store.saveOutfits(outfits);
            Toast.makeText(this, getString(R.string.outfit_deleted), Toast.LENGTH_SHORT).show();
            List<OutfitOverlap> remaining = findSubsetOutfitOverlaps();
            if (remaining.isEmpty()) {
                renderOutfits();
            } else {
                showSubsetOutfitChoice(remaining.get(0));
            }
        });
        row.addView(delete, new LinearLayout.LayoutParams(-1, dp(48)));
        return row;
    }

    private List<OutfitOverlap> findSubsetOutfitOverlaps() {
        List<OutfitOverlap> overlaps = new ArrayList<>();
        for (int i = 0; i < outfits.size(); i++) {
            Outfit first = outfits.get(i);
            Set<String> firstIds = outfitIdSet(first);
            if (firstIds.isEmpty()) continue;
            for (int j = i + 1; j < outfits.size(); j++) {
                Outfit second = outfits.get(j);
                Set<String> secondIds = outfitIdSet(second);
                if (secondIds.isEmpty() || firstIds.size() == secondIds.size()) continue;
                if (firstIds.containsAll(secondIds)) {
                    overlaps.add(new OutfitOverlap(second, first));
                } else if (secondIds.containsAll(firstIds)) {
                    overlaps.add(new OutfitOverlap(first, second));
                }
            }
        }
        return overlaps;
    }

    private Set<String> outfitIdSet(Outfit outfit) {
        Set<String> ids = new LinkedHashSet<>();
        if (outfit == null) return ids;
        for (String id : outfit.clothingIds) {
            if (!isBlank(id)) ids.add(id);
        }
        return ids;
    }

    private int duplicateOutfitCount() {
        return findDuplicateOutfits().size();
    }

    private void deduplicateOutfits() {
        List<Outfit> duplicates = findDuplicateOutfits();
        if (duplicates.isEmpty()) {
            Toast.makeText(this, getString(R.string.deduplicate_outfits_none), Toast.LENGTH_SHORT).show();
            renderOutfits();
            return;
        }
        outfits.removeAll(duplicates);
        store.saveOutfits(outfits);
        Toast.makeText(this, getString(R.string.deduplicate_outfits_done, duplicates.size()), Toast.LENGTH_SHORT).show();
        renderOutfits();
    }

    private List<Outfit> findDuplicateOutfits() {
        Map<String, Outfit> newestBySignature = new LinkedHashMap<>();
        List<Outfit> duplicates = new ArrayList<>();
        for (Outfit outfit : outfits) {
            String signature = outfitSignature(outfit);
            if (isBlank(signature)) continue;
            Outfit existing = newestBySignature.get(signature);
            if (existing == null) {
                newestBySignature.put(signature, outfit);
            } else if (outfit.updatedAt > existing.updatedAt) {
                duplicates.add(existing);
                newestBySignature.put(signature, outfit);
            } else {
                duplicates.add(outfit);
            }
        }
        return duplicates;
    }

    private String outfitSignature(Outfit outfit) {
        if (outfit == null || outfit.clothingIds.isEmpty()) return "";
        List<String> ids = new ArrayList<>();
        for (String id : outfit.clothingIds) {
            if (!isBlank(id) && !ids.contains(id)) ids.add(id);
        }
        if (ids.isEmpty()) return "";
        Collections.sort(ids);
        StringBuilder signature = new StringBuilder();
        for (String id : ids) {
            if (signature.length() > 0) signature.append("|");
            signature.append(id);
        }
        return signature.toString();
    }

    private void createOutfitInspiration() {
        if (outfitInspirationLoading) return;
        if (clothes.isEmpty()) {
            Toast.makeText(this, getString(R.string.inspiration_empty_clothes), Toast.LENGTH_LONG).show();
            return;
        }
        if (isBlank(openAiApiKey)) {
            Toast.makeText(this, getString(R.string.openai_api_key_not_set), Toast.LENGTH_LONG).show();
            renderCategories();
            return;
        }

        outfitInspirationLoading = true;
        renderOutfits();
        Toast.makeText(this, getString(R.string.creating_outfit_inspiration), Toast.LENGTH_SHORT).show();
        String apiKey = openAiApiKey;
        String baseUrl = openAiBaseUrl;
        String model = openAiModel;
        ioExecutor.execute(() -> {
            try {
                Outfit suggestion = requestOutfitSuggestion(apiKey, baseUrl, model);
                mainHandler.post(() -> {
                    outfitInspirationLoading = false;
                    outfitEditingTarget = null;
                    outfitDraft = suggestion;
                    outfitFormStep = 3;
                    outfitFilterCategory = "All";
                    renderOutfitFormStep();
                });
            } catch (Exception exception) {
                mainHandler.post(() -> {
                    outfitInspirationLoading = false;
                    renderOutfits();
                    Toast.makeText(this, getString(R.string.inspiration_failed) + ": " + exception.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private Outfit requestOutfitSuggestion(String apiKey, String baseUrl, String model) throws IOException, JSONException {
        HttpURLConnection connection = (HttpURLConnection) new URL(openAiResponsesUrl(baseUrl)).openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(45000);
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("Content-Type", "application/json");

        JSONObject request = new JSONObject();
        request.put("model", normalizedOpenAiModel(model));
        request.put("store", false);
        request.put("max_output_tokens", 700);
        request.put("input", buildOutfitSuggestionPrompt());

        byte[] bytes = request.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream output = connection.getOutputStream()) {
            output.write(bytes);
        }

        int code = connection.getResponseCode();
        InputStream responseStream = code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream();
        String responseText = readStream(responseStream);
        connection.disconnect();

        if (code < 200 || code >= 300) {
            throw new IOException("OpenAI API " + code + " " + extractOpenAiError(responseText));
        }

        JSONObject response = new JSONObject(responseText);
        String outputText = extractOpenAiOutputText(response);
        if (isBlank(outputText)) {
            throw new IOException("empty recommendation");
        }
        return parseOutfitSuggestion(outputText);
    }

    private String buildOutfitSuggestionPrompt() throws JSONException {
        JSONArray wardrobe = new JSONArray();
        for (ClothingItem item : clothes) {
            JSONObject json = new JSONObject();
            json.put("id", item.id);
            json.put("name", value(item.name, item.category));
            json.put("category", value(item.category, "Uncategorized"));
            json.put("group", categoryGroup(item.category));
            json.put("color", value(item.color, ""));
            json.put("season", value(item.season, ""));
            json.put("brand", value(item.brand, ""));
            json.put("material", value(item.material, ""));
            json.put("size", value(joinNonEmpty(item.size, item.waist, item.length), ""));
            json.put("notes", value(item.notes, ""));
            wardrobe.put(json);
        }

        return "Create one practical outfit using only the wardrobe items below. "
                + "Choose 2 to 5 items that work together. Prefer a complete outfit with top, bottom, shoes, and outerwear/accessory when available. "
                + "Consider this wearer profile when useful: eye color=" + value(profileEyeColor, "unknown")
                + ", hair color=" + value(profileHairColor, "unknown") + ". "
                + "Use exact item ids from the wardrobe. Return only valid JSON with this shape: "
                + "{\"name\":\"Short outfit name\",\"occasion\":\"Casual\",\"season\":\"Any season\",\"notes\":\"Why these pieces work together\",\"clothingIds\":[\"id1\",\"id2\"]}. "
                + "Do not include markdown. Wardrobe items: " + wardrobe;
    }

    private String extractOpenAiOutputText(JSONObject response) {
        String direct = response.optString("output_text", "");
        if (!isBlank(direct)) return direct;
        JSONArray output = response.optJSONArray("output");
        if (output == null) return "";
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < output.length(); i++) {
            JSONObject item = output.optJSONObject(i);
            if (item == null) continue;
            JSONArray content = item.optJSONArray("content");
            if (content == null) continue;
            for (int j = 0; j < content.length(); j++) {
                JSONObject part = content.optJSONObject(j);
                if (part == null) continue;
                String text = part.optString("text", "");
                if (!isBlank(text)) {
                    if (builder.length() > 0) builder.append('\n');
                    builder.append(text);
                }
            }
        }
        return builder.toString();
    }

    private String extractOpenAiError(String responseText) {
        if (isBlank(responseText)) return "";
        try {
            JSONObject json = new JSONObject(responseText);
            JSONObject errorJson = json.optJSONObject("error");
            if (errorJson != null) {
                return errorJson.optString("message", responseText);
            }
        } catch (JSONException ignored) {
        }
        return responseText.length() > 180 ? responseText.substring(0, 180) : responseText;
    }

    private Outfit parseOutfitSuggestion(String outputText) throws JSONException, IOException {
        String jsonText = extractJsonObject(outputText);
        JSONObject json = new JSONObject(jsonText);
        Outfit outfit = new Outfit();
        outfit.name = trimLength(json.optString("name", "Suggested outfit"), 60);
        if (isBlank(outfit.name)) outfit.name = "Suggested outfit";
        outfit.occasion = trimLength(json.optString("occasion", ""), 40);
        outfit.season = trimLength(json.optString("season", SEASON_ANY), 60);
        if (isBlank(outfit.season)) outfit.season = SEASON_ANY;
        outfit.notes = trimLength(json.optString("notes", ""), 220);

        JSONArray ids = json.optJSONArray("clothingIds");
        if (ids == null) ids = json.optJSONArray("items");
        if (ids != null) {
            for (int i = 0; i < ids.length(); i++) {
                String id;
                JSONObject object = ids.optJSONObject(i);
                if (object != null) {
                    id = object.optString("id", object.optString("clothingId", ""));
                } else {
                    id = ids.optString(i, "");
                }
                if (!isBlank(id) && findItem(id) != null && !outfit.clothingIds.contains(id)) {
                    outfit.clothingIds.add(id);
                }
            }
        }
        if (outfit.clothingIds.isEmpty()) {
            throw new IOException(getString(R.string.inspiration_no_items));
        }
        outfit.updatedAt = System.currentTimeMillis();
        return outfit;
    }

    private String extractJsonObject(String outputText) throws IOException {
        int start = outputText.indexOf('{');
        int end = outputText.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new IOException("recommendation was not valid JSON");
        }
        return outputText.substring(start, end + 1);
    }

    private String trimLength(String value, int maxLength) {
        if (value == null) return "";
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }

    private View outfitCard(Outfit outfit) {
        LinearLayout card = card();
        card.setPadding(dp(16), dp(14), dp(16), dp(16));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(text(value(outfit.name, "Untitled outfit"), 20, ink, true), new LinearLayout.LayoutParams(0, -2, 1));
        header.addView(transparentIconButton(R.drawable.ic_edit, primary, v -> renderOutfitForm(outfit)), new LinearLayout.LayoutParams(dp(42), dp(42)));
        header.addView(transparentIconButton(R.drawable.ic_delete, error, v -> confirmDeleteOutfit(outfit)), new LinearLayout.LayoutParams(dp(42), dp(42)));
        card.addView(header);

        card.addView(outfitPreview(outfit));

        TextView items = text(outfitItemNames(outfit), 14, ink, false);
        items.setPadding(0, dp(8), 0, 0);
        card.addView(items);

        String metaText = joinNonEmpty(outfit.occasion, outfit.season);
        if (!isBlank(metaText)) {
            TextView detail = text(metaText, 12, muted, false);
            detail.setPadding(0, dp(3), 0, 0);
            card.addView(detail);
        }
        card.addView(text("Updated " + DateFormat.getDateInstance().format(outfit.updatedAt), 12, muted, false));
        return card;
    }

    private String outfitItemNames(Outfit outfit) {
        StringBuilder itemNames = new StringBuilder();
        for (String id : outfit.clothingIds) {
            ClothingItem item = findItem(id);
            if (item != null) {
                if (itemNames.length() > 0) itemNames.append(", ");
                itemNames.append(value(item.name, item.category));
            }
        }
        return itemNames.length() == 0 ? "No items selected" : itemNames.toString();
    }

    private View outfitPreview(Outfit outfit) {
        HorizontalScrollView scroller = new HorizontalScrollView(this);
        scroller.setHorizontalScrollBarEnabled(false);
        scroller.setFillViewport(false);

        LinearLayout preview = new LinearLayout(this);
        preview.setOrientation(LinearLayout.HORIZONTAL);
        preview.setGravity(Gravity.CENTER_VERTICAL);
        preview.setPadding(0, dp(12), 0, dp(4));

        int added = 0;
        for (String id : outfit.clothingIds) {
            ClothingItem item = findItem(id);
            if (item == null) continue;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(82), dp(82));
            params.setMargins(added == 0 ? 0 : dp(6), 0, 0, 0);
            preview.addView(outfitPreviewTile(item), params);
            added++;
        }
        scroller.addView(preview, new HorizontalScrollView.LayoutParams(-2, -2));
        return scroller;
    }

    private View outfitPreviewTile(ClothingItem item) {
        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setPadding(dp(6), dp(6), dp(6), dp(6));
        image.setBackground(rounded(palette.softSurface(), 16, outline, 1));
        image.setClipToOutline(true);
        if (!isBlank(item.imageUri)) {
            loadImage(image, item.imageUri, R.drawable.ic_clothes, muted, 110);
        } else {
            image.setImageResource(R.drawable.ic_clothes);
            image.setColorFilter(muted);
        }
        return image;
    }

    private void renderOutfitForm(Outfit existing) {
        outfitEditingTarget = existing;
        outfitDraft = existing == null ? new Outfit() : copyOutfit(existing);
        outfitDraft.clothingIds.removeIf(id -> findItem(id) == null);
        outfitFormStep = 1;
        outfitFilterCategory = "All";
        renderOutfitFormStep();
    }

    private void renderOutfitFormStep() {
        if (outfitDraft == null) {
            renderOutfits();
            return;
        }
        activeTab = "outfitForm";
        renderTopBar(outfitFormTitle(), true, null, false);
        setStickyBar(outfitStepIndicator(), 18);
        updateBottomNavigation();
        if (outfitFormStep == 1) {
            renderOutfitItemsStep();
            return;
        }

        useScrollContent();
        content.removeAllViews();
        content.setPadding(dp(18), dp(2), dp(18), dp(28));
        if (outfitFormStep == 2) {
            renderOutfitDetailsStep();
        } else {
            renderOutfitReviewStep();
        }
    }

    private String outfitFormTitle() {
        if (outfitFormStep == 2) return "Outfit details";
        if (outfitFormStep == 3) return "Review outfit";
        return outfitEditingTarget == null ? "Create outfit" : "Edit outfit";
    }

    private Outfit copyOutfit(Outfit source) {
        Outfit copy = new Outfit();
        copy.name = source.name;
        copy.occasion = source.occasion;
        copy.season = source.season;
        copy.notes = source.notes;
        copy.updatedAt = source.updatedAt;
        copy.clothingIds.addAll(source.clothingIds);
        return copy;
    }

    private void clearOutfitDraft() {
        outfitDraft = null;
        outfitEditingTarget = null;
        outfitFormStep = 1;
        outfitFilterCategory = "All";
    }

    private View outfitStepIndicator() {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(4), 0, dp(10));
        row.addView(outfitStep("1", "Items", outfitFormStep >= 1, 1), new LinearLayout.LayoutParams(-2, dp(44)));
        row.addView(outfitStepLine(outfitFormStep >= 2), new LinearLayout.LayoutParams(0, dp(1), 1));
        row.addView(outfitStep("2", "Details", outfitFormStep >= 2, 2), new LinearLayout.LayoutParams(-2, dp(44)));
        row.addView(outfitStepLine(outfitFormStep >= 3), new LinearLayout.LayoutParams(0, dp(1), 1));
        row.addView(outfitStep("3", "Review", outfitFormStep >= 3, 3), new LinearLayout.LayoutParams(-2, dp(44)));
        return row;
    }

    private View outfitStep(String number, String label, boolean active, int targetStep) {
        LinearLayout step = new LinearLayout(this);
        step.setGravity(Gravity.CENTER_VERTICAL);
        step.setPadding(dp(2), 0, dp(2), 0);
        if (targetStep < outfitFormStep) {
            step.setOnClickListener(v -> {
                outfitFormStep = targetStep;
                renderOutfitFormStep();
            });
        }
        TextView badge = text(number, 12, active ? onPrimary : muted, true);
        badge.setGravity(Gravity.CENTER);
        badge.setBackground(oval(active ? primary : Color.TRANSPARENT, active ? primary : outline, 1));
        step.addView(badge, new LinearLayout.LayoutParams(dp(24), dp(24)));
        TextView copy = text(label, 12, active ? ink : muted, false);
        copy.setPadding(dp(6), 0, dp(6), 0);
        step.addView(copy);
        return step;
    }

    private View outfitStepLine(boolean active) {
        TextView line = new TextView(this);
        line.setBackgroundColor(active ? primary : palette.outlineVariant);
        return line;
    }

    private void renderOutfitItemsStep() {
        List<ClothingItem> items = filteredOutfitClothes();
        ListView list = new ListView(this);
        list.setDivider(null);
        list.setClipToPadding(false);
        list.setPadding(dp(18), dp(4), dp(18), dp(24));
        list.addHeaderView(outfitItemsHeader(items.isEmpty()), null, false);

        TextView selectedCount = text(outfitSelectedCountText(), 13, primary, false);
        View footer = outfitItemsFooter(selectedCount, () -> {
            if (outfitDraft.clothingIds.isEmpty()) {
                Toast.makeText(this, "Select at least one clothing item", Toast.LENGTH_SHORT).show();
                return;
            }
            outfitFormStep = 2;
            renderOutfitFormStep();
        });
        list.addFooterView(footer, null, false);

        OutfitSelectionAdapter adapter = new OutfitSelectionAdapter(items);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view, position, id) -> {
            int index = position - list.getHeaderViewsCount();
            if (index < 0 || index >= items.size()) return;
            ClothingItem item = items.get(index);
            if (outfitDraft.clothingIds.contains(item.id)) {
                outfitDraft.clothingIds.remove(item.id);
            } else {
                outfitDraft.clothingIds.add(item.id);
            }
            selectedCount.setText(outfitSelectedCountText());
            adapter.notifyDataSetChanged();
        });
        useBodyView(list);
    }

    private List<ClothingItem> filteredOutfitClothes() {
        List<ClothingItem> filtered = new ArrayList<>();
        for (ClothingItem item : clothes) {
            if (outfitMatchesFilter(item)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private View outfitItemsHeader(boolean empty) {
        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.VERTICAL);
        TextView heading = text("Select clothing items", 16, ink, true);
        heading.setPadding(0, 0, 0, dp(8));
        header.addView(heading);
        header.addView(outfitFilterChips());
        if (empty) {
            header.addView(emptyState("No clothing items match this filter."));
        }
        return header;
    }

    private View outfitItemsFooter(TextView selectedCount, Runnable nextAction) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(12), 0, dp(10));
        row.addView(selectedCount, new LinearLayout.LayoutParams(0, -2, 1));
        Button button = primaryButton("Next");
        button.setOnClickListener(v -> nextAction.run());
        row.addView(button, new LinearLayout.LayoutParams(dp(190), dp(50)));
        return row;
    }

    private String outfitSelectedCountText() {
        int count = outfitDraft == null ? 0 : outfitDraft.clothingIds.size();
        return count + (count == 1 ? " item selected" : " items selected");
    }

    private View outfitFilterChips() {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, dp(10));
        List<String> filters = outfitFilterOptions();
        int shown = 0;
        for (String filter : filters) {
            Button chip = chip(filter, filter.equals(outfitFilterCategory));
            chip.setOnClickListener(v -> {
                outfitFilterCategory = filter;
                renderOutfitFormStep();
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, dp(38));
            params.setMargins(0, 0, dp(6), 0);
            row.addView(chip, params);
            shown++;
            if (shown >= 5) break;
        }
        return row;
    }

    private List<String> outfitFilterOptions() {
        List<String> filters = new ArrayList<>();
        filters.add("All");
        for (CategoryOption option : CATEGORY_OPTIONS) {
            if (!filters.contains(option.group)) filters.add(option.group);
        }
        for (ClothingItem item : clothes) {
            String group = categoryGroup(item.category);
            if (!isBlank(group) && !filters.contains(group)) filters.add(group);
        }
        if (!filters.contains(outfitFilterCategory)) filters.add(outfitFilterCategory);
        return filters;
    }

    private boolean outfitMatchesFilter(ClothingItem item) {
        return categoryMatchesFilter(item.category, outfitFilterCategory);
    }

    private String categoryGroup(String category) {
        if (isBlank(category)) return "";
        CategoryOption resolved = categoryOption(category);
        if (resolved != null) {
            return resolved.group;
        }
        for (CategoryOption option : CATEGORY_OPTIONS) {
            if (option.label.equalsIgnoreCase(category) || option.group.equalsIgnoreCase(category)) {
                return option.group;
            }
        }
        return category;
    }

    private void renderOutfitDetailsStep() {
        content.addView(outfitPreview(outfitDraft));
        LinearLayout form = form();
        form.addView(outfitDraftField("Outfit name", outfitDraft.name, false, value -> outfitDraft.name = value));
        form.addView(outfitSelectField(R.drawable.ic_detail_tag, "Occasion", value(outfitDraft.occasion, "Select occasion"), v -> {
            showOccasionPicker(outfitDraft.occasion, value -> outfitDraft.occasion = value, this::renderOutfitFormStep);
        }));
        form.addView(outfitSelectField(R.drawable.ic_detail_season, "Season", value(outfitDraft.season, SEASON_ANY), v -> {
            showSeasonPicker(outfitDraft.season, value -> outfitDraft.season = value, this::renderOutfitFormStep);
        }));
        form.addView(outfitDraftField("Notes", outfitDraft.notes, true, value -> outfitDraft.notes = value));
        content.addView(form);
        content.addView(outfitStepActions("Next", () -> {
            outfitFormStep = 1;
            renderOutfitFormStep();
        }, () -> {
            outfitFormStep = 3;
            renderOutfitFormStep();
        }));
    }

    private View outfitDraftField(String hint, String currentValue, boolean multiline, FieldUpdater updater) {
        EditText input = input(hint);
        input.setText(currentValue);
        input.setSingleLine(!multiline);
        input.setMinLines(multiline ? 3 : 1);
        input.setBackground(rounded(Color.WHITE, 18, palette.outlineVariant, 1));
        input.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void afterTextChanged(Editable editable) {
                updater.update(editable.toString().trim());
            }
        });
        return input;
    }

    private View outfitSelectField(int iconRes, String label, String value, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(8), dp(14), dp(8));
        row.setMinimumHeight(dp(58));
        row.setBackground(rounded(Color.WHITE, 18, palette.outlineVariant, 1));
        row.setOnClickListener(listener);

        ImageView icon = iconView(iconRes);
        icon.setColorFilter(palette.iconMuted);
        icon.setPadding(dp(6), dp(6), dp(6), dp(6));
        row.addView(icon, new LinearLayout.LayoutParams(dp(36), dp(40)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(12), 0, 0, 0);
        copy.addView(text(label, 11, muted, true));
        copy.addView(text(value, 15, ink, false));
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));

        TextView arrow = text("›", 24, palette.arrow, false);
        arrow.setGravity(Gravity.CENTER);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(26), dp(40)));
        row.setLayoutParams(blockParams());
        return row;
    }

    private void renderOutfitReviewStep() {
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout titleBlock = new LinearLayout(this);
        titleBlock.setOrientation(LinearLayout.VERTICAL);
        titleBlock.addView(text(value(outfitDraft.name, "Untitled outfit"), 20, ink, true));
        TextView count = text(outfitDraft.clothingIds.size() + (outfitDraft.clothingIds.size() == 1 ? " item" : " items"), 12, onPrimaryContainer, true);
        count.setPadding(dp(8), dp(3), dp(8), dp(3));
        count.setBackground(rounded(primaryContainer, 12, Color.TRANSPARENT, 0));
        LinearLayout.LayoutParams countParams = new LinearLayout.LayoutParams(-2, -2);
        countParams.setMargins(0, dp(4), 0, 0);
        titleBlock.addView(count, countParams);
        header.addView(titleBlock, new LinearLayout.LayoutParams(0, -2, 1));
        TextView edit = text("Edit", 14, primary, true);
        edit.setGravity(Gravity.CENTER);
        edit.setOnClickListener(v -> {
            outfitFormStep = 2;
            renderOutfitFormStep();
        });
        header.addView(edit, new LinearLayout.LayoutParams(dp(64), dp(48)));
        content.addView(header);

        content.addView(outfitReviewPreview());

        LinearLayout summary = groupedRows();
        summary.addView(reviewRow(R.drawable.ic_detail_category, "Items", outfitItemNames(outfitDraft)));
        summary.addView(reviewRow(R.drawable.ic_detail_tag, "Occasion", value(outfitDraft.occasion, "No occasion")));
        summary.addView(reviewRow(R.drawable.ic_detail_season, "Season", value(outfitDraft.season, "Any season")));
        summary.addView(reviewRow(R.drawable.ic_detail_notes, "Notes", value(outfitDraft.notes, "No notes added")));
        content.addView(summary);

        content.addView(outfitStepActions(outfitEditingTarget == null ? "Create outfit" : "Save outfit", () -> {
            outfitFormStep = 2;
            renderOutfitFormStep();
        }, this::saveOutfitDraft));
    }

    private View outfitReviewPreview() {
        LinearLayout card = card();
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        HorizontalScrollView scroller = new HorizontalScrollView(this);
        scroller.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        int added = 0;
        for (String id : outfitDraft.clothingIds) {
            ClothingItem item = findItem(id);
            if (item == null) continue;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(116), dp(116));
            params.setMargins(added == 0 ? 0 : dp(8), 0, 0, 0);
            row.addView(outfitReviewPreviewTile(item), params);
            added++;
        }
        scroller.addView(row, new HorizontalScrollView.LayoutParams(-2, -2));
        card.addView(scroller);
        return card;
    }

    private View outfitReviewPreviewTile(ClothingItem item) {
        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setPadding(dp(8), dp(8), dp(8), dp(8));
        image.setBackground(rounded(palette.softSurface(), 18, palette.outlineVariant, 1));
        image.setClipToOutline(true);
        if (!isBlank(item.imageUri)) {
            loadImage(image, item.imageUri, categoryDrawable(item.category), muted, 150);
        } else {
            image.setImageResource(categoryDrawable(item.category));
            image.setColorFilter(muted);
        }
        return image;
    }

    private View outfitPreviewEmptySlot() {
        TextView slot = text("+", 22, muted, false);
        slot.setGravity(Gravity.CENTER);
        slot.setBackground(rounded(palette.softSurface(), 18, palette.outlineVariant, 1));
        return slot;
    }

    private View reviewRow(int iconRes, String label, String value) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(9), dp(14), dp(9));
        ImageView icon = iconView(iconRes);
        icon.setColorFilter(palette.iconMuted);
        icon.setPadding(dp(6), dp(6), dp(6), dp(6));
        row.addView(icon, new LinearLayout.LayoutParams(dp(36), dp(40)));
        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(12), 0, 0, 0);
        copy.addView(text(label, 11, muted, true));
        copy.addView(text(value, 14, ink, false));
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        return row;
    }

    private View outfitBottomAction(String detail, String action, Runnable listener) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(12), 0, 0);
        Button button = primaryButton(action);
        button.setOnClickListener(v -> listener.run());
        if (isBlank(detail)) {
            row.addView(button, new LinearLayout.LayoutParams(-1, dp(50)));
        } else {
            TextView label = text(detail, 13, primary, false);
            row.addView(label, new LinearLayout.LayoutParams(0, -2, 1));
            row.addView(button, new LinearLayout.LayoutParams(dp(190), dp(50)));
        }
        return row;
    }

    private View outfitStepActions(String nextLabel, Runnable previousAction, Runnable nextAction) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(12), 0, 0);
        Button previous = outlinedButton("Previous");
        previous.setOnClickListener(v -> previousAction.run());
        LinearLayout.LayoutParams previousParams = new LinearLayout.LayoutParams(0, dp(50), 1);
        previousParams.setMargins(0, 0, dp(8), 0);
        row.addView(previous, previousParams);

        Button next = primaryButton(nextLabel);
        next.setOnClickListener(v -> nextAction.run());
        LinearLayout.LayoutParams nextParams = new LinearLayout.LayoutParams(0, dp(50), 1);
        nextParams.setMargins(dp(8), 0, 0, 0);
        row.addView(next, nextParams);
        return row;
    }

    private void saveOutfitDraft() {
        if (outfitDraft == null) return;
        outfitDraft.updatedAt = System.currentTimeMillis();
        if (outfitEditingTarget == null) {
            outfits.add(0, outfitDraft);
        } else {
            outfitEditingTarget.name = outfitDraft.name;
            outfitEditingTarget.occasion = outfitDraft.occasion;
            outfitEditingTarget.season = outfitDraft.season;
            outfitEditingTarget.notes = outfitDraft.notes;
            outfitEditingTarget.clothingIds.clear();
            outfitEditingTarget.clothingIds.addAll(outfitDraft.clothingIds);
            outfitEditingTarget.updatedAt = outfitDraft.updatedAt;
        }
        store.saveOutfits(outfits);
        clearOutfitDraft();
        renderOutfits();
    }

    private LinearLayout outfitSelectionCard(ClothingItem item, boolean selected) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(10), dp(12), dp(10));

        ImageView thumbnail = new ImageView(this);
        thumbnail.setScaleType(ImageView.ScaleType.FIT_CENTER);
        thumbnail.setBackground(rounded(palette.softSurface(), 14, palette.outlineVariant, 1));
        thumbnail.setClipToOutline(true);
        row.addView(thumbnail, new LinearLayout.LayoutParams(dp(62), dp(62)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(14), 0, 0, 0);
        TextView title = text("", 16, ink, true);
        TextView category = text("", 13, muted, false);
        TextView meta = text("", 12, muted, false);
        copy.addView(title);
        copy.addView(category);
        copy.addView(meta);
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));

        TextView check = text("", 17, onPrimary, true);
        check.setGravity(Gravity.CENTER);
        check.setTag("check");
        row.addView(check, new LinearLayout.LayoutParams(dp(34), dp(34)));
        row.setTag(new OutfitSelectionHolder(thumbnail, title, category, meta));
        bindOutfitSelectionCard(row, item, selected);
        return row;
    }

    private void bindOutfitSelectionCard(LinearLayout row, ClothingItem item, boolean selected) {
        Object holderObject = row.getTag();
        if (holderObject instanceof OutfitSelectionHolder) {
            OutfitSelectionHolder holder = (OutfitSelectionHolder) holderObject;
            holder.title.setText(value(item.name, item.category));
            holder.category.setText(value(item.category, "Uncategorized"));
            holder.meta.setText(joinNonEmpty(item.color, item.season));
            if (!isBlank(item.imageUri)) {
                holder.thumbnail.setPadding(0, 0, 0, 0);
                loadImage(holder.thumbnail, item.imageUri, R.drawable.ic_clothes, muted, 90);
            } else {
                holder.thumbnail.setTag(null);
                holder.thumbnail.setImageResource(R.drawable.ic_clothes);
                holder.thumbnail.setColorFilter(muted);
                holder.thumbnail.setPadding(dp(12), dp(12), dp(12), dp(12));
            }
        }
        updateOutfitSelectionCard(row, selected);
    }

    private void updateOutfitSelectionCard(LinearLayout row, boolean selected) {
        row.setBackground(rounded(selected ? palette.tonalSurface() : Color.WHITE, 18, selected ? primary : palette.outlineVariant, 1));
        TextView check = row.findViewWithTag("check");
        if (check != null) {
            check.setText(selected ? "✓" : "");
            check.setTextColor(selected ? onPrimary : Color.TRANSPARENT);
            check.setBackground(oval(selected ? primary : Color.TRANSPARENT, selected ? primary : outline, 2));
        }
    }

    private void renderCategories() {
        activeTab = "categories";
        renderTopBar(getString(R.string.settings_title), true);
        updateBottomNavigation();
        useScrollContent();
        content.removeAllViews();
        content.setPadding(dp(22), dp(8), dp(22), dp(28));

        content.addView(settingsProfileCard());

        content.addView(settingsSectionLabel(getString(R.string.settings_appearance)));
        LinearLayout appearance = groupedRows();
        appearance.addView(settingsRow(R.drawable.ic_theme, getString(R.string.theme), getString(R.string.system_theme), v -> showNotReady(getString(R.string.theme))));
        appearance.addView(settingsRow(R.drawable.ic_detail_color, getString(R.string.primary_color), primaryColorName(primary), v -> editPrimaryColor()));
        content.addView(appearance);

        content.addView(settingsSectionLabel(getString(R.string.settings_general)));
        LinearLayout general = groupedRows();
        general.addView(settingsRow(R.drawable.ic_detail_price, getString(R.string.currency), currency, v -> editCurrencyField()));
        general.addView(settingsRow(R.drawable.ic_ruler, getString(R.string.units), getString(R.string.metric_units), v -> showNotReady(getString(R.string.units))));
        general.addView(settingsRow(R.drawable.ic_language, getString(R.string.language), getString(R.string.english), v -> showNotReady(getString(R.string.language))));
        content.addView(general);

        content.addView(settingsSectionLabel(getString(R.string.settings_wardrobe)));
        LinearLayout wardrobe = groupedRows();
        wardrobe.addView(settingsRow(R.drawable.ic_export, getString(R.string.export_wardrobe), getString(R.string.export_wardrobe_subtitle), v -> startWardrobeExport()));
        wardrobe.addView(settingsRow(R.drawable.ic_import, getString(R.string.import_wardrobe), getString(R.string.import_wardrobe_subtitle), v -> startWardrobeImport()));
        content.addView(wardrobe);

        content.addView(settingsSectionLabel(getString(R.string.settings_advanced)));
        LinearLayout advanced = groupedRows();
        advanced.addView(settingsRow(R.drawable.ic_folder, getString(R.string.custom_categories), getString(R.string.custom_categories_subtitle), v -> renderCustomCategories()));
        content.addView(advanced);

        content.addView(settingsSectionLabel(getString(R.string.settings_about)));
        LinearLayout about = groupedRows();
        about.addView(settingsRow(R.drawable.ic_info, getString(R.string.about_ai_outfit), getString(R.string.app_version), v -> showAboutDialog()));
        content.addView(about);
    }

    private View settingsProfileCard() {
        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(14), dp(14), dp(14));
        card.setBackground(rounded(Color.WHITE, 24, Color.TRANSPARENT, 0));
        card.setOnClickListener(v -> renderAiProfile());
        LinearLayout.LayoutParams params = blockParams();
        params.setMargins(0, dp(2), 0, dp(18));
        card.setLayoutParams(params);

        TextView avatar = text("A", 18, onPrimaryContainer, true);
        avatar.setGravity(Gravity.CENTER);
        avatar.setBackground(oval(primaryContainer, Color.TRANSPARENT, 0));
        card.addView(avatar, new LinearLayout.LayoutParams(dp(52), dp(52)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(14), 0, 0, 0);
        copy.addView(text(getString(R.string.app_name), 16, ink, true));
        copy.addView(text(getString(R.string.personalize_experience), 12, muted, false));
        card.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));

        TextView arrow = text("›", 24, palette.arrow, false);
        arrow.setGravity(Gravity.CENTER);
        card.addView(arrow, new LinearLayout.LayoutParams(dp(26), dp(40)));
        return card;
    }

    private View settingsSectionLabel(String label) {
        TextView title = text(label, 12, muted, true);
        title.setAllCaps(true);
        title.setPadding(dp(2), dp(10), dp(2), dp(4));
        return title;
    }

    private View settingsRow(int iconRes, String title, String subtitle, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(68));
        row.setPadding(dp(14), dp(8), dp(14), dp(8));
        row.setOnClickListener(listener);

        ImageView icon = iconView(iconRes);
        icon.setColorFilter(ink);
        icon.setPadding(dp(6), dp(6), dp(6), dp(6));
        row.addView(icon, new LinearLayout.LayoutParams(dp(40), dp(44)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(14), 0, 0, 0);
        copy.addView(text(title, 15, ink, true));
        copy.addView(text(subtitle, 12, muted, false));
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));

        TextView arrow = text("›", 24, palette.arrow, false);
        arrow.setGravity(Gravity.CENTER);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(26), dp(40)));
        return row;
    }

    private void renderAiProfile() {
        activeTab = "aiProfile";
        renderTopBar(getString(R.string.app_name), true, null, false);
        updateBottomNavigation();
        useScrollContent();
        content.removeAllViews();
        content.setPadding(dp(22), dp(8), dp(22), dp(28));

        content.addView(settingsSectionLabel(getString(R.string.profile_photos)));
        LinearLayout photos = groupedRows();
        photos.addView(profileImageRow(R.drawable.ic_clothes, getString(R.string.full_body_photo), profileBodyImageUri, v -> pickProfileImage(PICK_PROFILE_BODY_IMAGE_REQUEST)));
        photos.addView(profileImageRow(R.drawable.ic_camera, getString(R.string.face_photo), profileFaceImageUri, v -> pickProfileImage(PICK_PROFILE_FACE_IMAGE_REQUEST)));
        content.addView(photos);

        content.addView(settingsSectionLabel(getString(R.string.profile_appearance)));
        LinearLayout appearance = groupedRows();
        appearance.addView(settingsRow(R.drawable.ic_detail_color, getString(R.string.eye_color), value(profileEyeColor, getString(R.string.add_eye_color)), v -> editProfileTextField(getString(R.string.eye_color), profileEyeColor, value -> {
            profileEyeColor = value;
            store.saveProfileEyeColor(profileEyeColor);
        })));
        appearance.addView(settingsRow(R.drawable.ic_detail_color, getString(R.string.hair_color), value(profileHairColor, getString(R.string.add_hair_color)), v -> editProfileTextField(getString(R.string.hair_color), profileHairColor, value -> {
            profileHairColor = value;
            store.saveProfileHairColor(profileHairColor);
        })));
        content.addView(appearance);

        content.addView(settingsSectionLabel(getString(R.string.openai_settings)));
        LinearLayout openAi = groupedRows();
        openAi.addView(settingsRow(R.drawable.ic_openai, getString(R.string.openai_api_key),
                isBlank(openAiApiKey) ? getString(R.string.openai_api_key_not_set) : getString(R.string.openai_api_key_set), v -> editOpenAiApiKey()));
        openAi.addView(settingsRow(R.drawable.ic_openai, getString(R.string.openai_base_url), openAiBaseUrl, v -> editOpenAiBaseUrl()));
        openAi.addView(settingsRow(R.drawable.ic_openai, getString(R.string.openai_model), openAiModel, v -> editOpenAiModel()));
        content.addView(openAi);
    }

    private View profileImageRow(int iconRes, String title, String uri, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(76));
        row.setPadding(dp(14), dp(8), dp(14), dp(8));
        row.setOnClickListener(listener);

        ImageView preview = new ImageView(this);
        preview.setScaleType(ImageView.ScaleType.CENTER_CROP);
        preview.setPadding(dp(8), dp(8), dp(8), dp(8));
        preview.setBackground(rounded(palette.softSurface(), 14, palette.outlineVariant, 1));
        preview.setClipToOutline(true);
        if (isBlank(uri)) {
            preview.setImageResource(iconRes);
            preview.setColorFilter(muted);
        } else {
            loadImage(preview, uri, iconRes, muted, 84);
        }
        row.addView(preview, new LinearLayout.LayoutParams(dp(52), dp(52)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(14), 0, 0, 0);
        copy.addView(text(title, 15, ink, true));
        copy.addView(text(isBlank(uri) ? getString(R.string.add_photo) : getString(R.string.photo_set), 12, muted, false));
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));

        TextView arrow = text("›", 24, palette.arrow, false);
        arrow.setGravity(Gravity.CENTER);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(26), dp(40)));
        return row;
    }

    private void pickProfileImage(int requestCode) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        startActivityForResult(intent, requestCode);
    }

    private void editProfileTextField(String title, String currentValue, FieldUpdater updater) {
        EditText input = input(title);
        input.setSingleLine(true);
        input.setText(currentValue);
        showMaterialDialog(title, input, "Cancel", "Save", null, () -> {
            updater.update(input.getText().toString().trim());
            renderAiProfile();
        });
    }

    private void renderCustomCategories() {
        activeTab = "customCategories";
        renderTopBar(getString(R.string.custom_categories), true, null, false);
        topBar.addView(iconButton(R.drawable.ic_add, v -> showCustomCategoryEditor(null)), new LinearLayout.LayoutParams(dp(48), dp(48)));
        updateBottomNavigation();
        useScrollContent();
        content.removeAllViews();
        content.setPadding(dp(22), dp(8), dp(22), dp(28));

        content.addView(customCategoriesHero());

        if (categories.isEmpty()) {
            content.addView(emptyState(getString(R.string.no_custom_categories)));
        } else {
            for (String category : new ArrayList<>(categories)) {
                content.addView(customCategoryRow(category));
            }
        }

        Button add = outlinedButton("+ " + getString(R.string.add_category));
        add.setOnClickListener(v -> showCustomCategoryEditor(null));
        content.addView(add, blockParams());

        TextView hint = text(getString(R.string.drag_reorder_categories), 13, muted, false);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(dp(14), dp(12), dp(14), dp(12));
        hint.setBackground(rounded(Color.WHITE, 14, palette.outlineVariant, 1));
        content.addView(hint, blockParams());
    }

    private View customCategoriesHero() {
        LinearLayout hero = new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER_HORIZONTAL);
        hero.setPadding(0, dp(10), 0, dp(22));

        ImageView image = iconView(R.drawable.ic_folder);
        image.setColorFilter(primary);
        image.setPadding(dp(24), dp(24), dp(24), dp(24));
        image.setBackground(oval(primaryContainer, Color.TRANSPARENT, 0));
        hero.addView(image, new LinearLayout.LayoutParams(dp(112), dp(112)));

        TextView title = text(getString(R.string.make_it_your_way), 22, ink, true);
        title.setGravity(Gravity.CENTER);
        title.setPadding(0, dp(14), 0, dp(6));
        hero.addView(title);

        TextView body = text(getString(R.string.custom_categories_description), 14, muted, false);
        body.setGravity(Gravity.CENTER);
        hero.addView(body);
        return hero;
    }

    private View customCategoryRow(String category) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(14), dp(10), dp(10), dp(10));
        row.setMinimumHeight(dp(58));
        row.setBackground(rounded(Color.WHITE, 16, palette.outlineVariant, 1));
        LinearLayout.LayoutParams params = blockParams();
        params.setMargins(0, dp(5), 0, dp(5));
        row.setLayoutParams(params);

        TextView drag = text("⋮⋮", 17, muted, true);
        drag.setGravity(Gravity.CENTER);
        row.addView(drag, new LinearLayout.LayoutParams(dp(32), dp(40)));

        row.addView(text(category, 15, ink, true), new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(transparentIconButton(R.drawable.ic_edit, ink, v -> showCustomCategoryEditor(category)), new LinearLayout.LayoutParams(dp(44), dp(44)));
        row.addView(transparentIconButton(R.drawable.ic_delete, error, v -> confirmDeleteCategory(category)), new LinearLayout.LayoutParams(dp(44), dp(44)));
        return row;
    }

    private void showCustomCategoryEditor(String existingCategory) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(20), dp(14), dp(20), dp(20));
        sheet.setBackground(roundedTop(surfaceDialog, 32, Color.TRANSPARENT, 0));

        TextView handle = new TextView(this);
        handle.setBackground(rounded(outline, 3, Color.TRANSPARENT, 0));
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(dp(44), dp(5));
        handleParams.gravity = Gravity.CENTER_HORIZONTAL;
        handleParams.setMargins(0, 0, 0, dp(18));
        sheet.addView(handle, handleParams);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(text(isBlank(existingCategory) ? getString(R.string.add_category) : getString(R.string.edit_category), 22, ink, true), new LinearLayout.LayoutParams(0, -2, 1));
        TextView save = text("Save", 14, primary, true);
        save.setGravity(Gravity.CENTER);
        header.addView(save, new LinearLayout.LayoutParams(dp(72), dp(48)));
        sheet.addView(header);

        TextView label = text(getString(R.string.category_name), 12, muted, false);
        label.setPadding(0, dp(16), 0, dp(6));
        sheet.addView(label);

        EditText input = input(getString(R.string.category_name_hint));
        input.setText(existingCategory == null ? "" : existingCategory);
        input.setSingleLine(true);
        sheet.addView(input, new LinearLayout.LayoutParams(-1, dp(56)));

        save.setOnClickListener(v -> {
            String value = input.getText().toString().trim();
            if (value.isEmpty()) {
                Toast.makeText(this, getString(R.string.category_name_required), Toast.LENGTH_SHORT).show();
                return;
            }
            if (existingCategory == null && categories.contains(value)) {
                Toast.makeText(this, getString(R.string.category_exists), Toast.LENGTH_SHORT).show();
                return;
            }
            if (existingCategory == null) {
                categories.add(value);
            } else {
                int index = categories.indexOf(existingCategory);
                if (index >= 0) categories.set(index, value);
            }
            store.saveCategories(categories);
            dialog.dismiss();
            renderCustomCategories();
        });

        dialog.setContentView(sheet);
        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        dialog.show();
        Window shownWindow = dialog.getWindow();
        if (shownWindow != null) {
            shownWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            shownWindow.setGravity(Gravity.BOTTOM);
            shownWindow.setLayout(-1, -2);
        }
    }

    private void showNotReady(String feature) {
        Toast.makeText(this, feature + " " + getString(R.string.coming_later), Toast.LENGTH_SHORT).show();
    }

    private void showAboutDialog() {
        showMaterialDialog(getString(R.string.about_ai_outfit), text(getString(R.string.app_version), 15, muted, false), "Close", null, null, null);
    }

    private void editCurrencyField() {
        EditText input = input("Currency symbol");
        input.setText(currency);
        showMaterialDialog(getString(R.string.currency), input, "Cancel", "Save", null, () -> {
            String value = input.getText().toString().trim();
            currency = value.isEmpty() ? "$" : value;
            store.saveCurrency(currency);
            renderCategories();
        });
    }

    private void editOpenAiApiKey() {
        EditText input = input(getString(R.string.openai_api_key_hint));
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setText(openAiApiKey);
        input.setSelection(input.getText().length());
        boolean fromProfile = activeTab.equals("aiProfile");
        showMaterialDialog(getString(R.string.openai_api_key), input, "Cancel", "Save", null, () -> {
            openAiApiKey = input.getText().toString().trim();
            store.saveOpenAiApiKey(openAiApiKey);
            Toast.makeText(this, isBlank(openAiApiKey) ? getString(R.string.openai_key_cleared) : getString(R.string.openai_key_saved), Toast.LENGTH_SHORT).show();
            if (fromProfile) renderAiProfile(); else renderCategories();
        });
    }

    private void editOpenAiBaseUrl() {
        EditText input = input(getString(R.string.openai_base_url_hint));
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setText(openAiBaseUrl);
        input.setSelection(input.getText().length());
        boolean fromProfile = activeTab.equals("aiProfile");
        showMaterialDialog(getString(R.string.openai_base_url), input, "Cancel", "Save", null, () -> {
            openAiBaseUrl = normalizedOpenAiBaseUrl(input.getText().toString());
            store.saveOpenAiBaseUrl(openAiBaseUrl);
            Toast.makeText(this, getString(R.string.openai_base_url_saved), Toast.LENGTH_SHORT).show();
            if (fromProfile) renderAiProfile(); else renderCategories();
        });
    }

    private void editOpenAiModel() {
        EditText input = input(getString(R.string.openai_model_hint));
        input.setSingleLine(true);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        input.setText(openAiModel);
        input.setSelection(input.getText().length());
        boolean fromProfile = activeTab.equals("aiProfile");
        showMaterialDialog(getString(R.string.openai_model), input, "Cancel", "Save", null, () -> {
            openAiModel = normalizedOpenAiModel(input.getText().toString());
            store.saveOpenAiModel(openAiModel);
            Toast.makeText(this, getString(R.string.openai_model_saved), Toast.LENGTH_SHORT).show();
            if (fromProfile) renderAiProfile(); else renderCategories();
        });
    }

    private void editPrimaryColor() {
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        Dialog[] holder = new Dialog[1];
        body.addView(colorChoice(getString(R.string.forest), 0xFF2A584B, holder));
        body.addView(colorChoice(getString(R.string.ink), 0xFF1A1C1B, holder));
        body.addView(colorChoice(getString(R.string.blue), 0xFF2457A6, holder));
        body.addView(colorChoice(getString(R.string.plum), 0xFF6D3F6D, holder));
        holder[0] = showMaterialDialog(getString(R.string.primary_color), body, null, null, null, null);
    }

    private View colorChoice(String label, int color, Dialog[] holder) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));
        row.setOnClickListener(v -> {
            if (holder[0] != null) holder[0].dismiss();
            store.savePrimaryColor(color);
            applyPalette(color);
            getWindow().setStatusBarColor(surface);
            getWindow().setNavigationBarColor(surface);
            setContentView(buildRoot());
            renderCategories();
        });
        TextView swatch = new TextView(this);
        swatch.setBackground(rounded(color, 14, Color.TRANSPARENT, 0));
        row.addView(swatch, new LinearLayout.LayoutParams(dp(28), dp(28)));
        TextView text = text(label, 16, ink, false);
        text.setPadding(dp(14), 0, 0, 0);
        row.addView(text, new LinearLayout.LayoutParams(0, -2, 1));
        return row;
    }

    private String primaryColorName(int color) {
        if (color == 0xFF2A584B) return getString(R.string.forest);
        if (color == 0xFF1A1C1B) return getString(R.string.ink);
        if (color == 0xFF2457A6) return getString(R.string.blue);
        if (color == 0xFF6D3F6D) return getString(R.string.plum);
        return getString(R.string.custom_color);
    }

    private void confirmDeleteItem(ClothingItem item) {
        showMaterialDialog("Delete clothing item?", text("This also removes it from outfits.", 15, muted, false), "Cancel", "Delete", null, () -> {
            clothes.remove(item);
            rebuildItemIndex();
            for (Outfit outfit : outfits) outfit.clothingIds.remove(item.id);
            store.saveClothes(clothes);
            store.saveOutfits(outfits);
            renderClothes();
        });
    }

    private void confirmDeleteOutfit(Outfit outfit) {
        showMaterialDialog("Delete outfit?", text("This outfit will be removed.", 15, muted, false), "Cancel", "Delete", null, () -> {
            outfits.remove(outfit);
            store.saveOutfits(outfits);
            renderOutfits();
        });
    }

    private void confirmDeleteCategory(String category) {
        showMaterialDialog("Delete category?", text("Existing clothing items keep their category text.", 15, muted, false), "Cancel", "Delete", null, () -> {
            categories.remove(category);
            store.saveCategories(categories);
            renderCustomCategories();
        });
    }

    private void startWardrobeExport() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/zip");
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        intent.putExtra(Intent.EXTRA_TITLE, "ai-outfit-backup-" + date + ".aioutfitbackup");
        startActivityForResult(intent, EXPORT_BACKUP_REQUEST);
    }

    private void startWardrobeImport() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(intent, IMPORT_BACKUP_REQUEST);
    }

    private void confirmImportBackup(Uri uri) {
        showMaterialDialog("Import wardrobe?", text("This replaces your current wardrobe with the selected backup.", 15, muted, false), "Cancel", "Import", null, () -> importWardrobeBackup(uri));
    }

    private void exportWardrobeBackup(Uri destination) {
        Toast.makeText(this, "Exporting wardrobe…", Toast.LENGTH_SHORT).show();
        ioExecutor.execute(() -> {
            int skippedImages = 0;
            try (OutputStream output = getContentResolver().openOutputStream(destination);
                 ZipOutputStream zip = new ZipOutputStream(output)) {
                JSONObject backup = new JSONObject();
                backup.put("schemaVersion", BACKUP_SCHEMA_VERSION);
                backup.put("exportedAt", System.currentTimeMillis());
                backup.put("currency", currency);
                backup.put("primaryColor", primary);

                JSONArray categoryArray = new JSONArray();
                for (String category : categories) {
                    categoryArray.put(category);
                }
                backup.put("categories", categoryArray);

                JSONArray clothesArray = new JSONArray();
                for (ClothingItem item : clothes) {
                    JSONObject itemJson = item.toJson();
                    JSONArray images = new JSONArray();
                    List<String> uris = photoUris(item);
                    for (int i = 0; i < uris.size(); i++) {
                        String sourceUri = uris.get(i);
                        String entryName = "images/" + item.id + "-" + i + extensionForUri(sourceUri);
                        if (writeUriToZip(sourceUri, zip, entryName)) {
                            images.put(entryName);
                        } else {
                            skippedImages++;
                        }
                    }
                    itemJson.put("imageUris", images);
                    itemJson.put("imageUri", images.length() > 0 ? images.optString(0) : "");
                    clothesArray.put(itemJson);
                }
                backup.put("clothes", clothesArray);

                JSONArray outfitsArray = new JSONArray();
                for (Outfit outfit : outfits) {
                    outfitsArray.put(outfit.toJson());
                }
                backup.put("outfits", outfitsArray);

                zip.putNextEntry(new ZipEntry("wardrobe.json"));
                byte[] metadata = backup.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8);
                zip.write(metadata);
                zip.closeEntry();
                int skipped = skippedImages;
                mainHandler.post(() -> Toast.makeText(this, skipped == 0 ? "Wardrobe exported" : "Wardrobe exported. Skipped " + skipped + " image(s).", Toast.LENGTH_LONG).show());
            } catch (Exception exception) {
                mainHandler.post(() -> Toast.makeText(this, "Export failed: " + exception.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private boolean writeUriToZip(String sourceUri, ZipOutputStream zip, String entryName) {
        try (InputStream input = getContentResolver().openInputStream(Uri.parse(sourceUri))) {
            if (input == null) return false;
            zip.putNextEntry(new ZipEntry(entryName));
            copy(input, zip);
            zip.closeEntry();
            return true;
        } catch (Exception ignored) {
            try {
                zip.closeEntry();
            } catch (IOException closeIgnored) {
            }
            return false;
        }
    }

    private void importWardrobeBackup(Uri source) {
        Toast.makeText(this, "Importing wardrobe…", Toast.LENGTH_SHORT).show();
        ioExecutor.execute(() -> {
            String backupId = String.valueOf(System.currentTimeMillis());
            File importDirectory = new File(getFilesDir(), "imported-clothes/" + backupId);
            if (!importDirectory.exists() && !importDirectory.mkdirs()) {
                mainHandler.post(() -> Toast.makeText(this, "Import failed: could not create image directory", Toast.LENGTH_LONG).show());
                return;
            }

            String metadataJson = null;
            Map<String, String> imageMap = new LinkedHashMap<>();
            try (InputStream input = getContentResolver().openInputStream(source);
                 ZipInputStream zip = new ZipInputStream(input)) {
                ZipEntry entry;
                while ((entry = zip.getNextEntry()) != null) {
                    String name = entry.getName();
                    if (entry.isDirectory() || !isSafeZipEntry(name)) {
                        zip.closeEntry();
                        continue;
                    }
                    if ("wardrobe.json".equals(name)) {
                        metadataJson = readZipEntry(zip);
                    } else if (name.startsWith("images/")) {
                        File output = new File(importDirectory, new File(name).getName());
                        try (FileOutputStream fileOutput = new FileOutputStream(output)) {
                            copy(zip, fileOutput);
                        }
                        imageMap.put(name, Uri.fromFile(output).toString());
                    }
                    zip.closeEntry();
                }
            } catch (Exception exception) {
                mainHandler.post(() -> Toast.makeText(this, "Import failed: " + exception.getMessage(), Toast.LENGTH_LONG).show());
                return;
            }

            if (isBlank(metadataJson)) {
                mainHandler.post(() -> Toast.makeText(this, "Import failed: backup metadata is missing", Toast.LENGTH_LONG).show());
                return;
            }

            try {
                JSONObject backup = new JSONObject(metadataJson);
                if (backup.optInt("schemaVersion", -1) != BACKUP_SCHEMA_VERSION) {
                    mainHandler.post(() -> Toast.makeText(this, "Import failed: unsupported backup version", Toast.LENGTH_LONG).show());
                    return;
                }
                mainHandler.post(() -> {
                    try {
                        applyImportedWardrobe(backup, imageMap);
                        Toast.makeText(this, "Wardrobe imported", Toast.LENGTH_SHORT).show();
                    } catch (Exception exception) {
                        Toast.makeText(this, "Import failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception exception) {
                mainHandler.post(() -> Toast.makeText(this, "Import failed: " + exception.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void applyImportedWardrobe(JSONObject backup, Map<String, String> imageMap) throws JSONException {
        List<ClothingItem> importedClothes = new ArrayList<>();
        JSONArray clothingArray = backup.optJSONArray("clothes");
        if (clothingArray != null) {
            for (int i = 0; i < clothingArray.length(); i++) {
                JSONObject itemJson = clothingArray.getJSONObject(i);
                JSONArray rewrittenImages = new JSONArray();
                JSONArray images = itemJson.optJSONArray("imageUris");
                if (images != null) {
                    for (int imageIndex = 0; imageIndex < images.length(); imageIndex++) {
                        String relative = images.optString(imageIndex);
                        String localUri = imageMap.get(relative);
                        if (!isBlank(localUri)) {
                            rewrittenImages.put(localUri);
                        }
                    }
                }
                itemJson.put("imageUris", rewrittenImages);
                itemJson.put("imageUri", rewrittenImages.length() > 0 ? rewrittenImages.optString(0) : "");
                importedClothes.add(ClothingItem.fromJson(itemJson));
            }
        }

        List<Outfit> importedOutfits = new ArrayList<>();
        JSONArray outfitArray = backup.optJSONArray("outfits");
        if (outfitArray != null) {
            for (int i = 0; i < outfitArray.length(); i++) {
                importedOutfits.add(Outfit.fromJson(outfitArray.getJSONObject(i)));
            }
        }

        List<String> importedCategories = new ArrayList<>();
        JSONArray categoryArray = backup.optJSONArray("categories");
        if (categoryArray != null) {
            for (int i = 0; i < categoryArray.length(); i++) {
                String category = categoryArray.optString(i).trim();
                if (!category.isEmpty() && !importedCategories.contains(category)) {
                    importedCategories.add(category);
                }
            }
        }

        clothes.clear();
        clothes.addAll(importedClothes);
        rebuildItemIndex();
        outfits.clear();
        outfits.addAll(importedOutfits);
        categories.clear();
        categories.addAll(importedCategories);
        currency = backup.optString("currency", "$");
        int importedPrimary = backup.optInt("primaryColor", primary);

        store.saveClothes(clothes);
        store.saveOutfits(outfits);
        store.saveCategories(categories);
        store.saveCurrency(currency);
        store.savePrimaryColor(importedPrimary);
        applyPalette(importedPrimary);
        getWindow().setStatusBarColor(surface);
        getWindow().setNavigationBarColor(surface);
        setContentView(buildRoot());
        renderClothes();
    }

    private String readZipEntry(InputStream input) throws IOException {
        java.io.ByteArrayOutputStream output = new java.io.ByteArrayOutputStream();
        copy(input, output);
        return output.toString("UTF-8");
    }

    private void copy(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
    }

    private boolean isSafeZipEntry(String name) {
        return !isBlank(name) && !name.contains("..") && !name.startsWith("/") && !name.startsWith("\\");
    }

    private String extensionForUri(String uri) {
        if (uri == null) return ".img";
        String lower = uri.toLowerCase(Locale.US);
        if (lower.contains(".jpg") || lower.contains(".jpeg")) return ".jpg";
        if (lower.contains(".webp")) return ".webp";
        if (lower.contains(".gif")) return ".gif";
        return ".png";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EXPORT_BACKUP_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            exportWardrobeBackup(data.getData());
            return;
        }
        if (requestCode == IMPORT_BACKUP_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            confirmImportBackup(data.getData());
            return;
        }
        if ((requestCode == PICK_PROFILE_BODY_IMAGE_REQUEST || requestCode == PICK_PROFILE_FACE_IMAGE_REQUEST)
                && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException ignored) {
            }
            String uriText = uri.toString();
            if (requestCode == PICK_PROFILE_BODY_IMAGE_REQUEST) {
                profileBodyImageUri = uriText;
                store.saveProfileBodyImage(profileBodyImageUri);
            } else {
                profileFaceImageUri = uriText;
                store.saveProfileFaceImage(profileFaceImageUri);
            }
            renderAiProfile();
            return;
        }
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null && imageTarget != null) {
            Uri uri = data.getData();
            try {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } catch (SecurityException ignored) {
            }
            String uriText = uri.toString();
            if (appendPickedPhoto) {
                if (!imageTarget.imageUris.contains(uriText)) {
                    imageTarget.imageUris.add(uriText);
                }
                activePhotoIndex = imageTarget.imageUris.size() - 1;
                if (isBlank(imageTarget.imageUri)) {
                    imageTarget.imageUri = uriText;
                }
            } else {
                imageTarget.imageUri = uriText;
                if (imageTarget.imageUris.isEmpty()) {
                    imageTarget.imageUris.add(uriText);
                } else {
                    imageTarget.imageUris.set(0, uriText);
                }
                activePhotoIndex = 0;
            }
            imageTarget.updatedAt = System.currentTimeMillis();
            boolean targetIsDraft = activeTab.equals("clothingForm") && !clothes.contains(imageTarget);
            if (!targetIsDraft) {
                store.saveClothes(clothes);
            }
            if (imageTargetView != null) {
                loadImage(imageTargetView, uri.toString(), android.R.drawable.ic_menu_gallery, palette.placeholder, 360);
            }
            if (targetIsDraft) {
                renderClothingForm(imageTarget, true);
            } else if (activeTab.equals("clothingForm")) {
                renderClothingForm(imageTarget, false);
            } else if (appendPickedPhoto || imageTargetView == null) {
                showClothingDetail(imageTarget);
            }
            appendPickedPhoto = false;
            imageTarget = null;
            imageTargetView = null;
            Toast.makeText(this, "Photo attached", Toast.LENGTH_SHORT).show();
        }
    }

    private EditText field(LinearLayout form, String hint, String value) {
        EditText input = input(hint);
        input.setText(value);
        form.addView(input);
        return input;
    }

    private EditText input(String hint) {
        EditText input = new EditText(this);
        input.setHint(hint);
        input.setSingleLine(false);
        input.setTextSize(15);
        input.setMinHeight(dp(56));
        input.setPadding(dp(16), dp(10), dp(16), dp(10));
        input.setTextColor(ink);
        input.setHintTextColor(muted);
        input.setBackground(rounded(Color.WHITE, 16, outline, 1));
        input.setLayoutParams(blockParams());
        return input;
    }

    private LinearLayout card() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.setBackground(rounded(Color.WHITE, 24, Color.TRANSPARENT, 0));
        card.setElevation(dp(1));
        LinearLayout.LayoutParams params = blockParams();
        params.setMargins(0, dp(7), 0, dp(7));
        card.setLayoutParams(params);
        return card;
    }

    private LinearLayout form() {
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(2), dp(8), dp(2), dp(8));
        return form;
    }

    private ScrollView wrapScrollable(View view) {
        ScrollView scrollView = new ScrollView(this);
        scrollView.addView(view);
        return scrollView;
    }

    private View sectionTitle(String title, String detail) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(2), dp(8), dp(2), dp(12));
        row.addView(text(title, 24, ink, true), new LinearLayout.LayoutParams(0, -2, 1));
        TextView count = text(detail, 13, onPrimaryContainer, true);
        count.setGravity(Gravity.CENTER);
        count.setPadding(dp(12), dp(6), dp(12), dp(6));
        count.setBackground(rounded(primaryContainer, 18, Color.TRANSPARENT, 0));
        row.addView(count);
        return row;
    }

    private TextView emptyState(String message) {
        TextView view = text(message, 15, muted, false);
        view.setGravity(Gravity.CENTER);
        view.setPadding(dp(20), dp(36), dp(20), dp(36));
        view.setBackground(rounded(surfaceContainer, 24, Color.TRANSPARENT, 0));
        return view;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView text = new TextView(this);
        text.setText(value == null ? "" : value);
        text.setTextSize(sp);
        text.setTextColor(color);
        text.setLineSpacing(dp(2), 1.0f);
        if (bold) text.setTypeface(text.getTypeface(), Typeface.BOLD);
        return text;
    }

    private Button primaryButton(String label) {
        Button button = baseButton(label);
        button.setTextColor(onPrimary);
        button.setTypeface(button.getTypeface(), Typeface.BOLD);
        button.setBackground(rounded(primary, 28, Color.TRANSPARENT, 0));
        return button;
    }

    private Button filledButton(String label) {
        Button button = baseButton(label);
        button.setTextColor(onPrimaryContainer);
        button.setTypeface(button.getTypeface(), Typeface.BOLD);
        button.setBackground(rounded(primaryContainer, 20, Color.TRANSPARENT, 0));
        return button;
    }

    private Button textButton(String label) {
        Button button = baseButton(label);
        button.setTextColor(primary);
        button.setBackgroundColor(Color.TRANSPARENT);
        return button;
    }

    private Button smallButton(String label) {
        Button button = baseButton(label);
        button.setTextColor(primary);
        button.setBackground(rounded(Color.TRANSPARENT, 22, outline, 1));
        return button;
    }

    private Button outlinedButton(String label) {
        Button button = baseButton(label);
        button.setTextColor(primary);
        button.setTypeface(button.getTypeface(), Typeface.BOLD);
        button.setBackground(rounded(Color.TRANSPARENT, 24, outline, 1));
        button.setElevation(0);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            button.setStateListAnimator(null);
        }
        return button;
    }

    private Button tonalButton(String label) {
        Button button = baseButton(label);
        button.setTextColor(onPrimaryContainer);
        button.setBackground(rounded(primaryContainer, 22, Color.TRANSPARENT, 0));
        return button;
    }

    private Button dangerButton(String label) {
        Button button = baseButton(label);
        button.setTextColor(error);
        button.setBackground(rounded(Color.TRANSPARENT, 22, palette.dangerOutline, 1));
        return button;
    }

    private Button baseButton(String label) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setMinHeight(dp(48));
        button.setMinWidth(dp(72));
        button.setPadding(dp(14), 0, dp(14), 0);
        return button;
    }

    private LinearLayout actionRow() {
        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.RIGHT);
        actions.setPadding(0, dp(12), 0, 0);
        return actions;
    }

    private LinearLayout.LayoutParams blockParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(6), 0, dp(6));
        return params;
    }

    private LinearLayout.LayoutParams weightParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -2, 1);
        params.setMargins(dp(4), 0, dp(4), 0);
        return params;
    }

    private GradientDrawable rounded(int color, int radiusDp, int strokeColor, int strokeDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radiusDp));
        if (strokeDp > 0) {
            drawable.setStroke(dp(strokeDp), strokeColor);
        }
        return drawable;
    }

    private GradientDrawable roundedTop(int color, int radiusDp, int strokeColor, int strokeDp) {
        GradientDrawable drawable = new GradientDrawable();
        float radius = dp(radiusDp);
        drawable.setColor(color);
        drawable.setCornerRadii(new float[]{
                radius, radius,
                radius, radius,
                0, 0,
                0, 0
        });
        if (strokeDp > 0) {
            drawable.setStroke(dp(strokeDp), strokeColor);
        }
        return drawable;
    }

    private GradientDrawable oval(int color, int strokeColor, int strokeDp) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(color);
        if (strokeDp > 0) {
            drawable.setStroke(dp(strokeDp), strokeColor);
        }
        return drawable;
    }

    private String readStream(InputStream stream) throws IOException {
        if (stream == null) return "";
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        }
        return builder.toString();
    }

    private ClothingItem findItem(String id) {
        return itemById.get(id);
    }

    private String joinNonEmpty(String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (!isBlank(value)) {
                if (builder.length() > 0) builder.append(" / ");
                builder.append(value);
            }
        }
        return builder.toString();
    }

    private String value(String text, String fallback) {
        return isBlank(text) ? fallback : text;
    }

    private String normalizedLink(String raw) {
        if (raw == null) return "";
        String value = raw.trim();
        if (value.isEmpty()) return "";
        if (value.contains("://")) return value;
        return "https://" + value;
    }

    private String normalizedOpenAiBaseUrl(String raw) {
        String value = raw == null ? "" : raw.trim();
        if (value.isEmpty()) value = "https://eu.api.openai.com/v1";
        if (!value.contains("://")) value = "https://" + value;
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        if (!value.endsWith("/v1")) {
            value = value + "/v1";
        }
        return value;
    }

    private String openAiResponsesUrl(String baseUrl) {
        return normalizedOpenAiBaseUrl(baseUrl) + "/responses";
    }

    private String normalizedOpenAiModel(String raw) {
        String value = raw == null ? "" : raw.trim();
        return value.isEmpty() ? DEFAULT_OPENAI_MODEL : value;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
        @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }
    }

    private static class ColorOption {
        final String name;
        final int color;

        ColorOption(String name, int color) {
            this.name = name;
            this.color = color;
        }
    }

    private static class OccasionOption {
        final String icon;
        final String label;

        OccasionOption(String icon, String label) {
            this.icon = icon;
            this.label = label;
        }
    }

    private static class CategoryOption {
        final String group;
        final String label;
        final int iconRes;

        CategoryOption(String group, String label, int iconRes) {
            this.group = group;
            this.label = label;
            this.iconRes = iconRes;
        }
    }

    private static class MaterialOption {
        final String name;
        final int color;

        MaterialOption(String name, int color) {
            this.name = name;
            this.color = color;
        }
    }

    private static class ClothingTileHolder {
        final ImageView image;

        ClothingTileHolder(ImageView image) {
            this.image = image;
        }
    }

    private static class OutfitSelectionHolder {
        final ImageView thumbnail;
        final TextView title;
        final TextView category;
        final TextView meta;

        OutfitSelectionHolder(ImageView thumbnail, TextView title, TextView category, TextView meta) {
            this.thumbnail = thumbnail;
            this.title = title;
            this.category = category;
            this.meta = meta;
        }
    }

    private static class OutfitOverlap {
        final Outfit smaller;
        final Outfit larger;

        OutfitOverlap(Outfit smaller, Outfit larger) {
            this.smaller = smaller;
            this.larger = larger;
        }
    }

    private class ClothingGridAdapter extends BaseAdapter {
        private final List<ClothingItem> items;

        ClothingGridAdapter(List<ClothingItem> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public ClothingItem getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).id.hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            LinearLayout tile = convertView instanceof LinearLayout ? (LinearLayout) convertView : (LinearLayout) clothingTile(getItem(position));
            bindClothingTile(tile, getItem(position));
            tile.setLayoutParams(new AbsListView.LayoutParams(-1, dp(104)));
            return tile;
        }
    }

    private class OutfitSelectionAdapter extends BaseAdapter {
        private final List<ClothingItem> items;

        OutfitSelectionAdapter(List<ClothingItem> items) {
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public ClothingItem getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return getItem(position).id.hashCode();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ClothingItem item = getItem(position);
            LinearLayout row = convertView instanceof LinearLayout ? (LinearLayout) convertView : outfitSelectionCard(item, false);
            bindOutfitSelectionCard(row, item, outfitDraft != null && outfitDraft.clothingIds.contains(item.id));
            row.setLayoutParams(new AbsListView.LayoutParams(-1, dp(88)));
            return row;
        }
    }

    private class ZoomImageView extends ImageView {
        private final ScaleGestureDetector scaleDetector;
        private float currentScale = 1f;
        private float lastX;
        private float lastY;
        private float downX;
        private float downY;
        private SwipeListener swipeListener;

        ZoomImageView(android.content.Context context) {
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
    }

    private interface SwipeListener {
        void onSwipe(int direction);
    }
}
