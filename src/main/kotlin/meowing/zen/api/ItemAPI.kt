package meowing.zen.api

import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import meowing.zen.Zen
import meowing.zen.Zen.Companion.LOGGER
import meowing.zen.Zen.Companion.scope
import meowing.zen.utils.DataUtils
import meowing.zen.utils.ItemUtils.skyblockID
import meowing.zen.utils.LoopUtils
import meowing.zen.utils.NetworkUtils
import net.minecraft.item.ItemStack

@Zen.Module
object ItemApi {
    private var loadedNEUItems = false

    private var skyblockItemData = JsonObject() // All items from NEU Repo with its item ids, with pricing data appended on
    private var liveAuctionData = JsonObject() // All items from SBT API with its custom data-string ids

    private val itemDataFile = DataUtils("itemData", JsonObject())
    private val liveAuctionDataFile = DataUtils("liveAuctionData", JsonObject())

    init {
        // Load saved data from files
        if (itemDataFile.getData().entrySet().isNotEmpty()) {
            skyblockItemData = itemDataFile.getData()
        }

        if (liveAuctionDataFile.getData().entrySet().isNotEmpty()) {
            liveAuctionData = liveAuctionDataFile.getData()
        }

        updateSkyblockItemData(false)
        LOGGER.info("Loaded ${skyblockItemData.entrySet().size} Items from saved data file")

        // Update Item Prices every 10 Minutes
        LoopUtils.loop(1000 * 60 * 10) {
            updateSkyblockItemData(false)
        }
    }

    // Convert NEU ID system to Zen ID system
    // MEGALODON;3 -> MEGALODON-EPIC
    // ULTIMATE_WISE;2 -> ENCHANTMENT_ULTIMATE_WISE_2
    fun convertNeuItemToZen(neuId: String, neuData: JsonObject): String {
        var newId = neuId
        if (newId.contains(";")) {
            if (neuData.asJsonObject.get("displayname").asString.contains("[Lvl")) {
                newId = convertNeuPetID(neuId)
            }

            if (neuData.asJsonObject.get("itemid").asString == "minecraft:enchanted_book") {
                newId = "ENCHANTMENT_${newId.replace(";", "_")}"
            }
        }
        return newId
    }

    // MEGALODON;3 -> MEGALODON-EPIC
    fun convertNeuPetID(neuId: String): String {
        val parts = neuId.split(";")

        return "${parts[0]}-${intToPetTier(Integer.parseInt(parts[1]))}"
    }

    fun intToPetTier(tier: Int): String {
        if (tier == 5) return "MYTHIC"
        if (tier == 4) return "LEGENDARY"
        if (tier == 3) return "EPIC"
        if (tier == 2) return "RARE"
        if (tier == 1) return "UNCOMMON"
        if (tier == 0) return "COMMON"
        return "UNKNOWN"
    }

    private fun loadNeuRepo(force: Boolean) {
        scope.launch {
            LOGGER.info("Loading Skyblock Items from NEU Repo..")
            skyblockItemData = JsonObject()

            try {
                NEUApi.downloadAndProcessRepo(force)
                NEUApi.NeuItemData.getData().entrySet().forEach {
                    val newKey = convertNeuItemToZen(it.key, it.value.asJsonObject)
                    // Add custom ID to item properties
                    it.value.asJsonObject.addProperty("sbtID", newKey)

                    // Add rarity to item properties
                    if (it.value.asJsonObject.has("lore")) {
                        val lore = it.value.asJsonObject.get("lore").asJsonArray
                        lore.reversed().find { it2 ->
                            val rarity = extractRarity(it2.asString) ?: return@find false
                            it.value.asJsonObject.addProperty("rarity", rarity)
                            return@find true
                        }
                    }
                    skyblockItemData.add(newKey, it.value)
                }

                if (skyblockItemData.entrySet().isNotEmpty()) loadedNEUItems = true

                LOGGER.info("Loaded Skyblock Items from NEU Repo!")
            } catch (e: Exception) {
                LOGGER.error("There was a problem loading NEU Repo.. ${e.message}")
            }
        }
    }

    fun extractRarity(itemDescription: String): String? {
        val rarities = listOf(
            "COMMON",
            "UNCOMMON",
            "RARE",
            "EPIC",
            "LEGENDARY",
            "MYTHIC",
            "DIVINE",
            "SPECIAL",
            "VERY_SPECIAL",
            "SUPREME"
        )
        // Reverse so uncommon is checked before common
        for (rarity in rarities.reversed()) {
            if (itemDescription.contains(rarity, ignoreCase = true)) {
                return rarity
            }
        }

        return null
    }

    /*
    * Responsible for getting item data from NEU Repo and SBT API,
    * if fails, it has a backup of the last known data.
    * If force is true, it will clear the cache and get all data, from NEU Repo and SBT API
    */
    fun updateSkyblockItemData(force: Boolean = false) {
        scope.launch {
            LOGGER.info("Updating Skyblock Item Data..")
            if (force) {
                // Clear existing data
                skyblockItemData = JsonObject()
                loadedNEUItems = false
            }

            if (!loadedNEUItems) loadNeuRepo(force)

            loadPricingData()
//            loadLiveAuctionData()

            itemDataFile.setData(skyblockItemData)
            liveAuctionDataFile.setData(liveAuctionData)
            itemDataFile.save()
            liveAuctionDataFile.save()
        }
    }

    private fun loadPricingData() {
        LOGGER.info("Loading Lowest Item Prices from SBT API")

        scope.launch {
            try {
                NetworkUtils.getJson(
                    url = "https://app.mrfast-developer.com/api/pricingData",
                    onSuccess = { json ->
                        if (json.entrySet().isEmpty()) {
                            LOGGER.warn("There was a problem loading SBT Prices..")
                            return@getJson
                        }

                        LOGGER.info("Loaded ${json.entrySet().size} Items From Skyblock-Tweaks Item API")

                        json.entrySet().forEach {
                            val item = it.value.asJsonObject
                            val itemId = it.key

                            if (!skyblockItemData.has(itemId)) {
                                skyblockItemData.add(itemId, item)
                                return@forEach
                            }

                            val itemJson = skyblockItemData[itemId]?.asJsonObject ?: JsonObject()

                            // Merge all properties from item into itemJson
                            for ((key, value) in item.entrySet()) {
                                itemJson.add(key, value)
                            }
                        }
                    },
                    onError = {
                        LOGGER.error("There was a problem loading SBT Prices.. ${it.message}")
                        it.printStackTrace()
                    }
                )
            } catch (e: Exception) {
                e.printStackTrace()
                LOGGER.error("There was a problem loading SBT Pricing Data..")
            }
        }
    }

    fun getItemInfo(itemId: String): JsonObject? = skyblockItemData[itemId]?.asJsonObject

    fun getSkyblockItems(): JsonObject = skyblockItemData

    fun getItemInfo(stack: ItemStack): JsonObject? = stack.skyblockID.let { skyblockItemData[it]?.asJsonObject }
}