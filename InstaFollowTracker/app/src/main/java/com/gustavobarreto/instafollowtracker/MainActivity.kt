package com.gustavobarreto.instafollowtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.gustavobarreto.instafollowtracker.ui.InstaFollowTrackerApp
import com.gustavobarreto.instafollowtracker.ui.theme.InstaFollowTrackerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            InstaFollowTrackerTheme {
                InstaFollowTrackerApp()
            }
        }
    }
}
