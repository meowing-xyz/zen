package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.events.ScoreboardEvent
import meowing.zen.utils.LoopUtils.setTimeout
import meowing.zen.utils.TickScheduler
import meowing.zen.utils.Utils.removeFormatting
import cc.polyfrost.oneconfig.hud.TextHud
import net.minecraft.client.Minecraft
import net.minecraft.entity.monster.EntityBlaze
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.event.entity.player.AttackEntityEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.regex.Pattern

object vengtimer {
    private val mc = Minecraft.getMinecraft()
    private val fail = Pattern.compile("^ {2}SLAYER QUEST FAILED!$")
    private var isFighting = false
    var starttime: Long = 0
    var hit = false

    @JvmStatic
    fun initialize() {
        Zen.registerListener("vengtimer", this)
    }

    @SubscribeEvent
    fun onScoreboard(event: ScoreboardEvent) {
        val scoreboard = mc.theWorld?.scoreboard ?: return
        val objective = scoreboard.getObjectiveInDisplaySlot(1) ?: return

        for (score in scoreboard.getSortedScores(objective)) {
            val playerName = score.playerName ?: continue
            if (playerName.startsWith("#")) continue
            val displayName = scoreboard.getPlayersTeam(playerName)?.formatString(playerName) ?: playerName

            when {
                displayName.removeFormatting().contains("Slay the boss!") && !isFighting -> {
                    isFighting = true
                    MinecraftForge.EVENT_BUS.register(attackEntity)
                }
                displayName.removeFormatting().contains("Boss slain!") && isFighting -> {
                    isFighting = false
                    MinecraftForge.EVENT_BUS.unregister(attackEntity)
                }
            }
        }
    }

    @SubscribeEvent
    fun onChatReceive(event: ClientChatReceivedEvent) {
        if (fail.matcher(event.message.unformattedText.removeFormatting()).matches() && isFighting) {
            isFighting = false
            TickScheduler.scheduleServer(10) {
                MinecraftForge.EVENT_BUS.unregister(attackEntity)
            }
        }
    }

    object attackEntity {
        @SubscribeEvent
        fun onAttackEntity(event: AttackEntityEvent) {
            if (hit) return
            val player = event.entityPlayer
            val target = event.target
            if (target is EntityBlaze && player.name == mc.thePlayer?.name && mc.thePlayer?.heldItem?.displayName?.removeFormatting()?.contains("Pyrochaos Dagger") == true) {
                val nametagEntity = mc.theWorld.loadedEntityList.find { entity ->
                    val name = entity.name?.removeFormatting() ?: return@find false
                    name.contains("Spawned by") && name.endsWith("by: ${mc.thePlayer?.name}") && target.getDistanceToEntity(entity) <= 10
                }
                if (nametagEntity != null) {
                    starttime = System.currentTimeMillis() + 6000
                    hit = true
                    setTimeout(5950) {
                        starttime = 0
                        hit = false
                    }
                }
            }
        }
    }
}

class VengTimer : TextHud(true, 100, 200) {
    override fun getLines(lines: MutableList<String>, example: Boolean) {
        if (example) lines.add("§fVeng proc: §c4.3s")
        if (vengtimer.hit && vengtimer.starttime > 0) lines.add("§fVeng proc: §c${"%.1f".format((vengtimer.starttime - System.currentTimeMillis()) / 1000.0)}s")
    }
}
