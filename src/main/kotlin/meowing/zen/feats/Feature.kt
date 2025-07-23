package meowing.zen.feats

import meowing.zen.Zen
import meowing.zen.Zen.Companion.prefix
import meowing.zen.config.ui.ConfigUI
import meowing.zen.events.Event
import meowing.zen.events.EventBus
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.LocationUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.gui.FontRenderer
import net.minecraft.client.multiplayer.WorldClient
import net.minecraft.world.World

/*
 * Modified from Devonian code
 * Under GPL 3.0 License
 */
open class Feature(
    val configKey: String? = null,
    val checkSB: Boolean = false,
    area: Any? = null,
    subarea: Any? = null
) {
    val events = mutableListOf<EventBus.EventCall>()
    private var isRegistered = false
    private val areas = when (area) {
        is String -> listOf(area.lowercase())
        is List<*> -> area.filterIsInstance<String>().map { it.lowercase() }
        else -> emptyList()
    }
    private val subareas = when (subarea) {
        is String -> listOf(subarea.lowercase())
        is List<*> -> subarea.filterIsInstance<String>().map { it.lowercase() }
        else -> emptyList()
    }

    init {
        initialize()
        configKey?.let {
            Zen.registerListener(it, this)
        }
        Zen.addFeature(this)
        update()
    }

    private fun checkConfig(): Boolean {
        return try {
            val configEnabled = configKey?.let {
                config.getValue(it, false)
            } ?: true
            configEnabled
        } catch (_: Exception) {
            false
        }
    }

    protected val mc: Minecraft = Zen.mc
    protected val fontRenderer: FontRenderer = mc.fontRendererObj
    protected inline val config get() = Zen.config
    protected inline val player: EntityPlayerSP? get() = mc.thePlayer
    protected inline val world: WorldClient? get() = mc.theWorld

    open fun initialize() {}

    open fun onRegister() {
        if (Debug.debugmode) ChatUtils.addMessage("$prefix §fRegistering §b$configKey")
    }

    open fun onUnregister() {
        if (Debug.debugmode) ChatUtils.addMessage("$prefix §fUnregistering §b$configKey")
    }

    open fun addConfig(configUI: ConfigUI): ConfigUI = configUI

    fun isEnabled(): Boolean = checkConfig() && (!checkSB || LocationUtils.inSkyblock) && inArea() && inSubarea()

    fun update() = onToggle(isEnabled())

    @Synchronized
    open fun onToggle(state: Boolean) {
        if (state == isRegistered) return

        if (state) {
            events.forEach { it.register() }
            onRegister()
            isRegistered = true
        } else {
            events.forEach { it.unregister() }
            onUnregister()
            isRegistered = false
        }
    }

    fun inArea(): Boolean = areas.isEmpty() || areas.any { LocationUtils.checkArea(it) }

    fun inSubarea(): Boolean = subareas.isEmpty() || subareas.any { LocationUtils.checkSubarea(it) }

    inline fun <reified T : Event> register(noinline cb: (T) -> Unit) {
        events.add(EventBus.register<T>(cb, false))
    }

    fun hasAreas(): Boolean = areas.isNotEmpty()
    fun hasSubareas(): Boolean = subareas.isNotEmpty()
    fun checksSkyblock(): Boolean = checkSB
}