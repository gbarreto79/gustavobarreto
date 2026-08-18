package com.gustavobarreto.instafollowtracker.ui.screens.importdata

import android.app.Application
import android.content.ContentResolver
import android.net.Uri
import android.provider.OpenableColumns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gustavobarreto.instafollowtracker.InstaTrackerApplication
import com.gustavobarreto.instafollowtracker.R
import com.gustavobarreto.instafollowtracker.data.model.InstaProfile
import com.gustavobarreto.instafollowtracker.data.parser.InstagramExportParser
import com.gustavobarreto.instafollowtracker.data.repository.FollowersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

sealed interface ImportUiState {
    data object Idle : ImportUiState
    data object Loading : ImportUiState
    data class Success(val followersCount: Int, val followingCount: Int) : ImportUiState
    data class Error(val messageRes: Int) : ImportUiState
}

private class MissingFollowersException : Exception()
private class MissingFollowingException : Exception()

class ImportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FollowersRepository =
        (application as InstaTrackerApplication).repository

    private val _uiState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val uiState: StateFlow<ImportUiState> = _uiState.asStateFlow()

    fun importZip(uri: Uri) {
        runImport {
            val resolver = getApplication<Application>().contentResolver
            val contents = resolver.openInputStream(uri)?.use { input ->
                InstagramExportParser.parseZip(input)
            } ?: throw IOException("Não foi possível abrir o arquivo selecionado.")

            val followers = contents.followers ?: throw MissingFollowersException()
            val following = contents.following ?: throw MissingFollowingException()
            followers to following
        }
    }

    fun importJsonFiles(uris: List<Uri>) {
        runImport {
            val resolver = getApplication<Application>().contentResolver
            var followers: List<InstaProfile>? = null
            var following: List<InstaProfile>? = null

            for (uri in uris) {
                val displayName = queryDisplayName(resolver, uri)
                resolver.openInputStream(uri)?.use { input ->
                    val (kind, profiles) = InstagramExportParser.detectAndParse(input, displayName)
                    when (kind) {
                        InstagramExportParser.ListKind.FOLLOWERS -> followers = profiles
                        InstagramExportParser.ListKind.FOLLOWING -> following = profiles
                    }
                }
            }

            val resolvedFollowers = followers ?: throw MissingFollowersException()
            val resolvedFollowing = following ?: throw MissingFollowingException()
            resolvedFollowers to resolvedFollowing
        }
    }

    fun resetState() {
        _uiState.value = ImportUiState.Idle
    }

    private fun runImport(block: suspend () -> Pair<List<InstaProfile>, List<InstaProfile>>) {
        viewModelScope.launch {
            _uiState.value = ImportUiState.Loading
            try {
                val (followers, following) = withContext(Dispatchers.IO) { block() }
                repository.importSnapshot(followers, following)
                _uiState.value = ImportUiState.Success(followers.size, following.size)
            } catch (e: MissingFollowersException) {
                _uiState.value = ImportUiState.Error(R.string.import_error_missing_followers)
            } catch (e: MissingFollowingException) {
                _uiState.value = ImportUiState.Error(R.string.import_error_missing_following)
            } catch (e: Exception) {
                _uiState.value = ImportUiState.Error(R.string.import_error_generic)
            }
        }
    }

    private fun queryDisplayName(resolver: ContentResolver, uri: Uri): String? {
        return resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index >= 0 && cursor.moveToFirst()) cursor.getString(index) else null
        }
    }
}
