package com.rudderstack.android.sample.kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rudderlabs.android.sample.kotlin.R
import com.rudderstack.android.sdk.core.RudderProperty
import com.rudderstack.android.sdk.core.RudderTraits

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        MainApplication.rudderClient.screen(localClassName)

        val property = RudderProperty()
        property.put("key_1", "val_1")
        property.put("key_2", "val_2")
        val childProperty = RudderProperty()
        childProperty.put("key_c_1", "val_c_1")
        childProperty.put("key_c_2", "val_c_2")
        property.put("child_key", childProperty)
        MainApplication.rudderClient.track("challenge: applied points", property)
        MainApplication.rudderClient.track("article: viewed")
        MainApplication.rudderClient.identify(
            "test_user_id",
            RudderTraits()
                .putEmail("example@gmail.com")
                .putFirstName("Foo")
                .putLastName("Bar")
                .putName("Ruchira"),
            null
        )
        MainApplication.rudderClient.track("account: created")
        MainApplication.rudderClient.track("account: authenticated")
    }
}
