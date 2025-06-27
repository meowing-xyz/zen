package meowing.zen

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import meowing.zen.utils.ChatUtils
import net.minecraft.event.ClickEvent
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CompletableFuture

object UpdateChecker {
    private const val current = "1.0.1"
    private var lastCheck = 0L
    private var isMessageShown = false
    private val removeCharsRegex = Regex("[^0-9.]")

    data class Release(
        val tag_name: String,
        val html_url: String,
        val prerelease: Boolean
    )

    fun checkForUpdates() {
        if (System.currentTimeMillis() - lastCheck < 300000 || isMessageShown) return
        lastCheck = System.currentTimeMillis()

        CompletableFuture.supplyAsync {
            try {
                val connection = URL("https://api.github.com/repos/kiwidotzip/zen/releases").openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Zen)")
                connection.setRequestProperty("Accept", "application/json")
                connection.connectTimeout = 10000
                connection.readTimeout = 30000

                if (connection.responseCode == 200) {
                    val response = connection.inputStream.bufferedReader().readText()
                    val type = object : TypeToken<List<Release>>() {}.type
                    val releases: List<Release> = Gson().fromJson(response, type)

                    if (releases.isEmpty()) {
                        ChatUtils.addMessage("§c[Zen] §fNo releases found.")
                        return@supplyAsync
                    }

                    val latestRelease = releases.firstOrNull { !it.prerelease }
                    if (latestRelease == null) return@supplyAsync

                    val latestVersion = latestRelease.tag_name.replace("v", "").replace(removeCharsRegex, "")
                    val currentVersion = current.replace(removeCharsRegex, "")

                    if (currentVersion == latestVersion) return@supplyAsync

                    if (isNewerVersion(latestVersion, currentVersion)) {
                        isMessageShown = true
                        ChatUtils.addMessage("§c[Zen] §fUpdate available! §c$current §f-> §c$latestVersion")
                        ChatUtils.addMessage(
                            message = "§c[Zen] §fDownload: ",
                            hover = "Click to open in browser",
                            clickAction = ClickEvent.Action.OPEN_URL,
                            clickValue = latestRelease.html_url,
                            siblingText = "§c${latestRelease.html_url}"
                        )
                    }
                } else ChatUtils.addMessage("§c[Zen] §fFailed to check for updates (${connection.responseCode})")
            } catch (e: Exception) {
                ChatUtils.addMessage("§c[Zen] §fUpdate check failed: ${e.message}")
            }
        }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").map { it.toIntOrNull() ?: 0 }
        val currentParts = current.split(".").map { it.toIntOrNull() ?: 0 }

        for (i in 0 until maxOf(latestParts.size, currentParts.size)) {
            val latestPart = latestParts.getOrElse(i) { 0 }
            val currentPart = currentParts.getOrElse(i) { 0 }

            when {
                latestPart > currentPart -> return true
                latestPart < currentPart -> return false
            }
        }
        return false
    }
}