package com.example.aioutfit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

class OpenAiClient {
    private OpenAiClient() {
    }

    static String requestText(String apiKey, String baseUrl, String model, String prompt, int maxOutputTokens) throws IOException, JSONException {
        HttpURLConnection connection = (HttpURLConnection) new URL(responsesUrl(baseUrl)).openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(20000);
        connection.setReadTimeout(45000);
        connection.setDoOutput(true);
        connection.setRequestProperty("Authorization", "Bearer " + apiKey);
        connection.setRequestProperty("Content-Type", "application/json");

        JSONObject request = new JSONObject();
        request.put("model", model == null || model.trim().isEmpty() ? "gpt-5.4" : model.trim());
        request.put("store", false);
        request.put("max_output_tokens", maxOutputTokens);
        request.put("input", prompt);

        byte[] bytes = request.toString().getBytes(StandardCharsets.UTF_8);
        try (OutputStream output = connection.getOutputStream()) {
            output.write(bytes);
        }

        int code = connection.getResponseCode();
        InputStream responseStream = code >= 200 && code < 300 ? connection.getInputStream() : connection.getErrorStream();
        String responseText = readStream(responseStream);
        connection.disconnect();

        if (code < 200 || code >= 300) {
            throw new IOException("OpenAI API " + code + " " + extractError(responseText));
        }

        JSONObject response = new JSONObject(responseText);
        return extractOutputText(response);
    }

    private static String responsesUrl(String baseUrl) {
        String normalized = baseUrl == null || baseUrl.trim().isEmpty() ? "https://eu.api.openai.com/v1" : baseUrl.trim();
        if (normalized.endsWith("/responses")) return normalized;
        if (normalized.endsWith("/")) return normalized + "responses";
        return normalized + "/responses";
    }

    private static String extractOutputText(JSONObject response) {
        JSONArray output = response.optJSONArray("output");
        if (output == null) return response.optString("output_text", "");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < output.length(); i++) {
            JSONObject item = output.optJSONObject(i);
            if (item == null) continue;
            JSONArray content = item.optJSONArray("content");
            if (content == null) continue;
            for (int j = 0; j < content.length(); j++) {
                JSONObject part = content.optJSONObject(j);
                if (part == null) continue;
                String text = part.optString("text");
                if (!text.isEmpty()) {
                    if (builder.length() > 0) builder.append("\n");
                    builder.append(text);
                }
            }
        }
        return builder.toString().trim();
    }

    private static String extractError(String responseText) {
        try {
            JSONObject json = new JSONObject(responseText);
            JSONObject error = json.optJSONObject("error");
            if (error != null) {
                return error.optString("message", responseText);
            }
        } catch (JSONException ignored) {
        }
        return responseText;
    }

    private static String readStream(InputStream stream) throws IOException {
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
}
