package meowing.zen.utils

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import meowing.zen.utils.LoopUtils.loop
import java.awt.Color
import java.io.File
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap

class DataUtils<T: Any>(fileName: String, private val defaultObject: T) {
    private val dataFile = File("config/Zen/${fileName}.json")
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(Color::class.java, object : JsonSerializer<Color>, JsonDeserializer<Color> {
            override fun serialize(
                src: Color,
                typeOfSrc: Type,
                context: JsonSerializationContext
            ): JsonElement {
                val obj = JsonObject()
                obj.addProperty("r", src.red.toDouble())
                obj.addProperty("g", src.green.toDouble())
                obj.addProperty("b", src.blue.toDouble())
                obj.addProperty("a", src.alpha.toDouble())
                return obj
            }

            override fun deserialize(
                json: JsonElement,
                typeOfT: Type,
                context: JsonDeserializationContext
            ): Color {
                val obj = json.asJsonObject
                val r = obj.get("r").asFloat.toInt()
                val g = obj.get("g").asFloat.toInt()
                val b = obj.get("b").asFloat.toInt()
                val a = obj.get("a").asFloat.toInt()
                return Color(r, g, b, a)
            }
        })
        .create()
    private var data: T = loadData() ?: defaultObject
    private var lastSavedTime = System.currentTimeMillis()

    companion object {
        private val autosaveIntervals = ConcurrentHashMap<DataUtils<*>, Long>()
        private var isLoopStarted = false

        @Synchronized
        private fun startSaveLoop() {
            if (isLoopStarted) return
            isLoopStarted = true

            loop(10_000) {
                val currentTime = System.currentTimeMillis()
                autosaveIntervals.forEach { (dataUtils, interval) ->
                    if (currentTime - dataUtils.lastSavedTime < interval) return@forEach
                    if (dataUtils.hasChanges()) {
                        dataUtils.save()
                        dataUtils.lastSavedTime = currentTime
                    }
                }
            }
        }
    }

    init {
        dataFile.parentFile.mkdirs()
        autosave(5)
        startSaveLoop()
    }

    private fun loadData(): T? {
        return try {
            if (dataFile.exists()) {
                fromJson(dataFile)
            } else null
        } catch (e: Exception) {
            null
        }
    }

    private fun fromJson(file: File): T? {
        return try {
            gson.fromJson(file.readText(), defaultObject::class.java) as T?
        } catch (e: Exception) {
            null
        }
    }

    private fun toJson(file: File, obj: T) {
        file.writeText(gson.toJson(obj))
    }

    private fun hasChanges(): Boolean {
        return try {
            val currentFileData = if (dataFile.exists()) fromJson(dataFile) else null
            currentFileData != data
        } catch (e: Exception) {
            true
        }
    }

    @Synchronized
    fun save() {
        try {
            toJson(dataFile, data)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun autosave(intervalMinutes: Long = 5) {
        autosaveIntervals[this] = intervalMinutes * 60_000
    }

    fun setData(newData: T) {
        data = newData
    }

    fun getData(): T = data
}