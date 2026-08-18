package com.gustavobarreto.instafollowtracker.data.parser

import com.gustavobarreto.instafollowtracker.data.model.InstaProfile
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.util.zip.ZipInputStream

/**
 * Parses the official Instagram data export ("Download your information",
 * JSON format, "Followers and following" category).
 *
 * Instagram's export format is not a public/versioned API contract, so this
 * parser is intentionally forgiving: it looks for the well-known file names
 * and JSON shapes used since 2020, and falls back to scanning for any array
 * of `string_list_data` entries if the expected keys are missing.
 */
object InstagramExportParser {

    class InstagramExportParseException(message: String) : Exception(message)

    data class ExportContents(
        val followers: List<InstaProfile>?,
        val following: List<InstaProfile>?
    )

    private val FOLLOWERS_FILE_REGEX = Regex("""^followers(_\d+)?\.json$""", RegexOption.IGNORE_CASE)
    private val FOLLOWING_FILE_REGEX = Regex("""^following\.json$""", RegexOption.IGNORE_CASE)

    /** Parses a `.zip` file downloaded directly from Instagram's data export tool. */
    fun parseZip(input: InputStream): ExportContents {
        val followers = mutableListOf<InstaProfile>()
        val following = mutableListOf<InstaProfile>()
        var foundFollowers = false
        var foundFollowing = false

        ZipInputStream(input).use { zip ->
            var entry = zip.nextEntry
            while (entry != null) {
                val fileName = entry.name.substringAfterLast('/')
                when {
                    !entry.isDirectory && FOLLOWERS_FILE_REGEX.matches(fileName) -> {
                        val text = zip.bufferedReader(Charsets.UTF_8).readText()
                        followers += parseFollowersArray(JSONArray(text))
                        foundFollowers = true
                    }
                    !entry.isDirectory && FOLLOWING_FILE_REGEX.matches(fileName) -> {
                        val text = zip.bufferedReader(Charsets.UTF_8).readText()
                        following += parseFollowingObject(JSONObject(text))
                        foundFollowing = true
                    }
                }
                zip.closeEntry()
                entry = zip.nextEntry
            }
        }

        return ExportContents(
            followers = if (foundFollowers) followers else null,
            following = if (foundFollowing) following else null
        )
    }

    /** Parses a single `followers_1.json` (or `followers_2.json`, etc.) file. */
    fun parseFollowersJson(input: InputStream): List<InstaProfile> {
        val text = input.bufferedReader(Charsets.UTF_8).readText()
        val json = try {
            JSONArray(text)
        } catch (e: Exception) {
            throw InstagramExportParseException("Formato inesperado para o arquivo de seguidores.")
        }
        return parseFollowersArray(json)
    }

    /** Parses a single `following.json` file. */
    fun parseFollowingJson(input: InputStream): List<InstaProfile> {
        val text = input.bufferedReader(Charsets.UTF_8).readText()
        val json = try {
            JSONObject(text)
        } catch (e: Exception) {
            throw InstagramExportParseException("Formato inesperado para o arquivo de quem você segue.")
        }
        return parseFollowingObject(json)
    }

    /**
     * Attempts to detect whether a standalone JSON file is a followers or a
     * following export, based on its file name and, failing that, its shape.
     */
    fun detectAndParse(input: InputStream, fileName: String?): Pair<ListKind, List<InstaProfile>> {
        val text = input.bufferedReader(Charsets.UTF_8).readText()
        val shortName = fileName?.substringAfterLast('/')

        if (shortName != null && FOLLOWERS_FILE_REGEX.matches(shortName)) {
            return ListKind.FOLLOWERS to parseFollowersArray(JSONArray(text))
        }
        if (shortName != null && FOLLOWING_FILE_REGEX.matches(shortName)) {
            return ListKind.FOLLOWING to parseFollowingObject(JSONObject(text))
        }

        // Fall back to sniffing the content itself.
        val trimmed = text.trimStart()
        return when {
            trimmed.startsWith("[") -> ListKind.FOLLOWERS to parseFollowersArray(JSONArray(text))
            trimmed.startsWith("{") -> ListKind.FOLLOWING to parseFollowingObject(JSONObject(text))
            else -> throw InstagramExportParseException("Não foi possível reconhecer o conteúdo do arquivo.")
        }
    }

    enum class ListKind { FOLLOWERS, FOLLOWING }

    private fun parseFollowersArray(array: JSONArray): List<InstaProfile> {
        return profilesFromArray(array)
    }

    private fun parseFollowingObject(json: JSONObject): List<InstaProfile> {
        val array = json.optJSONArray("relationships_following")
            ?: findFirstProfileArray(json)
            ?: throw InstagramExportParseException("Não encontramos a lista de \"seguindo\" no arquivo.")
        return profilesFromArray(array)
    }

    /** Fallback for export format variations: scan top-level keys for an array of profile entries. */
    private fun findFirstProfileArray(json: JSONObject): JSONArray? {
        for (key in json.keys()) {
            val candidate = json.optJSONArray(key) ?: continue
            if (candidate.length() == 0) continue
            val first = candidate.optJSONObject(0) ?: continue
            if (first.has("string_list_data")) return candidate
        }
        return null
    }

    private fun profilesFromArray(array: JSONArray): List<InstaProfile> {
        val result = mutableListOf<InstaProfile>()
        for (i in 0 until array.length()) {
            val entry = array.optJSONObject(i) ?: continue
            val stringListData = entry.optJSONArray("string_list_data") ?: continue
            val first = stringListData.optJSONObject(0) ?: continue
            val username = first.optString("value").takeIf { it.isNotBlank() } ?: continue
            val href = first.optString("href").takeIf { it.isNotBlank() }
            val timestamp = if (first.has("timestamp")) first.optLong("timestamp") else null
            result += InstaProfile(
                username = username,
                profileUrl = href,
                igTimestampEpochSeconds = timestamp
            )
        }
        return result
    }
}
