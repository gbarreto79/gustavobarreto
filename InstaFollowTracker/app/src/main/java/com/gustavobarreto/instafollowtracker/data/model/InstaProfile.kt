package com.gustavobarreto.instafollowtracker.data.model

/**
 * A single Instagram account as read from an official data export
 * (or from a stored snapshot in the local database).
 */
data class InstaProfile(
    val username: String,
    val profileUrl: String?,
    val igTimestampEpochSeconds: Long?
)

enum class ListType {
    FOLLOWER,
    FOLLOWING
}
