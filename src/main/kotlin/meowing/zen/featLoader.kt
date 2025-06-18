package meowing.zen

object FeatLoader {
    private val features = arrayOf(
        "meowing.automeow",
        "meowing.meowdeathsounds",
        "meowing.meowsounds",
        "general.cleanmsgs",
        "general.cleanjoin",
        "general.betterah",
        "general.betterbz",
        "general.customsize",
        "slayers.slayertimer",
        "slayers.slayerhighlight",
        "carrying.carrycounter",
        "dungeons.bloodtimer"
    )
    private var moduleCount = 0

    fun init() {
        features.forEach { className ->
            try {
                val fullClassName = "meowing.zen.feats.$className"
                Class.forName(fullClassName).getDeclaredMethod("initialize").invoke(null)
                moduleCount++
            } catch (e: Exception) {
                System.err.println("[Zen] Error initializing $className: $e")
                e.printStackTrace()
            }
        }
    }

    fun getModuleCount(): Int = moduleCount
}