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

        /*MainApplication.rudderClient.screen(localClassName)
        MainApplication.rudderClient.screen(
            "MainActivity",
            "HomeScreen",
            RudderProperty().putValue("foo", "bar"),
            null
        )*/
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
            "4521",
            RudderTraits()
                .putEmail("bobin@gmail.com")
                .putFirstName("Dasari")
                .putLastName("Bobby")
                .putName("Dasari Bobby")
                .put("friends",1)
                .put("SO","Guntur")
            ,
            null
        )
        /*MainApplication.rudderClient.group("new_group_id",
            RudderTraits().putAge("24")
                .putName("Test Group Name")
                .putPhone("1234567891")
                .put("company_id","RS")
                .put("company_name","RudderStack")
        )*/
        //MainApplication.rudderClient.alias("test_new_id")
        //MainApplication.rudderClient.track("account: created")
        //MainApplication.rudderClient.track("account: authenticated")

        //MainApplication.rudderClient.reset();
    }
}
