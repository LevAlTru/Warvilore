package uwu.levaltru.warvilore.tickables

import org.bukkit.*
import org.bukkit.Particle.BUBBLE_POP
import org.bukkit.Particle.DustOptions
import org.bukkit.block.data.Levelled
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.DeveloperMode
import uwu.levaltru.warvilore.Tickable
import uwu.levaltru.warvilore.trashcan.CauldronDataObject
import uwu.levaltru.warvilore.trashcan.LevsUtils.getAsCustomItem
import uwu.levaltru.warvilore.trashcan.LevsUtils.getSoulInTheBottle
import uwu.levaltru.warvilore.trashcan.LevsUtils.isSoulInTheBottle
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
            MNode({
                when (it.type) {
                    Material.SOUL_SAND, Material.SOUL_SOIL -> true
                    else -> false
                }
            },
                MNode({
                    when (it.type) {
                        Material.SAND, Material.RED_SAND -> true
                        else -> false
                    }
                }) { l, _, _ ->
                    l.world.spawnParticle(Particle.END_ROD, l.toCenterLocation(), 100, .2, .2, .2, .3, null, true)
                    l.world.playSound(l, Sound.ENTITY_WARDEN_SONIC_BOOM, 1f, .5f)
                    CauldronDataObject(0, null)
                },
                MNode({ it.itemMeta.hasEnchant(Enchantment.EFFICIENCY) },
                    MNode({ it.type == Material.NETHERRACK }) { l, _, _ ->
                        l.world.spawnParticle(
                            Particle.SMALL_FLAME,
                            l.toCenterLocation(),
                            100,
                            .2,
                            .2,
                            .2,
                            .3,
                            null,
                            true
                        )
                        l.world.playSound(l, Sound.ENTITY_WARDEN_SONIC_BOOM, 1f, .5f)
                        CauldronDataObject(50, null)
                    }
                ) { l, _, _ ->
                    l.world.spawnParticle(
                        Particle.SOUL_FIRE_FLAME,
                        l.toCenterLocation(),
                        100,
                        .2,
                        .2,
                        .2,
                        .3,
                        null,
                        true
                    )
                    l.world.playSound(l, Sound.ENTITY_WARDEN_SONIC_BOOM, 1f, .5f)
                    CauldronDataObject(50, null)
                }
            ),
            MNode({ it.type == Material.BOOK }) { l, i, p ->
                val itemInOffHand = p.inventory.itemInOffHand
                if (itemInOffHand.type == Material.BOOK) {
                    l.world.spawnParticle(
                        Particle.CAMPFIRE_COSY_SMOKE,
                        l.toCenterLocation(),
                        100,
                        .2,
                        .2,
                        .2,
                        .02,
                        null,
                        true
                    )
                    l.world.playSound(l, Sound.ENTITY_WARDEN_SONIC_BOOM, 1f, .5f)
                    itemInOffHand.subtract()
                    val list = i.toMutableList()
                    list[0] = list[0].subtract()
                    CauldronDataObject(100, list)
                } else null
            },
            MNode({ it.itemMeta.getAsCustomItem()?.isSoulInTheBottle() == true },
                MNode({ it.type == Material.REDSTONE_BLOCK }) { l, i, p ->

                    val nickname = i[0].itemMeta.getSoulInTheBottle() ?: return@MNode null
                    val player = Bukkit.getPlayer(nickname)
                    player?.health = 0.0

                    val list = i.toMutableList()
                    list[0].subtract(1)
                    CauldronDataObject(200, list)
                }
            )
        )

        fun getNeatestCauldron(loc: Location) = LIST.maxByOrNull { it.location.distanceSquared(loc.toCenterLocation()) }
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
                        random.nextFloat() * .2f + .8f,
                        random.nextFloat() * .2f + .8f
                    )
                    it.world.spawnParticle(
                        Particle.DUST,
                        location.toCenterLocation().add(0.0, 0.45, 0.0),
                        25,
                        .2,
                        .0,
                        .2,
                        .05,
                        DustOptions(
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

    fun activate(player: Player): Int? {
        if (DeveloperMode) player.sendMessage("cauldron activate")
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
        location.world.spawnParticle(BUBBLE_POP, location.clone().add(0.0, 0.45, 0.0), 100, .2, .0, .2, .1)
        location.world.playSound(location, Sound.ENTITY_PLAYER_SPLASH_HIGH_SPEED, 1f, 1.3f)
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
}