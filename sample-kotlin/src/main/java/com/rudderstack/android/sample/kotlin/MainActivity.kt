package com.rudderstack.android.sample.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rudderlabs.android.sample.kotlin.R
import com.rudderstack.android.sdk.core.RudderProperty
import com.rudderstack.android.sdk.core.RudderTraits
import org.json.JSONArray
import org.json.JSONObject
import java.util.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //TC:1. Track before identify
        MainApplication.rudderClient.track("Shopping Done")


        //TC: 2.
        MainApplication.rudderClient.identify("User_111");


        //TC:3. Identify with the previous user id and new user properties (Merging should properly done)

        val traits = RudderTraits()
        traits.putBirthday(Date())
        traits.putEmail("abc@123.com")
        traits.putFirstName("Manashi")
        traits.putLastName("Mazumder")
        traits.putGender("F")
        traits.putPhone("5555555555")
        MainApplication.rudderClient.identify("User_111", traits, null)


        //TC:4. Track after identify

        MainApplication.rudderClient.track(
            "Shopping Done After Identify",
            RudderProperty()
                .putValue("details", "anything")
        )


        //TC:5. Sending revenue event with revenue, product id and without quantity (using their native SDK revenue is created)

        val Pro1 = mutableMapOf(
            "productId" to "345",
            "sku" to "F-32",
            "name" to "UNO",
            "price" to 20.00,
            "quantity" to 2
        )
        val Pro2 = mutableMapOf(
            "productId" to "456",
            "sku" to "F-32",
            "name" to "UNO",
            "price" to 12.00,
            "quantity" to 5
        )
        val products = mutableListOf(Pro1, Pro2)
        MainApplication.rudderClient.track(
            "Order Completed",
            RudderProperty()
                .putValue("orderId", "101010")
                .putValue("revenue", 220.00)
                .putValue("products", products)
        )


        //TC: **6. Sending revenue event with revenue, quantity, product id, receipt, receipt signature to check the verified revenue

        val Pro3 = mutableMapOf(
            "productId" to "111",
            "sku" to "F-32",
            "name" to "UNO",
            "price" to 19,
            "quantity" to 2
        )
        val Pro4 = mutableMapOf(
            "productId" to "222",
            "sku" to "F-32",
            "name" to "UNO",
            "price" to 6,
            "quantity" to 5
        )
        val products1 = mutableListOf(Pro3, Pro4)
        MainApplication.rudderClient.track(
            "Order Completed",
            RudderProperty()
                .putValue("orderId", "202020")
                .putValue("revenue", 100)
                .putValue("quantity", 2)
                .putValue("products", products1)
                .putValue("receipt", "reciept name")
                .putValue("receiptSignature", "receipt Signature")
        )


        //TC: 7. Sending revenue event with revenue as String/Integer/Empty

        val Pro5 = mutableMapOf(
            "productId" to "111",
            "sku" to "F-32",
            "name" to "UNO",
            "price" to 19,
            "quantity" to 2
        )
        val Pro6 = mutableMapOf(
            "productId" to "222",
            "sku" to "F-32",
            "name" to "UNO",
            "price" to 6,
            "quantity" to 5
        )
        val products2 = mutableListOf(Pro1, Pro2)
        MainApplication.rudderClient.track(
            "Order Completed",
            RudderProperty()
                .putValue("orderId", "303030")
                .putValue("revenue", "30")
                .putValue("products", products2)
        )

        // OR

        MainApplication.rudderClient.track(
            "Order Completed",
            RudderProperty()
                .putValue("orderId", "404040")
                .putValue("products", products2)
        )


        //TC: 8. Sending revenue event with multiple products by enabling "Track revenue per product" ("revenue or price should be present in each product object")

        val Pro7 = mutableMapOf(
            "productId" to "111",
            "sku" to "F-32",
            "name" to "UNO",
            "price" to 19,
            "quantity" to 2
        )
        val Pro8 = mutableMapOf(
            "productId" to "222",
            "sku" to "F-32",
            "name" to "UNO",
            "price" to 6,
            "quantity" to 5
        )
        val products3 = mutableListOf(Pro7, Pro8)
        MainApplication.rudderClient.track(
            "Order Completed",
            RudderProperty()
                .putValue("orderId", "404040")
                .putValue("revenue", 34)
                .putValue("products", products3)
        )

        //TC:9. Sending revenue event by enabling "Track product once"

        val Pro9 = mutableMapOf(
            "productId" to "111",
            "sku" to "F-32",
            "name" to "UNO",
            "price" to 19,
            "quantity" to 2
        )
        val Pro10 = mutableMapOf(
            "productId" to "222",
            "sku" to "F-32",
            "name" to "UNO",
            "price" to 6,
            "quantity" to 5
        )
        val products4 = mutableListOf(Pro1, Pro2)
        MainApplication.rudderClient.track(
            "Order Completed",
            RudderProperty()
                .putValue("orderId", "404040")
                .putValue("revenue", 34)
                .putValue("products", products4)
        )


        //TC: 10. Sending screen call with name, category by enabling only "Track name page" => ("viewed {name} screen")
        //WITH CATEGORY
        MainApplication.rudderClient.screen(
            "MainActivity",
            "HomeScreen",
            RudderProperty().putValue("foo", "bar"),
            null
        )

        //WITHOUT CATEGORY
        MainApplication.rudderClient.screen(
            "Manashi-screen",
            RudderProperty().putValue("details", "settings checking"),
            null
        )


        //TC: 11. Sending screen call with name, category by enabling only "Track category page" => ("viewed {category} screen")

        MainApplication.rudderClient.screen(
            "2ndname",
            "2ndcategory",
            RudderProperty().putValue("foo", "bar"),
            null
        )


        //TC: 12. Sending screen call with name by enabling "Track all pages" => ("viewed {name} screen")

        MainApplication.rudderClient.screen(
            "3rdname",
            "3rdcategory",
            RudderProperty().putValue("details", "settings checking"),
            null
        )


        //TC:13. Sending screen call without name by enabling "Track all pages" => ("Loaded a screen")
        //WITH CATEGORY
        MainApplication.rudderClient.screen(
            "",
            "3rdcategory",
            RudderProperty().putValue("details", "settings checking"),
            null
        )

        //WITHOUT CATEGORY
        MainApplication.rudderClient.screen(
            "",
            RudderProperty().putValue("details", "settings checking"),
            null
        )


        MainApplication.rudderClient.screen(
            "Manashi-screen",
            RudderProperty().putValue("details", "settings checking"),
            null
        )


