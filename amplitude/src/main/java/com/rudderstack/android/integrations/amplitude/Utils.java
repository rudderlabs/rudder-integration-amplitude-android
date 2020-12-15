package com.rudderstack.android.integrations.amplitude;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

class Utils {
    private static final String TRAITS_KEY = "traits";

    @NonNull
    static Set<String> getStringSet(@Nullable List<Map<String, Object>> traitsList) {
        Set<String> stringSet = new HashSet<>();
        if (traitsList != null) {
            for (int i = 0; i < traitsList.size(); i++) {
                Map<String, Object> traitsMap = traitsList.get(i);
                stringSet.add((String) traitsMap.get(TRAITS_KEY));
            }
        }
        return stringSet;
    }

    @Nullable
    static JSONArray getProducts(@Nullable Map<String, Object> eventProperties) {
        JSONArray products = null;
        if (eventProperties != null) {
            if (eventProperties.containsKey("products")) {
                products = getJSONArray(eventProperties.get("products"));
            }
        }
        return products;
    }

    @NonNull
    static JSONArray simplifyProducts(@Nullable JSONArray products) throws Exception {
        Gson gson = new Gson();
        JSONArray allProducts = new JSONArray();
        if (products != null) {
            for (int i = 0; i < products.length(); i++) {
                ECommerceProduct eCommerceProduct = gson.fromJson(
                        products.getJSONObject(i).toString(),
                        ECommerceProduct.class
                );
                allProducts.put(new JSONObject(gson.toJson(eCommerceProduct)));
            }
        }
        return allProducts;
    }

    @Nullable
    static Map<String, Object> jsonToMap(@Nullable JSONObject json) throws JSONException {
        if (json == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        Iterator<?> keys = json.keys();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            map.put(key, json.get(key));
        }
        return map;
    }

    @NonNull
    static JSONArray getJSONArray(@Nullable Object object) {
        if (object instanceof JSONArray) {
            return (JSONArray) object;
        } else {
            // if the object received was ArrayList
            return new JSONArray((ArrayList) object);
        }
    }
}
