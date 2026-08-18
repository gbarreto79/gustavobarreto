package com.gustavobarreto.instafollowtracker.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gustavobarreto.instafollowtracker.R
import com.gustavobarreto.instafollowtracker.ui.components.EmptyState
import com.gustavobarreto.instafollowtracker.ui.components.SectionHeader
import com.gustavobarreto.instafollowtracker.ui.components.StatCard
import com.gustavobarreto.instafollowtracker.ui.theme.DangerRed
import com.gustavobarreto.instafollowtracker.ui.theme.SuccessGreen
import com.gustavobarreto.instafollowtracker.ui.theme.WarningAmber
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardScreen(
    onNavigateToUnfollowers: () -> Unit,
    onNavigateToNewFollowers: () -> Unit,
    onNavigateToNotFollowingBack: () -> Unit,
    onNavigateToImport: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is DashboardUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is DashboardUiState.NoData -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center
            ) {
                EmptyState(
                    title = stringResource(R.string.dashboard_no_data_title),
                    body = stringResource(R.string.dashboard_no_data_body)
                )
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Button(onClick = onNavigateToImport) {
                        Text(stringResource(R.string.dashboard_import_cta))
                    }
                }
            }
        }
        is DashboardUiState.Data -> {
            DashboardContent(
                state = state,
                onNavigateToUnfollowers = onNavigateToUnfollowers,
                onNavigateToNewFollowers = onNavigateToNewFollowers,
                onNavigateToNotFollowingBack = onNavigateToNotFollowingBack
            )
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState.Data,
    onNavigateToUnfollowers: () -> Unit,
    onNavigateToNewFollowers: () -> Unit,
    onNavigateToNotFollowingBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        SectionHeader(stringResource(R.string.dashboard_title))

        Text(
            text = stringResource(R.string.dashboard_last_import, formatDate(state.lastImportEpochMillis)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = stringResource(R.string.dashboard_followers),
                value = state.followersCount.toString(),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.dashboard_following),
                value = state.followingCount.toString(),
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = stringResource(R.string.dashboard_new_followers),
                value = state.newFollowersCount.toString(),
                accentColor = SuccessGreen,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToNewFollowers
            )
            StatCard(
                title = stringResource(R.string.dashboard_unfollowers),
                value = state.lostFollowersCount.toString(),
                accentColor = DangerRed,
                modifier = Modifier.weight(1f),
                onClick = onNavigateToUnfollowers
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        StatCard(
            title = stringResource(R.string.dashboard_not_following_back),
            value = state.notFollowingBackCount.toString(),
            accentColor = WarningAmber,
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .fillMaxWidth(),
            onClick = onNavigateToNotFollowingBack
        )

        if (state.isFirstImport) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.dashboard_first_import_note),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
}

private fun formatDate(epochMillis: Long): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm", Locale("pt", "BR"))
    return Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(formatter)
}
