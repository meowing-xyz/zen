package meowing.zen.feats

object FeatureLoader {
    private val features = arrayOf(
        "meowing.automeow",
        "meowing.meowdeathsounds",
        "meowing.meowsounds",
        "general.guildmessage",
        "general.partymessage",
        "general.guildjoinleave",
        "general.friendjoinleave",
        "general.betterah",
        "general.betterbz",
        "general.customsize",
        "general.worldage",
        "general.nohurtcam",
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
                Class.forName(fullClassName)
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