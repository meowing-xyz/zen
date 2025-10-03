package xyz.meowing.zen.api

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import xyz.meowing.zen.Zen
import xyz.meowing.zen.Zen.Companion.LOGGER
import xyz.meowing.zen.events.EventBus
import xyz.meowing.zen.events.InternalEvent
import xyz.meowing.zen.utils.DataUtils
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpHead
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream

@Zen.Module
object NEUApi {
    private var client: CloseableHttpClient = HttpClients.createDefault()
    private const val NeuZipUrl = "https://github.com/NotEnoughUpdates/NotEnoughUpdates-Repo/archive/master.zip"
    private val isDownloading = AtomicBoolean(false)

    var NeuItemData = DataUtils("NeuItems", JsonObject())
    var NeuMobData = DataUtils("NeuMobs", JsonObject())
    var NeuConstantData = DataUtils("NeuConstants", JsonObject())
    val etagData = DataUtils("NEUAPI-ETAG", JsonObject())

    fun downloadAndProcessRepo(force: Boolean = false) {
        if (!isDownloading.compareAndSet(false, true)) {
            return
        }

        try {
            val eTagString = etagData.getData().get("tag")?.asString ?: ""

            val request = HttpHead(NeuZipUrl).apply {
                if (eTagString.isNotEmpty()) {
                    setHeader("If-None-Match", eTagString)
                }
            }

            val response = client.execute(request)
            val matchesLastETag = response.statusLine.statusCode == 304

            if (!matchesLastETag || force) {
                client.execute(HttpGet(NeuZipUrl))
                    .takeIf { it.statusLine.statusCode == 200 }?.entity?.content?.use { zipStream ->
                        ZipInputStream(zipStream).use { zip ->
                            var entry: ZipEntry? = zip.nextEntry

                            val NeuItems = JsonObject()
                            val NeuMobs = JsonObject()
                            val NeuConstants = JsonObject()

                            while (entry != null) {
                                if (entry.name.endsWith(".json")) {
                                    val jsonContent = zip.bufferedReader().readText()
                                    val name = entry.name.split("/").last().removeSuffix(".json")

                                    try {
                                        val value = JsonParser().parse(jsonContent).asJsonObject

                                        when {
                                            entry.name.contains("/items/") -> NeuItems.add(name, value)
                                            entry.name.contains("/mobs/") -> NeuMobs.add(name, value)
                                            entry.name.contains("/constants/") -> NeuConstants.add(name, value)
                                        }
                                    } catch (e: Exception) {
                                        LOGGER.error("Failed to parse JSON from entry ${entry.name}, skipping...")
                                    }
                                }
                                entry = zip.nextEntry
                            }

                            NeuItemData.setData(NeuItems)
                            NeuMobData.setData(NeuMobs)
                            NeuConstantData.setData(NeuConstants)

                            zip.closeEntry()
                            LOGGER.info("NEU API data downloaded and processed successfully.")

                            val newETag = response.getFirstHeader("ETag")?.value
                            val newEtagJson = JsonObject().apply {
                                addProperty("tag", newETag ?: "")
                            }

                            etagData.setData(newEtagJson)
                            LOGGER.info("Saved NEU API Data to file and updated ETag.")

                            NeuItemData.save()
                            NeuMobData.save()
                            NeuConstantData.save()
                            etagData.save()
                        }
                    }
            } else {
                LOGGER.info("ETag matches. No need to download. Loading from file...")

                if (NeuItemData.getData().entrySet().isEmpty() || NeuMobData.getData().entrySet().isEmpty() || NeuConstantData.getData().entrySet().isEmpty()) {
                    LOGGER.warn("Failed to load NEU API data from file. Redownloading...")
                    isDownloading.set(false)
                    downloadAndProcessRepo(true)
                    return
                }
            }
        } finally {
            isDownloading.set(false)
            EventBus.post(InternalEvent.NeuAPI.Load())
        }
    }
}