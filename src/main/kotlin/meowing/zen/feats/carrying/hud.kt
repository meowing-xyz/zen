package meowing.zen.feats.carrying

import cc.polyfrost.oneconfig.hud.TextHud

class CarryHud : TextHud(true, 200, 100) {
    override fun getLines(lines: MutableList<String>, example: Boolean) {
        if (example) {
            lines.add("§c[Zen] §f§lCarries:")
            lines.add("§7> §bPlayer1§f: §b5§f/§b10 §7(2.3s | 45/hr)")
            lines.add("§7> §bPlayer2§f: §b1§f/§b3 §7(15.7s | §e32/hr)")
            return
        }

        if (carrycounter.carryees.isEmpty()) return

        lines.add("§c[Zen] §f§lCarries:")
        carrycounter.carryees.forEach { carryee ->
            lines.add("§7> §b${carryee.name}§f: §b${carryee.count}§f/§b${carryee.total} §7(${carryee.getTimeSinceLastBoss()} | ${carryee.getBossPerHour()}§7)")
        }
    }
}