package meowing.zen

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import meowing.zen.Zen.Companion.mc
import net.minecraft.event.ClickEvent
import net.minecraft.event.HoverEvent
import net.minecraft.util.ChatComponentText
import net.minecraft.util.ChatStyle
import net.minecraft.util.IChatComponent
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.CompletableFuture

object UpdateChecker {
    private const val current = "1.1.1"
    private var lastCheck = 0L
    private var isMessageShown = false

    data class GitHubRelease(val tag_name: String, val html_url: String, val prerelease: Boolean)
    data class ModrinthVersion(val id: String, val version_number: String, val date_published: String, val game_versions: List<String>, val loaders: List<String>, val status: String, val version_type: String)

    fun checkForUpdates() {
        if (System.currentTimeMillis() - lastCheck < 300000 || isMessageShown) return
        lastCheck = System.currentTimeMillis()

        CompletableFuture.supplyAsync {
            val github = checkGitHub()
            val modrinth = checkModrinth()

            val latest = listOfNotNull(github?.first, modrinth?.first)
                .maxByOrNull { compareVersions(it, current) } ?: return@supplyAsync

            if (compareVersions(latest, current) > 0) {
                isMessageShown = true
                val message = ChatComponentText("§c[Zen] §fUpdate available! §c$current §f-> §c$latest")

                val downloadMsg = ChatComponentText("§c[Zen] §fDownload: ")

                modrinth?.second?.let { url ->
                    val modrinthBtn = ChatComponentText("§a[Modrinth]")
                    modrinthBtn.chatStyle = ChatStyle()
                        .setChatClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, url))
                        .setChatHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText("Open Modrinth")))
                    downloadMsg.appendSibling(modrinthBtn)
                }

                if (github?.second != null && modrinth?.second != null)
                    downloadMsg.appendSibling(ChatComponentText("§f | "))

                github?.second?.let { url ->
                    val githubBtn = ChatComponentText("§b[GitHub]")
                    githubBtn.chatStyle = ChatStyle()
                        .setChatClickEvent(ClickEvent(ClickEvent.Action.OPEN_URL, url))
                        .setChatHoverEvent(HoverEvent(HoverEvent.Action.SHOW_TEXT, ChatComponentText("Open GitHub")))
                    downloadMsg.appendSibling(githubBtn)
                }

                mc.thePlayer!!.addChatMessage(message)
                mc.thePlayer!!.addChatMessage(downloadMsg)
            }
        }
    }

    private fun checkGitHub(): Pair<String, String>? {
        return try {
            val conn = URL("https://api.github.com/repos/kiwidotzip/zen/releases").openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "Zen")
            conn.connectTimeout = 10000
            conn.readTimeout = 30000

            if (conn.responseCode == 200) {
                val releases: List<GitHubRelease> = Gson().fromJson(conn.inputStream.reader(),
                    object : TypeToken<List<GitHubRelease>>() {}.type)
                releases.firstOrNull { !it.prerelease }?.let {
                    it.tag_name.replace("v", "") to it.html_url
                }
            } else null
        } catch (e: Exception) { null }
    }

    private fun checkModrinth(): Pair<String, String>? {
        return try {
            val conn = URL("https://api.modrinth.com/v2/project/zenmod/version").openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.setRequestProperty("User-Agent", "Zen")
            conn.connectTimeout = 10000
            conn.readTimeout = 30000

            if (conn.responseCode == 200) {
                val versions: List<ModrinthVersion> = Gson().fromJson(conn.inputStream.reader(),
                    object : TypeToken<List<ModrinthVersion>>() {}.type)
                versions.filter {
                    it.loaders.contains("forge") && it.status == "listed" && it.version_type == "release" && it.game_versions.contains("1.8.9")
                }.maxByOrNull { it.date_published }?.let {
                    it.version_number to "https://modrinth.com/mod/zenmod/version/${it.id}"
                }
            } else null
        } catch (e: Exception) { null }
    }

    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.replace(Regex("[^0-9.]"), "").split(".").map { it.toIntOrNull() ?: 0 }
        val parts2 = v2.replace(Regex("[^0-9.]"), "").split(".").map { it.toIntOrNull() ?: 0 }

        for (i in 0 until maxOf(parts1.size, parts2.size)) {
            val p1 = parts1.getOrElse(i) { 0 }
            val p2 = parts2.getOrElse(i) { 0 }
            if (p1 != p2) return p1 - p2
        }
        return 0
    }
}