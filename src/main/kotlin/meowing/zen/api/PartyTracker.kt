package meowing.zen.api

import meowing.zen.Zen
import meowing.zen.Zen.Companion.mc
import meowing.zen.events.*
import meowing.zen.utils.ChatUtils
import meowing.zen.utils.ItemUtils.lore
import meowing.zen.utils.LoopUtils.setTimeout
import meowing.zen.utils.Utils.chestName
import meowing.zen.utils.Utils.getRegexGroups
import meowing.zen.utils.Utils.removeFormatting
import net.minecraft.client.gui.inventory.GuiChest

@Zen.Module
object PartyTracker {
    private val destructionRegex = """^The party was disbanded because all invites expired and the party was empty\.|[^ ]+ has disbanded the party!|You (?:left|have left|have been kicked from|were kicked from) the party\.|You (?:are not in a party right now|are not in a party|are not currently in a party\.)""".toRegex()
    private val partyTransferRegex = """^The party was transferred to (?:\[[^]]+]\s*)?(?<newLeader>[^ ]+)(?: because (?<leavingPlayer>[^ ]+) left| by .*)?""".toRegex()
    private val partyJoinRegex = """^(?:\[[^]]+]\s*)?([^ ]+) joined the party\.""".toRegex()
    private val partyRemoveRegex = """^(?:\[[^]]+]\s*)?([^ ]+) has (?:left|been removed from) the party\.""".toRegex()
    private val joinPartyRegex = """^You have joined (?:\[[^]]+]\s*)?(?<partyLeader>[^ ]+)'s? party!""".toRegex()
    private val partyListRegex = """(Party (?:Leader|Members|Moderators):|\[[^]]+]\s*| ●)""".toRegex()
    private val partyDisconnectRegex = """^(?:The party leader, )?(?:\[[^]]+] )?(?<partyLeader>.*?) has disconnected""".toRegex()
    private val partyDetailsRegex = """^Party (?:Leader|Moderators|Members): .*""".toRegex()
    private val partyFinderJoinRegex = """^Party Finder > (?<playerName>\S+) joined the dungeon group! \((?<classType>.*) Level (?<classLvl>\d+)\)""".toRegex()
    private val partyFinderErrorRegex = """^Party Finder > (?:This group has been de-listed|You are already in a party!)|^You have just sent a join request recently!""".toRegex()
    private val classLoreRegex = """^(\w+):\s(\w+)\s\((\d+)\)$""".toRegex()

    private var hadProblemJoiningParty = false
    private val partyMembers = mutableMapOf<String, PartyMember>()
    private var playerInParty = false
    private var hidePartyList = false

    data class PartyMember(
        val name: String,
        var leader: Boolean = false,
        var online: Boolean = true,
        var className: String = "",
        var classLvl: String = ""
    )

    init {
        EventBus.register<ChatEvent.Receive> ({ event ->
            val clean = event.event.message.unformattedText.removeFormatting()

            when {
                clean.trim().matches(partyDetailsRegex) && hidePartyList -> {
                    event.cancel()
                    handlePartyMessage(clean)
                    return@register
                }
                clean == "-----------------------------------------------------" && hidePartyList -> {
                    hidePartyList = false
                    return@register
                }
            }

            handlePartyMessage(clean)
        })

        EventBus.register<GuiEvent.Slot.Click> ({ event ->
            val gui = event.gui as? GuiChest ?: return@register
            if (!gui.chestName.startsWith("Party Finder")) return@register

            val stackName = event.slot.stack?.displayName?.removeFormatting() ?: return@register
            if (!stackName.endsWith("'s Party")) return@register

            val stack = event.slot.stack
            if (stack.lore.any { it.removeFormatting().startsWith("Requires") }) return@register

            hadProblemJoiningParty = false

            setTimeout(1000) {
                if (hadProblemJoiningParty) return@setTimeout
                partyMembers.entries.removeIf { it.key != mc.thePlayer?.name }
                addSelfToParty(false)
                val stackLore = stack.lore
                for (line in stackLore) {
                    val clean = line.removeFormatting().trim()
                    if (clean.matches(classLoreRegex)) {
                        val groups = clean.getRegexGroups(classLoreRegex) ?: continue
                        val playerName = groups[1]?.value ?: continue
                        val className = groups[2]?.value ?: continue
                        val classLvl = groups[3]?.value ?: continue

                        partyMembers[playerName] = PartyMember(playerName).apply {
                            this.className = className
                            this.classLvl = classLvl
                        }
                    }
                }

                val leaderName = stackName.removeFormatting().split("'")[0]
                partyMembers[leaderName]?.leader = true
                playerInParty = true
            }
        })
    }

