package com.rudderstack.android.integrations.amplitude

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json
@JsonIgnoreProperties(ignoreUnknown = true)

internal class ECommerceProduct {
    @SerializedName(value = "productId", alternate = ["product_id"])
    @JsonProperty(value = "productId")
    @JsonAlias("product_id")
    @Json(name = "productId")
    var productId: String? = null
    get() = field?: product_id
    @SerializedName("sku")
    @JsonProperty("sku")
    @Json(name = "sku")
    var sku: String? = null

    @SerializedName("category")
    @JsonProperty("category")
    @Json(name = "category")
    var category: String? = null

    @SerializedName("name")
    @JsonProperty("name")
    @Json(name = "name")
    var name: String? = null

    @SerializedName("price")
    @JsonProperty("price")
    @Json(name = "price")
    var price = 0.0

    @SerializedName("quantity")
    @JsonProperty("quantity")
    @Json(name = "quantity")
    var quantity = 0.0
    @Json(name = "product_id")
    @JsonIgnore
    private var product_id : String? = null

}