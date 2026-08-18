package com.gustavobarreto.instafollowtracker.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gustavobarreto.instafollowtracker.data.model.ListType
import kotlinx.coroutines.flow.Flow

@Dao
interface SnapshotDao {

    @Insert
    suspend fun insert(snapshot: SnapshotEntity): Long

    @Query("SELECT * FROM snapshots ORDER BY importedAtEpochMillis DESC")
    fun observeAll(): Flow<List<SnapshotEntity>>

    @Query("SELECT * FROM snapshots ORDER BY importedAtEpochMillis DESC LIMIT 1")
    suspend fun getLatest(): SnapshotEntity?

    @Query(
        """
        SELECT * FROM snapshots
        WHERE importedAtEpochMillis < (SELECT importedAtEpochMillis FROM snapshots WHERE id = :snapshotId)
        ORDER BY importedAtEpochMillis DESC
        LIMIT 1
        """
    )
    suspend fun getPrevious(snapshotId: Long): SnapshotEntity?

    @Query("DELETE FROM snapshots WHERE id = :snapshotId")
    suspend fun delete(snapshotId: Long)

    @Query("SELECT COUNT(*) FROM snapshots")
    suspend fun count(): Int
}

@Dao
interface ProfileRecordDao {

    @Insert
    suspend fun insertAll(records: List<ProfileRecordEntity>)

    @Query("SELECT * FROM profile_records WHERE snapshotId = :snapshotId AND listType = :listType ORDER BY username COLLATE NOCASE ASC")
    suspend fun getRecords(snapshotId: Long, listType: ListType): List<ProfileRecordEntity>
}
