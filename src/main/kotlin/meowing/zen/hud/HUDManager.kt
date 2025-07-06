package meowing.zen.hud

import meowing.zen.utils.DataUtils

data class HUDPosition(var x: Float, var y: Float, var scale: Float = 1f, var enabled: Boolean = true)
data class HUDPositions(val positions: MutableMap<String, HUDPosition> = mutableMapOf())

object HUDManager {
    private val elements = mutableMapOf<String, String>()
    private val hudData = DataUtils("hud_positions", HUDPositions())

    fun register(name: String, exampleText: String) {
        elements[name] = exampleText
    }

    fun getElements(): Map<String, String> = elements

    fun getX(name: String): Float = hudData.getData().positions[name]?.x ?: 50f
    fun getY(name: String): Float = hudData.getData().positions[name]?.y ?: 50f
    fun getScale(name: String): Float = hudData.getData().positions[name]?.scale ?: 1f
    fun isEnabled(name: String): Boolean = hudData.getData().positions[name]?.enabled ?: true

    fun setPosition(name: String, x: Float, y: Float, scale: Float = 1f, enabled: Boolean = true) {
        hudData.getData().positions[name] = HUDPosition(x, y, scale, enabled)
        hudData.save()
    }

    fun toggle(name: String) {
        val positions = hudData.getData().positions
        val current = positions[name] ?: HUDPosition(10f, 10f)
        positions[name] = current.copy(enabled = !current.enabled)
        hudData.save()
    }
}