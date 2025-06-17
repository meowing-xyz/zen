package meowing.zen.feats.carrying

import cc.polyfrost.oneconfig.hud.TextHud

class CarryHud : TextHud(true, 200, 100) {
    override fun getLines(lines: MutableList<String>, example: Boolean) {
        if (example) {
            lines.add("§c[Zen] §f§lCarries:")
            lines.add("§7> §bPlayer1§f: §b5§f/§b10 §7(§c2.3s §7| §e45/hr§7)")
            lines.add("§7> §bPlayer2§f: §b1§f/§b3 §7(§c15.7s §7| §e32/hr§7)")
            return
        }

        if (carrycounter.carryees.isEmpty()) return

        lines.add("§c[Zen] §f§lCarries:")
        carrycounter.carryees.forEach { carryee ->
            lines.add("§7> §b${carryee.name}§f: §b${carryee.count}§f/§b${carryee.total} §7(${carryee.getTimeSinceLastBoss()} §7| ${carryee.getBossPerHour()}§7)")
        }
    }
}