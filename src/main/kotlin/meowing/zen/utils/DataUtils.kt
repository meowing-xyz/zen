package meowing.zen.utils

import meowing.zen.utils.JsonUtils.fromJson
import meowing.zen.utils.JsonUtils.toJson
import meowing.zen.utils.LoopUtils.loop
import java.io.File

// Code from https://github.com/Noamm9/NoammAddons/blob/master/src/main/kotlin/noammaddons/config/PogObject.kt
class DataUtils<T: Any>(fileName: String, private val defaultObject: T) {
    private val dataFile = File("config/Zen/${fileName}.json")
    private var data: T = loadData() ?: defaultObject
    private val autosaveIntervals = mutableMapOf<DataUtils<*>, Long>()
    private var lastSavedTime = System.currentTimeMillis()

    init {
        dataFile.parentFile.mkdirs()
        autosave(5)
    }

    private fun loadData(): T? {
        return try {
            if (dataFile.exists()) {
                val loadedData = fromJson(dataFile, defaultObject::class.java)
                if (loadedData != null && loadedData::class.java == defaultObject::class.java) {
                    loadedData
                }
                else {
                    println("[Zen-Data] No existing data found, loading defaults.")
                    null
                }
            }
            else {
                println("[Zen-Data] No existing data found, loading defaults.")
                null
            }
        }
        catch (e: Exception) {
            println("[Zen-Data] No existing data found, loading defaults.")
            null
        }
    }

    @Synchronized
    fun save() {
        try {
            toJson(dataFile, data)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun autosave(intervalMinutes: Long = 5) {
        scheduleSave(this, intervalMinutes)
    }

    fun setData(newData: T) {
        data = newData
    }

    fun getData(): T = data

    private fun scheduleSave(pogObject: DataUtils<*>, intervalMinutes: Long) {
        autosaveIntervals[pogObject] = intervalMinutes * 1000 * 60
    }

    init {
        loop(10_000) {
            val currentTime = System.currentTimeMillis()
            autosaveIntervals.forEach { (pogObject, interval) ->
                if (currentTime - pogObject.lastSavedTime < interval) return@forEach
                if (fromJson(pogObject.dataFile, defaultObject::class.java) == pogObject.data) return@forEach

                pogObject.save()
                pogObject.lastSavedTime = currentTime
            }
        }
    }
}