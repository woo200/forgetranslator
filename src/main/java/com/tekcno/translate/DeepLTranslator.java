package com.tekcno.translate;

import com.google.gson.Gson;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Access DEEPL REST api
 */
public class DeepLTranslator {

    protected String api_key;

    /**
     * Custom DEEPL translator implementation
     * @param api_key API key for DEEPL
     */
    public DeepLTranslator(String api_key) {
        this.api_key = api_key;
    }

    /**
     * Custom DEEPL translator implementation
     * @param api_key API key for DEEPL
     */
    public void setApi_key(String api_key) {
        this.api_key = api_key;
    }

    /**
     * Stores single translation details
     */
    public static class Translation {
        private String detected_source_language;
        private String text;

        /**
         * Get the detected source language
         * @return detected source language
         */
        public String getDetected_source_language() {
            return detected_source_language;
        }

        /**
         * Set detected source language
         * @param detected_source_language detected source language
         */
        public void setDetected_source_language(String detected_source_language) {
            this.detected_source_language = detected_source_language;
        }

        /**
         * Get translated Text
         * @return translated text
         */
        public String getText() {
            return text;
        }

        /**
         * Set translated text
         * @param text translated text
         */
        public void setText(String text) {
            this.text = text;
        }
    }

    /**
     * Stores multiple translations
     * For GSON decoding
     */
    public static class Translations {
        private List<Translation> translations;

        /**
         * Get list of translations
         * @return list of translations
         */
        public List<Translation> getTranslations() {
            return translations;
        }
    }

    /**
     * URL encode value
     * @param value value to encode
     * @return URL encoded value
     * @throws UnsupportedEncodingException :(
     */
    private String encodeValue(String value) throws UnsupportedEncodingException {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
    }

    /**
     * Translate text to ENGLISH US
     * @param v Text to translate
     * @return Translated text
     */
    public Translation translate(String v) {
        return translate(v, "EN-US");
    }

    /**
     * Translate text to {@code target_lang}
     * @param v Text to translate
     * @param target_lang Language to translate {@code v} to
     * @return translated text
     */
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
