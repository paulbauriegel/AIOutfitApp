package com.example.aioutfit;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class WardrobeStore {
    private static final String PREFS = "wardrobe";
    private static final String KEY_CLOTHES = "clothes";
    private static final String KEY_OUTFITS = "outfits";
    private static final String KEY_PLANNER_ENTRIES = "plannerEntries";
    private static final String KEY_CATEGORIES = "categories";
    private static final String KEY_CURRENCY = "currency";
    private static final String KEY_PRIMARY_COLOR = "primaryColor";
    private static final String KEY_OPENAI_API_KEY = "openAiApiKey";
    private static final String KEY_OPENAI_BASE_URL = "openAiBaseUrl";
    private static final String KEY_OPENAI_MODEL = "openAiModel";
    private static final String KEY_PROFILE_BODY_IMAGE = "profileBodyImage";
    private static final String KEY_PROFILE_FACE_IMAGE = "profileFaceImage";
    private static final String KEY_PROFILE_GENDER = "profileGender";
    private static final String KEY_PROFILE_EYE_COLOR = "profileEyeColor";
    private static final String KEY_PROFILE_HAIR_COLOR = "profileHairColor";
    private static final String DEFAULT_OPENAI_BASE_URL = "https://eu.api.openai.com/v1";
    private static final String DEFAULT_OPENAI_MODEL = "gpt-5.4";
    private final SharedPreferences prefs;

    public WardrobeStore(Context context) {
        prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<ClothingItem> loadClothes() {
        List<ClothingItem> clothes = new ArrayList<>();
        JSONArray array = readArray(KEY_CLOTHES);
        for (int i = 0; i < array.length(); i++) {
            JSONObject json = array.optJSONObject(i);
            if (json != null) {
                clothes.add(ClothingItem.fromJson(json));
            }
        }
        return clothes;
    }

    public void saveClothes(List<ClothingItem> clothes) {
        JSONArray array = new JSONArray();
        for (ClothingItem item : clothes) {
            try {
                array.put(item.toJson());
            } catch (JSONException ignored) {
            }
        }
        prefs.edit().putString(KEY_CLOTHES, array.toString()).apply();
    }

    public List<Outfit> loadOutfits() {
        List<Outfit> outfits = new ArrayList<>();
        JSONArray array = readArray(KEY_OUTFITS);
        for (int i = 0; i < array.length(); i++) {
            JSONObject json = array.optJSONObject(i);
            if (json != null) {
                outfits.add(Outfit.fromJson(json));
            }
        }
        return outfits;
    }

    public void saveOutfits(List<Outfit> outfits) {
        JSONArray array = new JSONArray();
        for (Outfit outfit : outfits) {
            try {
                array.put(outfit.toJson());
            } catch (JSONException ignored) {
            }
        }
        prefs.edit().putString(KEY_OUTFITS, array.toString()).apply();
    }

    public List<PlannerEntry> loadPlannerEntries() {
        List<PlannerEntry> entries = new ArrayList<>();
        JSONArray array = readArray(KEY_PLANNER_ENTRIES);
        for (int i = 0; i < array.length(); i++) {
            JSONObject json = array.optJSONObject(i);
            if (json != null) {
                PlannerEntry entry = PlannerEntry.fromJson(json);
                if (entry.dateKey != null && !entry.dateKey.trim().isEmpty()) {
                    entries.add(entry);
                }
            }
        }
        return entries;
    }

    public void savePlannerEntries(List<PlannerEntry> entries) {
        JSONArray array = new JSONArray();
        for (PlannerEntry entry : entries) {
            try {
                array.put(entry.toJson());
            } catch (JSONException ignored) {
            }
        }
        prefs.edit().putString(KEY_PLANNER_ENTRIES, array.toString()).apply();
    }

    public List<String> loadCategories() {
        String saved = prefs.getString(KEY_CATEGORIES, null);
        Set<String> categories = new LinkedHashSet<>();
        if (saved == null) {
            categories.addAll(Arrays.asList("Tops", "Bottoms", "Dresses", "Outerwear", "Shoes", "Accessories", "Sportswear", "Formalwear"));
        } else {
            JSONArray array = readArray(KEY_CATEGORIES);
            for (int i = 0; i < array.length(); i++) {
                String category = array.optString(i).trim();
                if (!category.isEmpty()) {
                    categories.add(category);
                }
            }
        }
        return new ArrayList<>(categories);
    }

    public void saveCategories(List<String> categories) {
        JSONArray array = new JSONArray();
        for (String category : categories) {
            if (category != null && !category.trim().isEmpty()) {
                array.put(category.trim());
            }
        }
        prefs.edit().putString(KEY_CATEGORIES, array.toString()).apply();
    }

    public String loadCurrency() {
        return prefs.getString(KEY_CURRENCY, "$");
    }

    public void saveCurrency(String currency) {
        String value = currency == null || currency.trim().isEmpty() ? "$" : currency.trim();
        prefs.edit().putString(KEY_CURRENCY, value).apply();
    }

    public int loadPrimaryColor() {
        return prefs.getInt(KEY_PRIMARY_COLOR, 0xFF2A584B);
    }

    public void savePrimaryColor(int color) {
        prefs.edit().putInt(KEY_PRIMARY_COLOR, color).apply();
    }

    public String loadOpenAiApiKey() {
        return prefs.getString(KEY_OPENAI_API_KEY, "");
    }

    public void saveOpenAiApiKey(String apiKey) {
        String value = apiKey == null ? "" : apiKey.trim();
        prefs.edit().putString(KEY_OPENAI_API_KEY, value).apply();
    }

    public String loadOpenAiBaseUrl() {
        return prefs.getString(KEY_OPENAI_BASE_URL, DEFAULT_OPENAI_BASE_URL);
    }

    public void saveOpenAiBaseUrl(String baseUrl) {
        String value = baseUrl == null || baseUrl.trim().isEmpty() ? DEFAULT_OPENAI_BASE_URL : baseUrl.trim();
        prefs.edit().putString(KEY_OPENAI_BASE_URL, value).apply();
    }

    public String loadOpenAiModel() {
        return prefs.getString(KEY_OPENAI_MODEL, DEFAULT_OPENAI_MODEL);
    }

    public void saveOpenAiModel(String model) {
        String value = model == null || model.trim().isEmpty() ? DEFAULT_OPENAI_MODEL : model.trim();
        prefs.edit().putString(KEY_OPENAI_MODEL, value).apply();
    }

    public String loadProfileBodyImage() {
        return prefs.getString(KEY_PROFILE_BODY_IMAGE, "");
    }

    public void saveProfileBodyImage(String uri) {
        prefs.edit().putString(KEY_PROFILE_BODY_IMAGE, uri == null ? "" : uri.trim()).apply();
    }

    public String loadProfileFaceImage() {
        return prefs.getString(KEY_PROFILE_FACE_IMAGE, "");
    }

    public void saveProfileFaceImage(String uri) {
        prefs.edit().putString(KEY_PROFILE_FACE_IMAGE, uri == null ? "" : uri.trim()).apply();
    }

    public String loadProfileGender() {
        return prefs.getString(KEY_PROFILE_GENDER, "");
    }

    public void saveProfileGender(String gender) {
        prefs.edit().putString(KEY_PROFILE_GENDER, gender == null ? "" : gender.trim()).apply();
    }

    public String loadProfileEyeColor() {
        return prefs.getString(KEY_PROFILE_EYE_COLOR, "");
    }

    public void saveProfileEyeColor(String color) {
        prefs.edit().putString(KEY_PROFILE_EYE_COLOR, color == null ? "" : color.trim()).apply();
    }

    public String loadProfileHairColor() {
        return prefs.getString(KEY_PROFILE_HAIR_COLOR, "");
    }

    public void saveProfileHairColor(String color) {
        prefs.edit().putString(KEY_PROFILE_HAIR_COLOR, color == null ? "" : color.trim()).apply();
    }

    private JSONArray readArray(String key) {
        try {
            return new JSONArray(prefs.getString(key, "[]"));
        } catch (JSONException exception) {
            return new JSONArray();
        }
    }
}
