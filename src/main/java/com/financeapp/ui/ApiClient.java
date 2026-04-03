package com.financeapp.ui;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiClient {

    private static final String BASE = "http://localhost:8080";

    public static void post(String endpoint, String json) {
        try {
            URL url = new URL(BASE + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(json.getBytes());
            os.flush();

            conn.getResponseCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void put(String endpoint) {
        try {
            URL url = new URL(BASE + endpoint);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("PUT");
            conn.getResponseCode();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}