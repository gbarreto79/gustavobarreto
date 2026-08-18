package com.gustavobarreto.instafollowtracker.ui.screens.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gustavobarreto.instafollowtracker.InstaTrackerApplication
import com.gustavobarreto.instafollowtracker.data.db.SnapshotEntity
import com.gustavobarreto.instafollowtracker.data.repository.FollowersRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FollowersRepository =
        (application as InstaTrackerApplication).repository

    val snapshots: StateFlow<List<SnapshotEntity>> = repository.observeSnapshots()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteSnapshot(id: Long) {
        viewModelScope.launch {
            repository.deleteSnapshot(id)
        }
    }
}
