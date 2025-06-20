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
        "slayers.MetadataHandler",
        "slayers.slayertimer",
        "slayers.slayerhighlight",
        "slayers.vengdmg",
        "slayers.vengtimer",
        "carrying.carrycounter",
        "dungeons.bloodtimer",
        "dungeons.termtracker",
        "dungeons.keyalert",
        "dungeons.keyhighlight",
        "dungeons.partyfinder",
        "dungeons.serverlagtimer",
        "dungeons.firefreeze"
    )
    private var moduleCount = 0
    private var loadtime: Long = 0

    fun init() {
        val starttime = System.currentTimeMillis()
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
        loadtime = System.currentTimeMillis() - starttime
    }

    fun getModuleCount(): Int = moduleCount
    fun getLoadtime(): Long = loadtime
}