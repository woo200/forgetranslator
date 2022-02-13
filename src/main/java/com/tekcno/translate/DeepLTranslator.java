package com.tekcno.translate;

import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class DeepLTranslator {

    protected String api_key;

    public DeepLTranslator(String api_key) {
        this.api_key = api_key;
    }

    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }

    public static class Translation {
        private String detected_source_language;
        private String text;

        public String getDetected_source_language() {
            return detected_source_language;
        }

        public void setDetected_source_language(String detected_source_language) {
            this.detected_source_language = detected_source_language;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    public static class Translations {
        private List<Translation> translations;

        public List<Translation> getTranslations() {
            return translations;
        }
    }

    private String encodeValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }

    public Translation translate(String v) {
        return translate(v, "EN-US");
    }

    public Translation translate(String v, String target_lang)
    {
        try {
            URL url = new URL("https://api-free.deepl.com/v2/translate?auth_key=" + encodeValue(api_key));
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept", "application/json");
            con.setDoOutput(true);
            String requestBody = "auth_key=" + encodeValue(api_key) + "&text=" + encodeValue(v) + "&target_lang=" + target_lang;
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = requestBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }
                Translations translations = new Gson().fromJson(response.toString(), Translations.class);
                return translations.getTranslations().get(0);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
