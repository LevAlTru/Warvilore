package uwu.levaltru.warvilore.tickables

import org.bukkit.*
import org.bukkit.block.data.Levelled
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.DeveloperMode
import uwu.levaltru.warvilore.Tickable
import uwu.levaltru.warvilore.abilities.AbilitiesCore.Companion.getAbilities
import uwu.levaltru.warvilore.abilities.abilities.OneAngelZero
import uwu.levaltru.warvilore.abilities.abilities.TheColdestOne
import uwu.levaltru.warvilore.trashcan.CauldronDataObject
import uwu.levaltru.warvilore.trashcan.CustomItems
import uwu.levaltru.warvilore.trashcan.LevsUtils.getAsCustomItem
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private const val MAX_ITEM_AMOUNT = 16

class MagicCauldron(loc: Location) : Tickable() {

    class MNode(
        val item: (ItemStack) -> Boolean,
        vararg val list: MNode,
        val execute: ((Location, List<ItemStack>, Player) -> CauldronDataObject?)? = null
    ) {
        fun getPath(vararg items: ItemStack): ((Location, List<ItemStack>, Player) -> CauldronDataObject?)? {
            if (items.isEmpty()) return this.execute
            for ((i, node) in list.withIndex()) {
                val ye = node.item(items.first())
                if (DeveloperMode) Bukkit.getOnlinePlayers()
                    .forEach { it.sendMessage("${items.first().type.name}   #$i   $ye") }
//                Bukkit.getOnlinePlayers().forEach { it.sendMessage(node.name) }
                if (ye) return node.getPath(*items.copyOfRange(1, items.lastIndex + 1))
                    ?: continue
            }
            return null
        }
    }

    companion object {
        private val LIST = mutableListOf<MagicCauldron>()
//        private val RANDOM = Random()

        fun isValidCauldron(location: Location?): Boolean {
            if (location == null) return false
            val block = location.block
            if (when (block.type) {
//                    Material.LAVA_CAULDRON, Material.POWDER_SNOW_CAULDRON -> true
                    Material.WATER_CAULDRON -> {
                        val blockData = block.blockData
                        (blockData as Levelled).level == blockData.maximumLevel
                    }

                    else -> false
                } && location.clone().add(0.0, -1.0, 0.0).block.type == Material.SOUL_FIRE
            ) return true
            return false
        }

        fun recipeTree() = MNode(
            { true },
            MNode(
                { it.type == Material.EXPERIENCE_BOTTLE && it.amount >= 32 },
                MNode(
                    { it.type == Material.ANCIENT_DEBRIS && it.amount >= 8 },
                    MNode(
                        { it.itemMeta.getAsCustomItem() == CustomItems.SHARD_OF_MORTUUS || it.itemMeta.getAsCustomItem() == CustomItems.FRAGMENT_OF_VICTUS },
                        MNode(
                            { it.itemMeta.getAsCustomItem() == CustomItems.SHARD_OF_MORTUUS || it.itemMeta.getAsCustomItem() == CustomItems.FRAGMENT_OF_VICTUS },
                            MNode(
                                { it.type == Material.CRYING_OBSIDIAN && it.amount >= 12 },
                                execute = {l, i, p ->

                                    if (!((i[2].itemMeta.getAsCustomItem() == CustomItems.SHARD_OF_MORTUUS && i[3].itemMeta.getAsCustomItem() == CustomItems.FRAGMENT_OF_VICTUS) ||
                                                (i[2].itemMeta.getAsCustomItem() == CustomItems.FRAGMENT_OF_VICTUS && i[3].itemMeta.getAsCustomItem() == CustomItems.SHARD_OF_MORTUUS))
                                    ) return@MNode null

                                    l.world.spawnParticle(Particle.END_ROD, l, 25, .2, .2, .2, .1, null, true)

                                    LegendaryItemSpawner(l.clone().add(0.0, 2.5, 0.0), CustomItems.YOUR_REALITY_HAS_COLLAPSED.getAsItem(), 20 * 10)

                                    val list = i.toMutableList()

                                    list[0] = list[0].subtract(32)
                                    list[1] = list[1].subtract(8)
                                    list[2] = list[2].subtract()
                                    list[3] = list[3].subtract()
                                    list[4] = list[4].subtract(12)

                                    CauldronDataObject(8000, list)
                                }
                            )
                        )
                    )
                )
            ),
            MNode(
                { it.type == Material.SOUL_SAND },
                MNode(
                    { it.itemMeta.getAsCustomItem() == CustomItems.SOUL_BOTTLE },
                    MNode(
                        { it.type == Material.GOLD_INGOT && it.amount >= 4 },
                        execute = { l, i, p -> shardAndFragment(l, i, false) }
                    )
                )
            ),
            MNode(
                { it.type == Material.SOUL_SOIL },
                MNode(
                    { it.itemMeta.getAsCustomItem() == CustomItems.OMINOUS_SOUL_BOTTLE },
                    MNode(
                        { it.type == Material.ANCIENT_DEBRIS },
                        execute = { l, i, _ -> shardAndFragment(l, i, true) }
                    )
                )
            )
        )

        private fun shardAndFragment(
            l: Location,
            i: List<ItemStack>,
            isShard: Boolean
        ): CauldronDataObject? {
            var selectedPlayer: Player? = null
            for (player in l.getNearbyPlayers(8.0)) {
                if (isShard) {
                    if (player.getAbilities() is TheColdestOne) {
                        selectedPlayer = player
                        break
                    }
                } else {
                    if (player.getAbilities() is OneAngelZero) {
                        selectedPlayer = player
                        break
                    }
                }
            }
            if (selectedPlayer == null) return null

            SoulSucker(l.clone().add(0.0, 2.5, 0.0), selectedPlayer, isShard)
            l.world.playSound(l, Sound.BLOCK_TRIAL_SPAWNER_SPAWN_ITEM_BEGIN, 3f, .5f)

            val list = i.toMutableList()

            list[0] = list[0].subtract()
            list[1] = list[1].subtract()
            list[2] = list[2].subtract()

            return CauldronDataObject(800, list)
        }

        fun getNeatestCauldron(loc: Location) =
            LIST.maxByOrNull { it.location.distanceSquared(loc.toCenterLocation()) }
    }

