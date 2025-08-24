package meowing.zen.utils

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import meowing.zen.events.EventBus
import meowing.zen.events.GameEvent
import meowing.zen.utils.LoopUtils.loop
import meowing.zen.utils.TimeUtils.millis
import java.awt.Color
import java.io.File
import java.lang.reflect.Type
import java.util.concurrent.ConcurrentHashMap

class DataUtils<T: Any>(fileName: String, private val defaultObject: T, private val typeToken: TypeToken<T>? = null) {
    constructor(fileName: String, defaultObject: T) : this(fileName, defaultObject, null)
    private val dataFile = File("config/Zen/${fileName}.json")
    private val gson = GsonBuilder()
        .setPrettyPrinting()
        .registerTypeAdapter(Number::class.java, object : TypeAdapter<Number>() {
            override fun write(out: JsonWriter, value: Number?) {
                if (value == null) out.nullValue() else out.value(value)
            }

            override fun read(reader: JsonReader): Number? {
                if (reader.peek() == JsonToken.NULL) {
                    reader.nextNull()
                    return null
                }
                val value = reader.nextDouble()
                return if (value % 1.0 == 0.0 && value >= Int.MIN_VALUE && value <= Int.MAX_VALUE) value.toInt() else value
            }
        })
        .registerTypeAdapter(Color::class.java, object : JsonSerializer<Color>, JsonDeserializer<Color> {
            override fun serialize(
                src: Color,
                typeOfSrc: Type,
                context: JsonSerializationContext
            ): JsonElement {
                val obj = JsonObject()
                obj.addProperty("r", src.red)
                obj.addProperty("g", src.green)
                obj.addProperty("b", src.blue)
                obj.addProperty("a", src.alpha)
                return obj
            }

            override fun deserialize(
                json: JsonElement,
                typeOfT: Type,
                context: JsonDeserializationContext
            ): Color {
                val obj = json.asJsonObject
                val r = obj.get("r").asInt
                val g = obj.get("g").asInt
                val b = obj.get("b").asInt
                val a = obj.get("a").asInt
                return Color(r, g, b, a)
            }
        })
        .create()
    private var data: T = loadData() ?: defaultObject
    private var lastSavedTime = TimeUtils.now

    companion object {
        private val autosaveIntervals = ConcurrentHashMap<DataUtils<*>, Long>()
        private var isLoopStarted = false

        @Synchronized
        private fun startSaveLoop() {
            if (isLoopStarted) return
            isLoopStarted = true

            loop(10_000) {
                autosaveIntervals.forEach { (dataUtils, interval) ->
                    if (dataUtils.lastSavedTime.since.millis < interval) return@forEach
                    if (dataUtils.hasChanges()) {
                        dataUtils.save()
                        dataUtils.lastSavedTime = TimeUtils.now
                    }
                }
            }
        }
    }

    init {
        dataFile.parentFile.mkdirs()
        autosave(5)
        startSaveLoop()
        EventBus.register<GameEvent.Unload> ({ save() })
    }

    private fun loadData(): T? {
        return try {
            if (dataFile.exists()) {
                fromJson(dataFile)
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun fromJson(file: File): T? {
        return try {
            val type = typeToken?.type ?: defaultObject::class.java
            gson.fromJson(file.readText(), type)
        } catch (_: Exception) {
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
        } catch (_: Exception) {
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

    operator fun invoke(): T = data

    fun update(block: T.() -> Unit) {
        block(data)
    }

    fun updateAndSave(block: T.() -> Unit) {
        update(block)
        save()
    }

    fun reset() {
        data = defaultObject
    }

    fun resetAndSave() {
        reset()
        save()
    }

    fun reload() {
        loadData()?.let { data = it }
    }

    fun copy(): T {
        return gson.fromJson(gson.toJson(data), data::class.java)
    }

    fun exists(): Boolean = dataFile.exists()

    fun delete(): Boolean {
        return try {
            dataFile.delete()
        } catch (_: Exception) {
            false
        }
    }
}