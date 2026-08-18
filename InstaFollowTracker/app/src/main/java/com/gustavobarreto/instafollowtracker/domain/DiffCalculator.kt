package com.gustavobarreto.instafollowtracker.domain

import com.gustavobarreto.instafollowtracker.data.model.InstaProfile

data class FollowersDiff(
    val newFollowers: List<InstaProfile>,
    val lostFollowers: List<InstaProfile>
)

/**
 * Pure comparison logic shared by the repository and unit tests. Usernames
 * are compared case-insensitively since Instagram handles them the same way.
 */
object DiffCalculator {

    fun diffFollowers(previous: List<InstaProfile>, current: List<InstaProfile>): FollowersDiff {
        val previousUsernames = previous.map { it.username.lowercase() }.toHashSet()
        val currentUsernames = current.map { it.username.lowercase() }.toHashSet()

        val newFollowers = current
            .filter { it.username.lowercase() !in previousUsernames }
            .sortedBy { it.username.lowercase() }

        val lostFollowers = previous
            .filter { it.username.lowercase() !in currentUsernames }
            .sortedBy { it.username.lowercase() }

        return FollowersDiff(newFollowers, lostFollowers)
    }

    /** People the account follows who do not follow back, based on a single snapshot. */
    fun notFollowingBack(followers: List<InstaProfile>, following: List<InstaProfile>): List<InstaProfile> {
        val followerUsernames = followers.map { it.username.lowercase() }.toHashSet()
        return following
            .filter { it.username.lowercase() !in followerUsernames }
            .sortedBy { it.username.lowercase() }
    }
}
