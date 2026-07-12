package com.example.aioutfit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class WardrobeGapAnalysis {
    public String question;
    public long createdAt;
    public int overallCompletenessPercent;
    public String summaryTitle;
    public String summaryBody;
    public final List<MissingCategory> missingCategories = new ArrayList<>();
    public final List<String> impact = new ArrayList<>();

    public WardrobeGapAnalysis() {
        createdAt = System.currentTimeMillis();
    }

    public JSONObject toJson() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("question", value(question));
        json.put("createdAt", createdAt);
        json.put("overallCompletenessPercent", overallCompletenessPercent);
        json.put("summaryTitle", value(summaryTitle));
        json.put("summaryBody", value(summaryBody));
        JSONArray categories = new JSONArray();
        for (MissingCategory category : missingCategories) {
            categories.put(category.toJson());
        }
        json.put("missingCategories", categories);
        JSONArray impactArray = new JSONArray();
        for (String item : impact) {
            if (item != null && !item.trim().isEmpty()) impactArray.put(item.trim());
        }
        json.put("impact", impactArray);
        return json;
    }

    public static WardrobeGapAnalysis fromJson(JSONObject json) {
        WardrobeGapAnalysis analysis = new WardrobeGapAnalysis();
        analysis.question = json.optString("question");
        analysis.createdAt = json.optLong("createdAt", System.currentTimeMillis());
        analysis.overallCompletenessPercent = Math.max(0, Math.min(100, json.optInt("overallCompletenessPercent", 0)));
        analysis.summaryTitle = json.optString("summaryTitle");
        analysis.summaryBody = json.optString("summaryBody");
        JSONArray categories = json.optJSONArray("missingCategories");
        if (categories != null) {
            for (int i = 0; i < categories.length(); i++) {
                JSONObject category = categories.optJSONObject(i);
                if (category != null) analysis.missingCategories.add(MissingCategory.fromJson(category));
            }
        }
        JSONArray impactArray = json.optJSONArray("impact");
        if (impactArray != null) {
            for (int i = 0; i < impactArray.length(); i++) {
                String item = impactArray.optString(i);
                if (item != null && !item.trim().isEmpty()) analysis.impact.add(item.trim());
            }
        }
        return analysis;
    }

    private static String value(String text) {
        return text == null ? "" : text;
    }

    public static class MissingCategory {
        public String title;
        public String priority;
        public String iconCategory;
        public String shortReason;
        public final List<MissingPieceSuggestion> suggestedItems = new ArrayList<>();
        public final List<String> howItHelps = new ArrayList<>();
        public final List<String> outfitIdeas = new ArrayList<>();

        JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("title", value(title));
            json.put("priority", value(priority));
            json.put("iconCategory", value(iconCategory));
            json.put("shortReason", value(shortReason));
            JSONArray suggestions = new JSONArray();
            for (MissingPieceSuggestion suggestion : suggestedItems) {
                suggestions.put(suggestion.toJson());
            }
            json.put("suggestedItems", suggestions);
            json.put("howItHelps", stringArray(howItHelps));
            json.put("outfitIdeas", stringArray(outfitIdeas));
            return json;
        }

        static MissingCategory fromJson(JSONObject json) {
            MissingCategory category = new MissingCategory();
            category.title = json.optString("title");
            category.priority = json.optString("priority");
            category.iconCategory = json.optString("iconCategory");
            category.shortReason = json.optString("shortReason");
            JSONArray suggestions = json.optJSONArray("suggestedItems");
            if (suggestions != null) {
                for (int i = 0; i < suggestions.length(); i++) {
                    JSONObject suggestion = suggestions.optJSONObject(i);
                    if (suggestion != null) category.suggestedItems.add(MissingPieceSuggestion.fromJson(suggestion));
                }
            }
            readStrings(json.optJSONArray("howItHelps"), category.howItHelps);
            readStrings(json.optJSONArray("outfitIdeas"), category.outfitIdeas);
            return category;
        }
    }

    public static class MissingPieceSuggestion {
        public String name;
        public String color;
        public String reason;
        public final List<String> tags = new ArrayList<>();

        JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("name", value(name));
            json.put("color", value(color));
            json.put("reason", value(reason));
            json.put("tags", stringArray(tags));
            return json;
        }

        static MissingPieceSuggestion fromJson(JSONObject json) {
            MissingPieceSuggestion suggestion = new MissingPieceSuggestion();
            suggestion.name = json.optString("name");
            suggestion.color = json.optString("color");
            suggestion.reason = json.optString("reason");
            readStrings(json.optJSONArray("tags"), suggestion.tags);
            return suggestion;
        }
    }

    private static JSONArray stringArray(List<String> values) {
        JSONArray array = new JSONArray();
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) array.put(value.trim());
        }
        return array;
    }

    private static void readStrings(JSONArray array, List<String> target) {
        if (array == null) return;
        for (int i = 0; i < array.length(); i++) {
            String value = array.optString(i);
            if (value != null && !value.trim().isEmpty()) target.add(value.trim());
        }
    }
}
