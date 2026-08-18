package com.gustavobarreto.instafollowtracker.ui.screens.userlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gustavobarreto.instafollowtracker.InstaTrackerApplication
import com.gustavobarreto.instafollowtracker.data.model.InstaProfile
import com.gustavobarreto.instafollowtracker.data.repository.FollowersRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

enum class UserListKind {
    UNFOLLOWERS,
    NEW_FOLLOWERS,
    NOT_FOLLOWING_BACK
}

data class UserListUiState(
    val isLoading: Boolean = true,
    val hasSnapshot: Boolean = false,
    val isFirstImport: Boolean = false,
    val profiles: List<InstaProfile> = emptyList()
)

class UserListViewModel(
    application: Application,
    private val kind: UserListKind
) : AndroidViewModel(application) {

    private val repository: FollowersRepository =
        (application as InstaTrackerApplication).repository

    val uiState: StateFlow<UserListUiState> = repository.observeSnapshots()
        .map { snapshots ->
            if (snapshots.isEmpty()) return@map UserListUiState(isLoading = false, hasSnapshot = false)
            val comparison = repository.getLatestComparison()
                ?: return@map UserListUiState(isLoading = false, hasSnapshot = false)
            val profiles = when (kind) {
                UserListKind.UNFOLLOWERS -> comparison.followersDiff.lostFollowers
                UserListKind.NEW_FOLLOWERS -> comparison.followersDiff.newFollowers
                UserListKind.NOT_FOLLOWING_BACK -> comparison.notFollowingBack
            }
            UserListUiState(
                isLoading = false,
                hasSnapshot = true,
                isFirstImport = comparison.previousSnapshot == null,
                profiles = profiles
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserListUiState())

    class Factory(
        private val application: Application,
        private val kind: UserListKind
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserListViewModel(application, kind) as T
        }
    }
}
