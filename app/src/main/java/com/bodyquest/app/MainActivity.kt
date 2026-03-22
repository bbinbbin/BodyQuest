package com.bodyquest.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.bodyquest.app.ui.navigation.BodyQuestNavGraph
import com.bodyquest.app.ui.theme.BodyQuestTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as BodyQuestApp

        setContent {
            BodyQuestTheme {
                BodyQuestNavGraph(
                    userRepository = app.userRepository,
                    questRepository = app.questRepository,
                    workoutRepository = app.workoutRepository
                )
            }
        }
    }
}
