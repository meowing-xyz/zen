package meowing.zen.compat

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import java.io.File
import java.io.FileReader
import java.io.FileWriter

object OldConfig {
    private val gson = GsonBuilder().setPrettyPrinting().create()

    fun convertConfig(minecraftDir: File) {
        val oldConfigFile = File(minecraftDir, "config/Zen/ZenConfig.json")
        val newConfigFile = File(minecraftDir, "config/Zen/config.json")

        if (!oldConfigFile.exists()) return
        if (newConfigFile.exists()) return

        try {
            val oldConfig = gson.fromJson(FileReader(oldConfigFile), JsonObject::class.java)
            val newConfig = JsonObject()

            convertBooleanField(oldConfig, newConfig, "automeow", "automeow")
            convertBooleanField(oldConfig, newConfig, "meowsounds", "meowsounds")
            convertBooleanField(oldConfig, newConfig, "meowdeathsounds", "meowdeathsounds")
            newConfig.addProperty("meowmessage", false)

            convertBooleanField(oldConfig, newConfig, "guildmsg", "guildmessage")
            convertBooleanField(oldConfig, newConfig, "partymsg", "partymessage")
            convertBooleanField(oldConfig, newConfig, "guildjoinleave", "guildjoinleave")
            convertBooleanField(oldConfig, newConfig, "friendjoinleave", "friendjoinleave")

            convertBooleanField(oldConfig, newConfig, "betterah", "betterah")
            convertBooleanField(oldConfig, newConfig, "betterbz", "betterbz")

            convertBooleanField(oldConfig, newConfig, "customsize", "customsize")
            convertFloatField(oldConfig, newConfig, "customX", "customX")
            convertFloatField(oldConfig, newConfig, "customY", "customY")
            convertFloatField(oldConfig, newConfig, "customZ", "customZ")
            newConfig.addProperty("customself", false)

            convertBooleanField(oldConfig, newConfig, "worldagechat", "worldagechat")
            convertBooleanField(oldConfig, newConfig, "nohurtcam", "nohurtcam")

            convertBooleanField(oldConfig, newConfig, "blockoverlay", "blockoverlay")
            convertBooleanField(oldConfig, newConfig, "blockoverlayfill", "blockoverlayfill")
            convertColorField(oldConfig, "blockoverlaycolor")
            convertFloatField(oldConfig, newConfig, "blockoverlaywidth", "blockoverlaywidth")

            convertBooleanField(oldConfig, newConfig, "entityhighlight", "entityhighlight")
            convertColorField(oldConfig, "entityhighlightplayercolor")
            convertColorField(oldConfig, "entityhighlightmobcolor")
            convertColorField(oldConfig, "entityhighlightanimalcolor")
            convertColorField(oldConfig, "entityhighlightothercolor")
            convertFloatField(oldConfig, newConfig, "entityhighlightwidth", "entityhighlightwidth")

            convertBooleanField(oldConfig, newConfig, "arrowpoison", "arrowpoison")
            newConfig.addProperty("serveralert", false)

            convertBooleanField(oldConfig, newConfig, "slayertimer", "slayertimer")
            convertBooleanField(oldConfig, newConfig, "slayerhighlight", "slayerhighlight")
            convertColorField(oldConfig, "slayerhighlightcolor")
            convertFloatField(oldConfig, newConfig, "slayerhighlightwidth", "slayerhighlightwidth")
            convertBooleanField(oldConfig, newConfig, "slayerstats", "slayerstats")

            convertBooleanField(oldConfig, newConfig, "vengdmg", "vengdmg")
            convertBooleanField(oldConfig, newConfig, "vengtimer", "vengtimer")

            newConfig.addProperty("lasertimer", false)
            newConfig.addProperty("minibossspawn", false)

            convertBooleanField(oldConfig, newConfig, "carrycounter", "carrycounter")
            convertBooleanField(oldConfig, newConfig, "carrycountsend", "carrycountsend")
            convertStringField(oldConfig, newConfig, "carryvalue", "carryvalue")
            convertBooleanField(oldConfig, newConfig, "carrybosshighlight", "carrybosshighlight")
            convertColorField(oldConfig, "carrybosscolor")
            convertFloatField(oldConfig, newConfig, "carrybosswidth", "carrybosswidth")
            convertBooleanField(oldConfig, newConfig, "carryclienthighlight", "carryclienthighlight")
            convertColorField(oldConfig, "carryclientcolor",)
            convertFloatField(oldConfig, newConfig, "carryclientwidth", "carryclientwidth")

            convertBooleanField(oldConfig, newConfig, "bloodtimer", "bloodtimer")
            convertBooleanField(oldConfig, newConfig, "termtracker", "termtracker")

            convertBooleanField(oldConfig, newConfig, "keyalert", "keyalert")
            convertBooleanField(oldConfig, newConfig, "keyhighlight", "keyhighlight")
            convertColorField(oldConfig, "keyhighlightcolor")
            convertFloatField(oldConfig, newConfig, "keyhighlightwidth", "keyhighlightwidth")

            convertBooleanField(oldConfig, newConfig, "partyfindermsgs", "partyfindermsgs")
            convertBooleanField(oldConfig, newConfig, "serverlagtimer", "serverlagtimer")

            convertBooleanField(oldConfig, newConfig, "firefreeze", "firefreeze")
            convertBooleanField(oldConfig, newConfig, "cryptreminder", "cryptreminder")
            convertFloatField(oldConfig, newConfig, "cryptreminderdelay", "cryptreminderdelay")

            convertBooleanField(oldConfig, newConfig, "architectdraft", "architectdraft")
            convertBooleanField(oldConfig, newConfig, "selfdraft", "selfdraft")
            convertBooleanField(oldConfig, newConfig, "autogetdraft", "autogetdraft")

            newConfig.addProperty("boxstarmobs", false)
            convertColorField(newConfig, "boxstarmobscolor")
            newConfig.addProperty("boxstarmobswidth", 2.0)

            newConfig.addProperty("highlightlivid", false)
            convertColorField(newConfig, "highlightlividcolor")
            newConfig.addProperty("highlightlividwidth", 2.0)
            newConfig.addProperty("hidewronglivid", false)
            newConfig.addProperty("highlightlividline", false)

            convertBooleanField(oldConfig, newConfig, "hidedamage", "hidedamage")
            convertBooleanField(oldConfig, newConfig, "hidedeathanimation", "hidedeathanimation")
            convertBooleanField(oldConfig, newConfig, "hidefallingblocks", "hidefallingblocks")
            convertBooleanField(oldConfig, newConfig, "hidenonstarmobs", "hidenonstarmobs")
            convertBooleanField(oldConfig, newConfig, "hidestatuseffects", "hidestatuseffects")
            convertBooleanField(oldConfig, newConfig, "nothunder", "nothunder")
            convertBooleanField(oldConfig, newConfig, "noendermantp", "noendermantp")

            convertBooleanField(oldConfig, newConfig, "leapannounce", "leapannounce")
            convertStringField(oldConfig, newConfig, "leapmessage", "leapmessage")

            newConfig.addProperty("vanillahphud", false)
            newConfig.addProperty("scarfspawntimers", false)

            newConfigFile.parentFile.mkdirs()
            FileWriter(newConfigFile).use { writer ->
                gson.toJson(newConfig, writer)
            }

            oldConfigFile.delete()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun convertBooleanField(oldConfig: JsonObject, newConfig: JsonObject, oldKey: String, newKey: String, defaultValue: Boolean = false) {
        val value = if (oldConfig.has(oldKey)) oldConfig.get(oldKey).asBoolean else defaultValue
        newConfig.addProperty(newKey, value)
    }

    private fun convertFloatField(oldConfig: JsonObject, newConfig: JsonObject, oldKey: String, newKey: String, defaultValue: Float = 0.0f) {
        val value = if (oldConfig.has(oldKey)) oldConfig.get(oldKey).asFloat else defaultValue
        newConfig.addProperty(newKey, value)
    }

    private fun convertStringField(oldConfig: JsonObject, newConfig: JsonObject, oldKey: String, newKey: String, defaultValue: String = "") {
        val value = if (oldConfig.has(oldKey)) oldConfig.get(oldKey).asString else defaultValue
        newConfig.addProperty(newKey, value)
    }

    private fun convertColorField(config: JsonObject, key: String) {
        val color = JsonObject()
        color.addProperty("r", 0.0)
        color.addProperty("g", 255.0)
        color.addProperty("b", 255.0)
        color.addProperty("a", 127.0)
        config.add(key, color)
    }
}