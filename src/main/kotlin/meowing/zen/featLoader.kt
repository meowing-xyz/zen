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
        "general.worldage",
        "slayers.slayertimer",
        "slayers.slayerhighlight",
        "slayers.vengdmg",
        "slayers.vengtimer",
        "carrying.carrycounter",
        "dungeons.bloodtimer",
        "dungeons.termtracker",
        "dungeons.keyalert",
        "dungeons.keyhighlight",
        "dungeons.partyfinder"
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