package com.rudderstack.android.integrations.amplitude;

import com.amplitude.api.Identify;

import org.json.JSONArray;
import org.json.JSONObject;

import org.apache.commons.lang3.ArrayUtils;


public class TraitsHandler {
    public static void incrementTrait(String key, Object value, Identify identify) {
        if (value instanceof Double) {
            identify.add(key, (Double) value);
        }
        if (value instanceof Float) {
            identify.add(key, (Float) value);
        }
        if (value instanceof Integer) {
            identify.add(key, (Integer) value);
        }
        if (value instanceof Long) {
            identify.add(key, (Long) value);
        }
        if (value instanceof String) {
            identify.add(key, String.valueOf(value));
        }
        if (value instanceof JSONObject) {
            identify.add(key, (JSONObject) value);
        }
    }

    public static void appendTrait(String key, Object value, Identify identify) {
        if (value instanceof Integer) {
            identify.append(key, (Integer) value);
        }
        if (value instanceof Long) {
            identify.append(key, (Long) value);
        }
        if (value instanceof Float) {
            identify.append(key, (Float) value);
        }
        if (value instanceof Double) {
            identify.append(key, (Double) value);
        }
        if (value instanceof Integer[]) {
            identify.append(key, ArrayUtils.toPrimitive((Integer[]) value));
        }
        if (value instanceof String) {
            identify.append(key, String.valueOf(value));
        }
        if (value instanceof Boolean) {
            identify.append(key, (Boolean) value);
        }
        if (value instanceof long[]) {
            identify.append(key, (long[]) value);
        }
        if (value instanceof float[]) {
            identify.append(key, (float[]) value);
        }
        if (value instanceof double[]) {
            identify.append(key, (double[]) value);
        }
        if (value instanceof String[]) {
            identify.append(key, (String[]) value);
        }
        if (value instanceof boolean[]) {
            identify.append(key, (boolean[]) value);
        }
        if (value instanceof JSONArray) {
            identify.append(key, (JSONArray) value);
        }
        if (value instanceof JSONObject) {
            identify.append(key, (JSONObject) value);
        }
    }

    public static void prependTrait(String key, Object value, Identify identify) {
        if (value instanceof Integer) {
            identify.prepend(key, (Integer) value);
        }
        if (value instanceof Long) {
            identify.prepend(key, (Long) value);
        }
        if (value instanceof Float) {
            identify.prepend(key, (Float) value);
        }
        if (value instanceof Double) {
            identify.prepend(key, (Double) value);
        }
        if (value instanceof Integer[]) {
            identify.prepend(key, ArrayUtils.toPrimitive((Integer[]) value));
        }
        if (value instanceof String) {
            identify.prepend(key, String.valueOf(value));
        }
        if (value instanceof Boolean) {
            identify.prepend(key, (Boolean) value);
        }
        if (value instanceof long[]) {
            identify.prepend(key, (long[]) value);
        }
        if (value instanceof float[]) {
            identify.prepend(key, (float[]) value);
        }
        if (value instanceof double[]) {
            identify.prepend(key, (double[]) value);
        }
        if (value instanceof String[]) {
            identify.prepend(key, (String[]) value);
        }
        if (value instanceof boolean[]) {
            identify.prepend(key, (boolean[]) value);
        }
        if (value instanceof JSONArray) {
            identify.prepend(key, (JSONArray) value);
        }
        if (value instanceof JSONObject) {
            identify.prepend(key, (JSONObject) value);
        }

    }

    // amplitude does casting automatically for direct datatypes but not for arrays
    public static void setOnce(String key, Object value, Identify identify) {
        if (value instanceof Integer) {
            identify.setOnce(key, (Integer) value);
        }
        if (value instanceof Long) {
            identify.setOnce(key, (Long) value);
        }
        if (value instanceof Float) {
            identify.setOnce(key, (Float) value);
        }
        if (value instanceof Double) {
            identify.setOnce(key, (Double) value);
        }
        if (value instanceof int[]) {
            identify.setOnce(key, (int[]) value);
        }
        if (value instanceof String) {
            identify.setOnce(key, String.valueOf(value));
        }
        if (value instanceof Boolean) {
            identify.setOnce(key, (Boolean) value);
        }
        if (value instanceof long[]) {
            identify.setOnce(key, (long[]) value);
        }
        if (value instanceof float[]) {
            identify.setOnce(key, (float[]) value);
        }
        if (value instanceof double[]) {
            identify.setOnce(key, (double[]) value);
        }
        if (value instanceof String[]) {
            identify.setOnce(key, (String[]) value);
        }
        if (value instanceof boolean[]) {
            identify.setOnce(key, (boolean[]) value);
        }
        if (value instanceof JSONArray) {
            identify.setOnce(key, (JSONArray) value);
        }
        if (value instanceof JSONObject) {
            identify.setOnce(key, (JSONObject) value);
        }
    }

    // amplitude does casting automatically for datatypes but not for arrays
    public static void setTrait(String key, Object value, Identify identify) {
        if (value instanceof Integer) {
            identify.set(key, (Integer) value);
        }
        if (value instanceof Long) {
            identify.set(key, (Long) value);
        }
        if (value instanceof Float) {
            identify.set(key, (Float) value);
        }
        if (value instanceof Double) {
            identify.set(key, (Double) value);
        }
        if (value instanceof int[]) {
            identify.set(key, (int[]) value);
        }
        if (value instanceof String) {
            identify.set(key, String.valueOf(value));
        }
        if (value instanceof Boolean) {
            identify.set(key, (Boolean) value);
        }
        if (value instanceof long[]) {
            identify.set(key, (long[]) value);
        }
        if (value instanceof float[]) {
            identify.set(key, (float[]) value);
        }
        if (value instanceof double[]) {
            identify.set(key, (double[]) value);
        }
        if (value instanceof String[]) {
            identify.set(key, (String[]) value);
        }
        if (value instanceof boolean[]) {
            identify.set(key, (boolean[]) value);
        }
        if (value instanceof JSONArray) {
            identify.set(key, (JSONArray) value);
        }
        if (value instanceof JSONObject) {
            identify.set(key, (JSONObject) value);
        }
    }
}
