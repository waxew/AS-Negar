/*
 * AS-Negar update checker.
 *
 * Update checks are intentionally user-triggered and read-only. The checker
 * never downloads or installs an APK itself; it only compares the latest
 * published GitHub release with the installed version.
 */
package com.dot.gallery.core.update

import com.dot.gallery.core.branding.ASBrand
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

sealed interface ASUpdateCheckResult {
    data class Available(
        val versionName: String,
        val releaseUrl: String,
    ) : ASUpdateCheckResult

    data object UpToDate : ASUpdateCheckResult
    data object NoPublishedRelease : ASUpdateCheckResult
    data object Failed : ASUpdateCheckResult
}

object ASUpdateChecker {

    /**
     * آخرین Release عمومی نگار را بررسی می‌کند.
     *
     * هیچ خطای شبکه‌ای از این تابع به UI پرتاب نمی‌شود تا نبود اینترنت یا
     * محدودیت GitHub باعث اختلال در اجرای برنامه نشود.
     */
    suspend fun check(currentVersion: String): ASUpdateCheckResult = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL(ASBrand.LATEST_RELEASE_API_URL).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 7_000
                readTimeout = 7_000
                setRequestProperty("Accept", "application/vnd.github+json")
                setRequestProperty("User-Agent", ASBrand.APP_NAME)
                setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            }

            when (connection.responseCode) {
                HttpURLConnection.HTTP_OK -> {
                    val payload = connection.inputStream.bufferedReader().use { it.readText() }
                    val json = JSONObject(payload)
                    val latestVersion = json.optString("tag_name").trim()
                    val releaseUrl = json.optString("html_url", ASBrand.RELEASES_URL).ifBlank {
                        ASBrand.RELEASES_URL
                    }

                    if (latestVersion.isBlank()) {
                        ASUpdateCheckResult.Failed
                    } else if (isVersionNewer(latestVersion, currentVersion)) {
                        ASUpdateCheckResult.Available(
                            versionName = latestVersion.removePrefix("v").removePrefix("V"),
                            releaseUrl = releaseUrl,
                        )
                    } else {
                        ASUpdateCheckResult.UpToDate
                    }
                }

                HttpURLConnection.HTTP_NOT_FOUND -> ASUpdateCheckResult.NoPublishedRelease
                else -> ASUpdateCheckResult.Failed
            }
        } catch (_: Exception) {
            ASUpdateCheckResult.Failed
        } finally {
            connection?.disconnect()
        }
    }

    /** مقایسه عددی نسخه‌ها؛ پسوندهای متنی مانند beta/rc باعث Crash نمی‌شوند. */
    private fun isVersionNewer(latest: String, current: String): Boolean {
        val latestParts = versionParts(latest)
        val currentParts = versionParts(current)
        if (latestParts.isEmpty() || currentParts.isEmpty()) return false

        val length = maxOf(latestParts.size, currentParts.size)
        for (index in 0 until length) {
            val latestPart = latestParts.getOrElse(index) { 0 }
            val currentPart = currentParts.getOrElse(index) { 0 }
            if (latestPart != currentPart) return latestPart > currentPart
        }
        return false
    }

    private fun versionParts(value: String): List<Int> =
        Regex("\\d+")
            .findAll(value)
            .mapNotNull { it.value.toIntOrNull() }
            .take(4)
            .toList()
}
