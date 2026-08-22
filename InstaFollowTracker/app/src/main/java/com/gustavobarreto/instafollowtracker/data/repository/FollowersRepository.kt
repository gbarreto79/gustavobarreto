package com.gustavobarreto.instafollowtracker.data.repository

import com.gustavobarreto.instafollowtracker.data.db.AppDatabase
import com.gustavobarreto.instafollowtracker.data.db.ProfileRecordEntity
import com.gustavobarreto.instafollowtracker.data.db.SnapshotEntity
import com.gustavobarreto.instafollowtracker.data.model.InstaProfile
import com.gustavobarreto.instafollowtracker.data.model.ListType
import com.gustavobarreto.instafollowtracker.domain.DiffCalculator
import com.gustavobarreto.instafollowtracker.domain.FollowersDiff
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

data class SnapshotComparison(
    val latestSnapshot: SnapshotEntity,
    val previousSnapshot: SnapshotEntity?,
    val followersDiff: FollowersDiff,
    val notFollowingBack: List<InstaProfile>
)

class FollowersRepository(private val db: AppDatabase) {

    fun observeSnapshots(): Flow<List<SnapshotEntity>> = db.snapshotDao().observeAll()

    suspend fun hasAnySnapshot(): Boolean = db.snapshotDao().count() > 0

    suspend fun importSnapshot(
        followers: List<InstaProfile>,
        following: List<InstaProfile>,
        recentlyUnfollowedByInstagram: List<InstaProfile> = emptyList()
    ): Long {
        val snapshotId = db.snapshotDao().insert(
            SnapshotEntity(
                importedAtEpochMillis = System.currentTimeMillis(),
                followersCount = followers.size,
                followingCount = following.size
            )
        )

        val records = followers.map { it.toEntity(snapshotId, ListType.FOLLOWER) } +
            following.map { it.toEntity(snapshotId, ListType.FOLLOWING) } +
            recentlyUnfollowedByInstagram.map { it.toEntity(snapshotId, ListType.RECENTLY_UNFOLLOWED) }
        db.profileRecordDao().insertAll(records)

        return snapshotId
    }

    suspend fun deleteSnapshot(snapshotId: Long) {
        db.snapshotDao().delete(snapshotId)
    }

    suspend fun getLatestComparison(): SnapshotComparison? {
        val latest = db.snapshotDao().getLatest() ?: return null
        return buildComparison(latest)
    }

    suspend fun getComparisonFor(snapshotId: Long): SnapshotComparison? {
        val snapshot = observeSnapshots().first().find { it.id == snapshotId } ?: return null
        return buildComparison(snapshot)
    }

    private suspend fun buildComparison(snapshot: SnapshotEntity): SnapshotComparison {
        val previous = db.snapshotDao().getPrevious(snapshot.id)

        val currentFollowers = db.profileRecordDao().getRecords(snapshot.id, ListType.FOLLOWER).map { it.toDomain() }
        val currentFollowing = db.profileRecordDao().getRecords(snapshot.id, ListType.FOLLOWING).map { it.toDomain() }

        val diffFromSnapshots = if (previous == null) {
            FollowersDiff(newFollowers = emptyList(), lostFollowers = emptyList())
        } else {
            val previousFollowers = db.profileRecordDao().getRecords(previous.id, ListType.FOLLOWER).map { it.toDomain() }
            DiffCalculator.diffFollowers(previousFollowers, currentFollowers)
        }

        // Merge in Instagram's own "recently unfollowed" report (from
        // recently_unfollowed_profiles.json, when present): the exported
        // followers list can itself be incomplete, so snapshot-to-snapshot
        // diffing alone can miss real unfollows.
        val recentlyUnfollowedByInstagram = db.profileRecordDao()
            .getRecords(snapshot.id, ListType.RECENTLY_UNFOLLOWED)
            .map { it.toDomain() }
        val mergedLostFollowers = (diffFromSnapshots.lostFollowers + recentlyUnfollowedByInstagram)
            .distinctBy { it.username.lowercase() }
            .sortedBy { it.username.lowercase() }
        val followersDiff = diffFromSnapshots.copy(lostFollowers = mergedLostFollowers)

        val notFollowingBack = DiffCalculator.notFollowingBack(currentFollowers, currentFollowing)

        return SnapshotComparison(
            latestSnapshot = snapshot,
            previousSnapshot = previous,
            followersDiff = followersDiff,
            notFollowingBack = notFollowingBack
        )
    }

    private fun InstaProfile.toEntity(snapshotId: Long, listType: ListType) = ProfileRecordEntity(
        snapshotId = snapshotId,
        listType = listType,
        username = username,
        profileUrl = profileUrl,
        igTimestampEpochSeconds = igTimestampEpochSeconds
    )

    private fun ProfileRecordEntity.toDomain() = InstaProfile(
        username = username,
        profileUrl = profileUrl,
        igTimestampEpochSeconds = igTimestampEpochSeconds
    )
}
