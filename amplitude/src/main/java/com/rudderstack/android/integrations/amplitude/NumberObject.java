package com.rudderstack.android.integrations.amplitude;

public class NumberObject {
    private final Double number;

    public NumberObject(Object val) {
        String str = String.valueOf(val);
        this.number = Double.valueOf(str);
    }

    public Double getNumber() {
        return number;
    }
}
