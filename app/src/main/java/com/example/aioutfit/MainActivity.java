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
import android.view.Gravity;
import android.view.MotionEvent;
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

import androidx.core.content.FileProvider;

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
    private static final String FILTER_FAVORITES = "Favorites";
    private static final String[] SEASON_OPTIONS = {"🌱 Spring", "☀️ Summer", "🍂 Autumn", "❄️ Winter"};
    private static final String[] DAMAGE_OPTIONS = {"Stains", "Holes", "Washed out"};
    private static final CareOption[] CARE_WASHING_OPTIONS = {
            new CareOption("wash_30", "30 C wash", R.drawable.care_wash_30),
            new CareOption("wash_40", "40 C wash", R.drawable.care_wash_40),
            new CareOption("wash_50", "50 C wash", R.drawable.care_wash_50),
            new CareOption("wash_60", "60 C wash", R.drawable.care_wash_60),
            new CareOption("wash_70", "70 C wash", R.drawable.care_wash_70),
            new CareOption("wash_95", "95 C wash", R.drawable.care_wash_95),
            new CareOption("wash_normal", "Machine wash", R.drawable.care_wash_normal),
            new CareOption("wash_gentle", "Gentle wash", R.drawable.care_wash_gentle),
            new CareOption("wash_very_gentle", "Very gentle wash", R.drawable.care_wash_very_gentle),
            new CareOption("wash_hand", "Hand wash", R.drawable.care_wash_hand),
            new CareOption("wash_do_not", "Do not wash", R.drawable.care_wash_do_not)
    };
    private static final CareOption[] CARE_DRYING_OPTIONS = {
            new CareOption("dry_tumble_gentle", "Gentle tumble dry", R.drawable.care_dry_tumble_gentle),
            new CareOption("dry_tumble_medium", "Normal tumble dry", R.drawable.care_dry_tumble_medium),
            new CareOption("dry_tumble_high", "High heat tumble dry", R.drawable.care_dry_tumble_high),
            new CareOption("dry_tumble_do_not", "Do not tumble dry", R.drawable.care_dry_tumble_do_not)
    };
    private static final CareOption[] CARE_IRONING_OPTIONS = {
            new CareOption("iron_do_not", "Do not iron", R.drawable.care_iron_do_not),
            new CareOption("iron_low", "Iron low", R.drawable.care_iron_low),
            new CareOption("iron_medium", "Iron medium", R.drawable.care_iron_medium),
            new CareOption("iron_high", "Iron high", R.drawable.care_iron_high)
    };
    private static final CareOption[] CARE_PROFESSIONAL_OPTIONS = {
            new CareOption("dryclean_any", "Dry clean", R.drawable.care_dryclean_any),
            new CareOption("dryclean_petroleum", "Dry clean P", R.drawable.care_dryclean_petroleum),
            new CareOption("dryclean_pce", "Dry clean F", R.drawable.care_dryclean_pce),
            new CareOption("wetclean", "Wet clean", R.drawable.care_wetclean)
    };
    private static final CareOption[] CARE_BLEACHING_OPTIONS = {
            new CareOption("bleach_do_not", "Do not bleach", R.drawable.care_bleach_do_not),
            new CareOption("bleach_non_chlorine", "Non-chlorine bleach", R.drawable.care_bleach_non_chlorine),
            new CareOption("bleach_any", "Bleach allowed", R.drawable.care_bleach_any)
    };
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
    private static final ColorOption[] EYE_COLORS = {
            new ColorOption("Light blue", 0xFFA8D8F0),
            new ColorOption("Blue", 0xFF3A79B8),
            new ColorOption("Gray blue", 0xFF7F9AA8),
            new ColorOption("Gray", 0xFF8E9693),
            new ColorOption("Green", 0xFF5F8F4E),
            new ColorOption("Hazel", 0xFF8A7A35),
            new ColorOption("Amber", 0xFFC27A22),
            new ColorOption("Light brown", 0xFF9A642D),
            new ColorOption("Brown", 0xFF5E351E),
            new ColorOption("Dark brown", 0xFF2E1D14),
            new ColorOption("Black", 0xFF101010)
    };
    private static final ColorOption[] HAIR_COLORS = {
            new ColorOption("Black", 0xFF111111),
            new ColorOption("Dark brown", 0xFF2D1B12),
            new ColorOption("Brown", 0xFF5A321D),
            new ColorOption("Light brown", 0xFF8C5A32),
            new ColorOption("Dark blonde", 0xFFA17B43),
            new ColorOption("Blonde", 0xFFD9BD75),
            new ColorOption("Platinum blonde", 0xFFE8E0C7),
            new ColorOption("Auburn", 0xFF7B2E1E),
            new ColorOption("Copper", 0xFFC35A28),
            new ColorOption("Red", 0xFFA72B24),
            new ColorOption("Gray", 0xFF8D8D8D),
            new ColorOption("Silver", 0xFFC9C9C9),
            new ColorOption("White", 0xFFF5F2E8),
            new ColorOption("Blue", 0xFF2457A6),
            new ColorOption("Pink", 0xFFFF8FA1),
            new ColorOption("Purple", 0xFF8E4CC7),
            new ColorOption("Green", 0xFF2E8A4F)
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
            new CategoryOption("Bottoms", "Jeans", R.drawable.ic_cat_jeans),
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
            new CategoryOption("Shoes", "Dress shoes", R.drawable.ic_cat_dress_shoe),
            new CategoryOption("Shoes", "Sandals", R.drawable.ic_cat_sandal),
            new CategoryOption("Accessories", "Bag", R.drawable.ic_cat_bag),
            new CategoryOption("Accessories", "Belt", R.drawable.ic_cat_belt),
            new CategoryOption("Accessories", "Hat", R.drawable.ic_cat_hat),
            new CategoryOption("Accessories", "Scarf", R.drawable.ic_cat_scarf),
            new CategoryOption("Accessories", "Sunglasses", R.drawable.ic_cat_sunglasses),
            new CategoryOption("Accessories", "Watch", R.drawable.ic_cat_watch),
            new CategoryOption("Other", "Other", R.drawable.ic_cat_other)
    };
    private static final MaterialOption[] MATERIAL_OPTIONS = {
            new MaterialOption("Cotton", 0xFFE6E2D6),
            new MaterialOption("Modal", 0xFFC8D7CE),
            new MaterialOption("Elastane", 0xFF2E2E2E),
            new MaterialOption("Linen", 0xFFD4C6B1),
            new MaterialOption("Denim", 0xFF2D4B63),
            new MaterialOption("Wool", 0xFF8E8E8E),
            new MaterialOption("Cashmere", 0xFFC7B59A),
            new MaterialOption("Leather", 0xFF5C3A25)
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
    private final List<PlannerEntry> plannerEntries = new ArrayList<>();
    private final List<String> categories = new ArrayList<>();
    private final Map<String, ClothingItem> itemById = new LinkedHashMap<>();
    private final Map<String, PlannerEntry> plannerEntryByDate = new LinkedHashMap<>();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService imageExecutor = Executors.newFixedThreadPool(2);
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private ImageLoader imageLoader;
    private LinearLayout topBar;
    private LinearLayout stickyBar;
    private FrameLayout bodyHost;
    private LinearLayout content;
    private TextView clothesNavLabel;
    private TextView plannerNavLabel;
    private TextView outfitsNavLabel;
    private TextView inspirationNavLabel;
    private View clothesNavIndicator;
    private View plannerNavIndicator;
    private View outfitsNavIndicator;
    private View inspirationNavIndicator;
    private LinearLayout clothesNavItem;
    private LinearLayout plannerNavItem;
    private LinearLayout outfitsNavItem;
    private LinearLayout inspirationNavItem;
    private String activeTab = "clothes";
    private float swipeStartX;
    private float swipeStartY;
    private float detailSwipeStartX;
    private float detailSwipeStartY;
    private boolean detailSwipeCancelled;
    private String searchQuery = "";
    private String selectedCategory = "All";
    private String selectedOutfitFilter = "All";
    private String currency = "$";
    private String openAiApiKey = "";
    private String openAiBaseUrl = "";
    private String openAiModel = DEFAULT_OPENAI_MODEL;
    private String profileBodyImageUri = "";
    private String profileFaceImageUri = "";
    private String profileGender = "";
    private String profileEyeColor = "";
    private String profileHairColor = "";
    private String profileStyle = "";
    private String savedShirtSize = "";
    private String savedPantsWaist = "";
    private String savedPantsLength = "";
    private String savedShoeSize = "";
    private ClothingItem imageTarget;
    private ImageView imageTargetView;
    private ClothingItem detailSwipeItem;
    private Outfit clothingDetailReturnOutfit;
    private boolean appendPickedPhoto;
    private int activePhotoIndex;
    private boolean outfitInspirationLoading;
    private boolean wardrobeGapLoading;
    private WardrobeGapAnalysis wardrobeGapAnalysis;
    private String wardrobeGapQuestion = "";
    private int outfitCleanupReviewIndex;
    private final Set<String> ignoredOutfitCleanupPairs = new LinkedHashSet<>();
    private String outfitFilterCategory = "All";
    private Outfit outfitDraft;
    private Outfit outfitEditingTarget;
    private int outfitFormStep = 1;
    private boolean plannerMonthMode;
    private final Calendar plannerAnchor = Calendar.getInstance();
    private final Calendar plannerSelectedDate = Calendar.getInstance();
    private String pendingPlannerDateKey = "";

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
        plannerEntries.addAll(store.loadPlannerEntries());
        categories.addAll(store.loadCategories());
        currency = store.loadCurrency();
        openAiApiKey = store.loadOpenAiApiKey();
        openAiBaseUrl = store.loadOpenAiBaseUrl();
        openAiModel = store.loadOpenAiModel();
        profileBodyImageUri = store.loadProfileBodyImage();
        profileFaceImageUri = store.loadProfileFaceImage();
        profileGender = store.loadProfileGender();
        profileEyeColor = store.loadProfileEyeColor();
        profileHairColor = store.loadProfileHairColor();
        profileStyle = store.loadProfileStyle();
        savedShirtSize = store.loadSavedShirtSize();
        savedPantsWaist = store.loadSavedPantsWaist();
        savedPantsLength = store.loadSavedPantsLength();
        savedShoeSize = store.loadSavedShoeSize();
        wardrobeGapAnalysis = store.loadWardrobeGapAnalysis();
        imageLoader = new ImageLoader(this, imageExecutor, mainHandler);
        rebuildItemIndex();
        rebuildPlannerIndex();
        normalizePlannerCalendars();
        setContentView(buildRoot());
        renderClothes();
    }

    @Override
    protected void onDestroy() {
        imageExecutor.shutdownNow();
        ioExecutor.shutdownNow();
        super.onDestroy();
    }

    private void rebuildItemIndex() {
        itemById.clear();
        for (ClothingItem item : clothes) {
            itemById.put(item.id, item);
        }
    }

    private void rebuildPlannerIndex() {
        plannerEntryByDate.clear();
        for (PlannerEntry entry : plannerEntries) {
            if (!isBlank(entry.dateKey)) {
                plannerEntryByDate.put(entry.dateKey, entry);
            }
        }
    }

    private void normalizePlannerCalendars() {
        startOfDay(plannerAnchor);
        startOfDay(plannerSelectedDate);
        plannerAnchor.setFirstDayOfWeek(Calendar.MONDAY);
        plannerSelectedDate.setFirstDayOfWeek(Calendar.MONDAY);
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
        scroll.setOnTouchListener((view, event) -> handleScrollSwipe(event));
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
        if (!showBack && shouldShowTopBarSearch()) {
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

    private boolean shouldShowTopBarSearch() {
        return "clothes".equals(activeTab) || "outfits".equals(activeTab);
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
                String plannerDate = pendingPlannerDateKey;
                clearOutfitDraft();
                if (!isBlank(plannerDate)) {
                    pendingPlannerDateKey = "";
                    renderPlannerDay(plannerDate);
                } else {
                    renderOutfits();
                }
            }
            return true;
        }
        if (activeTab.equals("plannerDay")) {
            renderPlanner();
            return true;
        }
        if (activeTab.equals("outfitDetail")) {
            renderOutfits();
            return true;
        }
        if (activeTab.equals("outfitCleanup") || activeTab.equals("outfitCleanupReview")) {
            renderInspiration();
            return true;
        }
        if (activeTab.equals("wardrobeGapDetail")) {
            renderWardrobeGapOverview();
            return true;
        }
        if (activeTab.equals("wardrobeGapOverview")) {
            if (!isBlank(wardrobeGapQuestion)) {
                renderWardrobeGapAsk();
            } else {
                renderInspiration();
            }
            return true;
        }
        if (activeTab.equals("wardrobeGapAsk")) {
            renderInspiration();
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
        if (activeTab.equals("detail") && clothingDetailReturnOutfit != null) {
            Outfit returnTarget = clothingDetailReturnOutfit;
            clothingDetailReturnOutfit = null;
            renderOutfitDetail(returnTarget);
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
        if (imageLoader == null) {
            imageLoader = new ImageLoader(this, imageExecutor, mainHandler);
        }
        imageLoader.load(target, uri, fallbackRes, fallbackColor, dp(targetDp));
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
        clothesNavItem = navItem(getString(R.string.nav_clothes), R.drawable.ic_clothes, v -> renderClothes(), "clothes");
        plannerNavItem = navItem(getString(R.string.nav_planner), R.drawable.ic_planner, v -> renderPlanner(), "planner");
        outfitsNavItem = navItem(getString(R.string.nav_outfits), R.drawable.ic_outfit, v -> renderOutfits(), "outfits");
        inspirationNavItem = navItem(getString(R.string.nav_inspiration), R.drawable.ic_inspiration, v -> renderInspiration(), "inspiration");
        nav.addView(clothesNavItem, new LinearLayout.LayoutParams(0, -1, 1));
        nav.addView(outfitsNavItem, new LinearLayout.LayoutParams(0, -1, 1));
        TextView centerGap = new TextView(this);
        nav.addView(centerGap, new LinearLayout.LayoutParams(dp(76), -1));
        nav.addView(inspirationNavItem, new LinearLayout.LayoutParams(0, -1, 1));
        nav.addView(plannerNavItem, new LinearLayout.LayoutParams(0, -1, 1));

        FrameLayout.LayoutParams navParams = new FrameLayout.LayoutParams(-1, dp(84));
        navParams.gravity = Gravity.BOTTOM;
        container.addView(nav, navParams);

        ImageView add = addNavButton();
        FrameLayout.LayoutParams addParams = new FrameLayout.LayoutParams(dp(64), dp(64));
        addParams.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
        container.addView(add, addParams);
        return container;
    }

    private LinearLayout navItem(String label, int iconRes, View.OnClickListener listener, String key) {
        LinearLayout item = new LinearLayout(this);
        item.setOrientation(LinearLayout.VERTICAL);
        item.setGravity(Gravity.CENTER);
        item.setPadding(0, dp(4), 0, dp(4));
        item.setOnClickListener(listener);
        ImageView icon = iconView(iconRes);
        item.addView(icon, new LinearLayout.LayoutParams(dp(28), dp(28)));
        TextView text = text(label, 11, muted, true);
        text.setGravity(Gravity.CENTER);
        text.setSingleLine(true);
        item.addView(text);
        TextView indicator = new TextView(this);
        LinearLayout.LayoutParams indicatorParams = new LinearLayout.LayoutParams(dp(26), dp(3));
        indicatorParams.setMargins(0, dp(3), 0, 0);
        item.addView(indicator, indicatorParams);
        if ("clothes".equals(key)) {
            clothesNavLabel = text;
            clothesNavIndicator = indicator;
        } else if ("planner".equals(key)) {
            plannerNavLabel = text;
            plannerNavIndicator = indicator;
        } else if ("outfits".equals(key)) {
            outfitsNavLabel = text;
            outfitsNavIndicator = indicator;
        } else {
            inspirationNavLabel = text;
            inspirationNavIndicator = indicator;
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
        if (clothesNavLabel == null || plannerNavLabel == null || outfitsNavLabel == null || inspirationNavLabel == null
                || clothesNavItem == null || plannerNavItem == null || outfitsNavItem == null || inspirationNavItem == null
                || clothesNavIndicator == null || plannerNavIndicator == null || outfitsNavIndicator == null || inspirationNavIndicator == null) return;
        boolean clothesActive = activeTab.equals("clothes");
        boolean plannerActive = activeTab.equals("planner") || activeTab.equals("plannerDay") || (activeTab.equals("outfitForm") && !isBlank(pendingPlannerDateKey));
        boolean outfitsActive = activeTab.equals("outfits") || activeTab.equals("outfitDetail") || (activeTab.equals("outfitForm") && isBlank(pendingPlannerDateKey));
        boolean inspirationActive = activeTab.equals("inspiration");
        clothesNavLabel.setTextColor(clothesActive ? ink : muted);
        plannerNavLabel.setTextColor(plannerActive ? ink : muted);
        outfitsNavLabel.setTextColor(outfitsActive ? ink : muted);
        inspirationNavLabel.setTextColor(inspirationActive ? ink : muted);
        clothesNavIndicator.setBackground(clothesActive ? rounded(primary, 2, Color.TRANSPARENT, 0) : rounded(Color.TRANSPARENT, 2, Color.TRANSPARENT, 0));
        plannerNavIndicator.setBackground(plannerActive ? rounded(primary, 2, Color.TRANSPARENT, 0) : rounded(Color.TRANSPARENT, 2, Color.TRANSPARENT, 0));
        outfitsNavIndicator.setBackground(outfitsActive ? rounded(primary, 2, Color.TRANSPARENT, 0) : rounded(Color.TRANSPARENT, 2, Color.TRANSPARENT, 0));
        inspirationNavIndicator.setBackground(inspirationActive ? rounded(primary, 2, Color.TRANSPARENT, 0) : rounded(Color.TRANSPARENT, 2, Color.TRANSPARENT, 0));
        clothesNavItem.setBackgroundColor(Color.TRANSPARENT);
        plannerNavItem.setBackgroundColor(Color.TRANSPARENT);
        outfitsNavItem.setBackgroundColor(Color.TRANSPARENT);
        inspirationNavItem.setBackgroundColor(Color.TRANSPARENT);
    }

    private boolean handleMainSwipe(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                swipeStartX = event.getX();
                swipeStartY = event.getY();
                return false;
            case MotionEvent.ACTION_UP:
                if (!activeTab.equals("clothes") && !activeTab.equals("planner")
                        && !activeTab.equals("outfits") && !activeTab.equals("inspiration")) {
                    return false;
                }
                float deltaX = event.getX() - swipeStartX;
                float deltaY = event.getY() - swipeStartY;
                if (Math.abs(deltaX) > dp(72) && Math.abs(deltaX) > Math.abs(deltaY) * 1.4f) {
                    if (deltaX < 0 && activeTab.equals("clothes")) {
                        renderOutfits();
                    } else if (deltaX < 0 && activeTab.equals("outfits")) {
                        renderInspiration();
                    } else if (deltaX < 0 && activeTab.equals("inspiration")) {
                        renderPlanner();
                    } else if (deltaX > 0 && activeTab.equals("planner")) {
                        renderInspiration();
                    } else if (deltaX > 0 && activeTab.equals("inspiration")) {
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

    private boolean handleScrollSwipe(MotionEvent event) {
        if (activeTab.equals("detail") && detailSwipeItem != null) {
            return handleClothingDetailSwipe(event, detailSwipeItem);
        }
        if (activeTab.equals("outfitCleanupReview")) {
            return handleOutfitCleanupReviewSwipe(event);
        }
        return handleMainSwipe(event);
    }

    private boolean handleOutfitCleanupReviewSwipe(MotionEvent event) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                detailSwipeStartX = event.getX();
                detailSwipeStartY = event.getY();
                detailSwipeCancelled = false;
                return false;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX() - detailSwipeStartX;
                float moveY = event.getY() - detailSwipeStartY;
                if (Math.abs(moveY) > dp(24) && Math.abs(moveY) > Math.abs(moveX)) {
                    detailSwipeCancelled = true;
                }
                return false;
            case MotionEvent.ACTION_UP:
                float deltaX = event.getX() - detailSwipeStartX;
                float deltaY = event.getY() - detailSwipeStartY;
                if (!detailSwipeCancelled
                        && Math.abs(deltaX) > dp(96)
                        && Math.abs(deltaY) < dp(40)
                        && Math.abs(deltaX) > Math.abs(deltaY) * 2.5f) {
                    return moveOutfitCleanupReview(deltaX < 0 ? 1 : -1);
                }
                return false;
            case MotionEvent.ACTION_CANCEL:
                detailSwipeCancelled = true;
                return false;
            default:
                return false;
        }
    }

    private boolean handleClothingDetailSwipe(MotionEvent event, ClothingItem item) {
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                detailSwipeStartX = event.getX();
                detailSwipeStartY = event.getY();
                detailSwipeCancelled = false;
                return false;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getX() - detailSwipeStartX;
                float moveY = event.getY() - detailSwipeStartY;
                if (Math.abs(moveY) > dp(24) && Math.abs(moveY) > Math.abs(moveX)) {
                    detailSwipeCancelled = true;
                }
                return false;
            case MotionEvent.ACTION_UP:
                float deltaX = event.getX() - detailSwipeStartX;
                float deltaY = event.getY() - detailSwipeStartY;
                if (!detailSwipeCancelled
                        && Math.abs(deltaX) > dp(120)
                        && Math.abs(deltaY) < dp(32)
                        && Math.abs(deltaX) > Math.abs(deltaY) * 3f) {
                    return moveClothingDetail(item, deltaX < 0 ? 1 : -1);
                }
                return false;
            case MotionEvent.ACTION_CANCEL:
                detailSwipeCancelled = true;
                return false;
            default:
                return false;
        }
    }

    private void showSearchDialog() {
        boolean searchingOutfits = "outfits".equals(activeTab);
        EditText input = input(searchingOutfits ? "Search outfits" : "Search clothes");
        input.setText(searchQuery);
        showMaterialDialog("Search", input, "Clear", "Search", () -> {
            searchQuery = "";
            if (searchingOutfits) {
                renderOutfits();
            } else {
                renderClothes();
            }
        }, () -> {
            searchQuery = input.getText().toString().trim();
            if (searchingOutfits) {
                renderOutfits();
            } else {
                renderClothes();
            }
        });
    }

    private void showAddMenu() {
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        Dialog[] holder = new Dialog[1];
        if (activeTab.equals("planner") || activeTab.equals("plannerDay")) {
            menu.addView(menuRow(R.drawable.ic_planner, "Plan outfit", "Add an outfit to the selected day", v -> {
                if (holder[0] != null) holder[0].dismiss();
                showPlannerAddSheet(dateKey(plannerSelectedDate));
            }));
        }
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

    private void renderPlanner() {
        activeTab = "planner";
        renderTopBar(getString(R.string.nav_planner), false);
        updateBottomNavigation();
        useScrollContent();
        content.removeAllViews();
        content.setPadding(dp(18), dp(2), dp(18), dp(32));
        content.addView(plannerModeSwitch());
        if (plannerMonthMode) {
            renderPlannerMonth();
        } else {
            renderPlannerWeek();
        }
    }

    private View plannerModeSwitch() {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, dp(12));
        row.addView(plannerModeButton("Week", !plannerMonthMode), new LinearLayout.LayoutParams(0, dp(44), 1));
        row.addView(plannerModeButton("Month", plannerMonthMode), new LinearLayout.LayoutParams(0, dp(44), 1));
        return row;
    }

    private Button plannerModeButton(String label, boolean active) {
        Button button = baseButton(label);
        button.setTextColor(active ? onPrimary : muted);
        button.setTypeface(button.getTypeface(), Typeface.BOLD);
        button.setBackground(rounded(active ? primary : Color.TRANSPARENT, 22, active ? Color.TRANSPARENT : palette.outlineVariant, 1));
        button.setOnClickListener(v -> {
            plannerMonthMode = "Month".equals(label);
            renderPlanner();
        });
        return button;
    }

    private void renderPlannerWeek() {
        content.addView(plannerWeekControls());
        Calendar start = weekStart(plannerAnchor);
        for (int i = 0; i < 7; i++) {
            Calendar day = (Calendar) start.clone();
            day.add(Calendar.DAY_OF_MONTH, i);
            content.addView(plannerDayCard(day));
        }
    }

    private View plannerWeekControls() {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, dp(8));
        row.addView(iconButton(R.drawable.ic_move_left, v -> {
            plannerAnchor.add(Calendar.DAY_OF_MONTH, -7);
            renderPlanner();
        }), new LinearLayout.LayoutParams(dp(48), dp(48)));
        TextView range = text(plannerWeekRange(), 15, ink, true);
        range.setGravity(Gravity.CENTER);
        row.addView(range, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(iconButton(R.drawable.ic_move_right, v -> {
            plannerAnchor.add(Calendar.DAY_OF_MONTH, 7);
            renderPlanner();
        }), new LinearLayout.LayoutParams(dp(48), dp(48)));
        Button today = outlinedButton("Today");
        today.setOnClickListener(v -> {
            plannerAnchor.setTimeInMillis(System.currentTimeMillis());
            plannerSelectedDate.setTimeInMillis(System.currentTimeMillis());
            normalizePlannerCalendars();
            renderPlanner();
        });
        LinearLayout.LayoutParams todayParams = new LinearLayout.LayoutParams(dp(92), dp(44));
        todayParams.setMargins(dp(8), 0, 0, 0);
        row.addView(today, todayParams);
        return row;
    }

    private String plannerWeekRange() {
        Calendar start = weekStart(plannerAnchor);
        Calendar end = (Calendar) start.clone();
        end.add(Calendar.DAY_OF_MONTH, 6);
        SimpleDateFormat startFormat = new SimpleDateFormat("MMM d", Locale.US);
        SimpleDateFormat endFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
        return startFormat.format(start.getTime()) + " - " + endFormat.format(end.getTime());
    }

    private View plannerDayCard(Calendar day) {
        String key = dateKey(day);
        PlannerEntry entry = plannerEntryByDate.get(key);
        Outfit outfit = entry == null ? null : findOutfit(entry.outfitId);

        LinearLayout card = card();
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setOnClickListener(v -> renderPlannerDay(key));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout date = new LinearLayout(this);
        date.setOrientation(LinearLayout.VERTICAL);
        date.addView(text(new SimpleDateFormat("EEE", Locale.US).format(day.getTime()), 14, primary, true));
        date.addView(text(String.valueOf(day.get(Calendar.DAY_OF_MONTH)), 18, primary, true));
        top.addView(date, new LinearLayout.LayoutParams(dp(58), -2));

        TextView context = text(plannerContextText(entry), 13, muted, false);
        top.addView(context, new LinearLayout.LayoutParams(0, -2, 1));
        top.addView(transparentIconButton(R.drawable.ic_more_vertical, ink, v -> showPlannerDayMenu(key)), new LinearLayout.LayoutParams(dp(44), dp(44)));
        card.addView(top);

        if (outfit != null) {
            card.addView(outfitPreview(outfit));
            TextView name = text(value(outfit.name, "Untitled outfit"), 14, ink, true);
            name.setPadding(dp(72), dp(2), 0, 0);
            card.addView(name);
            if (entry != null && entry.worn) {
                TextView worn = text("Marked as worn", 12, primary, true);
                worn.setPadding(dp(72), dp(2), 0, 0);
                card.addView(worn);
            }
        } else if (entry != null && !isBlank(entry.outfitId)) {
            card.addView(plannerMissingOutfitView(key));
        } else {
            card.addView(plannerEmptyDayView(key));
        }
        return card;
    }

    private View plannerEmptyDayView(String dateKey) {
        LinearLayout empty = new LinearLayout(this);
        empty.setGravity(Gravity.CENTER_VERTICAL);
        empty.setPadding(dp(72), dp(8), 0, 0);
        empty.setOnClickListener(v -> showPlannerAddSheet(dateKey));
        TextView add = text("+", 20, primary, true);
        add.setGravity(Gravity.CENTER);
        add.setBackground(oval(Color.TRANSPARENT, primary, 1));
        empty.addView(add, new LinearLayout.LayoutParams(dp(34), dp(34)));
        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(12), 0, 0, 0);
        copy.addView(text("Add outfit", 14, ink, true));
        copy.addView(text("No outfit planned", 12, muted, false));
        empty.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        return empty;
    }

    private View plannerMissingOutfitView(String dateKey) {
        LinearLayout missing = new LinearLayout(this);
        missing.setOrientation(LinearLayout.VERTICAL);
        missing.setPadding(dp(72), dp(8), 0, 0);
        missing.addView(text("Planned outfit is missing", 14, error, true));
        missing.addView(text("Change outfit or remove this plan.", 12, muted, false));
        LinearLayout actions = new LinearLayout(this);
        actions.setPadding(0, dp(8), 0, 0);
        Button change = outlinedButton("Change");
        change.setOnClickListener(v -> showPlannerAddSheet(dateKey));
        actions.addView(change, new LinearLayout.LayoutParams(0, dp(44), 1));
        Button remove = dangerButton("Remove");
        remove.setOnClickListener(v -> removePlannerEntry(dateKey));
        LinearLayout.LayoutParams removeParams = new LinearLayout.LayoutParams(0, dp(44), 1);
        removeParams.setMargins(dp(8), 0, 0, 0);
        actions.addView(remove, removeParams);
        missing.addView(actions);
        return missing;
    }

    private String plannerContextText(PlannerEntry entry) {
        if (entry == null) return "Weather later · No occasion";
        return joinNonEmpty(value(entry.weatherSummary, "Weather later"), entry.occasion);
    }

    private void renderPlannerMonth() {
        content.addView(plannerMonthControls());
        content.addView(plannerMonthGrid());
        content.addView(plannerUpcomingCard());
        Button plan = primaryButton("+ Plan outfit");
        plan.setOnClickListener(v -> showPlannerAddSheet(dateKey(plannerSelectedDate)));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(54));
        params.setMargins(dp(36), dp(12), dp(36), 0);
        content.addView(plan, params);
    }

    private View plannerMonthControls() {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, 0, 0, dp(8));
        row.addView(iconButton(R.drawable.ic_move_left, v -> {
            plannerAnchor.add(Calendar.MONTH, -1);
            renderPlanner();
        }), new LinearLayout.LayoutParams(dp(48), dp(48)));
        TextView title = text(new SimpleDateFormat("MMMM yyyy", Locale.US).format(plannerAnchor.getTime()), 18, ink, true);
        title.setGravity(Gravity.CENTER);
        row.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(iconButton(R.drawable.ic_move_right, v -> {
            plannerAnchor.add(Calendar.MONTH, 1);
            renderPlanner();
        }), new LinearLayout.LayoutParams(dp(48), dp(48)));
        return row;
    }

    private View plannerMonthGrid() {
        LinearLayout card = card();
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        LinearLayout weekdays = new LinearLayout(this);
        String[] labels = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        for (String label : labels) {
            TextView day = text(label, 11, muted, true);
            day.setGravity(Gravity.CENTER);
            weekdays.addView(day, new LinearLayout.LayoutParams(0, dp(28), 1));
        }
        card.addView(weekdays);

        Calendar cursor = monthGridStart(plannerAnchor);
        for (int rowIndex = 0; rowIndex < 6; rowIndex++) {
            LinearLayout row = new LinearLayout(this);
            for (int col = 0; col < 7; col++) {
                Calendar day = (Calendar) cursor.clone();
                row.addView(monthDayCell(day), new LinearLayout.LayoutParams(0, dp(58), 1));
                cursor.add(Calendar.DAY_OF_MONTH, 1);
            }
            card.addView(row);
        }
        LinearLayout legend = new LinearLayout(this);
        legend.setGravity(Gravity.CENTER_VERTICAL);
        legend.setPadding(0, dp(8), 0, 0);
        legend.addView(text("● Outfit planned", 12, primary, false), new LinearLayout.LayoutParams(0, -2, 1));
        legend.addView(text("● No plan", 12, muted, false), new LinearLayout.LayoutParams(0, -2, 1));
        card.addView(legend);
        return card;
    }

    private View monthDayCell(Calendar day) {
        String key = dateKey(day);
        boolean sameMonth = day.get(Calendar.MONTH) == plannerAnchor.get(Calendar.MONTH);
        boolean selected = key.equals(dateKey(plannerSelectedDate));
        PlannerEntry entry = plannerEntryByDate.get(key);

        LinearLayout cell = new LinearLayout(this);
        cell.setOrientation(LinearLayout.VERTICAL);
        cell.setGravity(Gravity.CENTER);
        cell.setPadding(dp(2), dp(2), dp(2), dp(2));
        cell.setBackground(rounded(selected ? primaryContainer : Color.TRANSPARENT, 18, Color.TRANSPARENT, 0));
        cell.setOnClickListener(v -> {
            plannerSelectedDate.setTimeInMillis(day.getTimeInMillis());
            renderPlanner();
        });
        TextView number = text(String.valueOf(day.get(Calendar.DAY_OF_MONTH)), 13, sameMonth ? ink : muted, selected);
        number.setGravity(Gravity.CENTER);
        cell.addView(number);
        TextView dot = text(entry != null && !isBlank(entry.outfitId) ? "●" : "·", 16, entry != null && !isBlank(entry.outfitId) ? primary : palette.outlineVariant, true);
        dot.setGravity(Gravity.CENTER);
        cell.addView(dot);
        return cell;
    }

    private View plannerUpcomingCard() {
        LinearLayout card = card();
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(text("Upcoming (Next 7 days)", 16, ink, true), new LinearLayout.LayoutParams(0, -2, 1));
        TextView viewAll = text("View all", 13, primary, true);
        viewAll.setGravity(Gravity.CENTER);
        viewAll.setOnClickListener(v -> {
            plannerMonthMode = false;
            renderPlanner();
        });
        header.addView(viewAll, new LinearLayout.LayoutParams(dp(72), dp(40)));
        card.addView(header);
        boolean added = false;
        Calendar day = Calendar.getInstance();
        startOfDay(day);
        for (int i = 0; i < 7; i++) {
            PlannerEntry entry = plannerEntryByDate.get(dateKey(day));
            Outfit outfit = entry == null ? null : findOutfit(entry.outfitId);
            if (outfit != null) {
                card.addView(upcomingPlannerRow(day, outfit, entry));
                added = true;
            }
            day.add(Calendar.DAY_OF_MONTH, 1);
        }
        if (!added) {
            TextView empty = text("No outfits planned in the next 7 days.", 13, muted, false);
            empty.setPadding(0, dp(8), 0, 0);
            card.addView(empty);
        }
        return card;
    }

    private View upcomingPlannerRow(Calendar day, Outfit outfit, PlannerEntry entry) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));
        row.setOnClickListener(v -> renderPlannerDay(dateKey(day)));
        TextView date = text(new SimpleDateFormat("EEE, MMM d", Locale.US).format(day.getTime()), 12, muted, false);
        row.addView(date, new LinearLayout.LayoutParams(dp(92), -2));
        row.addView(outfitMiniPreview(outfit), new LinearLayout.LayoutParams(dp(58), dp(46)));
        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(10), 0, 0, 0);
        copy.addView(text(value(outfit.name, "Untitled outfit"), 13, ink, true));
        copy.addView(text(value(entry.occasion, "No occasion"), 12, muted, false));
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        return row;
    }

    private void renderPlannerDay(String dateKey) {
        activeTab = "plannerDay";
        plannerSelectedDate.setTimeInMillis(calendarForDateKey(dateKey).getTimeInMillis());
        renderTopBar(plannerDayTitle(plannerSelectedDate), true, null, false);
        topBar.addView(iconButton(R.drawable.ic_more_vertical, v -> showPlannerDayMenu(dateKey)), new LinearLayout.LayoutParams(dp(48), dp(48)));
        updateBottomNavigation();
        useScrollContent();
        content.removeAllViews();
        content.setPadding(dp(18), dp(8), dp(18), dp(32));

        PlannerEntry entry = plannerEntryByDate.get(dateKey);
        Outfit outfit = entry == null ? null : findOutfit(entry.outfitId);
        LinearLayout chips = new LinearLayout(this);
        chips.setPadding(0, 0, 0, dp(12));
        chips.addView(plannerContextChip(R.drawable.ic_weather_cloud, value(entry == null ? "" : entry.weatherSummary, "Weather later")));
        chips.addView(plannerContextChip(R.drawable.ic_planner, value(entry == null ? "" : entry.occasion, "No occasion")));
        content.addView(chips);

        if (outfit != null) {
            content.addView(plannerHeroOutfit(outfit));
        } else if (entry != null && !isBlank(entry.outfitId)) {
            content.addView(plannerMissingOutfitView(dateKey));
        } else {
            content.addView(plannerEmptyDayView(dateKey));
        }
        content.addView(plannerDayActions(dateKey, entry, outfit));
        content.addView(plannerNotesCard(dateKey, entry));
    }

    private View plannerContextChip(int iconRes, String label) {
        LinearLayout chip = new LinearLayout(this);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(12), 0, dp(12), 0);
        chip.setBackground(rounded(Color.TRANSPARENT, 20, palette.outlineVariant, 1));

        ImageView icon = iconView(iconRes);
        icon.setColorFilter(ink);
        chip.addView(icon, new LinearLayout.LayoutParams(dp(18), dp(18)));

        TextView copy = text(label, 13, ink, false);
        copy.setPadding(dp(8), 0, 0, 0);
        chip.addView(copy);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, dp(40));
        params.setMargins(0, 0, dp(8), 0);
        chip.setLayoutParams(params);
        return chip;
    }

    private View plannerContextChip(String icon, String label) {
        TextView chip = text(icon + "  " + label, 13, ink, false);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(12), 0, dp(12), 0);
        chip.setBackground(rounded(Color.TRANSPARENT, 20, palette.outlineVariant, 1));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, dp(40));
        params.setMargins(0, 0, dp(8), 0);
        chip.setLayoutParams(params);
        return chip;
    }

    private View plannerHeroOutfit(Outfit outfit) {
        LinearLayout card = card();
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.addView(outfitReviewPreviewFor(outfit));
        LinearLayout title = new LinearLayout(this);
        title.setGravity(Gravity.CENTER_VERTICAL);
        title.setPadding(0, dp(10), 0, 0);
        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.addView(text(value(outfit.name, "Untitled outfit"), 18, ink, true));
        copy.addView(text("Planned outfit", 13, muted, false));
        title.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        ImageView heart = iconButton(outfit.favorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_outline, v -> toggleOutfitFavorite(outfit));
        heart.setColorFilter(outfit.favorite ? primary : ink);
        heart.setBackground(oval(Color.TRANSPARENT, palette.outlineVariant, 1));
        title.addView(heart, new LinearLayout.LayoutParams(dp(48), dp(48)));
        card.addView(title);
        return card;
    }

    private void toggleOutfitFavorite(Outfit outfit) {
        toggleOutfitFavoriteState(outfit);
        if ("plannerDay".equals(activeTab)) {
            renderPlannerDay(dateKey(plannerSelectedDate));
        } else if ("outfitDetail".equals(activeTab)) {
            renderOutfitDetail(outfit);
        } else if ("outfits".equals(activeTab) && FILTER_FAVORITES.equals(selectedOutfitFilter) && !outfit.favorite) {
            renderOutfits();
        }
    }

    private void toggleOutfitFavoriteState(Outfit outfit) {
        outfit.favorite = !outfit.favorite;
        outfit.updatedAt = System.currentTimeMillis();
        store.saveOutfits(outfits);
    }

    private View plannerDayActions(String dateKey, PlannerEntry entry, Outfit outfit) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        row.setPadding(0, dp(8), 0, dp(8));
        row.addView(plannerActionButton(R.drawable.ic_swap, "Change\noutfit", false, v -> showPlannerAddSheet(dateKey)), new LinearLayout.LayoutParams(0, dp(76), 1));
        row.addView(plannerActionButton(R.drawable.ic_edit, "Edit\nitems", false, v -> {
            if (outfit != null) {
                pendingPlannerDateKey = dateKey;
                renderOutfitForm(outfit);
            } else {
                showPlannerAddSheet(dateKey);
            }
        }), new LinearLayout.LayoutParams(0, dp(76), 1));
        boolean worn = entry != null && entry.worn;
        row.addView(plannerActionButton(R.drawable.ic_check_circle, worn ? "Mark\nunworn" : "Mark\nworn", worn, v -> togglePlannerWorn(dateKey)), new LinearLayout.LayoutParams(0, dp(76), 1));
        return row;
    }

    private View plannerActionButton(int iconRes, String label, boolean active, View.OnClickListener listener) {
        LinearLayout button = new LinearLayout(this);
        button.setOrientation(LinearLayout.VERTICAL);
        button.setGravity(Gravity.CENTER);
        button.setPadding(dp(4), dp(6), dp(4), dp(6));
        button.setBackground(rounded(active ? primaryContainer : Color.WHITE, 16, active ? primary : palette.outlineVariant, 1));
        button.setOnClickListener(listener);
        ImageView icon = iconView(iconRes);
        icon.setColorFilter(active ? primary : ink);
        button.addView(icon, new LinearLayout.LayoutParams(dp(24), dp(24)));
        TextView copy = text(label, 11, active ? primary : ink, true);
        copy.setGravity(Gravity.CENTER);
        copy.setPadding(0, dp(4), 0, 0);
        button.addView(copy);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(76), 1);
        params.setMargins(dp(4), 0, dp(4), 0);
        button.setLayoutParams(params);
        return button;
    }

    private View plannerNotesCard(String dateKey, PlannerEntry entry) {
        LinearLayout card = card();
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.addView(text("Notes", 15, ink, true), new LinearLayout.LayoutParams(0, -2, 1));
        header.addView(transparentIconButton(R.drawable.ic_edit, primary, v -> editPlannerNotes(dateKey)), new LinearLayout.LayoutParams(dp(44), dp(44)));
        card.addView(header);
        card.addView(text(value(entry == null ? "" : entry.notes, "No notes added."), 14, muted, false));
        return card;
    }

    private void showPlannerDayMenu(String dateKey) {
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        Dialog[] holder = new Dialog[1];
        menu.addView(menuRow(R.drawable.ic_add_outfit, "Change outfit", "Choose, build, or ask AI", v -> {
            if (holder[0] != null) holder[0].dismiss();
            showPlannerAddSheet(dateKey);
        }));
        menu.addView(menuRow(R.drawable.ic_detail_tag, "Occasion", "Set day context", v -> {
            if (holder[0] != null) holder[0].dismiss();
            editPlannerOccasion(dateKey);
        }));
        menu.addView(menuRow(R.drawable.ic_detail_notes, "Notes", "Edit day notes", v -> {
            if (holder[0] != null) holder[0].dismiss();
            editPlannerNotes(dateKey);
        }));
        menu.addView(menuRow(R.drawable.ic_delete, "Remove plan", "Clear this day", v -> {
            if (holder[0] != null) holder[0].dismiss();
            removePlannerEntry(dateKey);
        }));
        holder[0] = showMaterialDialog("Plan options", menu, getString(R.string.close), null, null, null);
    }

    private void showPlannerAddSheet(String dateKey) {
        LinearLayout menu = new LinearLayout(this);
        menu.setOrientation(LinearLayout.VERTICAL);
        Dialog[] holder = new Dialog[1];
        menu.addView(menuRow(R.drawable.ic_add_outfit, "Choose existing outfit", "Pick from saved outfits", v -> {
            if (holder[0] != null) holder[0].dismiss();
            showPlannerOutfitPicker(dateKey);
        }));
        menu.addView(menuRow(R.drawable.ic_clothes, "Build from clothes", "Select individual items", v -> {
            if (holder[0] != null) holder[0].dismiss();
            pendingPlannerDateKey = dateKey;
            renderOutfitForm(null);
        }));
        menu.addView(menuRow(R.drawable.ic_openai, "Ask AI", "Get outfit suggestions", v -> {
            if (holder[0] != null) holder[0].dismiss();
            if (clothes.isEmpty()) {
                Toast.makeText(this, getString(R.string.inspiration_empty_clothes), Toast.LENGTH_LONG).show();
                return;
            }
            if (isBlank(openAiApiKey)) {
                Toast.makeText(this, getString(R.string.openai_api_key_not_set), Toast.LENGTH_LONG).show();
                renderCategories();
                return;
            }
            pendingPlannerDateKey = dateKey;
            promptOutfitInspirationStyle();
        }));
        holder[0] = showMaterialDialog("Add outfit", menu, getString(R.string.close), null, null, null);
    }

    private void showPlannerOutfitPicker(String dateKey) {
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        Dialog[] holder = new Dialog[1];
        if (outfits.isEmpty()) {
            list.addView(text("No saved outfits yet.", 14, muted, false));
        } else {
            for (Outfit outfit : outfits) {
                list.addView(plannerOutfitPickerRow(outfit, dateKey, holder));
            }
        }
        ScrollView scroller = new ScrollView(this);
        scroller.addView(list);
        scroller.setLayoutParams(new LinearLayout.LayoutParams(-1, dp(420)));
        holder[0] = showMaterialDialog("Choose outfit", scroller, getString(R.string.close), null, null, null);
    }

    private View plannerOutfitPickerRow(Outfit outfit, String dateKey, Dialog[] holder) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, dp(8));
        row.setOnClickListener(v -> {
            if (holder[0] != null) holder[0].dismiss();
            assignOutfitToPlannerDate(dateKey, outfit);
        });
        row.addView(outfitMiniPreview(outfit), new LinearLayout.LayoutParams(dp(68), dp(56)));
        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(12), 0, 0, 0);
        copy.addView(text(value(outfit.name, "Untitled outfit"), 15, ink, true));
        copy.addView(text(outfitItemNames(outfit), 12, muted, false));
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        return row;
    }

    private View outfitMiniPreview(Outfit outfit) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        row.setBackground(rounded(palette.softSurface(), 12, palette.outlineVariant, 1));
        int added = 0;
        for (String id : outfit.clothingIds) {
            ClothingItem item = findItem(id);
            if (item == null) continue;
            ImageView image = new ImageView(this);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            image.setPadding(dp(3), dp(3), dp(3), dp(3));
            if (!isBlank(item.imageUri)) {
                loadImage(image, item.imageUri, categoryDrawable(item.category), muted, 64);
            } else {
                image.setImageResource(categoryDrawable(item.category));
                image.setColorFilter(muted);
            }
            row.addView(image, new LinearLayout.LayoutParams(0, -1, 1));
            added++;
            if (added >= 3) break;
        }
        if (added == 0) {
            ImageView icon = iconView(R.drawable.ic_outfit);
            icon.setColorFilter(muted);
            row.addView(icon, new LinearLayout.LayoutParams(dp(44), dp(44)));
        }
        return row;
    }

    private View outfitReviewPreviewFor(Outfit outfit) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(0, 0, 0, 0);
        HorizontalScrollView scroller = new HorizontalScrollView(this);
        scroller.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        int added = 0;
        for (String id : outfit.clothingIds) {
            ClothingItem item = findItem(id);
            if (item == null) continue;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(120), dp(150));
            params.setMargins(added == 0 ? 0 : dp(8), 0, 0, 0);
            row.addView(outfitReviewPreviewTile(item), params);
            added++;
        }
        scroller.addView(row);
        card.addView(scroller);
        return card;
    }

    private void assignOutfitToPlannerDate(String dateKey, Outfit outfit) {
        PlannerEntry existing = plannerEntryForDate(dateKey, false);
        if (existing != null && !isBlank(existing.outfitId) && !existing.outfitId.equals(outfit.id)) {
            Outfit existingOutfit = findOutfit(existing.outfitId);
            String message = "This day already has "
                    + value(existingOutfit == null ? "" : existingOutfit.name, "an outfit")
                    + " planned. Replace it with "
                    + value(outfit.name, "this outfit")
                    + "?";
            showMaterialDialog("Replace planned outfit?", text(message, 15, muted, false), "Cancel", "Replace", null, () -> assignOutfitToPlannerDate(dateKey, outfit, true));
            return;
        }
        assignOutfitToPlannerDate(dateKey, outfit, false);
    }

    private void assignOutfitToPlannerDate(String dateKey, Outfit outfit, boolean replacing) {
        PlannerEntry entry = plannerEntryForDate(dateKey, true);
        entry.outfitId = outfit.id;
        if (isBlank(entry.occasion)) {
            entry.occasion = outfit.occasion;
        }
        savePlannerState();
        Toast.makeText(this, replacing ? "Planned outfit replaced" : "Outfit planned", Toast.LENGTH_SHORT).show();
        renderPlannerDay(dateKey);
    }

    private void editPlannerOccasion(String dateKey) {
        PlannerEntry entry = plannerEntryForDate(dateKey, true);
        showOccasionPicker(entry.occasion, value -> {
            entry.occasion = value;
            savePlannerState();
        }, () -> renderPlannerDay(dateKey));
    }

    private void editPlannerNotes(String dateKey) {
        PlannerEntry entry = plannerEntryForDate(dateKey, true);
        EditText input = input("Notes");
        input.setMinLines(3);
        input.setSingleLine(false);
        input.setText(entry.notes);
        showMaterialDialog("Notes", input, "Cancel", "Save", null, () -> {
            entry.notes = input.getText().toString().trim();
            savePlannerState();
            renderPlannerDay(dateKey);
        });
    }

    private void togglePlannerWorn(String dateKey) {
        PlannerEntry entry = plannerEntryForDate(dateKey, true);
        entry.worn = !entry.worn;
        savePlannerState();
        renderPlannerDay(dateKey);
    }

    private void removePlannerEntry(String dateKey) {
        PlannerEntry entry = plannerEntryByDate.remove(dateKey);
        if (entry != null) {
            plannerEntries.remove(entry);
            store.savePlannerEntries(plannerEntries);
        }
        if (activeTab.equals("plannerDay")) {
            renderPlannerDay(dateKey);
        } else {
            renderPlanner();
        }
    }

    private PlannerEntry plannerEntryForDate(String dateKey, boolean create) {
        PlannerEntry entry = plannerEntryByDate.get(dateKey);
        if (entry == null && create) {
            entry = new PlannerEntry(dateKey);
            plannerEntries.add(entry);
            plannerEntryByDate.put(dateKey, entry);
        }
        return entry;
    }

    private void savePlannerState() {
        List<PlannerEntry> empty = new ArrayList<>();
        for (PlannerEntry entry : plannerEntries) {
            if (isBlank(entry.outfitId) && isBlank(entry.occasion) && isBlank(entry.notes) && !entry.worn) {
                empty.add(entry);
            }
        }
        plannerEntries.removeAll(empty);
        rebuildPlannerIndex();
        store.savePlannerEntries(plannerEntries);
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
        if (FILTER_FAVORITES.equals(selectedCategory) && !item.favorite) {
            return false;
        }
        boolean categoryMatches = categoryMatchesFilter(item.category, selectedCategory);
        return categoryMatches && item.matches(searchQuery, "All");
    }

    private boolean categoryMatchesFilter(String itemCategory, String filter) {
        if (isBlank(filter) || "All".equalsIgnoreCase(filter)) return true;
        if (FILTER_FAVORITES.equals(filter)) return true;
        if (!isBlank(itemCategory) && filter.equalsIgnoreCase(itemCategory)) return true;
        return filter.equalsIgnoreCase(PromptBuilder.categoryGroup(itemCategory));
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
            if (matchesClothingFilter(item)) {
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
        addFilterSection(list, dialog, "Attributes", clothingAttributeFilters());
        addFilterSection(list, dialog, "Groups", clothingFilterGroups());
        addFilterSection(list, dialog, "Categories", clothingFilterCategories());

        showBottomSheetDialog(dialog, sheet);
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
            row.addView(checkIcon(primary), new LinearLayout.LayoutParams(dp(32), dp(40)));
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

    private List<String> clothingAttributeFilters() {
        List<String> filters = new ArrayList<>();
        filters.add(FILTER_FAVORITES);
        return filters;
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
        if (FILTER_FAVORITES.equals(option)) return R.drawable.ic_favorite;
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

        FrameLayout imageFrame = new FrameLayout(this);
        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setBackgroundColor(Color.TRANSPARENT);
        image.setPadding(dp(6), dp(6), dp(6), dp(6));
        imageFrame.addView(image, new FrameLayout.LayoutParams(-1, -1));

        ImageView heart = iconView(R.drawable.ic_favorite);
        heart.setColorFilter(muted);
        heart.setBackground(oval(Color.argb(210, 255, 255, 255), Color.TRANSPARENT, 0));
        heart.setPadding(dp(7), dp(7), dp(7), dp(7));
        FrameLayout.LayoutParams heartParams = new FrameLayout.LayoutParams(dp(32), dp(32), Gravity.TOP | Gravity.RIGHT);
        heartParams.setMargins(0, dp(6), dp(6), 0);
        imageFrame.addView(heart, heartParams);

        tile.setTag(new ClothingTileHolder(image, heart));
        tile.addView(imageFrame, new LinearLayout.LayoutParams(-1, 0, 1));
        bindClothingTile(tile, item);
        return tile;
    }

    private void bindClothingTile(LinearLayout tile, ClothingItem item) {
        Object holderObject = tile.getTag();
        if (!(holderObject instanceof ClothingTileHolder)) return;
        ClothingTileHolder holder = (ClothingTileHolder) holderObject;
        ImageView image = holder.image;
        holder.favorite.setVisibility(item.favorite ? View.VISIBLE : View.GONE);
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
        detailSwipeItem = item;
        updateBottomNavigation();
        renderTopBar(value(item.category, value(item.name, "Details")), true, null, false);
        topBar.addView(transparentIconButton(R.drawable.ic_delete, error, v -> confirmDeleteItem(item)), new LinearLayout.LayoutParams(dp(48), dp(48)));
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
        content.addView(favoriteToggleRow(item));
        View sizeWarning = sizeWarningCard(item);
        if (sizeWarning != null) {
            content.addView(sizeWarning);
        }

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
        details.addView(detailRow(R.drawable.ic_info, "Damage", value(item.damage, "No damage marked"), v -> editDamageField(item)));
        details.addView(detailRow(R.drawable.ic_detail_notes, "Notes", value(item.notes, "Add a note..."), v -> editTextField("Notes", item.notes, value -> item.notes = value, item)));
        details.addView(detailRow(R.drawable.ic_detail_link, "Link", value(item.link, "Add link"), v -> editLinkField(item)));
        details.addView(detailRow(R.drawable.ic_detail_size, "Fit", sizeRowValue(item), v -> editTextField("Size", item.size, value -> item.size = value, item)));
        if (isPantsItem(item)) {
            details.addView(detailRow(R.drawable.ic_detail_waist, "Waist", pantsPartRowValue(item.waist, savedPantsWaist), v -> editTextField("Waist (W)", item.waist, value -> item.waist = value, item)));
            details.addView(detailRow(R.drawable.ic_detail_length, "Length", pantsPartRowValue(item.length, savedPantsLength), v -> editTextField("Length (L)", item.length, value -> item.length = value, item)));
        }
        details.addView(detailRow(R.drawable.ic_detail_date, "Added", DateFormat.getDateInstance().format(item.addedAt), v -> editAddedDate(item)));
        details.addView(detailRow(R.drawable.ic_detail_season, "Season", value(item.season, "Add season"), v -> editSeasonField(item)));
        details.addView(detailRow(R.drawable.ic_detail_price, "Price", priceText(item.price), v -> editTextField("Price", item.price, value -> item.price = value, item)));
        details.addView(detailRow(R.drawable.ic_detail_care, "Care", value(item.care, "Add care instructions"), v -> editCareField(item)));
        content.addView(details);
    }

    private View sizeWarningCard(ClothingItem item) {
        SizeWarning warning = sizeWarning(item);
        if (warning == null || warning.matches) return null;
        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(14), dp(12), dp(14), dp(12));
        card.setBackground(rounded(0xFFFFFBF5, 18, 0xFFFFB86C, 1));

        ImageView icon = iconView(android.R.drawable.ic_dialog_alert);
        icon.setColorFilter(0xFFE06D00);
        card.addView(icon, new LinearLayout.LayoutParams(dp(32), dp(32)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(12), 0, 0, 0);
        copy.addView(text(warning.title, 14, 0xFFE06D00, true));
        copy.addView(text(warning.message, 12, ink, false));
        card.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.setMargins(0, dp(8), 0, dp(10));
        card.setLayoutParams(params);
        return card;
    }

    private String sizeRowValue(ClothingItem item) {
        String base = value(item.size, "Add fit or size");
        if (isPantsItem(item)) {
            return base;
        }
        SizeWarning warning = sizeWarning(item);
        if (warning == null) return base;
        return base + "\n" + warning.detail;
    }

    private String pantsPartRowValue(String itemValue, String savedValue) {
        String base = value(itemValue, "Add size");
        if (isBlank(itemValue) || isBlank(savedValue)) return base;
        int comparison = compareNumericSize(itemValue, savedValue);
        if (comparison == 0) return base + "\n" + getString(R.string.your_size) + ": " + savedValue + " · " + getString(R.string.matches_your_size);
        if (comparison > 0) return base + "\n" + getString(R.string.your_size) + ": " + savedValue + " · " + getString(R.string.may_fit_large);
        return base + "\n" + getString(R.string.your_size) + ": " + savedValue + " · " + getString(R.string.may_fit_small);
    }

    private SizeWarning sizeWarning(ClothingItem item) {
        if (item == null) return null;
        if (isPantsItem(item)) {
            int waistComparison = compareMaybeNumeric(item.waist, savedPantsWaist);
            int lengthComparison = compareMaybeNumeric(item.length, savedPantsLength);
            if (waistComparison == Integer.MIN_VALUE && lengthComparison == Integer.MIN_VALUE) return null;
            return buildSizeWarning("W" + value(item.waist, "-") + " / L" + value(item.length, "-"),
                    "W" + value(savedPantsWaist, "-") + " / L" + value(savedPantsLength, "-"), waistComparison, lengthComparison);
        }
        if (isShoeItem(item)) {
            int comparison = compareMaybeNumeric(item.size, savedShoeSize);
            if (comparison == Integer.MIN_VALUE) return null;
            return buildSizeWarning(item.size, savedShoeSize, comparison);
        }
        if (isTopItem(item)) {
            int comparison = compareMaybeLetterOrNumeric(item.size, savedShirtSize);
            if (comparison == Integer.MIN_VALUE) return null;
            return buildSizeWarning(item.size, savedShirtSize, comparison);
        }
        return null;
    }

    private SizeWarning buildSizeWarning(String itemSize, String savedSize, int comparison) {
        return buildSizeWarning(itemSize, savedSize, comparison, Integer.MIN_VALUE);
    }

    private SizeWarning buildSizeWarning(String itemSize, String savedSize, int firstComparison, int secondComparison) {
        boolean hasLarge = firstComparison > 0 || secondComparison > 0;
        boolean hasSmall = firstComparison < 0 || secondComparison < 0;
        if (hasLarge && hasSmall) {
            return new SizeWarning(getString(R.string.may_not_match),
                    "This item differs from your saved size.",
                    getString(R.string.your_size) + ": " + savedSize + " · " + getString(R.string.may_not_match), false);
        }
        if (hasLarge) {
            return new SizeWarning(getString(R.string.may_fit_large),
                    getString(R.string.your_size) + " is " + savedSize + " · This item is " + itemSize + ".",
                    getString(R.string.your_size) + ": " + savedSize + " · " + getString(R.string.may_fit_large), false);
        }
        if (hasSmall) {
            return new SizeWarning(getString(R.string.may_fit_small),
                    getString(R.string.your_size) + " is " + savedSize + " · This item is " + itemSize + ".",
                    getString(R.string.your_size) + ": " + savedSize + " · " + getString(R.string.may_fit_small), false);
        }
        return new SizeWarning(getString(R.string.matches_your_size),
                "This item matches your saved size.",
                getString(R.string.your_size) + ": " + savedSize + " · " + getString(R.string.matches_your_size), true);
    }

    private int compareMaybeLetterOrNumeric(String itemSize, String savedSize) {
        if (isBlank(itemSize) || isBlank(savedSize)) return Integer.MIN_VALUE;
        int itemRank = letterSizeRank(itemSize);
        int savedRank = letterSizeRank(savedSize);
        if (itemRank != Integer.MIN_VALUE && savedRank != Integer.MIN_VALUE) {
            return Integer.compare(itemRank, savedRank);
        }
        return compareMaybeNumeric(itemSize, savedSize);
    }

    private int compareMaybeNumeric(String itemSize, String savedSize) {
        if (isBlank(itemSize) || isBlank(savedSize)) return Integer.MIN_VALUE;
        Double itemNumber = firstNumber(itemSize);
        Double savedNumber = firstNumber(savedSize);
        if (itemNumber == null || savedNumber == null) return Integer.MIN_VALUE;
        return Double.compare(itemNumber, savedNumber);
    }

    private int compareNumericSize(String itemSize, String savedSize) {
        int comparison = compareMaybeNumeric(itemSize, savedSize);
        return comparison == Integer.MIN_VALUE ? 0 : comparison;
    }

    private int letterSizeRank(String value) {
        if (value == null) return Integer.MIN_VALUE;
        String normalized = value.trim().toUpperCase().replace(" ", "");
        if (normalized.equals("XXS")) return 0;
        if (normalized.equals("XS")) return 1;
        if (normalized.equals("S")) return 2;
        if (normalized.equals("M")) return 3;
        if (normalized.equals("L")) return 4;
        if (normalized.equals("XL")) return 5;
        if (normalized.equals("XXL") || normalized.equals("2XL")) return 6;
        if (normalized.equals("XXXL") || normalized.equals("3XL")) return 7;
        return Integer.MIN_VALUE;
    }

    private Double firstNumber(String value) {
        if (value == null) return null;
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+(?:[.,]\\d+)?)").matcher(value);
        if (!matcher.find()) return null;
        try {
            return Double.parseDouble(matcher.group(1).replace(',', '.'));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private boolean isTopItem(ClothingItem item) {
        if (item == null) return false;
        String group = PromptBuilder.categoryGroup(item.category);
        return "Tops".equals(group) || containsAny(item.category, "shirt", "top", "sweater", "hoodie", "cardigan", "polo");
    }

    private boolean isShoeItem(ClothingItem item) {
        return item != null && "Shoes".equals(PromptBuilder.categoryGroup(item.category));
    }

    private boolean containsAny(String value, String... needles) {
        if (value == null) return false;
        String lower = value.toLowerCase();
        for (String needle : needles) {
            if (lower.contains(needle)) return true;
        }
        return false;
    }

    private static class SizeWarning {
        final String title;
        final String message;
        final String detail;
        final boolean matches;

        SizeWarning(String title, String message, String detail, boolean matches) {
            this.title = title;
            this.message = message;
            this.detail = detail;
            this.matches = matches;
        }
    }

    private View favoriteToggleRow(ClothingItem item) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.LEFT | Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, dp(6));

        ImageView favorite = iconButton(item.favorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_outline, v -> {
            item.favorite = !item.favorite;
            item.updatedAt = System.currentTimeMillis();
            store.saveClothes(clothes);
            showClothingDetail(item);
        });
        favorite.setColorFilter(item.favorite ? primary : muted);
        favorite.setBackgroundColor(Color.TRANSPARENT);
        favorite.setPadding(dp(12), dp(12), dp(12), dp(12));
        row.addView(favorite, new LinearLayout.LayoutParams(dp(48), dp(48)));
        return row;
    }

    private void attachClothingDetailSwipe(View view, ClothingItem item) {
        final float[] downX = new float[1];
        final float[] downY = new float[1];
        view.setOnTouchListener((target, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downX[0] = event.getX();
                    downY[0] = event.getY();
                    return false;
                case MotionEvent.ACTION_UP:
                    float deltaX = event.getX() - downX[0];
                    float deltaY = event.getY() - downY[0];
                    if (Math.abs(deltaX) > dp(72) && Math.abs(deltaX) > Math.abs(deltaY) * 1.4f) {
                        if (moveClothingDetail(item, deltaX < 0 ? 1 : -1)) {
                            return true;
                        }
                    }
                    return false;
                default:
                    return false;
            }
        });
    }

    private boolean moveClothingDetail(ClothingItem current, int direction) {
        List<ClothingItem> items = filteredClothes();
        if (!items.contains(current)) {
            items = new ArrayList<>(clothes);
        }
        if (items.size() <= 1) {
            Toast.makeText(this, "No other clothes in this view", Toast.LENGTH_SHORT).show();
            return false;
        }
        int index = items.indexOf(current);
        if (index < 0) index = 0;
        int nextIndex = index + direction;
        if (nextIndex < 0) {
            nextIndex = items.size() - 1;
        } else if (nextIndex >= items.size()) {
            nextIndex = 0;
        }
        activePhotoIndex = 0;
        showClothingDetail(items.get(nextIndex));
        return true;
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
        sheet.addView(imageActionRow(R.drawable.ic_mask_edit, getString(R.string.edit_mask), v -> {
            showMaskEditor(item);
            dialog.dismiss();
        }));
        sheet.addView(imageActionRow(R.drawable.ic_rotate, "Rotate", v -> {
            dialog.dismiss();
            rotatePhoto(item, new ImageView(this));
            showClothingDetail(item);
        }));
        sheet.addView(imageActionRow(R.drawable.ic_crop, "Crop", v -> {
            showCropEditor(item);
            dialog.dismiss();
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

    private void showCropEditor(ClothingItem item) {
        String photoUri = selectedPhotoUri(item);
        if (isBlank(photoUri)) {
            Toast.makeText(this, "Choose a photo first", Toast.LENGTH_SHORT).show();
            return;
        }
        Bitmap source;
        try (InputStream stream = getContentResolver().openInputStream(Uri.parse(photoUri))) {
            source = BitmapFactory.decodeStream(stream);
        } catch (IOException exception) {
            Toast.makeText(this, "Could not read photo: " + exception.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }
        if (source == null) {
            Toast.makeText(this, "Could not read photo", Toast.LENGTH_LONG).show();
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        FrameLayout frame = new FrameLayout(this);
        frame.setBackgroundColor(surface);

        CropImageView cropView = new CropImageView(this, surface, primary);
        cropView.setBitmap(source);
        FrameLayout.LayoutParams cropParams = new FrameLayout.LayoutParams(-1, -1);
        cropParams.setMargins(0, dp(72), 0, dp(92));
        frame.addView(cropView, cropParams);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(12), dp(14), dp(12), dp(8));
        top.setBackgroundColor(surface);
        top.addView(iconButton(R.drawable.ic_back, v -> dialog.dismiss()), new LinearLayout.LayoutParams(dp(48), dp(48)));
        TextView title = text("Crop photo", 22, ink, true);
        title.setPadding(dp(8), 0, 0, 0);
        top.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        TextView save = text("Save", 15, primary, true);
        save.setGravity(Gravity.CENTER);
        save.setPadding(dp(12), 0, dp(12), 0);
        save.setOnClickListener(v -> saveCroppedPhoto(item, cropView, dialog));
        top.addView(save, new LinearLayout.LayoutParams(dp(72), dp(48)));
        FrameLayout.LayoutParams topParams = new FrameLayout.LayoutParams(-1, dp(72));
        topParams.gravity = Gravity.TOP;
        frame.addView(top, topParams);

        TextView hint = text("Drag the box to crop", 14, muted, false);
        hint.setGravity(Gravity.CENTER);
        hint.setPadding(dp(18), 0, dp(18), 0);
        hint.setBackground(rounded(surfaceDialog, 24, palette.outlineVariant, 1));
        FrameLayout.LayoutParams hintParams = new FrameLayout.LayoutParams(-2, dp(44));
        hintParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        hintParams.setMargins(dp(18), 0, dp(18), dp(28));
        frame.addView(hint, hintParams);

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

    private void saveCroppedPhoto(ClothingItem item, CropImageView cropView, Dialog dialog) {
        Bitmap cropped = cropView.createCroppedBitmap();
        if (cropped == null) {
            Toast.makeText(this, "Could not crop photo", Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, "Saving crop…", Toast.LENGTH_SHORT).show();
        ioExecutor.execute(() -> {
            try {
                File directory = new File(getFilesDir(), "cropped-clothes");
                if (!directory.exists() && !directory.mkdirs()) {
                    throw new IOException("Could not create image directory");
                }
                File output = new File(directory, item.id + "-cropped-" + System.currentTimeMillis() + ".png");
                try (FileOutputStream stream = new FileOutputStream(output)) {
                    cropped.compress(Bitmap.CompressFormat.PNG, 100, stream);
                }
                Uri uri = Uri.fromFile(output);
                mainHandler.post(() -> {
                    dialog.dismiss();
                    replaceSelectedPhoto(item, uri.toString());
                    item.updatedAt = System.currentTimeMillis();
                    store.saveClothes(clothes);
                    Toast.makeText(this, "Photo cropped", Toast.LENGTH_SHORT).show();
                    showClothingDetail(item);
                });
            } catch (IOException exception) {
                mainHandler.post(() -> Toast.makeText(this, "Could not save crop: " + exception.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void showMaskEditor(ClothingItem item) {
        String photoUri = selectedPhotoUri(item);
        if (isBlank(photoUri)) {
            Toast.makeText(this, "Choose a photo first", Toast.LENGTH_SHORT).show();
            return;
        }

        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        FrameLayout frame = new FrameLayout(this);
        frame.setBackgroundColor(surface);

        MaskEditorView editor = new MaskEditorView(this, surface, primary, error);
        FrameLayout.LayoutParams editorParams = new FrameLayout.LayoutParams(-1, -1);
        editorParams.setMargins(0, dp(72), 0, dp(170));
        frame.addView(editor, editorParams);

        TextView loading = text(getString(R.string.loading_photo), 15, muted, false);
        loading.setGravity(Gravity.CENTER);
        FrameLayout.LayoutParams loadingParams = new FrameLayout.LayoutParams(-1, -2, Gravity.CENTER);
        frame.addView(loading, loadingParams);

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        top.setPadding(dp(12), dp(14), dp(12), dp(8));
        top.setBackgroundColor(surface);
        top.addView(iconButton(R.drawable.ic_back, v -> dialog.dismiss()), new LinearLayout.LayoutParams(dp(48), dp(48)));
        TextView title = text(getString(R.string.edit_mask), 22, ink, true);
        title.setPadding(dp(8), 0, 0, 0);
        top.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        TextView save = text(getString(R.string.save), 15, primary, true);
        save.setGravity(Gravity.CENTER);
        save.setPadding(dp(12), 0, dp(12), 0);
        boolean[] editorReady = {false};
        save.setAlpha(0.45f);
        save.setOnClickListener(v -> {
            if (!editorReady[0]) {
                Toast.makeText(this, getString(R.string.loading_photo), Toast.LENGTH_SHORT).show();
                return;
            }
            saveMaskedPhoto(item, editor, dialog);
        });
        top.addView(save, new LinearLayout.LayoutParams(dp(72), dp(48)));
        FrameLayout.LayoutParams topParams = new FrameLayout.LayoutParams(-1, dp(72));
        topParams.gravity = Gravity.TOP;
        frame.addView(top, topParams);

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.VERTICAL);
        controls.setPadding(dp(18), dp(16), dp(18), dp(20));
        controls.setBackground(roundedTop(surfaceDialog, 32, palette.outlineVariant, 1));
        controls.setElevation(dp(3));
        controls.setAlpha(0.45f);
        setEnabledRecursive(controls, false);

        LinearLayout modeRow = new LinearLayout(this);
        TextView remove = maskToggleButton(getString(R.string.mask_remove), true);
        TextView restore = maskToggleButton(getString(R.string.mask_restore), false);
        remove.setOnClickListener(v -> {
            editor.setRestoreMode(false);
            updateMaskModeButtons(editor, remove, restore);
        });
        restore.setOnClickListener(v -> {
            editor.setRestoreMode(true);
            updateMaskModeButtons(editor, remove, restore);
        });
        modeRow.addView(remove, new LinearLayout.LayoutParams(0, dp(44), 1));
        LinearLayout.LayoutParams restoreParams = new LinearLayout.LayoutParams(0, dp(44), 1);
        restoreParams.setMargins(dp(8), 0, 0, 0);
        modeRow.addView(restore, restoreParams);
        controls.addView(modeRow);

        LinearLayout sizeRow = new LinearLayout(this);
        sizeRow.setGravity(Gravity.CENTER_VERTICAL);
        sizeRow.setPadding(0, dp(12), 0, 0);
        TextView label = text(getString(R.string.brush_size), 14, muted, false);
        sizeRow.addView(label, new LinearLayout.LayoutParams(0, -2, 1));
        TextView small = maskSizeButton(getString(R.string.brush_small), editor.brushSizeDp == 18);
        TextView medium = maskSizeButton(getString(R.string.brush_medium), editor.brushSizeDp == 32);
        TextView large = maskSizeButton(getString(R.string.brush_large), editor.brushSizeDp == 52);
        small.setOnClickListener(v -> {
            editor.setBrushSizeDp(18);
            updateMaskSizeButtons(editor, small, medium, large);
        });
        medium.setOnClickListener(v -> {
            editor.setBrushSizeDp(32);
            updateMaskSizeButtons(editor, small, medium, large);
        });
        large.setOnClickListener(v -> {
            editor.setBrushSizeDp(52);
            updateMaskSizeButtons(editor, small, medium, large);
        });
        sizeRow.addView(small, new LinearLayout.LayoutParams(dp(42), dp(38)));
        LinearLayout.LayoutParams mediumParams = new LinearLayout.LayoutParams(dp(42), dp(38));
        mediumParams.setMargins(dp(6), 0, 0, 0);
        sizeRow.addView(medium, mediumParams);
        LinearLayout.LayoutParams largeParams = new LinearLayout.LayoutParams(dp(42), dp(38));
        largeParams.setMargins(dp(6), 0, 0, 0);
        sizeRow.addView(large, largeParams);
        controls.addView(sizeRow);

        LinearLayout actionRow = new LinearLayout(this);
        actionRow.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        actionRow.setPadding(0, dp(10), 0, 0);
        actionRow.addView(imageActionIcon(R.drawable.ic_undo, getString(R.string.undo), v -> editor.undo()), new LinearLayout.LayoutParams(dp(48), dp(48)));
        LinearLayout.LayoutParams resetParams = new LinearLayout.LayoutParams(dp(48), dp(48));
        resetParams.setMargins(dp(8), 0, 0, 0);
        actionRow.addView(imageActionIcon(R.drawable.ic_reset, getString(R.string.reset), v -> editor.resetMask()), resetParams);
        controls.addView(actionRow);

        FrameLayout.LayoutParams controlParams = new FrameLayout.LayoutParams(-1, -2);
        controlParams.gravity = Gravity.BOTTOM;
        frame.addView(controls, controlParams);

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
        imageExecutor.execute(() -> {
            try (InputStream stream = getContentResolver().openInputStream(Uri.parse(photoUri))) {
                Bitmap loaded = BitmapFactory.decodeStream(stream);
                if (loaded == null) {
                    throw new IOException("Could not read photo");
                }
                if (loaded.getConfig() != Bitmap.Config.ARGB_8888) {
                    loaded = loaded.copy(Bitmap.Config.ARGB_8888, false);
                }
                boolean hasTransparency = MaskEditorView.bitmapHasTransparentPixels(loaded);
                Bitmap readyBitmap = loaded;
                mainHandler.post(() -> {
                    if (!dialog.isShowing()) return;
                    editor.setBitmap(readyBitmap, hasTransparency);
                    loading.setVisibility(View.GONE);
                    editorReady[0] = true;
                    save.setAlpha(1f);
                    controls.setAlpha(1f);
                    setEnabledRecursive(controls, true);
                    if (!hasTransparency) {
                        initializeMaskFromSegmentation(photoUri, editor);
                    }
                });
            } catch (IOException exception) {
                mainHandler.post(() -> {
                    if (dialog.isShowing()) {
                        Toast.makeText(this, "Could not read photo: " + exception.getMessage(), Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    private void setEnabledRecursive(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                setEnabledRecursive(group.getChildAt(i), enabled);
            }
        }
    }

    private TextView maskToggleButton(String label, boolean active) {
        TextView button = text(label, 14, active ? onPrimary : primary, true);
        button.setGravity(Gravity.CENTER);
        button.setBackground(rounded(active ? primary : Color.TRANSPARENT, 22, active ? Color.TRANSPARENT : outline, 1));
        return button;
    }

    private TextView maskSizeButton(String label, boolean active) {
        TextView button = text(label, 13, active ? onPrimaryContainer : primary, true);
        button.setGravity(Gravity.CENTER);
        button.setBackground(rounded(active ? primaryContainer : Color.TRANSPARENT, 19, active ? Color.TRANSPARENT : outline, 1));
        return button;
    }

    private ImageView imageActionIcon(int iconRes, String description, View.OnClickListener listener) {
        ImageView icon = transparentIconButton(iconRes, primary, listener);
        icon.setContentDescription(description);
        return icon;
    }

    private void updateMaskModeButtons(MaskEditorView editor, TextView remove, TextView restore) {
        boolean restoring = editor.isRestoreMode();
        remove.setTextColor(restoring ? primary : onPrimary);
        restore.setTextColor(restoring ? onPrimary : primary);
        remove.setBackground(rounded(restoring ? Color.TRANSPARENT : primary, 22, restoring ? outline : Color.TRANSPARENT, 1));
        restore.setBackground(rounded(restoring ? primary : Color.TRANSPARENT, 22, restoring ? Color.TRANSPARENT : outline, 1));
    }

    private void updateMaskSizeButtons(MaskEditorView editor, TextView small, TextView medium, TextView large) {
        TextView[] buttons = {small, medium, large};
        int[] sizes = {18, 32, 52};
        for (int i = 0; i < buttons.length; i++) {
            boolean active = editor.brushSizeDp == sizes[i];
            buttons[i].setTextColor(active ? onPrimaryContainer : primary);
            buttons[i].setBackground(rounded(active ? primaryContainer : Color.TRANSPARENT, 19, active ? Color.TRANSPARENT : outline, 1));
        }
    }

    private void initializeMaskFromSegmentation(String photoUri, MaskEditorView editor) {
        Toast.makeText(this, getString(R.string.preparing_mask), Toast.LENGTH_SHORT).show();
        try {
            InputImage image = InputImage.fromFilePath(this, Uri.parse(photoUri));
            SubjectSegmenterOptions options = new SubjectSegmenterOptions.Builder()
                    .enableForegroundBitmap()
                    .build();
            SubjectSegmenter segmenter = SubjectSegmentation.getClient(options);
            segmenter.process(image)
                    .addOnSuccessListener(result -> {
                        Bitmap foreground = result.getForegroundBitmap();
                        if (foreground != null) {
                            editor.setMaskFromAlpha(foreground);
                        }
                    })
                    .addOnFailureListener(error -> Toast.makeText(this, getString(R.string.mask_auto_failed), Toast.LENGTH_SHORT).show());
        } catch (IOException exception) {
            Toast.makeText(this, getString(R.string.mask_auto_failed), Toast.LENGTH_SHORT).show();
        }
    }

    private void saveMaskedPhoto(ClothingItem item, MaskEditorView editor, Dialog dialog) {
        Bitmap masked = editor.createMaskedBitmap();
        if (masked == null) {
            Toast.makeText(this, getString(R.string.mask_save_failed), Toast.LENGTH_LONG).show();
            return;
        }
        Toast.makeText(this, getString(R.string.saving_mask), Toast.LENGTH_SHORT).show();
        ioExecutor.execute(() -> {
            try {
                File directory = new File(getFilesDir(), "masked-clothes");
                if (!directory.exists() && !directory.mkdirs()) {
                    throw new IOException("Could not create image directory");
                }
                File output = new File(directory, item.id + "-masked-" + System.currentTimeMillis() + ".png");
                try (FileOutputStream stream = new FileOutputStream(output)) {
                    masked.compress(Bitmap.CompressFormat.PNG, 100, stream);
                }
                Uri uri = Uri.fromFile(output);
                mainHandler.post(() -> {
                    dialog.dismiss();
                    replaceSelectedPhoto(item, uri.toString());
                    item.updatedAt = System.currentTimeMillis();
                    store.saveClothes(clothes);
                    Toast.makeText(this, getString(R.string.mask_saved), Toast.LENGTH_SHORT).show();
                    showClothingDetail(item);
                });
            } catch (IOException exception) {
                mainHandler.post(() -> Toast.makeText(this, getString(R.string.mask_save_failed) + ": " + exception.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
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

    private void editDamageField(ClothingItem item) {
        showDamagePicker(item.damage, value -> item.damage = value, () -> {
            item.updatedAt = System.currentTimeMillis();
            store.saveClothes(clothes);
            showClothingDetail(item);
        });
    }

    private void editCareField(ClothingItem item) {
        showCarePicker(item.care, value -> item.care = value, () -> {
            item.updatedAt = System.currentTimeMillis();
            store.saveClothes(clothes);
            showClothingDetail(item);
        });
    }

    private void showCarePicker(String currentValue, FieldUpdater updater, Runnable afterSave) {
        Set<String> selected = selectedCareIds(currentValue);
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(20), dp(14), dp(20), dp(16));
        sheet.setBackground(rounded(surfaceDialog, 32, Color.TRANSPARENT, 0));

        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(-1, dp(30));
        handleParams.setMargins(0, 0, 0, dp(6));
        sheet.addView(sheetDragHandle(dialog, sheet), handleParams);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout titleCopy = new LinearLayout(this);
        titleCopy.setOrientation(LinearLayout.VERTICAL);
        titleCopy.addView(text("Care instructions", 22, ink, true));
        titleCopy.addView(text("Select all that apply", 14, muted, false));
        header.addView(titleCopy, new LinearLayout.LayoutParams(0, -2, 1));
        TextView clear = text("Clear all", 14, primary, true);
        clear.setGravity(Gravity.CENTER);
        header.addView(clear, new LinearLayout.LayoutParams(dp(88), dp(44)));
        sheet.addView(header);

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        sheet.addView(scroll, new LinearLayout.LayoutParams(-1, dp(520)));

        final Runnable[] render = new Runnable[1];
        render[0] = () -> renderCareSections(list, selected, render[0]);
        clear.setOnClickListener(v -> {
            selected.clear();
            render[0].run();
        });
        render[0].run();

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setPadding(0, dp(14), 0, 0);
        TextView count = text("Selected symbols", 13, muted, false);
        actions.addView(count, new LinearLayout.LayoutParams(0, dp(48), 1));
        Button cancel = textButton("Cancel");
        cancel.setOnClickListener(v -> dialog.dismiss());
        actions.addView(cancel, new LinearLayout.LayoutParams(dp(112), dp(48)));
        Button done = filledButton("Save");
        done.setOnClickListener(v -> {
            dialog.dismiss();
            updater.update(careValue(selected));
            afterSave.run();
        });
        LinearLayout.LayoutParams doneParams = new LinearLayout.LayoutParams(dp(132), dp(48));
        doneParams.setMargins(dp(12), 0, 0, 0);
        actions.addView(done, doneParams);
        sheet.addView(actions);

        showBottomSheetDialog(dialog, sheet);
    }

    private void renderCareSections(LinearLayout list, Set<String> selected, Runnable render) {
        list.removeAllViews();
        addCareSection(list, "1. Washing", CARE_WASHING_OPTIONS, selected, render);
        addCareSection(list, "2. Drying", CARE_DRYING_OPTIONS, selected, render);
        addCareSection(list, "3. Ironing", CARE_IRONING_OPTIONS, selected, render);
        addCareSection(list, "4. Professional cleaning", CARE_PROFESSIONAL_OPTIONS, selected, render);
        addCareSection(list, "5. Bleaching", CARE_BLEACHING_OPTIONS, selected, render);
    }

    private void addCareSection(LinearLayout list, String title, CareOption[] options, Set<String> selected, Runnable render) {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        section.setPadding(dp(12), dp(12), dp(12), dp(8));
        section.setBackground(rounded(Color.WHITE, 16, palette.outlineVariant, 1));
        LinearLayout.LayoutParams sectionParams = new LinearLayout.LayoutParams(-1, -2);
        sectionParams.setMargins(0, dp(12), 0, 0);
        list.addView(section, sectionParams);

        TextView heading = text(title, 16, ink, true);
        heading.setPadding(dp(2), 0, dp(2), dp(10));
        section.addView(heading);

        LinearLayout row = null;
        for (int i = 0; i < options.length; i++) {
            if (i % 3 == 0) {
                row = new LinearLayout(this);
                row.setOrientation(LinearLayout.HORIZONTAL);
                section.addView(row, new LinearLayout.LayoutParams(-1, -2));
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(112), 1);
            params.setMargins(dp(3), dp(3), dp(3), dp(7));
            row.addView(careOptionTile(options[i], selected, render), params);
        }
        if (row != null && options.length % 3 != 0) {
            for (int i = options.length % 3; i < 3; i++) {
                TextView spacer = new TextView(this);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(112), 1);
                params.setMargins(dp(3), dp(3), dp(3), dp(7));
                row.addView(spacer, params);
            }
        }
    }

    private View careOptionTile(CareOption option, Set<String> selected, Runnable render) {
        boolean isSelected = selected.contains(option.id);
        LinearLayout tile = new LinearLayout(this);
        tile.setOrientation(LinearLayout.VERTICAL);
        tile.setGravity(Gravity.CENTER);
        tile.setPadding(dp(6), dp(8), dp(6), dp(6));
        tile.setBackground(rounded(isSelected ? palette.tonalSurface() : Color.WHITE, 12,
                isSelected ? primary : palette.outlineVariant, isSelected ? 2 : 1));
        tile.setOnClickListener(v -> {
            if (selected.contains(option.id)) {
                selected.remove(option.id);
            } else {
                selected.add(option.id);
            }
            render.run();
        });

        ImageView icon = new ImageView(this);
        icon.setImageResource(option.iconRes);
        icon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        tile.addView(icon, new LinearLayout.LayoutParams(dp(48), dp(48)));

        TextView label = text(option.label, 11, ink, isSelected);
        label.setGravity(Gravity.CENTER);
        label.setMaxLines(2);
        label.setPadding(0, dp(7), 0, 0);
        tile.addView(label, new LinearLayout.LayoutParams(-1, 0, 1));
        return tile;
    }

    private Set<String> selectedCareIds(String value) {
        Set<String> selected = new LinkedHashSet<>();
        if (isBlank(value)) return selected;
        String lower = value.toLowerCase(Locale.ROOT);
        for (CareOption option : allCareOptions()) {
            if (lower.contains(option.id.toLowerCase(Locale.ROOT)) || lower.contains(option.label.toLowerCase(Locale.ROOT))) {
                selected.add(option.id);
            }
        }
        return selected;
    }

    private String careValue(Set<String> selected) {
        if (selected == null || selected.isEmpty()) return "";
        StringBuilder value = new StringBuilder();
        for (CareOption option : allCareOptions()) {
            if (!selected.contains(option.id)) continue;
            if (value.length() > 0) value.append(" · ");
            value.append(option.label);
        }
        return value.toString();
    }

    private String careSelectionText(Set<String> selected) {
        int count = selected == null ? 0 : selected.size();
        return count + (count == 1 ? " selected" : " selected");
    }

    private List<CareOption> allCareOptions() {
        List<CareOption> options = new ArrayList<>();
        Collections.addAll(options, CARE_WASHING_OPTIONS);
        Collections.addAll(options, CARE_DRYING_OPTIONS);
        Collections.addAll(options, CARE_IRONING_OPTIONS);
        Collections.addAll(options, CARE_PROFESSIONAL_OPTIONS);
        Collections.addAll(options, CARE_BLEACHING_OPTIONS);
        return options;
    }

    private void showDamagePicker(String currentValue, FieldUpdater updater, Runnable afterSave) {
        Set<String> selected = selectedDamageTypes(currentValue);
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);

        final Runnable[] render = new Runnable[1];
        render[0] = () -> renderDamageOptions(body, selected, render[0]);
        render[0].run();

        showMaterialDialog("Damage", body, "Cancel", "Save", null, () -> {
            updater.update(damageValue(selected));
            afterSave.run();
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

        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(-1, dp(30));
        handleParams.setMargins(0, 0, 0, dp(6));
        sheet.addView(sheetDragHandle(dialog, sheet), handleParams);

        EditText search = input("Search material");
        sheet.addView(search, new LinearLayout.LayoutParams(-1, dp(52)));

        ScrollView scroll = new ScrollView(this);
        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        scroll.addView(list);
        LinearLayout.LayoutParams scrollParams = new LinearLayout.LayoutParams(-1, dp(448));
        scrollParams.setMargins(0, dp(8), 0, 0);
        sheet.addView(scroll, scrollParams);

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

        showBottomSheetDialog(dialog, sheet);
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
        row.setPadding(dp(8), 0, dp(8), 0);
        row.setMinimumHeight(dp(56));

        TextView swatch = new TextView(this);
        swatch.setBackground(rounded(option.color, 9, palette.outlineVariant, 1));
        row.addView(swatch, new LinearLayout.LayoutParams(dp(40), dp(40)));

        TextView label = text(option.name, 18, palette.rowText, false);
        label.setPadding(dp(14), 0, 0, 0);
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
        LinearLayout.LayoutParams percentParams = new LinearLayout.LayoutParams(dp(58), dp(40));
        percentParams.setMargins(dp(8), 0, dp(10), 0);
        row.addView(percent, percentParams);

        ImageView check = circularCheckIcon(isSelected, primary, onPrimary, outline, 2);
        row.addView(check, new LinearLayout.LayoutParams(dp(30), dp(30)));

        row.setOnClickListener(v -> {
            if (selected.containsKey(option.name)) {
                selected.remove(option.name);
                percent.setVisibility(View.GONE);
                percent.setText("");
                updateCircularCheckIcon(check, false, primary, onPrimary, outline, 2);
            } else {
                selected.put(option.name, "100");
                percent.setText("100");
                percent.setVisibility(View.VISIBLE);
                updateCircularCheckIcon(check, true, primary, onPrimary, outline, 2);
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
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(24), dp(14), dp(24), dp(22));
        sheet.setBackground(rounded(surfaceDialog, 32, Color.TRANSPARENT, 0));

        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(-1, dp(30));
        handleParams.setMargins(0, 0, 0, dp(8));
        sheet.addView(sheetDragHandle(dialog, sheet), handleParams);

        TextView title = text("Season", 26, ink, true);
        sheet.addView(title);
        TextView subtitle = text("When is this item in your wardrobe?", 16, muted, false);
        subtitle.setPadding(0, dp(8), 0, dp(22));
        sheet.addView(subtitle);

        LinearLayout options = new LinearLayout(this);
        options.setOrientation(LinearLayout.VERTICAL);
        sheet.addView(options, new LinearLayout.LayoutParams(-1, -2));

        final Runnable[] render = new Runnable[1];
        render[0] = () -> renderSeasonPickerOptions(options, selected, render[0]);
        render[0].run();

        LinearLayout actions = new LinearLayout(this);
        actions.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);
        actions.setPadding(0, dp(22), 0, 0);
        Button cancel = textButton("Cancel");
        cancel.setOnClickListener(v -> dialog.dismiss());
        actions.addView(cancel, new LinearLayout.LayoutParams(dp(124), dp(48)));
        Button save = filledButton("Save");
        save.setOnClickListener(v -> {
            dialog.dismiss();
            updater.update(seasonValue(selected));
            afterSave.run();
        });
        LinearLayout.LayoutParams saveParams = new LinearLayout.LayoutParams(0, dp(48), 1);
        saveParams.setMargins(dp(14), 0, 0, 0);
        actions.addView(save, saveParams);
        sheet.addView(actions);

        showBottomSheetDialog(dialog, sheet);
    }

    private void renderSeasonPickerOptions(LinearLayout container, Set<String> selected, Runnable render) {
        container.removeAllViews();
        container.addView(allSeasonsCard(selected, render), new LinearLayout.LayoutParams(-1, dp(92)));

        LinearLayout firstRow = new LinearLayout(this);
        firstRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams firstRowParams = new LinearLayout.LayoutParams(-1, dp(120));
        firstRowParams.setMargins(0, dp(14), 0, 0);
        container.addView(firstRow, firstRowParams);
        addSeasonGridCard(firstRow, SEASON_OPTIONS[0], selected, render, true);
        addSeasonGridCard(firstRow, SEASON_OPTIONS[1], selected, render, false);

        LinearLayout secondRow = new LinearLayout(this);
        secondRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams secondRowParams = new LinearLayout.LayoutParams(-1, dp(120));
        secondRowParams.setMargins(0, dp(12), 0, 0);
        container.addView(secondRow, secondRowParams);
        addSeasonGridCard(secondRow, SEASON_OPTIONS[2], selected, render, true);
        addSeasonGridCard(secondRow, SEASON_OPTIONS[3], selected, render, false);
    }

    private View allSeasonsCard(Set<String> selected, Runnable render) {
        boolean active = selected.isEmpty();
        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(12), dp(16), dp(12));
        card.setBackground(rounded(active ? palette.tonalSurface() : Color.WHITE, 20,
                active ? primary : palette.outlineVariant, 1));
        card.setOnClickListener(v -> {
            selected.clear();
            render.run();
        });

        ImageView icon = iconView(R.drawable.ic_detail_date);
        icon.setColorFilter(primary);
        icon.setPadding(dp(10), dp(10), dp(10), dp(10));
        icon.setBackground(oval(Color.WHITE, Color.TRANSPARENT, 0));
        card.addView(icon, new LinearLayout.LayoutParams(dp(58), dp(58)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(16), 0, 0, 0);
        copy.addView(text("All seasons", 17, ink, true));
        copy.addView(text("Wear it all year round", 14, muted, false));
        card.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        card.addView(seasonCheck(active), new LinearLayout.LayoutParams(dp(38), dp(38)));
        return card;
    }

    private void addSeasonGridCard(LinearLayout row, String option, Set<String> selected, Runnable render, boolean left) {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, -1, 1);
        params.setMargins(left ? 0 : dp(8), 0, left ? dp(8) : 0, 0);
        row.addView(seasonGridCard(option, selected, render), params);
    }

    private View seasonGridCard(String option, Set<String> selected, Runnable render) {
        boolean active = selected.contains(option);
        FrameLayout frame = new FrameLayout(this);
        frame.setBackground(rounded(active ? seasonActiveColor(option) : seasonCardColor(option), 18,
                active ? primary : seasonBorderColor(option), 1));
        frame.setOnClickListener(v -> {
            if (selected.contains(option)) {
                selected.remove(option);
            } else {
                selected.add(option);
            }
            render.run();
        });

        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(12), dp(12), dp(42), dp(12));
        frame.addView(card, new FrameLayout.LayoutParams(-1, -1));

        TextView art = text(seasonArt(option), 28, ink, false);
        art.setGravity(Gravity.CENTER);
        card.addView(art, new LinearLayout.LayoutParams(dp(50), -1));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(8), 0, 0, 0);
        TextView title = text(seasonLabelWithoutIcon(option), 15, ink, true);
        title.setSingleLine(true);
        copy.addView(title);
        TextView body = text(seasonSubtitle(option), 12, muted, false);
        body.setMaxLines(2);
        copy.addView(body);
        card.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));

        FrameLayout.LayoutParams checkParams = new FrameLayout.LayoutParams(dp(30), dp(30), Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        checkParams.setMargins(0, 0, dp(12), 0);
        frame.addView(seasonCheck(active), checkParams);
        return frame;
    }

    private ImageView seasonCheck(boolean active) {
        return circularCheckIcon(active, primary, Color.WHITE, outline, 2);
    }

    private String seasonArt(String option) {
        String name = seasonLabelWithoutIcon(option).toLowerCase(Locale.ROOT);
        if (name.contains("spring")) return "🌿";
        if (name.contains("summer")) return "☀";
        if (name.contains("autumn")) return "🍂";
        if (name.contains("winter")) return "❄";
        return "•";
    }

    private String seasonSubtitle(String option) {
        String name = seasonLabelWithoutIcon(option).toLowerCase(Locale.ROOT);
        if (name.contains("spring")) return "Light layers\nfresh days";
        if (name.contains("summer")) return "Warm days\nlight & breathable";
        if (name.contains("autumn")) return "Cool days\ncozy layers";
        if (name.contains("winter")) return "Cold days\nwarm & insulated";
        return "";
    }

    private int seasonCardColor(String option) {
        String name = seasonLabelWithoutIcon(option).toLowerCase(Locale.ROOT);
        if (name.contains("spring")) return 0xFFF4FAF2;
        if (name.contains("summer")) return 0xFFFFF8EA;
        if (name.contains("autumn")) return 0xFFFFF3EA;
        if (name.contains("winter")) return 0xFFEFF8FF;
        return Color.WHITE;
    }

    private int seasonActiveColor(String option) {
        String name = seasonLabelWithoutIcon(option).toLowerCase(Locale.ROOT);
        if (name.contains("spring")) return 0xFFEAF5E8;
        if (name.contains("summer")) return 0xFFFFF0CC;
        if (name.contains("autumn")) return 0xFFFFE4D1;
        if (name.contains("winter")) return 0xFFDDF0FF;
        return palette.tonalSurface();
    }

    private int seasonBorderColor(String option) {
        String name = seasonLabelWithoutIcon(option).toLowerCase(Locale.ROOT);
        if (name.contains("spring")) return 0xFFDCE9D8;
        if (name.contains("summer")) return 0xFFEFE2C6;
        if (name.contains("autumn")) return 0xFFEED5C2;
        if (name.contains("winter")) return 0xFFD2E5F3;
        return palette.outlineVariant;
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

        showBottomSheetDialog(dialog, sheet);
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

        row.addView(circularCheckIcon(selected, primary, onPrimary, outline, 2), new LinearLayout.LayoutParams(dp(34), dp(34)));
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

    private String seasonLabelWithoutIcon(String season) {
        if (isBlank(season)) return "";
        String trimmed = season.trim();
        return trimmed.length() > 2 ? trimmed.substring(2).trim() : trimmed;
    }

    private void renderDamageOptions(LinearLayout body, Set<String> selected, Runnable render) {
        body.removeAllViews();
        body.addView(damageChoiceRow("No damage", selected.isEmpty(), v -> {
            selected.clear();
            render.run();
        }));
        for (String option : DAMAGE_OPTIONS) {
            body.addView(damageChoiceRow(option, selected.contains(option), v -> {
                if (selected.contains(option)) {
                    selected.remove(option);
                } else {
                    selected.add(option);
                }
                render.run();
            }));
        }
    }

    private View damageChoiceRow(String label, boolean selected, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(56));
        row.setPadding(dp(8), 0, dp(8), 0);
        row.setOnClickListener(listener);

        TextView copy = text(label, 16, ink, selected);
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(circularCheckIcon(selected, primary, onPrimary, outline, 2), new LinearLayout.LayoutParams(dp(30), dp(30)));
        return row;
    }

    private Set<String> selectedDamageTypes(String value) {
        Set<String> selected = new LinkedHashSet<>();
        if (isBlank(value)) return selected;
        String lower = value.toLowerCase();
        for (String option : DAMAGE_OPTIONS) {
            if (lower.contains(option.toLowerCase())) {
                selected.add(option);
            }
        }
        return selected;
    }

    private String damageValue(Set<String> selected) {
        if (selected == null || selected.isEmpty()) return "";
        StringBuilder value = new StringBuilder();
        for (String damage : selected) {
            if (value.length() > 0) value.append(" · ");
            value.append(damage);
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
            copy.addView(colorPreviewRow(item.color, dp(22)));
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
        showColorPicker(currentValue, CLOTHING_COLORS, updater, afterSave);
    }

    private void showProfileColorPicker(String title, String currentValue, ColorOption[] options, FieldUpdater updater) {
        showColorPicker(currentValue, options, value -> {
            updater.update(value);
            renderAiProfile();
        }, () -> { });
    }

    private void showColorPicker(String currentValue, ColorOption[] options, FieldUpdater updater, Runnable afterSave) {
        Set<String> selected = selectedColorNames(currentValue, options);
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(24), dp(14), dp(24), dp(22));
        sheet.setBackground(rounded(surfaceDialog, 32, Color.TRANSPARENT, 0));

        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(-1, dp(30));
        handleParams.setMargins(0, 0, 0, dp(6));
        sheet.addView(sheetDragHandle(dialog, sheet), handleParams);

        List<FrameLayout> swatches = new ArrayList<>();
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        for (int rowIndex = 0; rowIndex < Math.ceil(options.length / 6f); rowIndex++) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER);
            row.setOrientation(LinearLayout.HORIZONTAL);
            for (int col = 0; col < 6; col++) {
                int index = rowIndex * 6 + col;
                if (index >= options.length) {
                    TextView spacer = new TextView(this);
                    row.addView(spacer, new LinearLayout.LayoutParams(0, dp(54), 1));
                    continue;
                }
                ColorOption option = options[index];
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

        showBottomSheetDialog(dialog, sheet);
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
        row.setMinimumHeight(size + dp(8));
        row.setClipChildren(false);
        row.setClipToPadding(false);
        Set<String> selected = selectedColorNames(value);
        int shown = 0;
        for (ColorOption option : CLOTHING_COLORS) {
            if (!selected.contains(option.name)) continue;
            FrameLayout cell = new FrameLayout(this);
            cell.setClipChildren(false);
            cell.setClipToPadding(false);
            TextView dot = new TextView(this);
            int strokeColor = option.color == Color.WHITE || option.color == 0xFFFCFAEA ? outline : Color.TRANSPARENT;
            dot.setBackground(oval(option.color, strokeColor, strokeColor == Color.TRANSPARENT ? 0 : 1));
            FrameLayout.LayoutParams dotParams = new FrameLayout.LayoutParams(size, size, Gravity.CENTER);
            cell.addView(dot, dotParams);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size + dp(8), size + dp(8));
            params.setMargins(0, 0, dp(3), 0);
            row.addView(cell, params);
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
        return selectedColorNames(value, CLOTHING_COLORS);
    }

    private Set<String> selectedColorNames(String value, ColorOption[] options) {
        Set<String> selected = new LinkedHashSet<>();
        if (isBlank(value)) return selected;
        String normalized = value.replace(",", "·").replace("/", "·");
        String[] parts = normalized.split("·");
        for (String part : parts) {
            String token = part.trim();
            for (ColorOption option : options) {
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

    private String currentSeasonName() {
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        if (month >= Calendar.MARCH && month <= Calendar.MAY) {
            return "Spring";
        }
        if (month >= Calendar.JUNE && month <= Calendar.AUGUST) {
            return "Summer";
        }
        if (month >= Calendar.SEPTEMBER && month <= Calendar.NOVEMBER) {
            return "Autumn";
        }
        return "Winter";
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
        for (CategoryOption option : categoryPickerOptions()) {
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

    private List<CategoryOption> categoryPickerOptions() {
        List<CategoryOption> options = new ArrayList<>();
        Set<String> labels = new LinkedHashSet<>();
        for (CategoryOption option : CATEGORY_OPTIONS) {
            options.add(option);
            labels.add(option.label.toLowerCase(Locale.ROOT));
        }
        for (String category : categories) {
            if (isBlank(category)) continue;
            if (basicClothingFilters().contains(category) || clothingFilterGroups().contains(category)) continue;
            String key = category.toLowerCase(Locale.ROOT);
            if (labels.contains(key)) continue;
            options.add(new CategoryOption(getString(R.string.custom_categories), category, filterDrawable(category)));
            labels.add(key);
        }
        return options;
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
            row.addView(checkIcon(primary), new LinearLayout.LayoutParams(dp(32), dp(40)));
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
        details.addView(detailRow(R.drawable.ic_info, "Damage", value(draft.damage, "No damage marked"), v -> editDraftDamageField(draft, isNew)));
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
        details.addView(detailRow(R.drawable.ic_detail_care, "Care", value(draft.care, "Add care instructions"), v -> editDraftCareField(draft, isNew)));
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

    private void editDraftDamageField(ClothingItem draft, boolean isNew) {
        showDamagePicker(draft.damage, value -> draft.damage = value, () -> {
            draft.updatedAt = System.currentTimeMillis();
            renderClothingForm(draft, isNew);
        });
    }

    private void editDraftCareField(ClothingItem draft, boolean isNew) {
        showCarePicker(draft.care, value -> draft.care = value, () -> {
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
            copy.addView(colorPreviewRow(draft.color, dp(22)));
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
        updateBottomNavigation();
        useScrollContent();
        content.removeAllViews();
        content.setPadding(dp(22), dp(2), dp(22), dp(28));

        if (clothes.isEmpty()) {
            content.addView(emptyState("Add clothing items before creating outfits."));
            return;
        }
        content.addView(outfitOverviewFilters());
        if (outfits.isEmpty()) {
            content.addView(emptyState("No outfits yet."));
            return;
        }

        List<Outfit> visibleOutfits = filteredOutfits();
        LinearLayout summary = new LinearLayout(this);
        summary.setGravity(Gravity.CENTER_VERTICAL);
        summary.setPadding(0, dp(8), 0, dp(10));
        summary.addView(text(visibleOutfits.size() + (visibleOutfits.size() == 1 ? " outfit" : " outfits"), 14, muted, false), new LinearLayout.LayoutParams(0, -2, 1));
        Button create = outlinedButton("+  Create outfit");
        create.setOnClickListener(v -> renderOutfitForm(null));
        summary.addView(create, new LinearLayout.LayoutParams(dp(154), dp(44)));
        content.addView(summary);

        if (visibleOutfits.isEmpty()) {
            content.addView(emptyState("No outfits match this filter."));
            return;
        }
        for (Outfit outfit : visibleOutfits) {
            content.addView(outfitCard(outfit));
        }
    }

    private View outfitOverviewFilters() {
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(2), 0, dp(8));
        for (String option : outfitOverviewFilterOptions()) {
            Button chip = chip(option, option.equals(selectedOutfitFilter));
            chip.setOnClickListener(v -> {
                selectedOutfitFilter = option;
                renderOutfits();
            });
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, dp(38));
            params.setMargins(0, 0, dp(8), 0);
            row.addView(chip, params);
        }
        scroll.addView(row);
        return scroll;
    }

    private List<String> outfitOverviewFilterOptions() {
        LinkedHashSet<String> options = new LinkedHashSet<>();
        options.add("All");
        options.add(FILTER_FAVORITES);
        for (Outfit outfit : outfits) {
            if (!isBlank(outfit.occasion)) options.add(outfit.occasion);
            for (String season : selectedSeasons(outfit.season)) {
                String cleaned = seasonLabelWithoutIcon(season);
                if (!isBlank(cleaned)) options.add(cleaned);
            }
        }
        if (!options.contains(selectedOutfitFilter)) selectedOutfitFilter = "All";
        return new ArrayList<>(options);
    }

    private List<Outfit> filteredOutfits() {
        List<Outfit> visible = new ArrayList<>();
        for (Outfit outfit : outfits) {
            if (outfitMatchesOverviewFilter(outfit) && outfitMatchesSearch(outfit)) {
                visible.add(outfit);
            }
        }
        return visible;
    }

    private boolean outfitMatchesOverviewFilter(Outfit outfit) {
        if ("All".equals(selectedOutfitFilter)) return true;
        if (FILTER_FAVORITES.equals(selectedOutfitFilter)) return outfit.favorite;
        if (!isBlank(outfit.occasion) && outfit.occasion.equalsIgnoreCase(selectedOutfitFilter)) return true;
        if (!isBlank(outfit.season) && outfit.season.toLowerCase().contains(selectedOutfitFilter.toLowerCase())) return true;
        return false;
    }

    private boolean outfitMatchesSearch(Outfit outfit) {
        if (isBlank(searchQuery)) return true;
        String needle = searchQuery.trim().toLowerCase(Locale.ROOT);
        if (containsText(outfit.name, needle)
                || containsText(outfit.occasion, needle)
                || containsText(outfit.season, needle)
                || containsText(outfit.notes, needle)) {
            return true;
        }
        for (String id : outfit.clothingIds) {
            ClothingItem item = findItem(id);
            if (item == null) continue;
            if (containsText(item.name, needle)
                    || containsText(item.category, needle)
                    || containsText(item.color, needle)
                    || containsText(item.brand, needle)
                    || containsText(item.material, needle)
                    || containsText(item.damage, needle)) {
                return true;
            }
        }
        return false;
    }

    private boolean containsText(String value, String needle) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(needle);
    }

    private void renderInspiration() {
        activeTab = "inspiration";
        renderTopBar(getString(R.string.nav_inspiration), false);
        updateBottomNavigation();
        useScrollContent();
        content.removeAllViews();
        content.setPadding(dp(22), dp(2), dp(22), dp(28));
        renderInspirationView();
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
            TextView context = text(getString(R.string.inspiration_context,
                    value(profileStyle, getString(R.string.no_style_preference)),
                    currentSeasonName()), 12, muted, false);
            context.setPadding(0, 0, 0, dp(8));
            card.addView(context);
            Button create = textButton(outfitInspirationLoading
                    ? getString(R.string.creating_outfit_inspiration)
                    : getString(R.string.create_outfit_inspiration));
            create.setGravity(Gravity.CENTER);
            create.setEnabled(!outfitInspirationLoading);
            create.setOnClickListener(v -> promptOutfitInspirationStyle());
            card.addView(create, new LinearLayout.LayoutParams(-1, dp(48)));
        } else if (isBlank(openAiApiKey)) {
            Button settings = textButton(getString(R.string.openai_api_key));
            settings.setGravity(Gravity.CENTER);
            settings.setOnClickListener(v -> renderCategories());
            card.addView(settings, new LinearLayout.LayoutParams(-1, dp(48)));
        }

        content.addView(card);
        content.addView(outfitDedupeCard());
        content.addView(wardrobeGapCard());
    }

    private View wardrobeGapCard() {
        LinearLayout card = card();
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        card.addView(text(getString(R.string.wardrobe_gap_title), 20, ink, true));

        String message;
        if (clothes.isEmpty()) {
            message = getString(R.string.wardrobe_gap_empty_clothes);
        } else if (isBlank(openAiApiKey)) {
            message = getString(R.string.wardrobe_gap_empty_key);
        } else {
            message = getString(R.string.wardrobe_gap_ready);
        }
        TextView body = text(message, 14, muted, false);
        body.setPadding(0, dp(6), 0, dp(8));
        card.addView(body);

        if (!clothes.isEmpty() && !isBlank(openAiApiKey)) {
            Button propose = textButton(wardrobeGapLoading
                    ? getString(R.string.analyzing_wardrobe_gaps)
                    : "Open missing pieces");
            propose.setGravity(Gravity.CENTER);
            propose.setEnabled(!wardrobeGapLoading);
            propose.setOnClickListener(v -> renderWardrobeGapAsk());
            card.addView(propose, new LinearLayout.LayoutParams(-1, dp(48)));
            if (wardrobeGapAnalysis != null) {
                Button latest = outlinedButton("View latest analysis");
                latest.setOnClickListener(v -> renderWardrobeGapOverview());
                LinearLayout.LayoutParams params = blockParams();
                params.setMargins(0, dp(8), 0, 0);
                card.addView(latest, params);
            }
        } else if (isBlank(openAiApiKey)) {
            Button settings = textButton(getString(R.string.openai_api_key));
            settings.setGravity(Gravity.CENTER);
            settings.setOnClickListener(v -> renderCategories());
            card.addView(settings, new LinearLayout.LayoutParams(-1, dp(48)));
        }
        return card;
    }

    private void renderWardrobeGapAsk() {
        activeTab = "wardrobeGapAsk";
        renderTopBar("Missing pieces", true, null, false);
        updateBottomNavigation();
        useScrollContent();
        content.removeAllViews();
        content.setPadding(dp(18), dp(8), dp(18), dp(32));

        content.addView(text("What’s missing in my wardrobe?", 26, ink, true));
        TextView intro = text("Tell AI what you want to improve or achieve and get personalized suggestions.", 15, muted, false);
        intro.setPadding(0, dp(8), 0, dp(20));
        content.addView(intro);

        TextView examples = text("Examples", 13, ink, true);
        examples.setPadding(0, 0, 0, dp(8));
        content.addView(examples);
        LinearLayout exampleWrap = new LinearLayout(this);
        exampleWrap.setOrientation(LinearLayout.VERTICAL);
        exampleWrap.addView(gapExampleChip("Build more looks for winter"));
        exampleWrap.addView(gapExampleChip("What do I need for a business casual wardrobe?"));
        exampleWrap.addView(gapExampleChip("What’s missing for outdoor outfits?"));
        content.addView(exampleWrap);

        TextView questionLabel = text("Your question", 13, ink, true);
        questionLabel.setPadding(0, dp(18), 0, dp(8));
        content.addView(questionLabel);
        EditText input = input("What am I missing for a versatile smart casual wardrobe for spring and autumn?");
        input.setMinLines(4);
        input.setSingleLine(false);
        input.setText(wardrobeGapQuestion);
        TextView counter = text(input.getText().length() + "/300", 12, muted, false);
        counter.setGravity(Gravity.RIGHT);
        input.addTextChangedListener(new TextWatcher() {
            private boolean editing;

            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable editable) {
                if (editing) return;
                if (editable.length() > 300) {
                    editing = true;
                    editable.delete(300, editable.length());
                    editing = false;
                }
                wardrobeGapQuestion = editable.toString();
                counter.setText(editable.length() + "/300");
            }
        });
        content.addView(input);
        content.addView(counter);

        Button ask = primaryButton(wardrobeGapLoading ? getString(R.string.analyzing_wardrobe_gaps) : "Ask AI");
        ask.setEnabled(!wardrobeGapLoading);
        ask.setOnClickListener(v -> analyzeWardrobeGaps(input.getText().toString().trim()));
        LinearLayout.LayoutParams askParams = blockParams();
        askParams.setMargins(0, dp(10), 0, dp(18));
        content.addView(ask, askParams);

        content.addView(gapConsiderationCard());
    }

    private View gapExampleChip(String label) {
        TextView chip = text(label, 13, ink, false);
        chip.setPadding(dp(14), dp(9), dp(14), dp(9));
        chip.setBackground(rounded(surfaceContainer, 14, Color.TRANSPARENT, 0));
        chip.setOnClickListener(v -> {
            wardrobeGapQuestion = label;
            renderWardrobeGapAsk();
        });
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
        params.setMargins(0, 0, 0, dp(8));
        chip.setLayoutParams(params);
        return chip;
    }

    private View gapConsiderationCard() {
        LinearLayout card = card();
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.addView(text("AI will consider", 16, ink, true));
        card.addView(gapConsiderationRow(R.drawable.ic_clothes, "Your existing clothes", clothes.size() + " items"));
        card.addView(gapConsiderationRow(R.drawable.ic_outfit, "Your outfits", outfits.size() + " outfits"));
        card.addView(gapConsiderationRow(R.drawable.ic_info, "Your style preferences", joinNonEmpty(profileGender, profileEyeColor, profileHairColor)));
        card.addView(gapConsiderationRow(R.drawable.ic_detail_season, "Season context", "Spring / Autumn placeholder"));
        return card;
    }

    private View gapConsiderationRow(int iconRes, String title, String subtitle) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(10), 0, 0);
        ImageView icon = iconView(iconRes);
        icon.setColorFilter(primary);
        row.addView(icon, new LinearLayout.LayoutParams(dp(34), dp(34)));
        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(12), 0, 0, 0);
        copy.addView(text(title, 13, ink, true));
        copy.addView(text(value(subtitle, "Not set"), 12, muted, false));
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        return row;
    }

    private void renderWardrobeGapOverview() {
        if (wardrobeGapAnalysis == null) {
            renderWardrobeGapAsk();
            return;
        }
        activeTab = "wardrobeGapOverview";
        renderTopBar("AI analysis", true, null, false);
        updateBottomNavigation();
        useScrollContent();
        content.removeAllViews();
        content.setPadding(dp(18), dp(8), dp(18), dp(32));

        content.addView(text("Here’s what I found", 20, ink, true));
        TextView based = text("Based on your wardrobe and goal", 13, muted, false);
        based.setPadding(0, dp(4), 0, dp(12));
        content.addView(based);
        content.addView(gapCompletenessCard());
        content.addView(gapMissingCategoriesCard());
        content.addView(gapImpactSection());
        content.addView(gapRecommendedCarousel());
    }

    private View gapCompletenessCard() {
        LinearLayout card = card();
        card.setPadding(dp(16), dp(16), dp(16), dp(16));
        card.addView(text("Overall completeness", 13, ink, true));
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(8), 0, 0);
        row.addView(new PercentageRingView(this, wardrobeGapAnalysis.overallCompletenessPercent, palette.outlineVariant, primary, ink), new LinearLayout.LayoutParams(dp(88), dp(88)));
        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(16), 0, 0, 0);
        copy.addView(text(value(wardrobeGapAnalysis.summaryTitle, "Good start!"), 16, ink, true));
        copy.addView(text(value(wardrobeGapAnalysis.summaryBody, "These pieces will help you create more looks."), 13, muted, false));
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        card.addView(row);
        return card;
    }

    private View gapMissingCategoriesCard() {
        LinearLayout card = card();
        card.setPadding(dp(14), dp(14), dp(14), dp(14));
        card.addView(text("Top missing categories", 16, ink, true));
        for (int i = 0; i < wardrobeGapAnalysis.missingCategories.size(); i++) {
            int index = i;
            WardrobeGapAnalysis.MissingCategory category = wardrobeGapAnalysis.missingCategories.get(i);
            card.addView(gapCategoryRow(category, v -> renderWardrobeGapDetail(index)));
        }
        return card;
    }

    private View gapCategoryRow(WardrobeGapAnalysis.MissingCategory category, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(0, dp(10), 0, dp(10));
        row.setOnClickListener(listener);
        ImageView icon = iconView(filterDrawable(value(category.iconCategory, category.title)));
        icon.setColorFilter(primary);
        icon.setPadding(dp(9), dp(9), dp(9), dp(9));
        icon.setBackground(rounded(surfaceContainer, 14, Color.TRANSPARENT, 0));
        row.addView(icon, new LinearLayout.LayoutParams(dp(52), dp(52)));
        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(12), 0, dp(8), 0);
        copy.addView(text(value(category.title, "Missing category"), 14, ink, true));
        copy.addView(text(value(category.shortReason, "Recommended for more outfit variety."), 12, muted, false));
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout priority = new LinearLayout(this);
        priority.setGravity(Gravity.RIGHT | Gravity.CENTER_VERTICAL);
        priority.setOrientation(LinearLayout.VERTICAL);
        priority.addView(text(value(category.priority, "Medium"), 12, ink, false));
        priority.addView(priorityDots(category.priority));
        row.addView(priority, new LinearLayout.LayoutParams(dp(72), -2));
        return row;
    }

    private View priorityDots(String priority) {
        LinearLayout dots = new LinearLayout(this);
        dots.setGravity(Gravity.RIGHT);
        int count = "High".equalsIgnoreCase(priority) ? 3 : "Low".equalsIgnoreCase(priority) ? 1 : 2;
        for (int i = 0; i < 3; i++) {
            TextView dot = new TextView(this);
            dot.setBackground(oval(i < count ? primary : outline, Color.TRANSPARENT, 0));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(8), dp(8));
            params.setMargins(dp(3), dp(4), 0, 0);
            dots.addView(dot, params);
        }
        return dots;
    }

    private View gapImpactSection() {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        TextView title = text("Impact on your wardrobe", 16, ink, true);
        title.setPadding(0, dp(10), 0, dp(8));
        section.addView(title);
        LinearLayout row = new LinearLayout(this);
        List<String> impact = wardrobeGapAnalysis.impact;
        for (int i = 0; i < 3; i++) {
            String label = i < impact.size() ? impact.get(i) : (i == 0 ? "More outfit combinations" : i == 1 ? "More versatility" : "All weather ready");
            LinearLayout card = card();
            card.setGravity(Gravity.CENTER);
            card.setPadding(dp(8), dp(12), dp(8), dp(12));
            card.addView(text(label, 12, ink, true));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(92), 1);
            params.setMargins(i == 0 ? 0 : dp(6), 0, 0, 0);
            row.addView(card, params);
        }
        section.addView(row);
        return section;
    }

    private View gapRecommendedCarousel() {
        LinearLayout section = new LinearLayout(this);
        section.setOrientation(LinearLayout.VERTICAL);
        TextView title = text("Recommended for you", 16, ink, true);
        title.setPadding(0, dp(14), 0, dp(8));
        section.addView(title);
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        for (WardrobeGapAnalysis.MissingCategory category : wardrobeGapAnalysis.missingCategories) {
            for (WardrobeGapAnalysis.MissingPieceSuggestion suggestion : category.suggestedItems) {
                row.addView(gapSuggestionSmallCard(category, suggestion));
            }
        }
        scroll.addView(row);
        section.addView(scroll);
        return section;
    }

    private View gapSuggestionSmallCard(WardrobeGapAnalysis.MissingCategory category, WardrobeGapAnalysis.MissingPieceSuggestion suggestion) {
        LinearLayout card = card();
        card.setPadding(dp(10), dp(10), dp(10), dp(10));
        ImageView icon = iconView(filterDrawable(value(category.iconCategory, category.title)));
        icon.setColorFilter(primary);
        icon.setPadding(dp(18), dp(18), dp(18), dp(18));
        icon.setBackground(rounded(surfaceContainer, 14, Color.TRANSPARENT, 0));
        card.addView(icon, new LinearLayout.LayoutParams(-1, dp(92)));
        card.addView(text(value(suggestion.name, category.title), 13, ink, true));
        card.addView(text(value(suggestion.color, "Flexible color"), 12, muted, false));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(120), -2);
        params.setMargins(0, 0, dp(10), 0);
        card.setLayoutParams(params);
        return card;
    }

    private void renderWardrobeGapDetail(int index) {
        if (wardrobeGapAnalysis == null || wardrobeGapAnalysis.missingCategories.isEmpty()) {
            renderWardrobeGapOverview();
            return;
        }
        int safeIndex = Math.max(0, Math.min(index, wardrobeGapAnalysis.missingCategories.size() - 1));
        WardrobeGapAnalysis.MissingCategory category = wardrobeGapAnalysis.missingCategories.get(safeIndex);
        activeTab = "wardrobeGapDetail";
        renderTopBar(value(category.title, "Missing piece"), true, null, false);
        updateBottomNavigation();
        useScrollContent();
        content.removeAllViews();
        content.setPadding(dp(18), dp(8), dp(18), dp(32));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView priority = text(value(category.priority, "Medium") + " priority", 12, primary, true);
        priority.setPadding(dp(12), dp(6), dp(12), dp(6));
        priority.setBackground(rounded(primaryContainer, 16, Color.TRANSPARENT, 0));
        top.addView(priority, new LinearLayout.LayoutParams(0, -2, 1));
        top.addView(text("Why this?", 13, ink, true));
        content.addView(top);

        LinearLayout why = card();
        why.setPadding(dp(16), dp(16), dp(16), dp(16));
        why.addView(text(value(category.shortReason, "This would make your wardrobe more complete."), 14, ink, false));
        content.addView(why);

        TextView picks = text("Best picks for you", 16, ink, true);
        picks.setPadding(0, dp(12), 0, dp(8));
        content.addView(picks);
        for (WardrobeGapAnalysis.MissingPieceSuggestion suggestion : category.suggestedItems) {
            content.addView(gapSuggestionDetailCard(category, suggestion));
        }

        content.addView(gapChecklistCard("How it helps", category.howItHelps));
        content.addView(gapOutfitIdeasCard(category));
    }

    private View gapSuggestionDetailCard(WardrobeGapAnalysis.MissingCategory category, WardrobeGapAnalysis.MissingPieceSuggestion suggestion) {
        LinearLayout card = card();
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        ImageView icon = iconView(filterDrawable(value(category.iconCategory, category.title)));
        icon.setColorFilter(primary);
        icon.setPadding(dp(20), dp(20), dp(20), dp(20));
        icon.setBackground(rounded(surfaceContainer, 16, Color.TRANSPARENT, 0));
        card.addView(icon, new LinearLayout.LayoutParams(dp(96), dp(96)));
        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(0, dp(8), 0, 0);
        copy.addView(text(value(suggestion.name, category.title), 16, ink, true));
        copy.addView(text(value(suggestion.color, "Neutral / versatile"), 13, muted, false));
        copy.addView(text(value(suggestion.reason, "Works with existing clothes."), 13, ink, false));
        LinearLayout tags = new LinearLayout(this);
        tags.setPadding(0, dp(6), 0, 0);
        for (String tag : suggestion.tags) {
            TextView chip = text(tag, 11, muted, false);
            chip.setPadding(dp(9), dp(4), dp(9), dp(4));
            chip.setBackground(rounded(Color.TRANSPARENT, 14, outline, 1));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, -2);
            params.setMargins(0, 0, dp(6), 0);
            tags.addView(chip, params);
        }
        copy.addView(tags);
        card.addView(copy);
        return card;
    }

    private View gapChecklistCard(String title, List<String> values) {
        LinearLayout card = card();
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.addView(text(title, 16, ink, true));
        if (values.isEmpty()) {
            values = Collections.singletonList("Adds more outfit variety.");
        }
        for (String value : values) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, dp(8), 0, 0);
            row.addView(checkIcon(primary), new LinearLayout.LayoutParams(dp(28), dp(28)));
            TextView copy = text(value, 13, ink, false);
            copy.setPadding(dp(8), 0, 0, 0);
            row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
            card.addView(row);
        }
        return card;
    }

    private View gapOutfitIdeasCard(WardrobeGapAnalysis.MissingCategory category) {
        LinearLayout card = card();
        card.setPadding(dp(16), dp(14), dp(16), dp(14));
        card.addView(text("Outfits you can create", 16, ink, true));
        HorizontalScrollView scroll = new HorizontalScrollView(this);
        scroll.setHorizontalScrollBarEnabled(false);
        LinearLayout row = new LinearLayout(this);
        List<String> ideas = category.outfitIdeas.isEmpty() ? Collections.singletonList("Combine with your existing basics") : category.outfitIdeas;
        for (String idea : ideas) {
            LinearLayout ideaCard = new LinearLayout(this);
            ideaCard.setOrientation(LinearLayout.VERTICAL);
            ideaCard.setPadding(dp(8), dp(8), dp(8), dp(8));
            ideaCard.setBackground(rounded(surfaceContainer, 14, outline, 1));
            ideaCard.addView(missingPieceOutfitPreview(category), new LinearLayout.LayoutParams(-1, dp(112)));
            ideaCard.addView(text(idea, 12, ink, true));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(128), -2);
            params.setMargins(0, dp(10), dp(10), 0);
            row.addView(ideaCard, params);
        }
        scroll.addView(row);
        card.addView(scroll);
        return card;
    }

    private View missingPieceOutfitPreview(WardrobeGapAnalysis.MissingCategory category) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        int added = 0;
        for (ClothingItem item : clothes) {
            if (added >= 2) break;
            if (isBlank(item.imageUri)) continue;
            ImageView image = new ImageView(this);
            image.setScaleType(ImageView.ScaleType.FIT_CENTER);
            image.setPadding(dp(4), dp(4), dp(4), dp(4));
            loadImage(image, item.imageUri, categoryDrawable(item.category), muted, 64);
            row.addView(image, new LinearLayout.LayoutParams(0, -1, 1));
            added++;
        }
        ImageView missing = iconView(filterDrawable(value(category.iconCategory, category.title)));
        missing.setColorFilter(primary);
        missing.setPadding(dp(12), dp(12), dp(12), dp(12));
        row.addView(missing, new LinearLayout.LayoutParams(0, -1, 1));
        return row;
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
        return outfitCleanupGroups().size();
    }

    private void reviewOutfitDeduplication() {
        outfitCleanupReviewIndex = 0;
        if (outfitCleanupGroups().isEmpty()) {
            Toast.makeText(this, getString(R.string.deduplicate_outfits_none), Toast.LENGTH_SHORT).show();
            renderInspiration();
        } else {
            renderOutfitCleanupInbox();
        }
    }

    private void renderOutfitCleanupInbox() {
        activeTab = "outfitCleanup";
        renderTopBar(getString(R.string.outfit_cleanup_title), true, null, false);
        updateBottomNavigation();
        useScrollContent();
        content.removeAllViews();
        content.setPadding(dp(18), dp(2), dp(18), dp(32));

        List<OutfitOverlap> groups = outfitCleanupGroups();
        LinearLayout summary = card();
        summary.setPadding(dp(14), dp(14), dp(14), dp(14));
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        ImageView icon = iconView(R.drawable.ic_inspiration);
        icon.setColorFilter(primary);
        icon.setPadding(dp(10), dp(10), dp(10), dp(10));
        icon.setBackground(oval(primaryContainer, Color.TRANSPARENT, 0));
        row.addView(icon, new LinearLayout.LayoutParams(dp(54), dp(54)));
        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(12), 0, dp(8), 0);
        copy.addView(text(getString(R.string.outfit_cleanup_found, groups.size()), 16, ink, true));
        copy.addView(text(getString(R.string.outfit_cleanup_found_body), 13, muted, false));
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        row.addView(iconButton(R.drawable.ic_move_right, v -> renderOutfitCleanupReview(0)), new LinearLayout.LayoutParams(dp(48), dp(48)));
        summary.addView(row);
        content.addView(summary);

        LinearLayout chips = new LinearLayout(this);
        chips.setGravity(Gravity.CENTER_VERTICAL);
        chips.setPadding(0, dp(4), 0, dp(8));
        chips.addView(cleanupFilterChip(getString(R.string.outfit_cleanup_all, groups.size()), true));
        chips.addView(cleanupFilterChip(getString(R.string.outfit_cleanup_exact), false));
        chips.addView(cleanupFilterChip(getString(R.string.outfit_cleanup_smaller), false));
        content.addView(chips);

        if (groups.isEmpty()) {
            content.addView(emptyState(getString(R.string.deduplicate_outfits_none)));
            return;
        }

        for (int i = 0; i < groups.size(); i++) {
            content.addView(outfitCleanupInboxCard(groups.get(i), i));
        }
    }

    private View cleanupFilterChip(String label, boolean active) {
        TextView chip = text(label, 13, active ? onPrimary : muted, true);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(14), dp(7), dp(14), dp(7));
        chip.setBackground(rounded(active ? primary : Color.TRANSPARENT, 18, active ? Color.TRANSPARENT : outline, 1));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, dp(38));
        params.setMargins(0, 0, dp(8), 0);
        chip.setLayoutParams(params);
        return chip;
    }

    private View outfitCleanupInboxCard(OutfitOverlap overlap, int index) {
        Outfit keep = recommendedOutfit(overlap);
        Outfit remove = otherOutfit(overlap, keep);
        LinearLayout card = card();
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.setOnClickListener(v -> renderOutfitCleanupReview(index));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        TextView number = text(String.valueOf(index + 1), 13, onPrimary, true);
        number.setGravity(Gravity.CENTER);
        number.setBackground(oval(primary, Color.TRANSPARENT, 0));
        top.addView(number, new LinearLayout.LayoutParams(dp(30), dp(30)));
        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(0, dp(58), 1);
        previewParams.setMargins(dp(10), 0, dp(8), 0);
        top.addView(cleanupMiniPreview(keep), previewParams);
        TextView badge = text(cleanupBadge(overlap), 12, primary, true);
        badge.setGravity(Gravity.CENTER);
        badge.setPadding(dp(10), dp(7), dp(10), dp(7));
        badge.setBackground(rounded(primaryContainer, 16, Color.TRANSPARENT, 0));
        top.addView(badge, new LinearLayout.LayoutParams(-2, dp(38)));
        card.addView(top);

        TextView title = text(value(keep.name, getString(R.string.untitled_outfit)), 17, ink, true);
        title.setPadding(0, dp(10), 0, 0);
        card.addView(title);
        card.addView(text(cleanupInboxSubtitle(overlap, keep, remove), 13, muted, false));
        TextView detail = text(cleanupMeta(keep, remove), 13, muted, false);
        detail.setPadding(0, dp(4), 0, 0);
        card.addView(detail);
        if (!sameText(keep.notes, remove.notes)) {
            TextView notes = text(getString(R.string.outfit_cleanup_notes_differ), 12, 0xFF9A5A00, true);
            notes.setPadding(0, dp(6), 0, 0);
            card.addView(notes);
        }
        return card;
    }

    private void renderOutfitCleanupReview(int index) {
        List<OutfitOverlap> groups = outfitCleanupGroups();
        if (groups.isEmpty()) {
            Toast.makeText(this, getString(R.string.deduplicate_outfits_none), Toast.LENGTH_SHORT).show();
            renderInspiration();
            return;
        }
        outfitCleanupReviewIndex = Math.max(0, Math.min(index, groups.size() - 1));
        OutfitOverlap overlap = groups.get(outfitCleanupReviewIndex);
        Outfit keep = recommendedOutfit(overlap);
        Outfit remove = otherOutfit(overlap, keep);

        activeTab = "outfitCleanupReview";
        renderTopBar(getString(R.string.outfit_cleanup_review_title, outfitCleanupReviewIndex + 1, groups.size()), true, null, false);
        updateBottomNavigation();
        useScrollContent();
        content.removeAllViews();
        content.setPadding(dp(18), dp(2), dp(18), dp(24));

        content.addView(cleanupProgress(groups.size(), outfitCleanupReviewIndex));
        TextView title = text(cleanupGroupTitle(overlap), 21, ink, true);
        title.setPadding(0, dp(10), 0, 0);
        content.addView(title);
        TextView subtitle = text(cleanupGroupDescription(overlap), 14, muted, false);
        subtitle.setPadding(0, dp(4), 0, dp(8));
        content.addView(subtitle);
        content.addView(cleanupRecommendationCard(overlap, keep, remove));

        TextView keepLabel = text(getString(R.string.outfit_cleanup_keep_recommended), 13, ink, true);
        keepLabel.setPadding(0, dp(8), 0, dp(4));
        content.addView(keepLabel);
        content.addView(outfitCleanupReviewCard(keep, getString(R.string.outfit_cleanup_more_complete), true));

        TextView duplicateLabel = text(getString(R.string.outfit_cleanup_possible_duplicate), 13, ink, true);
        duplicateLabel.setPadding(0, dp(8), 0, dp(4));
        content.addView(duplicateLabel);
        content.addView(outfitCleanupReviewCard(remove, getString(R.string.outfit_cleanup_fewer_items), false));

        content.addView(cleanupDifferenceCard(keep, remove));
        content.addView(cleanupNotesCard(keep, remove));

        Button removeDuplicate = primaryButton(getString(R.string.outfit_cleanup_remove_duplicate, value(keep.name, getString(R.string.untitled_outfit))));
        removeDuplicate.setOnClickListener(v -> deleteOutfitFromCleanup(remove));
        LinearLayout.LayoutParams removeParams = blockParams();
        removeParams.setMargins(0, dp(12), 0, dp(8));
        content.addView(removeDuplicate, removeParams);

        Button removeRecommended = dangerButton(getString(R.string.outfit_cleanup_remove_recommended));
        removeRecommended.setOnClickListener(v -> deleteOutfitFromCleanup(keep));
        content.addView(removeRecommended, new LinearLayout.LayoutParams(-1, dp(48)));

        Button keepBoth = outlinedButton(getString(R.string.keep_both));
        keepBoth.setOnClickListener(v -> keepBothCleanup(overlap));
        LinearLayout.LayoutParams keepParams = blockParams();
        keepParams.setMargins(0, dp(8), 0, 0);
        content.addView(keepBoth, keepParams);
    }

    private boolean moveOutfitCleanupReview(int direction) {
        List<OutfitOverlap> groups = outfitCleanupGroups();
        if (groups.isEmpty()) return false;
        int nextIndex = outfitCleanupReviewIndex + direction;
        if (nextIndex < 0 || nextIndex >= groups.size()) {
            Toast.makeText(this,
                    nextIndex < 0 ? "First review group" : "Last review group",
                    Toast.LENGTH_SHORT).show();
            return true;
        }
        renderOutfitCleanupReview(nextIndex);
        return true;
    }

    private View cleanupProgress(int total, int index) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER);
        row.setPadding(dp(72), dp(4), dp(72), dp(8));
        for (int i = 0; i < total; i++) {
            TextView line = new TextView(this);
            line.setBackground(rounded(i <= index ? primary : palette.outlineVariant, 2, Color.TRANSPARENT, 0));
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, dp(4), 1);
            params.setMargins(dp(2), 0, dp(2), 0);
            row.addView(line, params);
        }
        return row;
    }

    private View cleanupRecommendationCard(OutfitOverlap overlap, Outfit keep, Outfit remove) {
        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(12), dp(12), dp(12), dp(12));
        card.setBackground(rounded(surfaceContainer, 18, outline, 1));
        LinearLayout.LayoutParams params = blockParams();
        params.setMargins(0, dp(6), 0, dp(8));
        card.setLayoutParams(params);

        ImageView icon = iconView(R.drawable.ic_inspiration);
        icon.setColorFilter(primary);
        icon.setPadding(dp(10), dp(10), dp(10), dp(10));
        icon.setBackground(oval(primaryContainer, Color.TRANSPARENT, 0));
        card.addView(icon, new LinearLayout.LayoutParams(dp(52), dp(52)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(12), 0, dp(8), 0);
        copy.addView(text(getString(R.string.outfit_cleanup_recommended), 12, primary, true));
        copy.addView(text(getString(R.string.outfit_cleanup_keep_name, value(keep.name, getString(R.string.untitled_outfit))), 16, ink, true));
        copy.addView(text(cleanupRecommendationText(overlap, keep, remove), 13, muted, false));
        card.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        card.addView(cleanupMiniPreview(keep), new LinearLayout.LayoutParams(dp(92), dp(58)));
        return card;
    }

    private View outfitCleanupReviewCard(Outfit outfit, String badgeText, boolean selected) {
        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(12), dp(10), dp(12), dp(10));
        card.setBackground(rounded(Color.WHITE, 18, selected ? primary : outline, 1));
        LinearLayout.LayoutParams params = blockParams();
        params.setMargins(0, 0, 0, dp(8));
        card.setLayoutParams(params);
        card.addView(cleanupMiniPreview(outfit), new LinearLayout.LayoutParams(dp(112), dp(66)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(12), 0, dp(8), 0);
        copy.addView(text(value(outfit.name, getString(R.string.untitled_outfit)), 16, ink, true));
        copy.addView(text(badgeText + " · " + outfitIdSet(outfit).size() + " items", 12, primary, false));
        copy.addView(text(outfitItemNames(outfit), 12, muted, false));
        if (!isBlank(outfit.notes)) {
            TextView notes = text(getString(R.string.notes) + ": " + outfit.notes, 12, ink, false);
            notes.setPadding(0, dp(4), 0, 0);
            copy.addView(notes);
        }
        card.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));

        card.addView(circularCheckIcon(selected, primary, onPrimary, outline, 1), new LinearLayout.LayoutParams(dp(34), dp(34)));
        return card;
    }

    private View cleanupDifferenceCard(Outfit keep, Outfit remove) {
        LinearLayout card = card();
        card.setPadding(dp(12), dp(10), dp(12), dp(10));
        card.addView(text(getString(R.string.outfit_cleanup_difference), 15, ink, true));
        Set<String> keepIds = outfitIdSet(keep);
        Set<String> removeIds = outfitIdSet(remove);
        Set<String> allIds = new LinkedHashSet<>();
        allIds.addAll(keepIds);
        allIds.addAll(removeIds);
        for (String id : allIds) {
            ClothingItem item = findItem(id);
            if (item == null) continue;
            boolean inKeep = keepIds.contains(id);
            boolean inRemove = removeIds.contains(id);
            String detail = inKeep && inRemove
                    ? getString(R.string.outfit_cleanup_in_both)
                    : inKeep
                    ? getString(R.string.outfit_cleanup_only_in, value(keep.name, getString(R.string.untitled_outfit)))
                    : getString(R.string.outfit_cleanup_only_in, value(remove.name, getString(R.string.untitled_outfit)));
            card.addView(cleanupDifferenceRow(value(item.name, item.category), detail, inKeep && inRemove));
        }
        return card;
    }

    private View cleanupDifferenceRow(String label, String detail, boolean shared) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(42));
        View mark;
        if (shared) {
            ImageView check = checkIcon(primary);
            check.setBackground(oval(primaryContainer, Color.TRANSPARENT, 0));
            mark = check;
        } else {
            TextView plus = text("+", 14, 0xFF625BCE, true);
            plus.setGravity(Gravity.CENTER);
            plus.setBackground(oval(0xFFE7DEFF, Color.TRANSPARENT, 0));
            mark = plus;
        }
        row.addView(mark, new LinearLayout.LayoutParams(dp(26), dp(26)));
        TextView name = text(label, 13, ink, true);
        name.setPadding(dp(10), 0, dp(8), 0);
        row.addView(name, new LinearLayout.LayoutParams(0, -2, 1));
        TextView meta = text(detail, 12, muted, false);
        meta.setGravity(Gravity.RIGHT);
        row.addView(meta, new LinearLayout.LayoutParams(0, -2, 1));
        return row;
    }

    private View cleanupNotesCard(Outfit keep, Outfit remove) {
        LinearLayout card = card();
        card.setPadding(dp(12), dp(10), dp(12), dp(10));
        card.addView(text(getString(R.string.outfit_cleanup_notes_comparison), 15, ink, true));
        card.addView(cleanupNoteRow(value(keep.name, getString(R.string.untitled_outfit)), keep.notes));
        card.addView(cleanupNoteRow(value(remove.name, getString(R.string.untitled_outfit)), remove.notes));
        return card;
    }

    private View cleanupNoteRow(String name, String notes) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(42));
        TextView chip = text(name, 12, primary, true);
        chip.setSingleLine(true);
        chip.setPadding(dp(8), dp(4), dp(8), dp(4));
        chip.setBackground(rounded(primaryContainer, 12, Color.TRANSPARENT, 0));
        row.addView(chip, new LinearLayout.LayoutParams(dp(118), -2));
        TextView copy = text(isBlank(notes) ? getString(R.string.no_notes_added) : notes, 12, muted, false);
        copy.setPadding(dp(8), 0, 0, 0);
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        return row;
    }

    private View cleanupMiniPreview(Outfit outfit) {
        LinearLayout preview = new LinearLayout(this);
        preview.setGravity(Gravity.CENTER_VERTICAL);
        int count = 0;
        for (String id : outfit.clothingIds) {
            ClothingItem item = findItem(id);
            if (item == null) continue;
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp(44), dp(54));
            params.setMargins(count == 0 ? 0 : dp(4), 0, 0, 0);
            preview.addView(cleanupTinyTile(item), params);
            count++;
            if (count == 4) break;
        }
        return preview;
    }

    private View cleanupTinyTile(ClothingItem item) {
        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setPadding(dp(4), dp(4), dp(4), dp(4));
        image.setBackground(rounded(palette.softSurface(), 12, palette.outlineVariant, 1));
        image.setClipToOutline(true);
        if (!isBlank(item.imageUri)) {
            loadImage(image, item.imageUri, R.drawable.ic_clothes, muted, 72);
        } else {
            image.setImageResource(R.drawable.ic_clothes);
            image.setColorFilter(muted);
        }
        return image;
    }

    private void deleteOutfitFromCleanup(Outfit outfit) {
        if (outfit == null) return;
        outfits.remove(outfit);
        store.saveOutfits(outfits);
        Toast.makeText(this, getString(R.string.outfit_deleted), Toast.LENGTH_SHORT).show();
        List<OutfitOverlap> groups = outfitCleanupGroups();
        if (groups.isEmpty()) {
            renderOutfitCleanupInbox();
        } else {
            renderOutfitCleanupReview(Math.min(outfitCleanupReviewIndex, groups.size() - 1));
        }
    }

    private void keepBothCleanup(OutfitOverlap overlap) {
        ignoredOutfitCleanupPairs.add(cleanupPairKey(overlap));
        List<OutfitOverlap> groups = outfitCleanupGroups();
        if (groups.isEmpty()) {
            renderOutfitCleanupInbox();
        } else {
            renderOutfitCleanupReview(Math.min(outfitCleanupReviewIndex, groups.size() - 1));
        }
    }

    private List<OutfitOverlap> outfitCleanupGroups() {
        List<OutfitOverlap> groups = new ArrayList<>();
        for (OutfitOverlap overlap : findExactOutfitOverlaps()) {
            if (!ignoredOutfitCleanupPairs.contains(cleanupPairKey(overlap))) groups.add(overlap);
        }
        for (OutfitOverlap overlap : findSubsetOutfitOverlaps()) {
            if (!ignoredOutfitCleanupPairs.contains(cleanupPairKey(overlap))) groups.add(overlap);
        }
        return groups;
    }

    private Outfit recommendedOutfit(OutfitOverlap overlap) {
        int smallerSize = outfitIdSet(overlap.smaller).size();
        int largerSize = outfitIdSet(overlap.larger).size();
        if (largerSize > smallerSize) return overlap.larger;
        return overlap.larger.updatedAt >= overlap.smaller.updatedAt ? overlap.larger : overlap.smaller;
    }

    private Outfit otherOutfit(OutfitOverlap overlap, Outfit outfit) {
        return overlap.smaller == outfit ? overlap.larger : overlap.smaller;
    }

    private String cleanupPairKey(OutfitOverlap overlap) {
        String first = overlap.smaller.id.compareTo(overlap.larger.id) <= 0 ? overlap.smaller.id : overlap.larger.id;
        String second = overlap.smaller.id.compareTo(overlap.larger.id) <= 0 ? overlap.larger.id : overlap.smaller.id;
        return first + "|" + second;
    }

    private String cleanupBadge(OutfitOverlap overlap) {
        return outfitIdSet(overlap.smaller).equals(outfitIdSet(overlap.larger))
                ? getString(R.string.outfit_cleanup_exact)
                : getString(R.string.outfit_cleanup_recommended_keep);
    }

    private String cleanupInboxSubtitle(OutfitOverlap overlap, Outfit keep, Outfit remove) {
        if (outfitIdSet(overlap.smaller).equals(outfitIdSet(overlap.larger))) {
            return getString(R.string.outfit_cleanup_exact_body);
        }
        return getString(R.string.outfit_cleanup_contains_smaller, value(keep.name, getString(R.string.untitled_outfit)), value(remove.name, getString(R.string.untitled_outfit)));
    }

    private String cleanupMeta(Outfit keep, Outfit remove) {
        int shared = sharedOutfitItemCount(keep, remove);
        return getString(R.string.outfit_cleanup_match_count, shared, Math.max(outfitIdSet(keep).size(), outfitIdSet(remove).size()));
    }

    private String cleanupGroupTitle(OutfitOverlap overlap) {
        Outfit keep = recommendedOutfit(overlap);
        return getString(R.string.outfit_cleanup_group_name, value(keep.name, getString(R.string.untitled_outfit)));
    }

    private String cleanupGroupDescription(OutfitOverlap overlap) {
        return outfitIdSet(overlap.smaller).equals(outfitIdSet(overlap.larger))
                ? getString(R.string.outfit_cleanup_exact_body)
                : getString(R.string.outfit_cleanup_subset_body);
    }

    private String cleanupRecommendationText(OutfitOverlap overlap, Outfit keep, Outfit remove) {
        if (outfitIdSet(overlap.smaller).equals(outfitIdSet(overlap.larger))) {
            return getString(R.string.outfit_cleanup_newer_or_named);
        }
        return getString(R.string.outfit_cleanup_recommendation, value(remove.name, getString(R.string.untitled_outfit)), outfitIdSet(keep).size() - outfitIdSet(remove).size());
    }

    private int sharedOutfitItemCount(Outfit first, Outfit second) {
        Set<String> firstIds = outfitIdSet(first);
        Set<String> secondIds = outfitIdSet(second);
        int count = 0;
        for (String id : firstIds) {
            if (secondIds.contains(id)) count++;
        }
        return count;
    }

    private boolean sameText(String first, String second) {
        return value(first, "").trim().equals(value(second, "").trim());
    }

    private List<OutfitOverlap> findExactOutfitOverlaps() {
        List<OutfitOverlap> overlaps = new ArrayList<>();
        for (int i = 0; i < outfits.size(); i++) {
            Outfit first = outfits.get(i);
            Set<String> firstIds = outfitIdSet(first);
            if (firstIds.isEmpty()) continue;
            for (int j = i + 1; j < outfits.size(); j++) {
                Outfit second = outfits.get(j);
                Set<String> secondIds = outfitIdSet(second);
                if (!secondIds.isEmpty() && firstIds.equals(secondIds)) {
                    overlaps.add(new OutfitOverlap(first, second));
                }
            }
        }
        return overlaps;
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

    private void promptOutfitInspirationStyle() {
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

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);

        String[] selectedOccasion = {selectedOccasion(defaultOutfitInspirationOccasion())};
        TextView occasionLabel = text(getString(R.string.occasion), 14, ink, true);
        occasionLabel.setPadding(0, 0, 0, dp(8));
        body.addView(occasionLabel);

        ScrollView occasionScroll = new ScrollView(this);
        LinearLayout occasionList = new LinearLayout(this);
        occasionList.setOrientation(LinearLayout.VERTICAL);
        occasionScroll.addView(occasionList);
        renderOccasionOptions(occasionList, selectedOccasion);
        body.addView(occasionScroll, new LinearLayout.LayoutParams(-1, dp(240)));

        TextView preferenceLabel = text(getString(R.string.outfit_preference_prompt), 14, ink, true);
        preferenceLabel.setPadding(0, dp(14), 0, dp(8));
        body.addView(preferenceLabel);

        EditText input = input(getString(R.string.outfit_style_prompt_hint));
        input.setSingleLine(false);
        input.setMinLines(2);
        input.setMaxLines(4);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setText(profileStyle);
        input.setSelection(input.getText().length());
        body.addView(input);

        showMaterialDialog(getString(R.string.outfit_style_prompt_title), body,
                "Cancel", getString(R.string.create_outfit_inspiration), null, () -> {
                    String requestedStyle = trimLength(input.getText().toString(), 240);
                    profileStyle = requestedStyle;
                    store.saveProfileStyle(profileStyle);
                    createOutfitInspiration(selectedOccasion[0], requestedStyle);
                });
    }

    private String defaultOutfitInspirationOccasion() {
        if (!isBlank(pendingPlannerDateKey)) {
            PlannerEntry entry = plannerEntryByDate.get(pendingPlannerDateKey);
            if (entry != null && !isBlank(entry.occasion)) {
                return entry.occasion;
            }
        }
        return "";
    }

    private void createOutfitInspiration(String requestedOccasion, String requestedStyle) {
        outfitInspirationLoading = true;
        renderOutfitAiLoadingContext();
        Toast.makeText(this, getString(R.string.creating_outfit_inspiration), Toast.LENGTH_SHORT).show();
        String apiKey = openAiApiKey;
        String baseUrl = openAiBaseUrl;
        String model = openAiModel;
        String occasion = requestedOccasion;
        String style = requestedStyle;
        ioExecutor.execute(() -> {
            try {
                Outfit suggestion = requestOutfitSuggestion(apiKey, baseUrl, model, occasion, style);
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
                    renderOutfitAiLoadingContext();
                    Toast.makeText(this, getString(R.string.inspiration_failed) + ": " + exception.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void renderOutfitAiLoadingContext() {
        if (!isBlank(pendingPlannerDateKey)) {
            renderPlanner();
        } else if (activeTab.equals("inspiration")) {
            renderInspiration();
        } else {
            renderOutfits();
        }
    }

    private void analyzeWardrobeGaps(String question) {
        if (wardrobeGapLoading) return;
        if (clothes.isEmpty()) {
            Toast.makeText(this, getString(R.string.wardrobe_gap_empty_clothes), Toast.LENGTH_LONG).show();
            return;
        }
        if (isBlank(openAiApiKey)) {
            Toast.makeText(this, getString(R.string.openai_api_key_not_set), Toast.LENGTH_LONG).show();
            renderCategories();
            return;
        }
        if (isBlank(question)) {
            question = "What am I missing for a versatile wardrobe?";
        }
        wardrobeGapQuestion = trimLength(question, 300);

        wardrobeGapLoading = true;
        renderWardrobeGapAsk();
        Toast.makeText(this, getString(R.string.analyzing_wardrobe_gaps), Toast.LENGTH_SHORT).show();
        String apiKey = openAiApiKey;
        String baseUrl = openAiBaseUrl;
        String model = openAiModel;
        String finalQuestion = wardrobeGapQuestion;
        ioExecutor.execute(() -> {
            try {
                WardrobeGapAnalysis analysis = requestWardrobeGapSuggestion(apiKey, baseUrl, model, finalQuestion);
                mainHandler.post(() -> {
                    wardrobeGapLoading = false;
                    wardrobeGapAnalysis = analysis;
                    store.saveWardrobeGapAnalysis(analysis);
                    renderWardrobeGapOverview();
                });
            } catch (Exception exception) {
                mainHandler.post(() -> {
                    wardrobeGapLoading = false;
                    renderWardrobeGapAsk();
                    Toast.makeText(this, getString(R.string.wardrobe_gap_failed) + ": " + exception.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private Outfit requestOutfitSuggestion(String apiKey, String baseUrl, String model, String requestedOccasion, String requestedStyle) throws IOException, JSONException {
        String prompt = PromptBuilder.buildOutfitSuggestionPrompt(clothes, profileGender, profileEyeColor, profileHairColor, requestedOccasion, requestedStyle, currentSeasonName());
        String outputText = OpenAiClient.requestText(apiKey, baseUrl, normalizedOpenAiModel(model), prompt, 1000);
        if (isBlank(outputText)) {
            throw new IOException("empty recommendation");
        }
        Outfit outfit = parseOutfitSuggestion(outputText);
        if (isBlank(outfit.occasion) && !isBlank(requestedOccasion)) {
            outfit.occasion = requestedOccasion;
        }
        return outfit;
    }

    private WardrobeGapAnalysis requestWardrobeGapSuggestion(String apiKey, String baseUrl, String model, String question) throws IOException, JSONException {
        String prompt = PromptBuilder.buildWardrobeGapPrompt(clothes, question, profileGender, profileEyeColor, profileHairColor);
        String outputText = OpenAiClient.requestText(apiKey, baseUrl, normalizedOpenAiModel(model), prompt, 1200);
        if (isBlank(outputText)) {
            throw new IOException("empty recommendation");
        }
        JSONObject json = extractJsonResponseObject(outputText);
        WardrobeGapAnalysis analysis = WardrobeGapAnalysis.fromJson(json);
        analysis.question = question;
        analysis.createdAt = System.currentTimeMillis();
        if (analysis.missingCategories.isEmpty()) {
            throw new IOException("no missing categories returned");
        }
        return analysis;
    }

    private JSONObject extractJsonResponseObject(String outputText) throws JSONException, IOException {
        String trimmed = outputText.trim();
        if (trimmed.startsWith("```")) {
            int firstBrace = trimmed.indexOf('{');
            int lastBrace = trimmed.lastIndexOf('}');
            if (firstBrace >= 0 && lastBrace > firstBrace) {
                trimmed = trimmed.substring(firstBrace, lastBrace + 1);
            }
        }
        try {
            return new JSONObject(trimmed);
        } catch (JSONException exception) {
            int firstBrace = trimmed.indexOf('{');
            int lastBrace = trimmed.lastIndexOf('}');
            if (firstBrace >= 0 && lastBrace > firstBrace) {
                return new JSONObject(trimmed.substring(firstBrace, lastBrace + 1));
            }
            throw new IOException("invalid JSON response");
        }
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
        outfit.notes = trimLength(json.optString("notes", ""), 600);

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
        LinearLayout card = new LinearLayout(this);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(10), dp(10), dp(10), dp(10));
        card.setBackground(rounded(Color.WHITE, 22, palette.outlineVariant, 1));
        card.setElevation(dp(1));
        card.setOnClickListener(v -> renderOutfitDetail(outfit));
        LinearLayout.LayoutParams cardParams = blockParams();
        cardParams.setMargins(0, dp(6), 0, dp(8));
        card.setLayoutParams(cardParams);

        card.addView(outfitOverviewPreview(outfit), new LinearLayout.LayoutParams(dp(158), dp(128)));

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(14), 0, 0, 0);
        LinearLayout titleRow = new LinearLayout(this);
        titleRow.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = text(value(outfit.name, "Untitled outfit"), 18, ink, true);
        title.setMaxLines(2);
        titleRow.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        titleRow.addView(transparentIconButton(R.drawable.ic_more_vertical, ink, v -> showOutfitViewer(outfit)), new LinearLayout.LayoutParams(dp(40), dp(40)));
        copy.addView(titleRow);

        TextView meta = text(outfitOverviewMeta(outfit), 12, muted, false);
        meta.setPadding(0, dp(8), 0, 0);
        copy.addView(meta);

        TextView items = text(outfitItemNamesBulleted(outfit), 13, ink, false);
        items.setPadding(0, dp(14), 0, 0);
        items.setMaxLines(3);
        copy.addView(items);

        LinearLayout footer = new LinearLayout(this);
        footer.setGravity(Gravity.CENTER_VERTICAL);
        footer.setPadding(0, dp(12), 0, 0);
        ImageView dateIcon = iconView(R.drawable.ic_detail_date);
        dateIcon.setColorFilter(muted);
        footer.addView(dateIcon, new LinearLayout.LayoutParams(dp(22), dp(22)));
        TextView updated = text("Updated " + DateFormat.getDateInstance().format(outfit.updatedAt), 11, muted, false);
        updated.setPadding(dp(4), 0, 0, 0);
        footer.addView(updated, new LinearLayout.LayoutParams(0, -2, 1));
        ImageView heart = iconButton(outfit.favorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_outline, null);
        heart.setColorFilter(outfit.favorite ? primary : muted);
        heart.setBackgroundColor(Color.TRANSPARENT);
        heart.setOnClickListener(v -> {
            toggleOutfitFavoriteState(outfit);
            if (FILTER_FAVORITES.equals(selectedOutfitFilter) && !outfit.favorite) {
                renderOutfits();
                return;
            }
            heart.setImageResource(outfit.favorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_outline);
            heart.setColorFilter(outfit.favorite ? primary : muted);
            updated.setText("Updated " + DateFormat.getDateInstance().format(outfit.updatedAt));
        });
        footer.addView(heart, new LinearLayout.LayoutParams(dp(40), dp(40)));
        copy.addView(footer);

        card.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        return card;
    }

    private void renderOutfitDetail(Outfit outfit) {
        if (outfit == null || !outfits.contains(outfit)) {
            renderOutfits();
            return;
        }
        activeTab = "outfitDetail";
        renderTopBar(value(outfit.name, "Untitled outfit"), true, null, false);
        updateBottomNavigation();
        useScrollContent();
        content.removeAllViews();
        content.setPadding(dp(18), dp(2), dp(18), dp(28));

        LinearLayout headline = new LinearLayout(this);
        headline.setGravity(Gravity.CENTER_VERTICAL);
        TextView meta = text(outfitOverviewMeta(outfit), 13, muted, false);
        headline.addView(meta, new LinearLayout.LayoutParams(0, -2, 1));
        ImageView favorite = iconButton(outfit.favorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_outline, v -> {
            outfit.favorite = !outfit.favorite;
            outfit.updatedAt = System.currentTimeMillis();
            store.saveOutfits(outfits);
            renderOutfitDetail(outfit);
        });
        favorite.setColorFilter(outfit.favorite ? primary : muted);
        favorite.setBackgroundColor(Color.TRANSPARENT);
        headline.addView(favorite, new LinearLayout.LayoutParams(dp(48), dp(48)));
        headline.addView(transparentIconButton(R.drawable.ic_more_vertical, ink, v -> showOutfitViewer(outfit)), new LinearLayout.LayoutParams(dp(48), dp(48)));
        content.addView(headline);

        LinearLayout.LayoutParams collageParams = new LinearLayout.LayoutParams(-1, -2);
        collageParams.setMargins(0, dp(8), 0, dp(16));
        content.addView(outfitDetailCollage(outfit), collageParams);

        LinearLayout chips = new LinearLayout(this);
        chips.setGravity(Gravity.CENTER_VERTICAL);
        addSoftChip(chips, value(outfit.occasion, "No occasion"));
        addSoftChip(chips, value(outfit.season, SEASON_ANY));
        LinearLayout.LayoutParams chipsParams = new LinearLayout.LayoutParams(-1, -2);
        chipsParams.setMargins(0, 0, 0, dp(14));
        content.addView(chips, chipsParams);

        if (!isBlank(outfit.notes)) {
            LinearLayout note = new LinearLayout(this);
            note.setOrientation(LinearLayout.VERTICAL);
            note.setPadding(dp(14), dp(12), dp(14), dp(12));
            note.setBackground(rounded(Color.WHITE, 16, palette.outlineVariant, 1));
            note.addView(text("Note", 12, muted, false));
            TextView noteText = text(outfit.notes, 14, ink, false);
            noteText.setPadding(0, dp(6), 0, 0);
            note.addView(noteText);
            LinearLayout.LayoutParams noteParams = blockParams();
            noteParams.setMargins(0, 0, 0, dp(16));
            content.addView(note, noteParams);
        }

        TextView itemsTitle = text("Items", 18, ink, true);
        itemsTitle.setPadding(0, dp(2), 0, dp(8));
        content.addView(itemsTitle);
        content.addView(outfitDetailItemsList(outfit), blockParams());

        LinearLayout actions = groupedRows();
        actions.addView(outfitViewerActionRow(R.drawable.ic_edit, "Edit outfit", ink, v -> renderOutfitForm(outfit)));
        actions.addView(outfitViewerActionRow(R.drawable.ic_duplicate, "Duplicate outfit", ink, v -> duplicateOutfit(outfit)));
        actions.addView(outfitViewerActionRow(R.drawable.ic_planner, "Add to planner", ink, v -> addOutfitToSelectedPlannerDate(outfit)));
        actions.addView(outfitViewerActionRow(R.drawable.ic_share, "Share outfit", ink, v -> shareOutfit(outfit)));
        actions.addView(outfitViewerActionRow(R.drawable.ic_delete, "Delete outfit", error, v -> confirmDeleteOutfit(outfit)));
        LinearLayout.LayoutParams actionsParams = blockParams();
        actionsParams.setMargins(0, dp(18), 0, 0);
        content.addView(actions, actionsParams);
    }

    private void addSoftChip(LinearLayout row, String label) {
        TextView chip = text(label, 13, primary, true);
        chip.setGravity(Gravity.CENTER);
        chip.setPadding(dp(14), 0, dp(14), 0);
        chip.setBackground(oval(palette.tonalSurface(), Color.TRANSPARENT, 0));
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-2, dp(34));
        params.setMargins(0, 0, dp(8), 0);
        row.addView(chip, params);
    }

    private String outfitOverviewMeta(Outfit outfit) {
        return outfit.clothingIds.size() + (outfit.clothingIds.size() == 1 ? " item" : " items")
                + "  •  " + value(outfit.occasion, "No occasion")
                + "  •  " + value(outfit.season, SEASON_ANY);
    }

    private String outfitItemNamesBulleted(Outfit outfit) {
        String names = outfitItemNames(outfit);
        return "No items selected".equals(names) ? names : names.replace(", ", " · ");
    }

    private void showOutfitViewer(Outfit outfit) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        LinearLayout sheet = new LinearLayout(this);
        sheet.setOrientation(LinearLayout.VERTICAL);
        sheet.setPadding(dp(18), dp(12), dp(18), dp(18));
        sheet.setBackground(roundedTop(surfaceDialog, 28, palette.outlineVariant, 1));

        TextView handle = new TextView(this);
        handle.setBackground(rounded(outline, 3, Color.TRANSPARENT, 0));
        LinearLayout.LayoutParams handleParams = new LinearLayout.LayoutParams(dp(44), dp(5));
        handleParams.gravity = Gravity.CENTER_HORIZONTAL;
        handleParams.setMargins(0, 0, 0, dp(12));
        sheet.addView(handle, handleParams);

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title = text(value(outfit.name, "Untitled outfit"), 19, ink, true);
        title.setMaxLines(2);
        header.addView(title, new LinearLayout.LayoutParams(0, -2, 1));
        ImageView favorite = iconButton(outfit.favorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_outline, v -> {
            toggleOutfitFavorite(outfit);
            dialog.dismiss();
            showOutfitViewer(outfit);
        });
        favorite.setColorFilter(outfit.favorite ? primary : muted);
        favorite.setBackgroundColor(Color.TRANSPARENT);
        header.addView(favorite, new LinearLayout.LayoutParams(dp(44), dp(44)));
        TextView close = text("×", 28, ink, false);
        close.setGravity(Gravity.CENTER);
        close.setOnClickListener(v -> dialog.dismiss());
        header.addView(close, new LinearLayout.LayoutParams(dp(44), dp(44)));
        sheet.addView(header);

        LinearLayout.LayoutParams previewParams = new LinearLayout.LayoutParams(-1, dp(250));
        previewParams.setMargins(0, dp(10), 0, dp(12));
        sheet.addView(outfitPreviewGrid(outfit, dp(250)), previewParams);

        TextView meta = text(outfitOverviewMeta(outfit), 13, muted, false);
        sheet.addView(meta);
        TextView items = text(outfitItemNamesBulleted(outfit), 14, ink, false);
        items.setPadding(0, dp(8), 0, 0);
        sheet.addView(items);
        if (!isBlank(outfit.notes)) {
            LinearLayout note = new LinearLayout(this);
            note.setGravity(Gravity.CENTER_VERTICAL);
            note.setPadding(dp(10), dp(8), dp(10), dp(8));
            note.setBackground(rounded(palette.tonalSurface(), 8, Color.TRANSPARENT, 0));
            ImageView icon = iconView(R.drawable.ic_detail_notes);
            icon.setColorFilter(muted);
            note.addView(icon, new LinearLayout.LayoutParams(dp(22), dp(22)));
            TextView copy = text(outfit.notes, 13, ink, false);
            copy.setPadding(dp(8), 0, 0, 0);
            note.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
            LinearLayout.LayoutParams noteParams = new LinearLayout.LayoutParams(-1, -2);
            noteParams.setMargins(0, dp(12), 0, dp(12));
            sheet.addView(note, noteParams);
        }

        LinearLayout actions = groupedRows();
        actions.addView(outfitViewerActionRow(R.drawable.ic_edit, "Edit outfit", ink, v -> {
            dialog.dismiss();
            renderOutfitForm(outfit);
        }));
        actions.addView(outfitViewerActionRow(R.drawable.ic_duplicate, "Duplicate outfit", ink, v -> {
            dialog.dismiss();
            duplicateOutfit(outfit);
        }));
        actions.addView(outfitViewerActionRow(R.drawable.ic_planner, "Add to planner", ink, v -> {
            dialog.dismiss();
            addOutfitToSelectedPlannerDate(outfit);
        }));
        actions.addView(outfitViewerActionRow(R.drawable.ic_share, "Share outfit", ink, v -> shareOutfit(outfit)));
        actions.addView(outfitViewerActionRow(R.drawable.ic_delete, "Delete outfit", error, v -> {
            dialog.dismiss();
            confirmDeleteOutfit(outfit);
        }));
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

    private View outfitViewerActionRow(int iconRes, String label, int color, View.OnClickListener listener) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(12), dp(12), dp(12), dp(12));
        row.setOnClickListener(listener);
        ImageView icon = iconView(iconRes);
        icon.setColorFilter(color);
        row.addView(icon, new LinearLayout.LayoutParams(dp(26), dp(26)));
        TextView copy = text(label, 15, color, false);
        copy.setPadding(dp(14), 0, 0, 0);
        row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
        return row;
    }

    private void duplicateOutfit(Outfit outfit) {
        Outfit copy = copyOutfit(outfit);
        copy.name = value(outfit.name, "Untitled outfit") + " copy";
        copy.updatedAt = System.currentTimeMillis();
        outfits.add(0, copy);
        store.saveOutfits(outfits);
        renderOutfits();
    }

    private void addOutfitToSelectedPlannerDate(Outfit outfit) {
        Calendar initial = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    Calendar selected = Calendar.getInstance();
                    selected.set(Calendar.YEAR, year);
                    selected.set(Calendar.MONTH, month);
                    selected.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    startOfDay(selected);
                    assignOutfitToPlannerDate(dateKey(selected), outfit);
                },
                initial.get(Calendar.YEAR),
                initial.get(Calendar.MONTH),
                initial.get(Calendar.DAY_OF_MONTH)
        );
        dialog.setTitle("Plan outfit");
        dialog.show();
    }

    private void shareOutfit(Outfit outfit) {
        Toast.makeText(this, getString(R.string.preparing_share_image), Toast.LENGTH_SHORT).show();
        ioExecutor.execute(() -> {
            try {
                OutfitShareImageRenderer renderer = new OutfitShareImageRenderer(this, palette);
                Bitmap image = renderer.render(outfit, outfitAllItems(outfit));
                File dir = new File(getCacheDir(), "shared-outfits");
                if (!dir.exists() && !dir.mkdirs()) {
                    throw new IOException("Could not create share cache");
                }
                File file = new File(dir, safeFileName(value(outfit.name, "outfit")) + ".png");
                try (FileOutputStream output = new FileOutputStream(file)) {
                    if (!image.compress(Bitmap.CompressFormat.PNG, 100, output)) {
                        throw new IOException("Could not encode share image");
                    }
                }
                Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", file);
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("image/png");
                intent.putExtra(Intent.EXTRA_STREAM, uri);
                intent.putExtra(Intent.EXTRA_TEXT, value(outfit.name, "Outfit") + "\n" + outfitItemNamesBulleted(outfit));
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                mainHandler.post(() -> startActivity(Intent.createChooser(intent, getString(R.string.share_outfit))));
            } catch (Exception exception) {
                mainHandler.post(() -> {
                    Toast.makeText(this, getString(R.string.share_image_failed), Toast.LENGTH_SHORT).show();
                    shareOutfitTextOnly(outfit);
                });
            }
        });
    }

    private void shareOutfitTextOnly(Outfit outfit) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, value(outfit.name, "Outfit") + "\n" + outfitItemNamesBulleted(outfit));
        startActivity(Intent.createChooser(intent, getString(R.string.share_outfit)));
    }

    private String safeFileName(String value) {
        String cleaned = value == null ? "outfit" : value.trim().toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_-]+", "-");
        cleaned = cleaned.replaceAll("^-+|-+$", "");
        return cleaned.isEmpty() ? "outfit" : cleaned;
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

    private View outfitOverviewPreview(Outfit outfit) {
        List<ClothingItem> allItems = outfitAllItems(outfit);
        LinearLayout preview = new LinearLayout(this);
        preview.setBackground(rounded(palette.softSurface(), 14, Color.TRANSPARENT, 0));
        preview.setClipToOutline(true);
        if (allItems.isEmpty()) {
            preview.addView(outfitMoreTile("+", 15), new LinearLayout.LayoutParams(-1, -1));
            return preview;
        }
        int count = allItems.size();
        if (count <= 3) {
            preview.setOrientation(LinearLayout.HORIZONTAL);
            for (int i = 0; i < count; i++) {
                preview.addView(outfitCollageTile(allItems.get(i), 5, 120), new LinearLayout.LayoutParams(0, -1, 1));
            }
            return preview;
        }

        preview.setOrientation(LinearLayout.VERTICAL);
        int visibleThumbs = count > 4 ? 3 : 4;
        int index = 0;
        for (int rowIndex = 0; rowIndex < 2; rowIndex++) {
            LinearLayout row = new LinearLayout(this);
            for (int col = 0; col < 2; col++) {
                View tile;
                if (count > 4 && index == 3) {
                    tile = outfitMoreTile("+" + (count - visibleThumbs), 17);
                } else {
                    tile = outfitCollageTile(allItems.get(index), 5, 110);
                }
                row.addView(tile, new LinearLayout.LayoutParams(0, -1, 1));
                index++;
            }
            preview.addView(row, new LinearLayout.LayoutParams(-1, 0, 1));
        }
        return preview;
    }

    private View outfitDetailCollage(Outfit outfit) {
        List<ClothingItem> items = outfitAllItems(outfit);
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(8), dp(8), dp(8), dp(8));
        card.setBackground(rounded(Color.WHITE, 22, palette.outlineVariant, 1));
        card.setClipToOutline(true);
        if (items.isEmpty()) {
            card.addView(outfitMoreTile("No items", 15), new LinearLayout.LayoutParams(-1, dp(220)));
            return card;
        }
        int count = items.size();
        if (count == 1) {
            card.addView(outfitCollageTile(items.get(0), 10, 220), new LinearLayout.LayoutParams(-1, dp(260)));
        } else if (count == 2) {
            LinearLayout row = new LinearLayout(this);
            row.addView(outfitCollageTile(items.get(0), 10, 220), new LinearLayout.LayoutParams(0, dp(270), 1));
            row.addView(outfitCollageTile(items.get(1), 10, 220), new LinearLayout.LayoutParams(0, dp(270), 1));
            card.addView(row);
        } else if (count == 3) {
            card.addView(outfitCollageTile(items.get(0), 10, 220), new LinearLayout.LayoutParams(-1, dp(170)));
            LinearLayout row = new LinearLayout(this);
            row.addView(outfitCollageTile(items.get(1), 10, 150), new LinearLayout.LayoutParams(0, dp(130), 1));
            row.addView(outfitCollageTile(items.get(2), 10, 150), new LinearLayout.LayoutParams(0, dp(130), 1));
            card.addView(row);
        } else if (count == 4) {
            LinearLayout top = new LinearLayout(this);
            top.addView(outfitCollageTile(items.get(0), 10, 210), new LinearLayout.LayoutParams(0, dp(190), 1.35f));
            LinearLayout right = new LinearLayout(this);
            right.setOrientation(LinearLayout.VERTICAL);
            right.addView(outfitCollageTile(items.get(1), 10, 120), new LinearLayout.LayoutParams(-1, 0, 1));
            right.addView(outfitCollageTile(items.get(2), 10, 120), new LinearLayout.LayoutParams(-1, 0, 1));
            top.addView(right, new LinearLayout.LayoutParams(0, dp(190), 1));
            card.addView(top);
            card.addView(outfitCollageTile(items.get(3), 10, 140), new LinearLayout.LayoutParams(-1, dp(116)));
        } else {
            LinearLayout top = new LinearLayout(this);
            top.addView(outfitCollageTile(items.get(0), 10, 220), new LinearLayout.LayoutParams(0, dp(220), 1.25f));
            LinearLayout right = new LinearLayout(this);
            right.setOrientation(LinearLayout.VERTICAL);
            right.addView(outfitCollageTile(items.get(1), 10, 120), new LinearLayout.LayoutParams(-1, 0, 1));
            LinearLayout bottom = new LinearLayout(this);
            bottom.addView(outfitCollageTile(items.get(2), 10, 110), new LinearLayout.LayoutParams(0, -1, 1));
            bottom.addView(outfitMoreTile("+" + (count - 3), 18), new LinearLayout.LayoutParams(0, -1, 1));
            right.addView(bottom, new LinearLayout.LayoutParams(-1, 0, 1));
            top.addView(right, new LinearLayout.LayoutParams(0, dp(220), 1));
            card.addView(top);
        }
        return card;
    }

    private View outfitDetailItemsList(Outfit outfit) {
        LinearLayout list = groupedRows();
        for (ClothingItem item : outfitAllItems(outfit)) {
            LinearLayout row = new LinearLayout(this);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(dp(12), dp(8), dp(10), dp(8));
            row.setMinimumHeight(dp(64));
            row.setOnClickListener(v -> {
                clothingDetailReturnOutfit = outfit;
                showClothingDetail(item);
            });
            row.addView(outfitSmallThumb(item), new LinearLayout.LayoutParams(dp(48), dp(48)));
            LinearLayout copy = new LinearLayout(this);
            copy.setOrientation(LinearLayout.VERTICAL);
            copy.setPadding(dp(12), 0, 0, 0);
            copy.addView(text(value(item.name, item.category), 14, ink, true));
            copy.addView(text(PromptBuilder.categoryGroup(item.category), 12, muted, false));
            row.addView(copy, new LinearLayout.LayoutParams(0, -2, 1));
            ImageView chevron = iconView(R.drawable.ic_move_right);
            chevron.setColorFilter(muted);
            row.addView(chevron, new LinearLayout.LayoutParams(dp(24), dp(24)));
            list.addView(row);
        }
        return list;
    }

    private ImageView outfitSmallThumb(ClothingItem item) {
        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setPadding(dp(4), dp(4), dp(4), dp(4));
        image.setBackground(rounded(palette.softSurface(), 10, Color.TRANSPARENT, 0));
        image.setClipToOutline(true);
        if (!isBlank(item.imageUri)) {
            loadImage(image, item.imageUri, categoryDrawable(item.category), muted, 80);
        } else {
            image.setImageResource(categoryDrawable(item.category));
            image.setColorFilter(muted);
        }
        return image;
    }

    private View outfitCollageTile(ClothingItem item, int radiusDp, int imageSize) {
        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setPadding(dp(8), dp(8), dp(8), dp(8));
        image.setBackground(rounded(palette.softSurface(), radiusDp, Color.WHITE, 1));
        image.setClipToOutline(true);
        if (!isBlank(item.imageUri)) {
            loadImage(image, item.imageUri, categoryDrawable(item.category), muted, imageSize);
        } else {
            image.setImageResource(categoryDrawable(item.category));
            image.setColorFilter(muted);
        }
        return image;
    }

    private View outfitMoreTile(String label, int textSize) {
        TextView more = text(label, textSize, muted, true);
        more.setGravity(Gravity.CENTER);
        more.setBackground(rounded(palette.softSurface(), 10, Color.WHITE, 1));
        return more;
    }

    private View outfitPreviewGrid(Outfit outfit, int sizePx) {
        LinearLayout grid = new LinearLayout(this);
        grid.setOrientation(LinearLayout.VERTICAL);
        grid.setBackground(rounded(palette.softSurface(), 12, Color.TRANSPARENT, 0));
        grid.setClipToOutline(true);
        List<ClothingItem> items = outfitItems(outfit);
        int index = 0;
        for (int rowIndex = 0; rowIndex < 2; rowIndex++) {
            LinearLayout row = new LinearLayout(this);
            for (int col = 0; col < 2; col++) {
                View tile = index < items.size() ? outfitGridTile(items.get(index)) : outfitGridEmptyTile();
                row.addView(tile, new LinearLayout.LayoutParams(0, sizePx / 2, 1));
                index++;
            }
            grid.addView(row, new LinearLayout.LayoutParams(-1, 0, 1));
        }
        return grid;
    }

    private View outfitGridTile(ClothingItem item) {
        ImageView image = new ImageView(this);
        image.setScaleType(ImageView.ScaleType.FIT_CENTER);
        image.setPadding(dp(7), dp(7), dp(7), dp(7));
        image.setBackground(rounded(Color.TRANSPARENT, 0, Color.WHITE, 1));
        if (!isBlank(item.imageUri)) {
            loadImage(image, item.imageUri, categoryDrawable(item.category), muted, 130);
        } else {
            image.setImageResource(categoryDrawable(item.category));
            image.setColorFilter(muted);
        }
        return image;
    }

    private View outfitGridEmptyTile() {
        TextView empty = text("", 1, muted, false);
        empty.setBackground(rounded(Color.TRANSPARENT, 0, Color.WHITE, 1));
        return empty;
    }

    private List<ClothingItem> outfitItems(Outfit outfit) {
        List<ClothingItem> items = new ArrayList<>();
        for (String id : outfit.clothingIds) {
            ClothingItem item = findItem(id);
            if (item != null) items.add(item);
            if (items.size() >= 4) break;
        }
        return items;
    }

    private List<ClothingItem> outfitAllItems(Outfit outfit) {
        List<ClothingItem> items = new ArrayList<>();
        for (String id : outfit.clothingIds) {
            ClothingItem item = findItem(id);
            if (item != null) items.add(item);
        }
        return items;
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
            String group = PromptBuilder.categoryGroup(item.category);
            if (!isBlank(group) && !filters.contains(group)) filters.add(group);
        }
        if (!filters.contains(outfitFilterCategory)) filters.add(outfitFilterCategory);
        return filters;
    }

    private boolean outfitMatchesFilter(ClothingItem item) {
        return categoryMatchesFilter(item.category, outfitFilterCategory);
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
        Outfit savedOutfit;
        if (outfitEditingTarget == null) {
            outfits.add(0, outfitDraft);
            savedOutfit = outfitDraft;
        } else {
            outfitEditingTarget.name = outfitDraft.name;
            outfitEditingTarget.occasion = outfitDraft.occasion;
            outfitEditingTarget.season = outfitDraft.season;
            outfitEditingTarget.notes = outfitDraft.notes;
            outfitEditingTarget.clothingIds.clear();
            outfitEditingTarget.clothingIds.addAll(outfitDraft.clothingIds);
            outfitEditingTarget.updatedAt = outfitDraft.updatedAt;
            savedOutfit = outfitEditingTarget;
        }
        store.saveOutfits(outfits);
        String plannerDate = pendingPlannerDateKey;
        clearOutfitDraft();
        if (!isBlank(plannerDate)) {
            pendingPlannerDateKey = "";
            PlannerEntry entry = plannerEntryForDate(plannerDate, true);
            entry.outfitId = savedOutfit.id;
            if (isBlank(entry.occasion)) {
                entry.occasion = savedOutfit.occasion;
            }
            savePlannerState();
            renderPlannerDay(plannerDate);
        } else {
            renderOutfits();
        }
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

        ImageView favorite = iconView(R.drawable.ic_favorite);
        favorite.setColorFilter(muted);
        favorite.setPadding(dp(8), dp(8), dp(8), dp(8));
        row.addView(favorite, new LinearLayout.LayoutParams(dp(40), dp(40)));

        ImageView check = circularCheckIcon(false, primary, onPrimary, outline, 2);
        check.setTag("check");
        row.addView(check, new LinearLayout.LayoutParams(dp(34), dp(34)));
        row.setTag(new OutfitSelectionHolder(thumbnail, title, category, meta, favorite));
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
            holder.favorite.setVisibility(item.favorite ? View.VISIBLE : View.INVISIBLE);
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
        ImageView check = row.findViewWithTag("check");
        if (check != null) {
            updateCircularCheckIcon(check, selected, primary, onPrimary, outline, 2);
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
        general.addView(settingsRow(R.drawable.ic_detail_size, getString(R.string.my_sizes), savedSizesSummary(), v -> editSavedSizes()));
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
        appearance.addView(settingsRow(genderIcon(profileGender), getString(R.string.gender), value(profileGender, getString(R.string.add_gender)), v -> showGenderPicker()));
        appearance.addView(settingsRow(R.drawable.ic_detail_color, getString(R.string.eye_color), value(profileEyeColor, getString(R.string.add_eye_color)), v -> showProfileColorPicker(getString(R.string.eye_color), profileEyeColor, EYE_COLORS, value -> {
            profileEyeColor = value;
            store.saveProfileEyeColor(profileEyeColor);
        })));
        appearance.addView(settingsRow(R.drawable.ic_detail_color, getString(R.string.hair_color), value(profileHairColor, getString(R.string.add_hair_color)), v -> showProfileColorPicker(getString(R.string.hair_color), profileHairColor, HAIR_COLORS, value -> {
            profileHairColor = value;
            store.saveProfileHairColor(profileHairColor);
        })));
        appearance.addView(settingsRow(R.drawable.ic_detail_notes, getString(R.string.style_preference), value(profileStyle, getString(R.string.add_style_preference)), v -> editProfileStyle()));
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

    private void showGenderPicker() {
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        Dialog[] holder = new Dialog[1];
        body.addView(genderChoiceRow(getString(R.string.gender_male), R.drawable.ic_gender_male, holder));
        body.addView(genderChoiceRow(getString(R.string.gender_female), R.drawable.ic_gender_female, holder));
        body.addView(genderChoiceRow(getString(R.string.gender_diverse), R.drawable.ic_gender_diverse, holder));
        holder[0] = showMaterialDialog(getString(R.string.gender), body, "Cancel", null, null, null);
    }

    private View genderChoiceRow(String label, int iconRes, Dialog[] holder) {
        LinearLayout row = new LinearLayout(this);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setMinimumHeight(dp(64));
        row.setPadding(dp(4), dp(8), dp(4), dp(8));
        row.setOnClickListener(v -> {
            if (holder[0] != null) holder[0].dismiss();
            profileGender = label;
            store.saveProfileGender(profileGender);
            renderAiProfile();
        });

        ImageView icon = iconView(iconRes);
        icon.setColorFilter(primary);
        icon.setPadding(dp(8), dp(8), dp(8), dp(8));
        icon.setBackground(rounded(primaryContainer, 16, Color.TRANSPARENT, 0));
        row.addView(icon, new LinearLayout.LayoutParams(dp(48), dp(48)));

        TextView title = text(label, 16, ink, true);
        title.setPadding(dp(14), 0, 0, 0);
        row.addView(title, new LinearLayout.LayoutParams(0, -2, 1));

        if (label.equals(profileGender)) {
            row.addView(checkIcon(primary), new LinearLayout.LayoutParams(dp(40), dp(48)));
        }
        return row;
    }

    private int genderIcon(String gender) {
        if (getString(R.string.gender_male).equals(gender)) return R.drawable.ic_gender_male;
        if (getString(R.string.gender_female).equals(gender)) return R.drawable.ic_gender_female;
        if (getString(R.string.gender_diverse).equals(gender)) return R.drawable.ic_gender_diverse;
        return R.drawable.ic_info;
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

    private String savedSizesSummary() {
        List<String> parts = new ArrayList<>();
        if (!isBlank(savedShirtSize)) {
            parts.add("Shirt " + savedShirtSize);
        }
        if (!isBlank(savedPantsWaist) || !isBlank(savedPantsLength)) {
            parts.add("Pants W" + value(savedPantsWaist, "-") + "/L" + value(savedPantsLength, "-"));
        }
        if (!isBlank(savedShoeSize)) {
            parts.add("Shoes " + savedShoeSize);
        }
        if (parts.isEmpty()) return getString(R.string.my_sizes_subtitle_empty);
        StringBuilder summary = new StringBuilder();
        for (String part : parts) {
            if (summary.length() > 0) summary.append(" · ");
            summary.append(part);
        }
        return summary.toString();
    }

    private void editSavedSizes() {
        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);

        EditText shirt = sizeInput(getString(R.string.shirt_size), savedShirtSize, "M", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        EditText waist = sizeInput(getString(R.string.pants_waist), savedPantsWaist, "32", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText length = sizeInput(getString(R.string.pants_length), savedPantsLength, "32", InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        EditText shoes = sizeInput(getString(R.string.shoe_size), savedShoeSize, "43", InputType.TYPE_CLASS_TEXT);
        body.addView(labeledField(getString(R.string.shirt_size), shirt));
        body.addView(labeledField(getString(R.string.pants_waist), waist));
        body.addView(labeledField(getString(R.string.pants_length), length));
        body.addView(labeledField(getString(R.string.shoe_size), shoes));

        TextView hint = text("These sizes are used only for fit warnings in clothing details.", 13, muted, false);
        hint.setPadding(0, dp(10), 0, 0);
        body.addView(hint);

        showMaterialDialog(getString(R.string.my_sizes), body, "Cancel", "Save", null, () -> {
            savedShirtSize = shirt.getText().toString().trim();
            savedPantsWaist = waist.getText().toString().trim();
            savedPantsLength = length.getText().toString().trim();
            savedShoeSize = shoes.getText().toString().trim();
            store.saveSavedShirtSize(savedShirtSize);
            store.saveSavedPantsWaist(savedPantsWaist);
            store.saveSavedPantsLength(savedPantsLength);
            store.saveSavedShoeSize(savedShoeSize);
            Toast.makeText(this, getString(R.string.my_sizes_saved), Toast.LENGTH_SHORT).show();
            renderCategories();
        });
    }

    private EditText sizeInput(String label, String currentValue, String hint, int inputType) {
        EditText input = input(hint);
        input.setSingleLine(true);
        input.setInputType(inputType);
        input.setText(currentValue);
        input.setSelection(input.getText().length());
        input.setContentDescription(label);
        return input;
    }

    private View labeledField(String label, EditText input) {
        LinearLayout wrapper = new LinearLayout(this);
        wrapper.setOrientation(LinearLayout.VERTICAL);
        wrapper.setPadding(0, dp(7), 0, dp(7));
        TextView title = text(label, 12, muted, true);
        title.setPadding(0, 0, 0, dp(5));
        wrapper.addView(title);
        wrapper.addView(input, new LinearLayout.LayoutParams(-1, dp(54)));
        return wrapper;
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

    private void editProfileStyle() {
        EditText input = input(getString(R.string.style_preference_hint));
        input.setMinLines(2);
        input.setMaxLines(4);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setText(profileStyle);
        input.setSelection(input.getText().length());
        showMaterialDialog(getString(R.string.style_preference), input, "Cancel", "Save", null, () -> {
            profileStyle = trimLength(input.getText().toString().trim(), 240);
            store.saveProfileStyle(profileStyle);
            renderAiProfile();
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
                backup.put("savedShirtSize", savedShirtSize);
                backup.put("savedPantsWaist", savedPantsWaist);
                backup.put("savedPantsLength", savedPantsLength);
                backup.put("savedShoeSize", savedShoeSize);

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

                JSONArray plannerArray = new JSONArray();
                for (PlannerEntry entry : plannerEntries) {
                    plannerArray.put(entry.toJson());
                }
                backup.put("plannerEntries", plannerArray);
                if (wardrobeGapAnalysis != null) {
                    backup.put("wardrobeGapAnalysis", wardrobeGapAnalysis.toJson());
                }

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

        List<PlannerEntry> importedPlannerEntries = new ArrayList<>();
        JSONArray plannerArray = backup.optJSONArray("plannerEntries");
        if (plannerArray != null) {
            for (int i = 0; i < plannerArray.length(); i++) {
                PlannerEntry entry = PlannerEntry.fromJson(plannerArray.getJSONObject(i));
                if (!isBlank(entry.dateKey)) {
                    importedPlannerEntries.add(entry);
                }
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
        plannerEntries.clear();
        plannerEntries.addAll(importedPlannerEntries);
        rebuildPlannerIndex();
        categories.clear();
        categories.addAll(importedCategories);
        currency = backup.optString("currency", "$");
        savedShirtSize = backup.optString("savedShirtSize", "");
        savedPantsWaist = backup.optString("savedPantsWaist", "");
        savedPantsLength = backup.optString("savedPantsLength", "");
        savedShoeSize = backup.optString("savedShoeSize", "");
        int importedPrimary = backup.optInt("primaryColor", primary);
        JSONObject gapJson = backup.optJSONObject("wardrobeGapAnalysis");
        wardrobeGapAnalysis = gapJson == null ? null : WardrobeGapAnalysis.fromJson(gapJson);

        store.saveClothes(clothes);
        store.saveOutfits(outfits);
        store.savePlannerEntries(plannerEntries);
        store.saveCategories(categories);
        store.saveCurrency(currency);
        store.saveSavedShirtSize(savedShirtSize);
        store.saveSavedPantsWaist(savedPantsWaist);
        store.saveSavedPantsLength(savedPantsLength);
        store.saveSavedShoeSize(savedShoeSize);
        store.savePrimaryColor(importedPrimary);
        store.saveWardrobeGapAnalysis(wardrobeGapAnalysis);
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
        button.setGravity(Gravity.CENTER);
        button.setMinHeight(dp(48));
        button.setMinWidth(dp(72));
        button.setPadding(dp(14), 0, dp(14), 0);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            button.setElevation(0);
            button.setStateListAnimator(null);
        }
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

    private View sheetDragHandle(Dialog dialog, View sheet) {
        FrameLayout target = new FrameLayout(this);
        TextView bar = new TextView(this);
        bar.setBackground(rounded(outline, 3, Color.TRANSPARENT, 0));
        FrameLayout.LayoutParams barParams = new FrameLayout.LayoutParams(dp(44), dp(5), Gravity.CENTER);
        target.addView(bar, barParams);
        enableSheetDragDismiss(target, sheet, dialog);
        return target;
    }

    private ImageView checkIcon(int color) {
        ImageView icon = iconView(R.drawable.ic_check);
        icon.setColorFilter(color);
        icon.setPadding(dp(6), dp(6), dp(6), dp(6));
        return icon;
    }

    private ImageView circularCheckIcon(boolean selected, int selectedColor, int checkColor, int borderColor, int strokeDp) {
        ImageView icon = new ImageView(this);
        icon.setScaleType(ImageView.ScaleType.CENTER);
        icon.setPadding(dp(6), dp(6), dp(6), dp(6));
        updateCircularCheckIcon(icon, selected, selectedColor, checkColor, borderColor, strokeDp);
        return icon;
    }

    private void updateCircularCheckIcon(ImageView icon, boolean selected, int selectedColor, int checkColor, int borderColor, int strokeDp) {
        if (selected) {
            icon.setImageResource(R.drawable.ic_check);
            icon.setColorFilter(checkColor);
        } else {
            icon.setImageDrawable(null);
            icon.clearColorFilter();
        }
        icon.setBackground(oval(selected ? selectedColor : Color.TRANSPARENT, selected ? selectedColor : borderColor, strokeDp));
    }

    private void showBottomSheetDialog(Dialog dialog, LinearLayout sheet) {
        int extraBottomPadding = bottomSheetExtraBottomPadding();
        sheet.setPadding(
                sheet.getPaddingLeft(),
                sheet.getPaddingTop(),
                sheet.getPaddingRight(),
                sheet.getPaddingBottom() + extraBottomPadding
        );
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

    private int bottomSheetExtraBottomPadding() {
        return Math.max(dp(16), navigationBarInset());
    }

    private int navigationBarInset() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            WindowInsets insets = getWindow().getDecorView().getRootWindowInsets();
            if (insets != null) {
                return insets.getStableInsetBottom();
            }
        }
        int resourceId = getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            return getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    private void enableSheetDragDismiss(View dragTarget, View sheet, Dialog dialog) {
        final float[] startY = new float[1];
        final boolean[] dragging = new boolean[1];
        dragTarget.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    startY[0] = event.getRawY();
                    dragging[0] = true;
                    sheet.animate().cancel();
                    if (view.getParent() != null) {
                        view.getParent().requestDisallowInterceptTouchEvent(true);
                    }
                    return true;
                case MotionEvent.ACTION_MOVE:
                    if (!dragging[0]) return true;
                    float dy = Math.max(0, event.getRawY() - startY[0]);
                    sheet.setTranslationY(dy);
                    return true;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    if (view.getParent() != null) {
                        view.getParent().requestDisallowInterceptTouchEvent(false);
                    }
                    dragging[0] = false;
                    if (sheet.getTranslationY() > dp(72)) {
                        dialog.dismiss();
                    } else {
                        sheet.animate().translationY(0).setDuration(160).start();
                    }
                    return true;
                default:
                    return true;
            }
        });
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

    private Outfit findOutfit(String id) {
        if (isBlank(id)) return null;
        for (Outfit outfit : outfits) {
            if (outfit.id.equals(id)) return outfit;
        }
        return null;
    }

    private String dateKey(Calendar calendar) {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.getTime());
    }

    private Calendar calendarForDateKey(String key) {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(key);
            if (date != null) calendar.setTime(date);
        } catch (Exception ignored) {
        }
        startOfDay(calendar);
        return calendar;
    }

    private void startOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private Calendar weekStart(Calendar source) {
        Calendar start = (Calendar) source.clone();
        start.setFirstDayOfWeek(Calendar.MONDAY);
        startOfDay(start);
        int day = start.get(Calendar.DAY_OF_WEEK);
        int delta = day == Calendar.SUNDAY ? -6 : Calendar.MONDAY - day;
        start.add(Calendar.DAY_OF_MONTH, delta);
        return start;
    }

    private Calendar monthGridStart(Calendar source) {
        Calendar start = (Calendar) source.clone();
        start.setFirstDayOfWeek(Calendar.MONDAY);
        start.set(Calendar.DAY_OF_MONTH, 1);
        startOfDay(start);
        int day = start.get(Calendar.DAY_OF_WEEK);
        int delta = day == Calendar.SUNDAY ? -6 : Calendar.MONDAY - day;
        start.add(Calendar.DAY_OF_MONTH, delta);
        return start;
    }

    private String plannerDayTitle(Calendar day) {
        return new SimpleDateFormat("EEEE, MMM d", Locale.US).format(day.getTime());
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

    private static class CareOption {
        final String id;
        final String label;
        final int iconRes;

        CareOption(String id, String label, int iconRes) {
            this.id = id;
            this.label = label;
            this.iconRes = iconRes;
        }
    }

    private static class ClothingTileHolder {
        final ImageView image;
        final ImageView favorite;

        ClothingTileHolder(ImageView image, ImageView favorite) {
            this.image = image;
            this.favorite = favorite;
        }
    }

    private static class OutfitSelectionHolder {
        final ImageView thumbnail;
        final TextView title;
        final TextView category;
        final TextView meta;
        final ImageView favorite;

        OutfitSelectionHolder(ImageView thumbnail, TextView title, TextView category, TextView meta, ImageView favorite) {
            this.thumbnail = thumbnail;
            this.title = title;
            this.category = category;
            this.meta = meta;
            this.favorite = favorite;
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

}
