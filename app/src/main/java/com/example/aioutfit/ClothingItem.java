package com.example.aioutfit;

import org.json.JSONException;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClothingItem {
    public final String id;
    public String name;
    public String category;
    public String color;
    public String season;
    public String size;
    public String waist;
    public String length;
    public String price;
    public String brand;
    public String material;
    public String damage;
    public String care;
    public String notes;
    public String link;
    public String imageUri;
    public boolean favorite;
    public final List<String> imageUris = new ArrayList<>();
    public long addedAt;
    public long updatedAt;

    public ClothingItem() {
        id = UUID.randomUUID().toString();
        addedAt = System.currentTimeMillis();
        updatedAt = System.currentTimeMillis();
    }

    private ClothingItem(String id) {
        this.id = id;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", value(name));
        json.put("category", value(category));
        json.put("color", value(color));
        json.put("season", value(season));
        json.put("size", value(size));
        json.put("waist", value(waist));
        json.put("length", value(length));
        json.put("price", value(price));
        json.put("brand", value(brand));
        json.put("material", value(material));
        json.put("damage", value(damage));
        json.put("care", value(care));
        json.put("notes", value(notes));
        json.put("link", value(link));
        json.put("imageUri", value(imageUri));
        json.put("favorite", favorite);
        JSONArray images = new JSONArray();
        for (String uri : imageUris) {
            if (uri != null && !uri.trim().isEmpty()) {
                images.put(uri);
            }
        }
        json.put("imageUris", images);
        json.put("addedAt", addedAt);
        json.put("updatedAt", updatedAt);
        return json;
    }

    public static ClothingItem fromJson(JSONObject json) {
        ClothingItem item = new ClothingItem(json.optString("id", UUID.randomUUID().toString()));
        item.name = json.optString("name");
        item.category = json.optString("category");
        item.color = json.optString("color");
        item.season = json.optString("season");
        item.size = json.optString("size");
        item.waist = json.optString("waist");
        item.length = json.optString("length");
        item.price = json.optString("price");
        item.brand = json.optString("brand");
        item.material = json.optString("material");
        item.damage = json.optString("damage");
        item.care = json.optString("care");
        item.notes = json.optString("notes");
        item.link = json.optString("link");
        item.imageUri = json.optString("imageUri");
        item.favorite = json.optBoolean("favorite", false);
        JSONArray images = json.optJSONArray("imageUris");
        if (images != null) {
            for (int i = 0; i < images.length(); i++) {
                String uri = images.optString(i);
                if (uri != null && !uri.trim().isEmpty() && !item.imageUris.contains(uri)) {
                    item.imageUris.add(uri);
                }
            }
        }
        if (item.imageUris.isEmpty() && item.imageUri != null && !item.imageUri.trim().isEmpty()) {
            item.imageUris.add(item.imageUri);
        } else if ((item.imageUri == null || item.imageUri.trim().isEmpty()) && !item.imageUris.isEmpty()) {
            item.imageUri = item.imageUris.get(0);
        }
        item.updatedAt = json.optLong("updatedAt", System.currentTimeMillis());
        item.addedAt = json.optLong("addedAt", item.updatedAt);
        return item;
    }

    public boolean matches(String query, String selectedCategory) {
        boolean categoryMatches = selectedCategory == null || selectedCategory.equals("All") || selectedCategory.equals(category);
        if (!categoryMatches) {
            return false;
        }
        if (query == null || query.trim().isEmpty()) {
            return true;
        }
        String needle = query.trim().toLowerCase();
        return contains(name, needle)
                || contains(category, needle)
                || contains(color, needle)
                || contains(season, needle)
                || contains(size, needle)
                || contains(waist, needle)
                || contains(length, needle)
                || contains(price, needle)
                || contains(brand, needle)
                || contains(material, needle)
                || contains(damage, needle)
                || contains(care, needle)
                || contains(notes, needle)
                || contains(link, needle)
                || (favorite && contains("favorite", needle));
    }

    private boolean contains(String value, String needle) {
        return value != null && value.toLowerCase().contains(needle);
    }

    private String value(String text) {
        return text == null ? "" : text;
    }
}