//TC: 14. Sending screen call without name by enabling "Track name pages" => (events should not go)

        MainApplication.rudderClient.screen(
            "",
            "4thcategory",
            RudderProperty().putValue("details", "settings checking"),
            null
        );


        //TC:15. Sending screen call with name, category by enabling "Track name page" and "Track category page"

        MainApplication.rudderClient.screen(
            "5thname",
            "5thcategory",
            RudderProperty().putValue("details", "settings checking"),
            null
        );

        //TC: 16. Sending track event by enabling “Prefer advertisingId for device id” settings on dashboard

        MainApplication.rudderClient.identify(
            "User_888",
            RudderTraits()
                .putFirstName("rina")
                .putLastName("Warne")
                .putEmail("rina@shane.com"),
            null
        );


        //TC: 17. Sending group call with group properties by giving the trait value to "Amplitude Group Type Trait" and "Amplitude Group Value Trait" (For example, if you specified group_type as the “Amplitude Group Type Trait”, and name as the “Amplitude Group Value Trait” Would associate the current user with the group with type "Organisation" and value "ExampleCorp, LLC") = PENDING
        //TC: 18. Legacy group call behaviour check by not giving any trait value to "Amplitude Group Type Trait" and "Amplitude Group Value Trait"  ([Rudder] Group: groupId) = PENDING

        //TC: 19. Sending reset call

        MainApplication.rudderClient.track("before reset track 1")
        MainApplication.rudderClient.track("before reset track 2")

        MainApplication.rudderClient.reset();

        MainApplication.rudderClient.track("after reset track 3")
        MainApplication.rudderClient.track("after reset track 4")


        //TC:20. Extra settings check:

        //a. Enable Location Listening = PASS
        //b. Sending custom language and country properties (analytics.track('Video Played', {language: 'Japanese'});) = We do not support as off now

        //c. Traits to increment

        MainApplication.rudderClient.identify(
            "User_888",
            RudderTraits()
                .put("Karma", 1)
                .put("friends", "Amp")
                .put("firstname", 2),
            null
        );
        MainApplication.rudderClient.identify(
            "User_888",
            RudderTraits()
                .put("Karma", 1)
                .put("friends", "123"),
            null
        );


        //d. Traits to append

        val num = arrayOf(10, 20)
        MainApplication.rudderClient.identify(
            "User_888",
            RudderTraits()
                .put("somelist", num)
                .put("firstname", "SRI"),
            null
        );


        //e. Traits to prepend

        val num1 = arrayOf(100, 200)
        MainApplication.rudderClient.identify(
            "User_888",
            RudderTraits()
                .put("some another list", num1)
                .put("lastname", "234"),
            null
        );


        //f. Traits to set once

        //Set (trait = sign_up_date):

        MainApplication.rudderClient.identify(
            "User_888",
            RudderTraits()
                .put("sign_up_date", "2016-04-01"),
            null
        );

        //Set (trait = sign_up_date):

        MainApplication.rudderClient.identify(
            "User_888",
            RudderTraits()
                .put("sign_up_date", "2018-04-01"),
            null
        );

        //Set (trait = nothing):

        MainApplication.rudderClient.identify(
            "User_888",
            RudderTraits()
                .put("sign_up_date", "2018-04-01"),
            null
        );

        //g. Track session events

        MainApplication.rudderClient.track("sample track")

        //h. optOutSession = PASS for identify and track call


    }
}
