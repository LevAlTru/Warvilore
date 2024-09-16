package uwu.levaltru.warvilore.tickables

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.Tickable
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class MagicCauldron(loc: Location, val summoner: String) : Tickable() {

    class MaterialNode(
        val item: (ItemStack) -> Boolean,
        vararg val list: MaterialNode,
        val execute: ((Location) -> Unit)? = null
    ) {
        fun getPath(vararg items: ItemStack): ((Location) -> Unit)? {
            if (items.isEmpty()) {
                if (this.execute != null) {
//                    Bukkit.getOnlinePlayers().forEach { it.sendMessage(this.name + "  execute") }
                    return this.execute
                } else return null
            }
            for (node in list) {
//                Bukkit.getOnlinePlayers().forEach { it.sendMessage(items.first().type.name) }
//                Bukkit.getOnlinePlayers().forEach { it.sendMessage(node.name) }
                if (node.item(items.first())) return node.getPath(*items.copyOfRange(1, items.lastIndex + 1))
                    ?: continue
            }
            return null
        }
    }

    companion object {
        private val LIST = mutableListOf<Location>()
//        private val RANDOM = Random()

        fun isValidCauldron(location: Location?): Boolean {
            if (location == null) return false
            if (when (location.block.type) {
                    Material.CAULDRON, Material.WATER_CAULDRON, Material.LAVA_CAULDRON, Material.POWDER_SNOW_CAULDRON -> true
                    else -> false
                } && location.clone().add(0.0, -1.0, 0.0).block.type == Material.SOUL_FIRE
            ) return true
            return false
        }

        fun recipeTree() = MaterialNode(
            { it.type == Material.GLASS },
            MaterialNode(
                { it.type == Material.GLASS_BOTTLE },
                MaterialNode({ it.enchantments.contains(Enchantment.EFFICIENCY) }) {
                    it.world.players.forEach { it.sendMessage("EFFICIENCY") }
                }
            ) {
                it.world.players.forEach { it.sendMessage("glass_bottle") }
            },
            MaterialNode({ it.type == Material.GLASS_PANE }) {
                it.world.players.forEach { it.sendMessage("glass_pane") }
            },
        )
    }

    var markedForRemoval = false
    val location = loc.toCenterLocation()
    val items = mutableListOf<ItemStack>()

    init {
        if (LIST.any { it.distanceSquared(location) < 25.0 }) markedForRemoval = true
        LIST.add(location.toCenterLocation())
    }

    override fun tick(): Boolean {
        if (!isValidCauldron(location)) markedForRemoval = true
        if (markedForRemoval) {
            ejectItems()
            LIST.remove(location)
            return true
        }

        if (age % 7 == 0) {
            val first = location.getNearbyEntitiesByType(Item::class.java, 2.0) {
                it.location.distanceSquared(location) < 0.33
            }.firstOrNull()
            first?.let {
                items.add(it.itemStack)
                val ye = recipeTree().getPath(*items.toTypedArray())
                ye?.invoke(location)
                it.world.spawnParticle(Particle.TRIAL_SPAWNER_DETECTION_OMINOUS, it.location, 5, .1, .1, .1, .05)
                it.world.playSound(it.location, Sound.BLOCK_SCULK_CATALYST_BLOOM, 1f, 1f)
                it.world.players.forEach { it.sendMessage(items.toString()) }
                it.remove()
            }
        }

        location.world.spawnParticle(Particle.TRIAL_SPAWNER_DETECTION_OMINOUS, location, 1, .3, .3, .3, .05)

        age++
        return false
    }

    fun ejectItems() {
        val add = location.clone().add(0.0, 0.7, 0.0)
        for (item in items) {
            add.world.spawn(add, Item::class.java) {
                it.itemStack = item
                val randomPI = Math.random() * PI * 2.0
                it.velocity = Vector(sin(randomPI) * .2, .3, cos(randomPI) * .2)
            }
        }
    }
}