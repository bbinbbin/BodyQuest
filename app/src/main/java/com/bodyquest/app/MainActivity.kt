package com.bodyquest.app

import android.content.SharedPreferences
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.bodyquest.app.ui.navigation.BodyQuestNavGraph
import com.bodyquest.app.ui.theme.BodyQuestTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                sharedPreferences.edit()
                    .putLong("last_active_time", System.currentTimeMillis())
                    .apply()
            }
        })

        setContent {
            BodyQuestTheme {
                BodyQuestNavGraph()
            }
        }
    }
}
