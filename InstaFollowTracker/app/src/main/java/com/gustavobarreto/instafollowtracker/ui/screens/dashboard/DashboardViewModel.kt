package com.gustavobarreto.instafollowtracker.ui.screens.dashboard

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gustavobarreto.instafollowtracker.InstaTrackerApplication
import com.gustavobarreto.instafollowtracker.data.repository.FollowersRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface DashboardUiState {
    data object Loading : DashboardUiState
    data object NoData : DashboardUiState
    data class Data(
        val followersCount: Int,
        val followingCount: Int,
        val newFollowersCount: Int,
        val lostFollowersCount: Int,
        val notFollowingBackCount: Int,
        val lastImportEpochMillis: Long,
        val isFirstImport: Boolean
    ) : DashboardUiState
}

class DashboardViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FollowersRepository =
        (application as InstaTrackerApplication).repository

    val uiState: StateFlow<DashboardUiState> = repository.observeSnapshots()
        .map { snapshots ->
            if (snapshots.isEmpty()) return@map DashboardUiState.NoData
            val comparison = repository.getLatestComparison() ?: return@map DashboardUiState.NoData
            DashboardUiState.Data(
                followersCount = comparison.latestSnapshot.followersCount,
                followingCount = comparison.latestSnapshot.followingCount,
                newFollowersCount = comparison.followersDiff.newFollowers.size,
                lostFollowersCount = comparison.followersDiff.lostFollowers.size,
                notFollowingBackCount = comparison.notFollowingBack.size,
                lastImportEpochMillis = comparison.latestSnapshot.importedAtEpochMillis,
                isFirstImport = comparison.previousSnapshot == null
            )
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DashboardUiState.Loading)
}