    private fun addSelfToParty(selfLeader: Boolean) {
        playerInParty = true
        val playerName = mc.thePlayer?.name ?: return
        if (!partyMembers.containsKey(playerName)) partyMembers[playerName] = PartyMember(playerName, selfLeader)
    }

    private fun handlePartyMessage(clean: String) {
        when {
            clean.matches(partyJoinRegex) -> {
                val playerName = partyJoinRegex.find(clean)?.groupValues?.get(1) ?: return
                partyMembers[playerName] = PartyMember(playerName)
                addSelfToParty(true)
                ChatUtils.command("/p list")
                hidePartyList = true
                EventBus.post(PartyEvent.Changed(PartyChangeType.MEMBER_JOINED, playerName, partyMembers.toMap()))
            }

            clean.matches(partyRemoveRegex) -> {
                val playerName = partyRemoveRegex.find(clean)?.groupValues?.get(1) ?: return
                partyMembers.remove(playerName)
                playerInParty = true
                EventBus.post(PartyEvent.Changed(PartyChangeType.MEMBER_LEFT, playerName, partyMembers.toMap()))
            }

            clean == "You have been kicked from the party" -> {
                partyMembers.clear()
                playerInParty = false
                EventBus.post(PartyEvent.Changed(PartyChangeType.PLAYER_LEFT, null, partyMembers.toMap()))
            }

            clean.matches(partyTransferRegex) -> {
                val match = partyTransferRegex.find(clean) ?: return
                partyMembers.values.forEach { it.leader = false }

                val newLeader = match.groups["newLeader"]?.value ?: return
                val oldLeader = match.groups["leavingPlayer"]?.value

                partyMembers[newLeader]?.leader = true
                oldLeader?.let { partyMembers.remove(it) }

                playerInParty = true
                EventBus.post(PartyEvent.Changed(PartyChangeType.LEADER_CHANGED, newLeader, partyMembers.toMap()))
            }

            clean.matches(destructionRegex) -> {
                partyMembers.clear()
                playerInParty = false
                EventBus.post(PartyEvent.Changed(PartyChangeType.DISBANDED, null, partyMembers.toMap()))
            }

            clean.matches(joinPartyRegex) -> {
                partyMembers.clear()
                val leaderName = joinPartyRegex.find(clean)?.groups?.get("partyLeader")?.value ?: return
                partyMembers[leaderName] = PartyMember(leaderName, true)
                addSelfToParty(false)
                EventBus.post(PartyEvent.Changed(PartyChangeType.PLAYER_JOINED, leaderName, partyMembers.toMap()))
            }

            clean.matches(partyDetailsRegex) -> {
                val playerNames = clean.replace(partyListRegex, "").trim().split(" ")
                val isLeaderLine = clean.startsWith("Party Leader:")

                playerNames.forEach { name ->
                    val member = partyMembers.getOrPut(name) { PartyMember(name) }
                    member.leader = isLeaderLine
                }

                playerInParty = true
                EventBus.post(PartyEvent.Changed(PartyChangeType.LIST, members = partyMembers.toMap()))
            }

            clean.matches(partyDisconnectRegex) -> {
                val leaderName = partyDisconnectRegex.find(clean)?.groups?.get("partyLeader")?.value ?: return
                partyMembers.values.forEach { it.leader = false }
                partyMembers[leaderName]?.let { member ->
                    member.online = false
                    member.leader = true
                }
            }

            clean.matches(partyFinderJoinRegex) -> {
                val match = partyFinderJoinRegex.find(clean) ?: return
                val playerName = match.groups["playerName"]?.value ?: return
                val className = match.groups["classType"]?.value ?: return
                val classLvl = match.groups["classLvl"]?.value ?: return

                partyMembers[playerName] = PartyMember(playerName).apply {
                    this.className = className
                    this.classLvl = classLvl
                }

                addSelfToParty(false)
                ChatUtils.command("/p list")
                hidePartyList = true

                if (playerName == mc.thePlayer?.name) partyMembers[playerName]?.leader = false
                EventBus.post(PartyEvent.Changed(PartyChangeType.PARTY_FINDER, playerName, partyMembers.toMap()))
            }

            clean.matches(partyFinderErrorRegex) -> {
                hadProblemJoiningParty = true
            }
        }
    }
}