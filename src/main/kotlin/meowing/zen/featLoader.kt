package meowing.zen

object FeatLoader {
    private val features = arrayOf(
        "meowing.automeow",
        "meowing.meowdeathsounds",
        "meowing.meowsounds",
        "general.cleanmsgs"
    )
    private var moduleCount = 0

    fun init() {
        features.forEach { className ->
            try {
                // Fix the class name construction
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