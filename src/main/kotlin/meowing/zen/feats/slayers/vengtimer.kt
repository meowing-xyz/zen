package meowing.zen.feats.slayers

import meowing.zen.Zen
import meowing.zen.events.ScoreboardEvent
import meowing.zen.utils.LoopUtils.setTimeout
import meowing.zen.utils.TickScheduler
import meowing.zen.utils.Utils.removeFormatting
import cc.polyfrost.oneconfig.hud.TextHud
import net.minecraft.client.Minecraft
import net.minecraft.entity.Entity
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
    private var cachedNametag: Entity? = null
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
            val cleanName = displayName.removeFormatting()

            when {
                cleanName.contains("Slay the boss!") && !isFighting -> {
                    isFighting = true
                    MinecraftForge.EVENT_BUS.register(attackListener)
                }
                cleanName.contains("Boss slain!") && isFighting -> cleanup()
            }
        }
    }

    @SubscribeEvent
    fun onChatReceive(event: ClientChatReceivedEvent) {
        if (fail.matcher(event.message.unformattedText.removeFormatting()).matches() && isFighting)
            TickScheduler.scheduleServer(10) {
                cleanup()
            }
    }

    private fun cleanup() {
        isFighting = false
        cachedNametag = null
        if (starttime > 0) starttime = 0
        try {
            MinecraftForge.EVENT_BUS.unregister(attackListener)
        } catch (ignored: Exception) {}
    }

    private object attackListener {
        @SubscribeEvent
        fun onAttackEntity(event: AttackEntityEvent) {
            if (hit || event.target !is EntityBlaze) return

            val player = mc.thePlayer ?: return
            val heldItem = player.heldItem ?: return

            if (event.entityPlayer.name != player.name || !heldItem.displayName.removeFormatting().contains("Pyrochaos Dagger", true)) return

            val nametagEntity = cachedNametag ?: mc.theWorld?.loadedEntityList?.find { entity ->
                val name = entity.name?.removeFormatting() ?: return@find false
                name.contains("Spawned by") && name.endsWith("by: ${player.name}")
            }?.also { cachedNametag = it }

            if (nametagEntity != null && event.target.entityId == (nametagEntity.entityId - 3)) {
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

class VengTimer : TextHud(true, 100, 200) {
    override fun getLines(lines: MutableList<String>, example: Boolean) {
        if (example) {
            lines.add("§bVeng proc: §c4.3s")
            return
        }

        if (vengtimer.hit && vengtimer.starttime > 0) {
            val timeLeft = (vengtimer.starttime - System.currentTimeMillis()) / 1000.0
            if (timeLeft > 0) lines.add("§bVeng proc: §c${"%.1f".format(timeLeft)}s")
        }
    }
}