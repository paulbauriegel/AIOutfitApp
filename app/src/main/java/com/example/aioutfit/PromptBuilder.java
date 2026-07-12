package com.example.aioutfit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class PromptBuilder {
    private PromptBuilder() {
    }

    static String buildOutfitSuggestionPrompt(List<ClothingItem> clothes, String profileGender, String profileEyeColor,
                                              String profileHairColor, String requestedOccasion, String profileStyle,
                                              String currentSeason) throws JSONException {
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
            json.put("damage", value(item.damage, ""));
            json.put("size", value(joinNonEmpty(item.size, item.waist, item.length), ""));
            json.put("notes", value(item.notes, ""));
            wardrobe.put(json);
        }

        return "Create one practical outfit using only the wardrobe items below. "
                + "Choose 2 to 5 items that work together. Prefer a complete outfit with top, bottom, shoes, and outerwear/accessory when available. "
                + "Avoid visibly damaged items unless the user specifically asks for them or the damage is irrelevant to the occasion. "
                + "Requested occasion: " + value(requestedOccasion, "not specified") + ". "
                + "Additional user preference or prompt: " + value(profileStyle, "not specified") + ". "
                + "Current season based on today's date is " + currentSeason + "; strongly prefer items marked for " + currentSeason + " or Any season, unless the wardrobe lacks them. "
                + "Consider this wearer profile when useful: gender=" + value(profileGender, "unknown")
                + ", eye color=" + value(profileEyeColor, "unknown")
                + ", hair color=" + value(profileHairColor, "unknown") + ". "
                + "If an occasion was requested, return that exact occasion value in the JSON occasion field. "
                + "Use exact item ids from the wardrobe. Return only valid JSON with this shape: "
                + "{\"name\":\"Short outfit name\",\"occasion\":\"" + value(requestedOccasion, "Casual") + "\",\"season\":\"" + currentSeason + "\",\"notes\":\"Why these pieces work together\",\"clothingIds\":[\"id1\",\"id2\"]}. "
                + "Do not include markdown. Wardrobe items: " + wardrobe;
    }

    static String buildWardrobeGapPrompt(List<ClothingItem> clothes, String question, String profileGender,
                                         String profileEyeColor, String profileHairColor) throws JSONException {
        JSONArray wardrobe = new JSONArray();
        Map<String, Integer> groupCounts = new LinkedHashMap<>();
        for (ClothingItem item : clothes) {
            JSONObject json = new JSONObject();
            String group = categoryGroup(item.category);
            groupCounts.put(group, groupCounts.containsKey(group) ? groupCounts.get(group) + 1 : 1);
            json.put("name", value(item.name, item.category));
            json.put("category", value(item.category, "Uncategorized"));
            json.put("group", group);
            json.put("color", value(item.color, ""));
            json.put("season", value(item.season, ""));
            json.put("material", value(item.material, ""));
            json.put("damage", value(item.damage, ""));
            json.put("notes", value(item.notes, ""));
            wardrobe.put(json);
        }

        JSONObject counts = new JSONObject();
        for (Map.Entry<String, Integer> entry : groupCounts.entrySet()) {
            counts.put(entry.getKey(), entry.getValue());
        }

        return "Analyze this wardrobe and recommend what type of clothing is missing. "
                + "User goal: " + value(question, "Improve wardrobe versatility") + ". "
                + "Use the wearer profile only when relevant: gender=" + value(profileGender, "unknown")
                + ", eye color=" + value(profileEyeColor, "unknown")
                + ", hair color=" + value(profileHairColor, "unknown") + ". "
                + "Focus on practical wardrobe gaps, versatility, seasons, and categories. "
                + "Return only valid JSON with this exact shape: "
                + "{\"overallCompletenessPercent\":78,\"summaryTitle\":\"Good start!\",\"summaryBody\":\"Short summary\","
                + "\"impact\":[\"+12 outfit combinations\",\"More versatility\",\"Spring/autumn ready\"],"
                + "\"missingCategories\":[{\"title\":\"Light outerwear\",\"priority\":\"High\",\"iconCategory\":\"Outerwear\","
                + "\"shortReason\":\"Why this is missing\",\"suggestedItems\":[{\"name\":\"Trench coat\",\"color\":\"Beige / sand\",\"reason\":\"Why it fits\",\"tags\":[\"Versatile\",\"Timeless\"]}],"
                + "\"howItHelps\":[\"Adds a finishing layer\"],\"outfitIdeas\":[\"Shirt + jeans + light outerwear\"]}]}. "
                + "Return 3 to 5 missingCategories. Priority must be High, Medium, or Low. "
                + "iconCategory must be one of: Tops, Bottoms, Dresses, Outerwear, Shoes, Accessories, Sportswear, Formalwear. "
                + "Do not return real shopping products, brands, URLs, or prices. Do not include markdown. "
                + "Category counts: " + counts + ". Wardrobe items: " + wardrobe;
    }

    static String categoryGroup(String category) {
        if (isBlank(category)) return "Other";
        String lower = category.toLowerCase();
        if (containsAny(lower, "shirt", "polo", "top", "blouse", "sweater", "sleeve", "hoodie", "cardigan", "knit", "tank", "bodysuit")) return "Tops";
        if (containsAny(lower, "pants", "jeans", "chinos", "shorts", "skirt", "trouser")) return "Bottoms";
        if (containsAny(lower, "dress", "jumpsuit")) return "Dresses";
        if (containsAny(lower, "coat", "jacket", "blazer", "outerwear")) return "Outerwear";
        if (containsAny(lower, "shoe", "sneaker", "boot", "sandal", "loafer")) return "Shoes";
        if (containsAny(lower, "bag", "belt", "hat", "scarf", "watch", "sunglass", "accessor")) return "Accessories";
        return category;
    }

    private static boolean containsAny(String value, String... needles) {
        for (String needle : needles) {
            if (value.contains(needle)) return true;
        }
        return false;
    }

    private static String joinNonEmpty(String... values) {
        StringBuilder builder = new StringBuilder();
        for (String value : values) {
            if (isBlank(value)) continue;
            if (builder.length() > 0) builder.append(" · ");
            builder.append(value.trim());
        }
        return builder.toString();
    }

    private static String value(String text, String fallback) {
        return isBlank(text) ? fallback : text.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
