package meowing.zen

import meowing.zen.feats.automeow

object FeatLoader {
    private var moduleCount = 0

    fun init() {
        automeow.initialize()
        moduleCount = 1
    }

    fun getModuleCount(): Int = moduleCount
}