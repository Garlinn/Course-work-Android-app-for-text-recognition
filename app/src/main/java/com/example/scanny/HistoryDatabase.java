package com.example.scanny;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HistoryDatabase {

    private static final String PREFS_NAME = "scanny_history";
    private static final String KEY_ITEMS  = "items";
    private static final int    MAX_ITEMS  = 100;

    public static class ScanRecord {
        public final String id;
        public final String text;
        public final long   timestamp;

        public ScanRecord(String id, String text, long timestamp) {
            this.id        = id;
            this.text      = text;
            this.timestamp = timestamp;
        }

        public String getDateLabel(Context context) {
            return formatDate(context, timestamp);
        }
    }

    private final SharedPreferences prefs;

    public HistoryDatabase(Context context) {
        prefs = context.getApplicationContext()
                       .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public ScanRecord save(String text) {
        long now = System.currentTimeMillis();
        String id = String.valueOf(now);
        ScanRecord record = new ScanRecord(id, text, now);
        try {
            JSONArray arr = loadJson();
            JSONObject obj = new JSONObject();
            obj.put("id", id);
            obj.put("text", text);
            obj.put("timestamp", now);
            JSONArray newArr = new JSONArray();
            newArr.put(obj);
            for (int i = 0; i < arr.length() && i < MAX_ITEMS - 1; i++) newArr.put(arr.get(i));
            prefs.edit().putString(KEY_ITEMS, newArr.toString()).apply();
        } catch (JSONException e) { e.printStackTrace(); }
        return record;
    }

    public List<ScanRecord> getAll() {
        List<ScanRecord> list = new ArrayList<>();
        try {
            JSONArray arr = loadJson();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                list.add(new ScanRecord(obj.getString("id"), obj.getString("text"), obj.getLong("timestamp")));
            }
        } catch (JSONException e) { e.printStackTrace(); }
        return list;
    }

    public ScanRecord getById(String id) {
        for (ScanRecord r : getAll()) if (r.id.equals(id)) return r;
        return null;
    }

    public void delete(String id) {
        try {
            JSONArray arr = loadJson();
            JSONArray newArr = new JSONArray();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (!obj.getString("id").equals(id)) newArr.put(obj);
            }
            prefs.edit().putString(KEY_ITEMS, newArr.toString()).apply();
        } catch (JSONException e) { e.printStackTrace(); }
    }

    private JSONArray loadJson() {
        String raw = prefs.getString(KEY_ITEMS, "[]");
        try { return new JSONArray(raw); }
        catch (JSONException e) { return new JSONArray(); }
    }

    public static String formatDate(Context context, long timestamp) {
        long diff    = System.currentTimeMillis() - timestamp;
        long minutes = diff / 60_000;
        long hours   = diff / 3_600_000;
        long days    = diff / 86_400_000;

        if (minutes < 1)  return context.getString(R.string.time_just_now);
        if (minutes < 60) return context.getString(R.string.time_minutes_ago, minutes);
        if (hours < 24)   return context.getString(R.string.time_hours_ago, hours);
        if (days == 1)    return context.getString(R.string.time_yesterday);
        if (days < 30)    return context.getString(R.string.time_days_ago, days);
        if (days < 60)    return context.getString(R.string.time_one_month_ago);
        long months = days / 30;
        if (months < 12)  return context.getString(R.string.time_months_ago, months);
        return new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(new Date(timestamp));
    }
}
