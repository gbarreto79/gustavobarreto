package com.gustavobarreto.instafollowtracker.ui.screens.userlist

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gustavobarreto.instafollowtracker.R
import com.gustavobarreto.instafollowtracker.ui.components.EmptyState
import com.gustavobarreto.instafollowtracker.ui.components.UserListItem

@Composable
fun UserListScreen(kind: UserListKind) {
    val context = LocalContext.current
    val application = context.applicationContext as android.app.Application
    val viewModel: UserListViewModel = viewModel(
        factory = UserListViewModel.Factory(application, kind)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    when {
        uiState.isLoading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        !uiState.hasSnapshot -> {
            EmptyState(title = stringResource(R.string.user_list_empty_no_history))
        }
        kind != UserListKind.NOT_FOLLOWING_BACK && uiState.isFirstImport -> {
            EmptyState(title = stringResource(R.string.user_list_empty_no_history))
        }
        uiState.profiles.isEmpty() -> {
            EmptyState(title = stringResource(emptyMessageRes(kind)))
        }
        else -> {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(uiState.profiles, key = { it.username }) { profile ->
                    UserListItem(
                        profile = profile,
                        onClick = {
                            val url = profile.profileUrl ?: "https://www.instagram.com/${profile.username}"
                            runCatching { uriHandler.openUri(url) }
                        }
                    )
                }
            }
        }
    }
}

private fun emptyMessageRes(kind: UserListKind): Int = when (kind) {
    UserListKind.UNFOLLOWERS -> R.string.user_list_empty_unfollowers
    UserListKind.NEW_FOLLOWERS -> R.string.user_list_empty_new_followers
    UserListKind.NOT_FOLLOWING_BACK -> R.string.user_list_empty_not_following_back
}
