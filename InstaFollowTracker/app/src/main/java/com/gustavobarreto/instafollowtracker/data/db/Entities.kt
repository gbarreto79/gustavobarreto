package com.gustavobarreto.instafollowtracker.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.gustavobarreto.instafollowtracker.data.model.ListType

@Entity(tableName = "snapshots")
data class SnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val importedAtEpochMillis: Long,
    val followersCount: Int,
    val followingCount: Int
)

@Entity(
    tableName = "profile_records",
    foreignKeys = [
        ForeignKey(
            entity = SnapshotEntity::class,
            parentColumns = ["id"],
            childColumns = ["snapshotId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["snapshotId", "listType"])]
)
data class ProfileRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val snapshotId: Long,
    val listType: ListType,
    val username: String,
    val profileUrl: String?,
    val igTimestampEpochSeconds: Long?
)
