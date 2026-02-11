@file:OptIn(ExperimentalTime::class)

package org.chats

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kotlin.time.ExperimentalTime

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        val container = (application as ChatterApplication).container

        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App(container)
        }
    }
}