package com.example.aioutfit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Outfit {
    public final String id;
    public String name;
    public String occasion;
    public String season;
    public String notes;
    public final List<String> clothingIds = new ArrayList<>();
    public long updatedAt;

    public Outfit() {
        id = UUID.randomUUID().toString();
        updatedAt = System.currentTimeMillis();
    }

    private Outfit(String id) {
        this.id = id;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", id);
        json.put("name", value(name));
        json.put("occasion", value(occasion));
        json.put("season", value(season));
        json.put("notes", value(notes));
        json.put("updatedAt", updatedAt);
        JSONArray ids = new JSONArray();
        for (String clothingId : clothingIds) {
            ids.put(clothingId);
        }
        json.put("clothingIds", ids);
        return json;
    }

    public static Outfit fromJson(JSONObject json) {
        Outfit outfit = new Outfit(json.optString("id", UUID.randomUUID().toString()));
        outfit.name = json.optString("name");
        outfit.occasion = json.optString("occasion");
        outfit.season = json.optString("season");
        outfit.notes = json.optString("notes");
        outfit.updatedAt = json.optLong("updatedAt", System.currentTimeMillis());
        JSONArray ids = json.optJSONArray("clothingIds");
        if (ids != null) {
            for (int i = 0; i < ids.length(); i++) {
                outfit.clothingIds.add(ids.optString(i));
            }
        }
        return outfit;
    }

    private String value(String text) {
        return text == null ? "" : text;
    }
}
