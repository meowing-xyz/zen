package meowing.zen

import com.google.gson.JsonParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import meowing.zen.compat.OldConfig
import meowing.zen.config.ZenConfig
import meowing.zen.config.ui.ConfigUI
import meowing.zen.events.*
import meowing.zen.features.Debug
import meowing.zen.features.Feature
import meowing.zen.features.FeatureLoader
import meowing.zen.features.general.ContributorColor
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.DataUtils
import meowing.zen.utils.NetworkUtils
import meowing.zen.utils.TickUtils
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.event.ClickEvent
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent
import net.minecraftforge.fml.common.event.FMLServerStoppingEvent
import org.apache.logging.log4j.LogManager

@Mod(modid = "zen", name = "Zen", version = "1.8.9", useMetadata = true, clientSideOnly = true)
class Zen {
    data class PersistentData (val isFirstInstall: Boolean = true)
    private var eventCall: EventBus.EventCall? = null
    private lateinit var FirstInstall: DataUtils<PersistentData>

    @Target(AnnotationTarget.CLASS)
    annotation class Module

    @Target(AnnotationTarget.CLASS)
    annotation class Command

    @Mod.EventHandler
    fun init(event: FMLInitializationEvent) {
        EventBus.post(GameEvent.Load())

        OldConfig.convertConfig(mc.mcDataDir)
        configUI = ZenConfig()
        FeatureLoader.init()
        initializeFeatures()
        executePending()

        mc.renderManager.skinMap.let {
            it["slim"]?.run { addLayer(ContributorColor.CosmeticRendering()) }
            it["default"]?.run { addLayer(ContributorColor.CosmeticRendering()) }
        }

        FirstInstall = DataUtils("zen-data", PersistentData())

        eventCall = EventBus.register<EntityEvent.Join> ({ event ->
            if (event.entity == mc.thePlayer) {
                ChatUtils.addMessage(
                    "$prefix §fMod loaded.",
                    "§c${FeatureLoader.getModuleCount()} modules §8- §c${FeatureLoader.getLoadtime()}ms §8- §c${FeatureLoader.getCommandCount()} commands"
                )
                val data = FirstInstall.getData()
                if (data.isFirstInstall) {
                    ChatUtils.addMessage("$prefix §fThanks for installing Zen!")
                    ChatUtils.addMessage("§7> §fUse §c/zen §fto open the config or §c/zenhud §fto edit HUD elements")
                    ChatUtils.addMessage("§7> §cDiscord:§b [Discord]", "Discord server", ClickEvent.Action.OPEN_URL, "https://discord.gg/KPmHQUC97G")
                    FirstInstall.setData(data.copy(isFirstInstall = false))
                    FirstInstall.save()
                }
                if (Debug.debugmode) ChatUtils.addMessage("$prefix §fYou have debug mode enabled, restart the game if this was not intentional.")
                UpdateChecker.checkForUpdates()
                eventCall?.unregister()
                eventCall = null
            }
        })

        EventBus.register<GuiEvent.Open> ({ event ->
            if (event.screen is GuiInventory) isInInventory = true
        })

        EventBus.register<GuiEvent.Close> ({
            isInInventory = false
        })

        EventBus.register<AreaEvent.Main> ({
            TickUtils.scheduleServer(1) {
                areaFeatures.forEach { it.update() }
            }
        })

        EventBus.register<AreaEvent.Sub> ({
            TickUtils.scheduleServer(1) {
                subareaFeatures.forEach { it.update() }
            }
        })

        NetworkUtils.getJson(
            "https://api.hypixel.net/v2/resources/skyblock/election",
            onSuccess = { jsonObject ->
                if (jsonObject.get("success")?.asBoolean != true) return@getJson

                val dataElement = jsonObject.get("data") ?: return@getJson
                val dataObj = JsonParser().parse(dataElement.toString()).asJsonObject
                mayorData = com.google.gson.Gson().fromJson(dataObj, ApiMayor::class.java)
            },
            onError = { exception ->
                LOGGER.warn("Failed to fetch election data: ${exception.message}")
            }
        )
    }

    @Mod.EventHandler
    fun stop(event: FMLServerStoppingEvent) {
        EventBus.post(GameEvent.Unload())
    }

    companion object {
        @JvmField val LOGGER = LogManager.getLogger("zen")
        private val pendingCallbacks = mutableListOf<Pair<String, (Any) -> Unit>>()
        private val pendingFeatures = mutableListOf<Feature>()
        private val areaFeatures = mutableListOf<Feature>()
        private val subareaFeatures = mutableListOf<Feature>()
        lateinit var configUI: ConfigUI
        const val prefix = "§7[§bZen§7]"
        val features = mutableListOf<Feature>()
        val mc: Minecraft = Minecraft.getMinecraft()
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        var isInInventory = false
        var mayorData: ApiMayor? = null

        private fun executePending() {
            pendingCallbacks.forEach { (configKey, callback) ->
                configUI.registerListener(configKey, callback)
            }
            pendingCallbacks.clear()
        }

        fun registerListener(configKey: String, instance: Any) {
            val callback: (Any) -> Unit = { _ ->
                if (instance is Feature) instance.update()
            }

            if (::configUI.isInitialized) configUI.registerListener(configKey, callback) else pendingCallbacks.add(configKey to callback)
        }

        fun initializeFeatures() {
            pendingFeatures.forEach { feature ->
                features.add(feature)
                if (feature.hasAreas()) areaFeatures.add(feature)
                if (feature.hasSubareas()) subareaFeatures.add(feature)
                feature.addConfig(configUI)
                feature.initialize()
                feature.configKey?.let {
                    registerListener(it, feature)
                }
                feature.update()
            }
            pendingFeatures.clear()
        }

        fun addFeature(feature: Feature) = pendingFeatures.add(feature)
        fun openConfig() = mc.displayGuiScreen(configUI)
    }
}

@Serializable
data class ApiMayor(@SerialName("mayor") val mayor: Candidate, ) {
    @Serializable
    data class Candidate(
        @SerialName("name")
        val name: String,
        @SerialName("perks")
        val perks: List<Perk> = emptyList(),
        @SerialName("minister")
        val minister: Candidate? = null
    )

    @Serializable
    data class Perk(val name: String, val description: String)
}