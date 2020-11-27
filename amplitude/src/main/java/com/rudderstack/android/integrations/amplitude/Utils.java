package com.rudderstack.android.integrations.amplitude;

import android.util.Log;

import com.google.gson.Gson;
import com.rudderstack.android.sdk.core.RudderLogger;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class Utils {
    private static final String TRAITS_KEY = "traits";

    public static boolean isNullOrEmpty(Collection collection) {
        return collection == null || collection.isEmpty();
    }

    public static Set<String> getStringSet(ArrayList<Map<String, Object>> traitsList) {
        Set<String> stringSet = new HashSet<>();
        for (int i = 0; i < traitsList.size(); i++) {
            Map<String, Object> traitsMap = traitsList.get(i);
            stringSet.add((String) traitsMap.get(TRAITS_KEY));
        }
        return stringSet;
    }

    public static JSONArray getProducts(Map<String,Object> eventProperties)throws Exception {
        JSONArray products = null;
        if (eventProperties != null) {
            if (eventProperties.containsKey("products")) {
                products = getJSONArray(eventProperties.get("products"));
            }
        }
        return products;
    }

    public static JSONArray simplifyProducts(JSONArray products) throws Exception
    {
        JSONArray allProducts = new JSONArray();
        for (int i = 0; i < products.length(); i++) {
            EcommerceProduct ecommerceProduct = new Gson().fromJson(((JSONObject) products.get(i)).toString(), EcommerceProduct.class);
            String ecommerceProductString = new Gson().toJson(ecommerceProduct);
            JSONObject newProduct = new Gson().fromJson(ecommerceProductString, JSONObject.class);
            allProducts.put(newProduct);
        }
        return allProducts;
    }


    public static Map<String, Object> jsonToMap(JSONObject json) {
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            Iterator<?> keys = json.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                Object value = json.get(key);
                map.put(key, value);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    public static JSONArray getJSONArray(Object object) {
        if (object instanceof JSONArray) {
            return (JSONArray) object;
        } else {
            // if the object receieved was ArrayList
            ArrayList arrayList = (ArrayList) object;
            JSONArray jsonArray = new JSONArray(arrayList);
            return jsonArray;
        }
    }

    public static int rudderLogToAndroidLog(int rudderLogLevel) {
        if (rudderLogLevel >= RudderLogger.RudderLogLevel.DEBUG) {
            return Log.VERBOSE;
        }
        return Log.ERROR;
    }
}