    var markedForRemoval = false
    val location = loc.toCenterLocation()
    val items = mutableListOf<ItemStack>()

    init {
        if (LIST.any { it.location.distanceSquared(location) < 25.0 }) markedForRemoval = true
        LIST.add(this)
    }

    override fun tick(): Boolean {
        if (!isValidCauldron(location)) markedForRemoval = true
        if (markedForRemoval) {
            ejectItems()
            LIST.remove(this)
            return true
        }

        if (age % 7 == 0) {
            location.getNearbyEntitiesByType(Item::class.java, 2.0) {
                it.location.distanceSquared(location) < 0.33
            }.firstOrNull()?.let {
                if (items.size > MAX_ITEM_AMOUNT) ejectItems()
                else {
                    val itemStack = it.itemStack
                    val lastOrNull = items.lastOrNull()
                    if (lastOrNull?.isSimilar(itemStack) == true) {
                        val overflow = lastOrNull.amount + itemStack.amount - lastOrNull.maxStackSize
                        lastOrNull.add(itemStack.amount)
                        if (overflow > 0) {
                            val temp = itemStack.clone()
                            temp.amount = overflow
                            items.add(temp)
                        }
                    } else items.add(itemStack)
                    val random = Random(itemStack.type.ordinal)
                    val hsBtoRGB = java.awt.Color.HSBtoRGB(
                        random.nextFloat(),
                        random.nextFloat() * .2f + .5f,
                        random.nextFloat() * .5f + .5f
                    )
                    it.world.spawnParticle(
                        Particle.DUST,
                        location.toCenterLocation().add(0.0, 0.45, 0.0),
                        25,
                        .2,
                        .0,
                        .2,
                        .05,
                        Particle.DustOptions(
                            Color.fromRGB(
                                hsBtoRGB and 0x00FFFFFF
                            ), itemStack.amount.toFloat() / itemStack.maxStackSize + 1.25f
                        )
                    )
                    it.world.playSound(it.location, Sound.BLOCK_BREWING_STAND_BREW, 1f, .7f)
                    if (DeveloperMode) it.world.players.forEach { it.sendMessage(items.toString()) }
                    it.remove()
                }
            }
        }

        location.world.spawnParticle(Particle.TRIAL_SPAWNER_DETECTION_OMINOUS, location, 1, .3, .3, .3, .05)

        age++
        return false
    }

    override fun collapse() {
        ejectItems()
    }

    fun activate(player: Player): Int? {
        val ye = recipeTree().getPath(*items.toTypedArray())
        val invoke = ye?.invoke(location, items, player)
        if (invoke == null) {
            ejectItems()
            return null
        }
        items.clear()
        invoke.itemList?.let { items.addAll(it) }
        return invoke.int
    }

    fun ejectItems() {
        if (items.isEmpty()) return
        splash()
        val add = location.clone().add(0.0, 0.7, 0.0)
        for (item in items) {
            add.world.spawn(add, Item::class.java) {
                it.itemStack = item
                val randomPI = Math.random() * PI * 2.0
                it.velocity = Vector(sin(randomPI) * .15, .2, cos(randomPI) * .15)
            }
        }
        items.clear()
    }

    fun splash() {
        location.world.spawnParticle(Particle.BUBBLE_POP, location.clone().add(0.0, 0.45, 0.0), 100, .2, .0, .2, .1)
        location.world.playSound(location, Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 1f, 1.3f)
    }
}