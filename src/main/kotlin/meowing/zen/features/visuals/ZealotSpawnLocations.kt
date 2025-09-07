package meowing.zen.features.visuals

import meowing.zen.Zen
import meowing.zen.config.ConfigDelegate
import meowing.zen.config.ui.ConfigUI
import meowing.zen.config.ui.types.ConfigElement
import meowing.zen.config.ui.types.ElementType
import meowing.zen.events.RenderEvent
import meowing.zen.events.SkyblockEvent
import meowing.zen.features.ClientTick
import meowing.zen.features.Feature
import meowing.zen.utils.LocationUtils
import meowing.zen.utils.Render3D
import meowing.zen.utils.SimpleTimeMark
import meowing.zen.utils.TimeUtils.fromNow
import meowing.zen.utils.TimeUtils.millis
import meowing.zen.utils.Utils.toFormattedDuration
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.Vec3
import java.awt.Color
import kotlin.time.Duration.Companion.seconds

@Zen.Module
object ZealotSpawnLocations : Feature("zealotspawnvisual", true, "the end", listOf("Zealot Bruiser Hideout", "Zealot Bruiser", "Dragon's Nest")) {
    private val zealotSpawns: List<BlockPos> = listOf(
        BlockPos(-646, 5, -274),
        BlockPos(-633, 5, -277),
        BlockPos(-639, 7, -305),
        BlockPos(-631, 5, -327),
        BlockPos(-619, 6, -313),
        BlockPos(-665, 10, -313),
        BlockPos(-632, 5, -260),
        BlockPos(-630, 7, -229),
        BlockPos(-647, 5, -221),
        BlockPos(-684, 5, -261),
        BlockPos(-699, 5, -263),
        BlockPos(-683, 5, -292),
        BlockPos(-698, 5, -319),
        BlockPos(-714, 5, -289),
        BlockPos(-732, 5, -295),
        BlockPos(-731, 5, -275)
    )

    private val bruiserSpawns: List<BlockPos> = listOf(
        BlockPos(-595, 80, -190),
        BlockPos(-575, 72, -201),
        BlockPos(-560, 64, -220),
        BlockPos(-554, 56, -237),
        BlockPos(-571, 51, -240),
        BlockPos(-585, 52, -232),
        BlockPos(-96, 55, -216),
        BlockPos(-578, 53, -214),
        BlockPos(-598, 55, -201),
        BlockPos(-532, 38, -223),
        BlockPos(-520, 38, -235),
        BlockPos(-530, 38, -246),
        BlockPos(-515, 39, -250),
        BlockPos(-516, 39, -264),
        BlockPos(-513, 38, -279),
        BlockPos(-524, 44, -268),
        BlockPos(-536, 48, -252),
        BlockPos(-526, 38, -294),
        BlockPos(-514, 39, -304),
        BlockPos(-526, 39, -317)
    )

    private var spawnTime = SimpleTimeMark(0)
    private var displayText = "§dZealot Spawn: §5Ready"

    private val drawzealotspawnbox by ConfigDelegate<Boolean>("drawzealotspawnbox")
    private val drawzealotspawncolor by ConfigDelegate<Color>("drawzealotspawncolor")

    override fun addConfig(configUI: ConfigUI): ConfigUI {
        return configUI
            .addElement("Visuals", "Zealot Spawn Locations", ConfigElement(
                "zealotspawnvisual",
                null,
                ElementType.Switch(false)
            ), isSectionToggle = true)
            .addElement("Visuals", "Zealot Spawn Locations", "Options", ConfigElement(
                "drawzealotspawnbox",
                "Zealot Spawn Location Boxes",
                ElementType.Switch(false)
            ))
            .addElement("Visuals", "Zealot Spawn Locations", "Options", ConfigElement(
                "drawzealotspawncolor",
                "Box color",
                ElementType.ColorPicker(Color(0, 255, 255, 127)),
                { config -> config["drawzealotspawnbox"] as? Boolean == true }
            ))
    }

    override fun initialize() {
        setupLoops {
            loop<ClientTick>(10) {
                val timeUntilSpawn = spawnTime.until
                val remaining = if (timeUntilSpawn.isPositive() && timeUntilSpawn.millis > 1000) timeUntilSpawn.millis.toFormattedDuration() else "§aReady"
                val mobType = if (LocationUtils.checkSubarea("dragon's nest")) "Zealot" else "Bruiser"
                displayText = "§d$mobType Spawn: §5$remaining"
            }
        }

        register<SkyblockEvent.EntitySpawn> { event ->
            val mobId = event.skyblockMob.id
            if ((LocationUtils.checkSubarea("zealot bruiser hideout") && mobId == "Zealot Bruiser") ||
                (LocationUtils.checkSubarea("dragon's nest") && mobId == "Zealot")) {
                spawnTime = 8.seconds.fromNow
            }
        }

        register<RenderEvent.World> { event ->
            val positions = if (LocationUtils.checkSubarea("dragon's nest")) zealotSpawns else bruiserSpawns
            positions.forEach { pos ->
                val aabb = AxisAlignedBB(pos.x - 5.0, pos.y + 0.1, pos.z - 5.0, pos.x + 5.0, pos.y - 3.0, pos.z + 5.0)
                if (drawzealotspawnbox) {
                    Render3D.drawSpecialBB(aabb, drawzealotspawncolor, event.partialTicks)
                }
                Render3D.drawString(
                    displayText,
                    Vec3(pos).addVector(0.0, 1.5,   0.0),
                    event.partialTicks,
                    true
                )
            }
        }
    }
}