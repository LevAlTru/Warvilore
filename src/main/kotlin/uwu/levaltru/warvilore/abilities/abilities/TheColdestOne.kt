package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.Levelled
import org.bukkit.damage.DamageType
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.abilities.bases.Undead
import uwu.levaltru.warvilore.abilities.interfaces.EvilAurable
import uwu.levaltru.warvilore.trashcan.CustomItems
import uwu.levaltru.warvilore.trashcan.LevsUtils
import uwu.levaltru.warvilore.trashcan.Namespaces
import kotlin.math.roundToInt

private const val MANA_REGEN_MULTIPLIER = 10
private const val MAX_MANA = 360_000
private const val MAX_COLDNESS = 15_000

class TheColdestOne(string: String) : Undead(string), EvilAurable {
    var mana: Int = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.MANA.namespace,
                PersistentDataType.INTEGER
            ) ?: MAX_MANA
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.MANA.namespace,
                PersistentDataType.INTEGER, value
            )
            field = value
        }
    var coldness: Int = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.COLDNESS.namespace,
                PersistentDataType.INTEGER
            ) ?: MAX_COLDNESS
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.COLDNESS.namespace,
                PersistentDataType.INTEGER, value
            )
            field = value
        }

    override fun onTick(event: ServerTickEndEvent) {
        super.onTick(event)

        if (player!!.freezeTicks > 0) player!!.freezeTicks = 0

        var coldNegative1to1 = coldnessFromNegative1To1()
        if (coldNegative1to1 < 0) coldNegative1to1 *= warmnessProtectionMultiplicator()
        mana = (mana + LevsUtils.roundToRandomInt(coldNegative1to1 * MANA_REGEN_MULTIPLIER)).coerceIn(0, MAX_MANA)

        val location = player!!.location
        val locy = location.add(0.0, player!!.height / 2, 0.0)
        var d = if (locy.world.isUltraWarm) 5.0
        else (locy.block.temperature - 0.25) * 4 / 3
        val i = LevsUtils.roundToRandomInt(d)
        coldness = (coldness - i).coerceIn(0, MAX_COLDNESS)

        player!!.sendActionBar("mana: $mana | i1: ${warmnessProtectionMultiplicator()} | cold: $coldness")

        if (player!!.isOnGround)
            for (x in -1..1) {
                for (y in -2..-1) {
                    for (z in -1..1) {
                        val add = location.toBlockLocation().add(x.toDouble(), y.toDouble(), z.toDouble())
                        val block = add.block
                        if (
                            (block.type == Material.WATER
                                    && add.clone().add(Vector(0, 1, 0)).block.type.isAir
                                    && (block.blockData as? Levelled)?.level == 0)
                        ) {
                            if (manaCheckAndRemove(5)) {
                                add.world.setType(add, Material.FROSTED_ICE)
                            }
                        } else if (block.type == Material.FROSTED_ICE) {
                            add.world.setBlockData(add, Material.FROSTED_ICE.createBlockData())
                        }
                    }
                }
            }

        if (isStandingOnColdBlock()) player!!.addPotionEffect(
            PotionEffect(
                PotionEffectType.SPEED,
                10, 2, true, false, true
            )
        )
    }

    private fun warmnessProtectionMultiplicator(): Double {
        var w = 0
        for (armor in player!!.inventory.armorContents)
            w += armor?.enchantments?.get(Enchantment.FIRE_PROTECTION) ?: 0
        if (player!!.activePotionEffects.map { it.type }.contains(PotionEffectType.FIRE_RESISTANCE)) {
            return (1 - w.toDouble() / 40)
        }
        return (1 - w.toDouble() / 20)
    }

    private fun coldnessFromNegative1To1() = (coldness.toDouble() * 2 / MAX_COLDNESS - 1)

    override fun onAction(event: PlayerInteractEvent) {
        if (event.action.name != "RIGHT_CLICK_AIR") return
        val item = player!!.inventory.itemInMainHand
        if (item.type != Material.NETHERITE_SWORD) return

        if (!item.isFrostmourne()) CustomItems.FROSTMOURNE.replaceItem(item, player!!.name)


//      Show stats

    }

    override fun onDeath(event: PlayerDeathEvent) {
        coldness = (MAX_COLDNESS * 0.75).roundToInt()
        mana = 0
    }

    override fun onDamage(event: EntityDamageEvent) {
        super.onDamage(event)
        when (event.damageSource.damageType) {
            DamageType.FIREBALL, DamageType.UNATTRIBUTED_FIREBALL,
            DamageType.IN_FIRE, DamageType.ON_FIRE,
            DamageType.LAVA -> {
                event.damage *= 3
                coldness -= 200
            }

            else -> {}
        }
    }

    override fun getEvilAura(): Double {
        TODO("ColdestOfThemAll: dark aura is not implemented yet")
    }

    fun manaCheckAndRemove(int: Int): Boolean {
        if (mana < int) return false
        mana -= int
        return true
    }

    fun isStandingOnColdBlock(): Boolean {
        val boundingBox = player!!.boundingBox
        val location = player!!.location
        val min = boundingBox.min
        val max = boundingBox.max
        for (x in min.blockX..max.blockX) {
            for (y in min.blockY - 1..max.blockY) {
                for (z in min.blockZ..max.blockZ) {
                    val add = Location(location.world, x.toDouble(), y.toDouble(), z.toDouble())
                    when (add.block.type) {
                        Material.ICE, Material.FROSTED_ICE, Material.PACKED_ICE, Material.BLUE_ICE,
                        Material.SNOW, Material.SNOW_BLOCK, Material.POWDER_SNOW -> return true

                        else -> {}
                    }
                }
            }
        }
        return false
    }

    companion object {
        fun ItemStack.isFrostmourne(): Boolean =
            CustomItems.FROSTMOURNE.equals(this)
    }

    override fun getAboutMe(): List<Component> {
        TODO("Not yet implemented")
    }

}