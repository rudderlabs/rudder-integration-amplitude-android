package com.rudderstack.android.integrations.amplitude;

import com.google.gson.annotations.SerializedName;

public class EcommerceProduct {
    @SerializedName(value = "productId", alternate = "product_id")
    String productId;
    @SerializedName("sku")
    String sku;
    @SerializedName("category")
    String category;
    @SerializedName("name")
    String name;
    @SerializedName("price")
    float price;
    @SerializedName("quantity")
    float quantity;
}

