package com.example.aioutfit;

import org.json.JSONException;
import org.json.JSONObject;

public class PlannerEntry {
    public String dateKey;
    public String outfitId;
    public String occasion;
    public String notes;
    public boolean worn;
    public String weatherSummary;
    public String temperature;
    public String weatherIcon;

    public PlannerEntry(String dateKey) {
        this.dateKey = dateKey;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("dateKey", value(dateKey));
        json.put("outfitId", value(outfitId));
        json.put("occasion", value(occasion));
        json.put("notes", value(notes));
        json.put("worn", worn);
        json.put("weatherSummary", value(weatherSummary));
        json.put("temperature", value(temperature));
        json.put("weatherIcon", value(weatherIcon));
        return json;
    }

    public static PlannerEntry fromJson(JSONObject json) {
        PlannerEntry entry = new PlannerEntry(json.optString("dateKey"));
        entry.outfitId = json.optString("outfitId");
        entry.occasion = json.optString("occasion");
        entry.notes = json.optString("notes");
        entry.worn = json.optBoolean("worn", false);
        entry.weatherSummary = json.optString("weatherSummary");
        entry.temperature = json.optString("temperature");
        entry.weatherIcon = json.optString("weatherIcon");
        return entry;
    }

    private String value(String text) {
        return text == null ? "" : text;
    }
}
