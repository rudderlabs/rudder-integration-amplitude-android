package com.rudderstack.android.integrations.amplitude;

import com.amplitude.android.events.Identify;
import com.google.gson.Gson;

import org.apache.commons.lang3.ArrayUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collections;
import java.util.List;
import java.util.Map;

class TraitsHandler {
    private static Gson gson = new Gson();

    static void incrementTrait(Identify identify, String key, Object value) {
        if (value instanceof Double) {
            identify.add(key, (Double) value);
        } else if (value instanceof Float) {
            identify.add(key, (Float) value);
        } else if (value instanceof Integer) {
            identify.add(key, (Integer) value);
        } else if (value instanceof Long) {
            identify.add(key, (Long) value);
        }
    }

    static void appendTrait(Identify identify, String key, Object value) {
        appendNumberTraitsIfApplicable(identify, key, value);
        appendStringToTraitsIfApplicable(identify, key, value);
        appendBooleanToTraitsIfApplicable(identify, key, value);
        appendArraysToTraitsIfApplicable(identify, key, value);
        appendJsonArrayToTraitsIfApplicable(identify, key, value);
        appendJsonObjectToTraitsIfApplicable(identify, key, value);
        appendMapToTraitsIfApplicable(identify, key, value);
        appendListToTraitsIfApplicable(identify, key, value);

    }

    private static void appendListToTraitsIfApplicable(Identify identify, String key, Object value) {
        if (value instanceof List) {
            try {
                identify.append(key, (List<? extends Object>) value);
            } catch (ClassCastException exception) {
                exception.printStackTrace();
            }
        }
    }

    private static void appendMapToTraitsIfApplicable(Identify identify, String key, Object value) {
        if (value instanceof Map) {
            try {
                identify.append(key, (Map<String, ? extends Object>) value);
            } catch (ClassCastException exception) {
                exception.printStackTrace();
            }
        }
    }


    private static void appendJsonObjectToTraitsIfApplicable(Identify identify, String key, Object value) {
        if (value instanceof JSONObject) {
            Map<String, ?> map = convertJsonObjectToMap((JSONObject) value);
            identify.append(key, map);
        }
    }

    private static void appendJsonArrayToTraitsIfApplicable(Identify identify, String key, Object value) {
        if (value instanceof JSONArray) {
            List<?> list = convertJsonArrayToList((JSONArray) value);
            identify.append(key, list);
        }
    }

    private static void appendBooleanToTraitsIfApplicable(Identify identify, String key, Object value) {
        if (value instanceof Boolean) {
            identify.append(key, (Boolean) value);
        }
    }

    private static void appendStringToTraitsIfApplicable(Identify identify, String key, Object value) {
        if (value instanceof String) {
            identify.append(key, String.valueOf(value));
        }
    }

    private static void appendArraysToTraitsIfApplicable(Identify identify, String key, Object value) {
        if (value instanceof Integer[]) {
            identify.append(key, (Integer[]) value);
        } else if (value instanceof Long[]) {
            identify.append(key, (Long[]) value);
        } else if (value instanceof Float[]) {
            identify.append(key, (Float[]) value);
        } else if (value instanceof Double[]) {
            identify.append(key, (Double[]) value);
        } else if (value instanceof String[]) {
            identify.append(key, (String[]) value);
        } else if (value instanceof Boolean[]) {
            identify.append(key, (Boolean[]) value);
        }
    }

    private static void appendNumberTraitsIfApplicable(Identify identify, String key, Object value) {
        if (value instanceof Integer) {
            identify.append(key, (Integer) value);
        } else if (value instanceof Long) {
            identify.append(key, (Long) value);
        } else if (value instanceof Float) {
            identify.append(key, (Float) value);
        } else if (value instanceof Double) {
            identify.append(key, (Double) value);
        }
    }

