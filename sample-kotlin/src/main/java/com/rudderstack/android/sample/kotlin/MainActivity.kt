package com.rudderstack.android.sample.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rudderlabs.android.sample.kotlin.R
import com.rudderstack.android.sdk.core.RudderOption
import com.rudderstack.android.sdk.core.RudderProperty
import com.rudderstack.android.sdk.core.RudderTraits
import org.json.JSONArray
import org.json.JSONObject


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Identify call
        MainApplication.rudderClient.identify(
            "user_id",
            RudderTraits()
                .putFirstName("Shane")
                .putLastName("Warne")
                .putEmail("warne@shane.com"),
            null
        )


        // screen call without name and category
        MainApplication.rudderClient.screen(
            "", "", RudderProperty().putValue("foo", "bar"),
            null
        )

        // screen call with name and category
        MainApplication.rudderClient.screen(
            "MainActivity",
            "HomeScreen",
            RudderProperty().putValue("foo", "bar"),
            null
        )

        // group call with group traits
        MainApplication.rudderClient.group(
            "new_group_id",
            RudderTraits().putAge("24")
                .putName("Test Group Name")
                .putPhone("1234567891")
                .put("company_id", "RS")
                .put("company_name", "RudderStack")
        )

        // normal track call with no event properties
        MainApplication.rudderClient.track("account: created")
        MainApplication.rudderClient.track("account: authenticated")

        // track call with event properties
        val property = RudderProperty()
        property.put("key_1", "val_1")
        property.put("key_2", "val_2")
        property.put("optOutOfSession",true)
        MainApplication.rudderClient.track("challenge: applied points", property)

        // payload for Ecommerce Track event
        val payload = RudderProperty()
        val productsArray = JSONArray()
        payload.put("order_id", 1234)
        payload.put("affiliation", "Apple Store")
        payload.put("value", 20)
        payload.put("revenue", 15.00)
        payload.put("shipping", 22)
        payload.put("tax", 1)
        payload.put("discount", 1.5)
        payload.put("coupon", "ImagePro")
        payload.put("currency", "USD")
        payload.put("products", productsArray)
        val product1 = JSONObject()
        product1.put("product_id", 123)
        product1.put("sku", "G-32")
        product1.put("name", "Monopoly")
        product1.put("price", 14)
        product1.put("quantity", 1)
        product1.put("category", "Games")
        product1.put("url", "https://www.website.com/product/path")
        product1.put("image_url", "https://www.website.com/product/path.jpg")
        val product2 = JSONObject()
        product2.put("product_id", 345)
        product2.put("sku", "F-32")
        product2.put("name", "UNO")
        product2.put("price", 3.45)
        product2.put("quantity", 2)
        product2.put("category", "Games")
        product2.put("url", "https://www.website.com/product/path")
        product2.put("image_url", "https://www.website.com/product/path.jpg")
        productsArray.put(product1)
        productsArray.put(product2)


        // Ecommerce Track Call
        MainApplication.rudderClient.track("Shopping Done", payload)

        val Pro1 = mutableMapOf("product_id" to 345, "sku" to "F-32", "name" to "UNO", "price" to 19, "quantity" to 2)
        val Pro2 = mutableMapOf("product_id" to 456, "sku" to "F-32", "name" to "UNO", "price" to 6, "quantity" to 5)
        val products = mutableListOf(Pro1, Pro2)
        MainApplication.rudderClient.track(
            "Item Purchased",
            RudderProperty()
                .putValue("orderId", "101010")
                .putValue("revenue",120)
                .putValue("products", products)
        )
        // Using Map as a Data type for Products
        val Pro1 = mutableMapOf("productId" to "345", "sku" to "F-32", "name" to "UNO", "price" to 20.00, "quantity" to 2)
        val Pro2 = mutableMapOf("productId" to "456", "sku" to "F-32", "name" to "UNO", "price" to 12.00, "quantity" to 5)
        val products = mutableListOf(Pro1, Pro2)
        MainApplication.rudderClient.track(
            "Order Completed",
            RudderProperty()
                .putValue("orderId", "40404040")
                .putValue("revenue",220.00)
                .putValue("products", products)
        )

        // reset call
        MainApplication.rudderClient.reset();

    }
}
