package com.gustavobarreto.instafollowtracker.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.ui.graphics.vector.ImageVector
import com.gustavobarreto.instafollowtracker.R

enum class Destination(val route: String, val labelRes: Int, val icon: ImageVector) {
    Dashboard("dashboard", R.string.nav_dashboard, Icons.Filled.Home),
    Unfollowers("unfollowers", R.string.nav_unfollowers, Icons.Filled.PersonOff),
    NewFollowers("new_followers", R.string.nav_new_followers, Icons.Filled.Group),
    NotFollowingBack("not_following_back", R.string.nav_not_following_back, Icons.Filled.PersonSearch),
    Import("import", R.string.nav_import, Icons.Filled.UploadFile);

    companion object {
        val bottomBarItems = listOf(Dashboard, Unfollowers, NewFollowers, NotFollowingBack, Import)
    }
}

const val HISTORY_ROUTE = "history"