    private static List<?> convertJsonArrayToList(JSONArray jsonArray) {
        try {
            return gson.fromJson((jsonArray).toString(2), List.class);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private static Map<String, ?> convertJsonObjectToMap(JSONObject jsonObject) {
        try {
            return gson.fromJson((jsonObject).toString(2), Map.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Collections.emptyMap();
    }

    static void prependTrait(Identify identify, String key, Object value) {
        prependNumbersIfApplicable(identify, key, value);
        prependArraysIfApplicable(identify, key, value);
        prependStringIfApplicable(identify, key, value);
        prependBooleanIfApplicable(identify, key, value);
        prependJsonArrayToTraitsIfApplicable(identify, key, value);
        prependJsonObjectToTraitsIfApplicable(identify, key, value);
        prependListToTraitsIfApplicable(identify, key, value);
        prependMapToTraitsIfApplicable(identify, key, value);

    }

    private static void prependListToTraitsIfApplicable(Identify identify, String key, Object value) {
        if (value instanceof List) {
            try {
                identify.prepend(key, (List<? extends Object>) value);
            } catch (ClassCastException exception) {
                exception.printStackTrace();
            }
        }
    }

    private static void prependMapToTraitsIfApplicable(Identify identify, String key, Object value) {
        if (value instanceof Map) {
            try {
                identify.prepend(key, (Map<String, ? extends Object>) value);
            } catch (ClassCastException exception) {
                exception.printStackTrace();
            }
        }
    }

    private static void prependJsonObjectToTraitsIfApplicable(Identify identify, String key, Object value) {
        if (value instanceof JSONObject) {
            Map<String, ?> map = convertJsonObjectToMap((JSONObject) value);
            identify.prepend(key, map);
        }
    }

    private static void prependJsonArrayToTraitsIfApplicable(Identify identify, String key, Object value) {
        if (value instanceof JSONArray) {
            List<?> list = convertJsonArrayToList((JSONArray) value);
            identify.prepend(key, list);
        }
    }

    private static void prependBooleanIfApplicable(Identify identify, String key, Object value) {

        if (value instanceof Boolean) {
            identify.prepend(key, (Boolean) value);
        }
    }

    private static void prependStringIfApplicable(Identify identify, String key, Object value) {
        if (value instanceof String) {
            identify.prepend(key, String.valueOf(value));
        }
    }

    private static void prependArraysIfApplicable(Identify identify, String key, Object value) {
        if (value instanceof Integer[]) {
            identify.prepend(key, (Integer[]) value);
        }
        if (value instanceof Long[]) {
            identify.prepend(key, (Long[]) value);
        }
        if (value instanceof Float[]) {
            identify.prepend(key, (Float[]) value);
        }
        if (value instanceof Double[]) {
            identify.prepend(key, (Double[]) value);
        }
        if (value instanceof String[]) {
            identify.prepend(key, (String[]) value);
        }
        if (value instanceof Boolean[]) {
            identify.prepend(key, (Boolean[]) value);
        }
    }

    private static void prependNumbersIfApplicable(Identify identify, String key, Object value) {
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
    }

    // amplitude does casting automatically for direct datatypes but not for arrays
    static void setOnce(Identify identify, String key, Object value) {
        setNumberOnceIfApplicable(identify, key, value);
        setArraysOnceIfApplicable(identify, key, value);
        setStringOnceIfApplicable(identify, key, value);
        setBooleanOnceIfApplicable(identify, key, value);
        setJsonArrayOnceIfApplicable(identify, key, value);
        setJsonObjectOnceIfApplicable(identify, key, value);
        setMapOnceIfApplicable(identify, key, value);
        setListOnceIfApplicable(identify, key, value);
    }

    private static void setListOnceIfApplicable(Identify identify, String key, Object value) {
        if (value instanceof List) {
            try {
                identify.setOnce(key, (List<? extends Object>) value);
            } catch (ClassCastException exception) {
                exception.printStackTrace();
            }
        }
    }

    private static void setMapOnceIfApplicable(Identify identify, String key, Object value) {
        if (value instanceof Map) {
            try {
                identify.setOnce(key, (Map<String, ? extends Object>) value);
            } catch (ClassCastException exception) {
                exception.printStackTrace();
            }
        }
    }
    private static void setJsonObjectOnceIfApplicable(Identify identify, String key, Object value) {
        if (value instanceof JSONObject) {
            Map<String, ?> map = convertJsonObjectToMap((JSONObject) value);
            identify.setOnce(key, map);
        }
    }

    private static void setJsonArrayOnceIfApplicable(Identify identify, String key, Object value) {
        if (value instanceof JSONArray) {
            List<?> list = convertJsonArrayToList((JSONArray) value);
            identify.setOnce(key, list);
        }
    }

    private static void setBooleanOnceIfApplicable(Identify identify, String key, Object value) {
        if (value instanceof Boolean) {
            identify.setOnce(key, (Boolean) value);
        }
    }

    private static void setStringOnceIfApplicable(Identify identify, String key, Object value) {
        if (value instanceof String) {
            identify.setOnce(key, String.valueOf(value));
        }
    }

    private static void setArraysOnceIfApplicable(Identify identify, String key, Object value) {
        if (value instanceof Integer[]) {
            identify.setOnce(key, (Integer[]) value);
        }
        if (value instanceof Long[]) {
            identify.setOnce(key, (Long[]) value);
        }
        if (value instanceof Float[]) {
            identify.setOnce(key, (Float[]) value);
        }
        if (value instanceof Double[]) {
            identify.setOnce(key, (Double[]) value);
        }
        if (value instanceof String[]) {
            identify.setOnce(key, (String[]) value);
        }
        if (value instanceof Boolean[]) {
            identify.setOnce(key, (Boolean[]) value);
        }
    }

    private static void setNumberOnceIfApplicable(Identify identify, String key, Object value) {
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
    }

    // amplitude does casting automatically for datatypes but not for arrays
    static void setTrait(Identify identify, String key, Object value) {
        setNumberTrait(identify, key, value);
        if (value instanceof String) {
            identify.set(key, String.valueOf(value));
        }
        if (value instanceof Boolean) {
            identify.set(key, value);
        }
        if (value instanceof Integer[]) {
            identify.set(key, ArrayUtils.toPrimitive((Integer[]) value));
        }
        if (value instanceof Long[]) {
            identify.set(key, ArrayUtils.toPrimitive((Long[]) value));
        }
        if (value instanceof Float[]) {
            identify.set(key, ArrayUtils.toPrimitive((Float[]) value));
        }
        if (value instanceof Double[]) {
            identify.set(key, ArrayUtils.toPrimitive((Double[]) value));
        }
        if (value instanceof String[]) {
            identify.set(key, (String[]) value);
        }
        if (value instanceof Boolean[]) {
            identify.set(key, ArrayUtils.toPrimitive((Boolean[]) value));
        }
        if (value instanceof JSONArray) {
            identify.set(key, (JSONArray) value);
        }
        if (value instanceof JSONObject) {
            identify.set(key, (JSONObject) value);
        }
    }

    private static void setNumberTrait(Identify identify, String key, Object value) {
        if (value instanceof Integer ||
                value instanceof Long ||
                value instanceof Float ||
                value instanceof Double) {
            identify.set(key, value);
        }
    }
}
