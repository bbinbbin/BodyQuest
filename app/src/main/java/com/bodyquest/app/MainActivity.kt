package com.bodyquest.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bodyquest.app.ui.navigation.BodyQuestNavGraph
import com.bodyquest.app.ui.theme.BodyQuestTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BodyQuestTheme {
                BodyQuestNavGraph()
            }
        }
    }
}
