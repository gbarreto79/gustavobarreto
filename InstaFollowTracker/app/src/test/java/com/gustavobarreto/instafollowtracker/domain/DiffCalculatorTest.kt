package com.gustavobarreto.instafollowtracker.domain

import com.gustavobarreto.instafollowtracker.data.model.InstaProfile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DiffCalculatorTest {

    private fun profile(username: String) = InstaProfile(username, null, null)

    @Test
    fun `diffFollowers detects new and lost followers`() {
        val previous = listOf(profile("alice"), profile("bob"), profile("carol"))
        val current = listOf(profile("alice"), profile("carol"), profile("dave"))

        val diff = DiffCalculator.diffFollowers(previous, current)

        assertEquals(listOf("dave"), diff.newFollowers.map { it.username })
        assertEquals(listOf("bob"), diff.lostFollowers.map { it.username })
    }

    @Test
    fun `diffFollowers is case insensitive`() {
        val previous = listOf(profile("Alice"))
        val current = listOf(profile("alice"))

        val diff = DiffCalculator.diffFollowers(previous, current)

        assertTrue(diff.newFollowers.isEmpty())
        assertTrue(diff.lostFollowers.isEmpty())
    }

    @Test
    fun `diffFollowers with empty previous returns everyone as unchanged when previous is empty list`() {
        val current = listOf(profile("alice"), profile("bob"))

        val diff = DiffCalculator.diffFollowers(emptyList(), current)

        assertEquals(2, diff.newFollowers.size)
        assertTrue(diff.lostFollowers.isEmpty())
    }

    @Test
    fun `notFollowingBack returns accounts followed that do not follow back`() {
        val followers = listOf(profile("alice"), profile("bob"))
        val following = listOf(profile("alice"), profile("carol"), profile("dave"))

        val result = DiffCalculator.notFollowingBack(followers, following)

        assertEquals(listOf("carol", "dave"), result.map { it.username })
    }

    @Test
    fun `notFollowingBack returns empty list when everyone follows back`() {
        val followers = listOf(profile("alice"), profile("bob"))
        val following = listOf(profile("alice"), profile("bob"))

        val result = DiffCalculator.notFollowingBack(followers, following)

        assertTrue(result.isEmpty())
    }
}
