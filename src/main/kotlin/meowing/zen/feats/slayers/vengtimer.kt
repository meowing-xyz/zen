package meowing.zen.feats.slayers

import meowing.zen.Zen.Companion.mc
import meowing.zen.utils.LoopUtils.setTimeout
import meowing.zen.utils.TickScheduler
import meowing.zen.utils.Utils.removeFormatting
import cc.polyfrost.oneconfig.hud.TextHud
import meowing.zen.events.AttackEntityEvent
import meowing.zen.events.ChatMessageEvent
import meowing.zen.events.ScoreboardEvent
import meowing.zen.feats.Feature
import net.minecraft.entity.monster.EntityBlaze
import java.util.regex.Pattern

object vengtimer : Feature("vengtimer") {
    var starttime: Long = 0
    var hit = false
    private val fail = Pattern.compile("^ {2}SLAYER QUEST FAILED!$")
    private var isFighting = false
    private var cachedNametag: net.minecraft.entity.Entity? = null

    override fun initialize()  {
        register<ScoreboardEvent> ({ event ->
            val scoreboard = mc.theWorld?.scoreboard ?: return@register
            val objective = scoreboard.getObjectiveInDisplaySlot(1) ?: return@register

            for (score in scoreboard.getSortedScores(objective)) {
                val playerName = score.playerName ?: continue
                if (playerName.startsWith("#")) continue
                val displayName = scoreboard.getPlayersTeam(playerName)?.formatString(playerName) ?: playerName
                val cleanName = displayName.removeFormatting()

                when {
                    cleanName.contains("Slay the boss!") && !isFighting -> isFighting = true
                    cleanName.contains("Boss slain!") && isFighting -> cleanup()
                }
            }
        })

        register<ChatMessageEvent> ({ event ->
            if (fail.matcher(event.message.removeFormatting()).matches() && isFighting) {
                TickScheduler.scheduleServer(10) { cleanup() }
            }
        })

        register<AttackEntityEvent> ({ event ->
            if (hit || event.target !is EntityBlaze || !isFighting) return@register

            val player = mc.thePlayer ?: return@register
            val heldItem = player.heldItem ?: return@register

            if (event.entityPlayer.name != player.name || !heldItem.displayName.removeFormatting().contains("Pyrochaos Dagger", true)) return@register

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
        })
    }

    private fun cleanup() {
        isFighting = false
        cachedNametag = null
        if (starttime > 0) starttime = 0
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