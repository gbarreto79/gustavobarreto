package com.gustavobarreto.instafollowtracker.data.parser

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class InstagramExportParserTest {

    private val followersJson = """
        [
          {
            "title": "",
            "media_list_data": [],
            "string_list_data": [
              {"href": "https://www.instagram.com/alice", "value": "alice", "timestamp": 1000}
            ]
          },
          {
            "title": "",
            "media_list_data": [],
            "string_list_data": [
              {"href": "https://www.instagram.com/bob", "value": "bob", "timestamp": 2000}
            ]
          }
        ]
    """.trimIndent()

    private val followingJson = """
        {
          "relationships_following": [
            {
              "title": "",
              "media_list_data": [],
              "string_list_data": [
                {"href": "https://www.instagram.com/alice", "value": "alice", "timestamp": 1500}
              ]
            }
          ]
        }
    """.trimIndent()

    @Test
    fun `parseFollowersJson reads usernames href and timestamp`() {
        val result = InstagramExportParser.parseFollowersJson(followersJson.byteInputStream())

        assertEquals(listOf("alice", "bob"), result.map { it.username })
        assertEquals("https://www.instagram.com/alice", result[0].profileUrl)
        assertEquals(1000L, result[0].igTimestampEpochSeconds)
    }

    @Test
    fun `parseFollowingJson reads relationships_following array`() {
        val result = InstagramExportParser.parseFollowingJson(followingJson.byteInputStream())

        assertEquals(listOf("alice"), result.map { it.username })
    }

    @Test
    fun `parseZip finds followers and following files regardless of folder path`() {
        val zipBytes = buildZip(
            "connections/followers_and_following/followers_1.json" to followersJson,
            "connections/followers_and_following/following.json" to followingJson
        )

        val contents = InstagramExportParser.parseZip(ByteArrayInputStream(zipBytes))

        assertEquals(2, contents.followers?.size)
        assertEquals(1, contents.following?.size)
    }

    @Test
    fun `parseZip merges multiple numbered followers files`() {
        val zipBytes = buildZip(
            "followers_1.json" to followersJson,
            "followers_2.json" to """[{"string_list_data":[{"href":"https://www.instagram.com/carol","value":"carol","timestamp":3000}]}]"""
        )

        val contents = InstagramExportParser.parseZip(ByteArrayInputStream(zipBytes))

        assertEquals(3, contents.followers?.size)
        assertNull(contents.following)
    }

    @Test
    fun `detectAndParse uses file name when available`() {
        val (kind, profiles) = InstagramExportParser.detectAndParse(
            followersJson.byteInputStream(),
            "followers_1.json"
        )

        assertEquals(InstagramExportParser.ListKind.FOLLOWERS, kind)
        assertEquals(2, profiles.size)
    }

    @Test
    fun `detectAndParse falls back to content sniffing when file name is unknown`() {
        val (kind, profiles) = InstagramExportParser.detectAndParse(
            followingJson.byteInputStream(),
            "export.json"
        )

        assertEquals(InstagramExportParser.ListKind.FOLLOWING, kind)
        assertEquals(1, profiles.size)
    }

    @Test(expected = InstagramExportParser.InstagramExportParseException::class)
    fun `parseFollowersJson throws on malformed content`() {
        InstagramExportParser.parseFollowersJson("not json".byteInputStream())
    }

    private fun buildZip(vararg entries: Pair<String, String>): ByteArray {
        val out = ByteArrayOutputStream()
        ZipOutputStream(out).use { zip ->
            for ((name, content) in entries) {
                zip.putNextEntry(ZipEntry(name))
                zip.write(content.toByteArray(Charsets.UTF_8))
                zip.closeEntry()
            }
        }
        return out.toByteArray()
    }
}
