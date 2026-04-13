package com.bodyquest.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.bodyquest.wear.ui.WearHomeScreen
import com.bodyquest.wear.ui.theme.BodyQuestWearTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BodyQuestWearTheme {
                WearHomeScreen()
            }
        }
    }
}
