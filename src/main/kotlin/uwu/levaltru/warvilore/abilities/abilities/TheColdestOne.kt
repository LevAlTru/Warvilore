package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.block.data.Levelled
import org.bukkit.block.data.type.Snow
import org.bukkit.damage.DamageType
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.LivingEntity
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

private const val STAND_STILL_TIME = 20
private const val MAKE_IT_SNOW_COOLDOWN = 8

private const val FROZEN_TICKS_LIMIT = 500

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
    var prevLoc: Location? = null
    var standStillTime = 0
    var cooldown = 0

    override fun onTick(event: ServerTickEndEvent) {
        super.onTick(event)

        if (player!!.freezeTicks > 0) player!!.freezeTicks = 0

        mana = (mana + LevsUtils.roundToRandomInt(coldnessFromNegative1To1() * MANA_REGEN_MULTIPLIER)).coerceIn(
            0,
            MAX_MANA
        )

        val location = player!!.location
        val locy = location.add(0.0, player!!.height / 2, 0.0)
        val temperature = locy.block.temperature
        var d = if (locy.world.isUltraWarm) 5.0
        else (temperature - 0.25) * 4 / 3
        if (temperature > 0.25) d *= warmnessProtectionMultiplicator()
        val i = LevsUtils.roundToRandomInt(d)
        coldness = (coldness - i).coerceIn(0, MAX_COLDNESS)

        player!!.sendActionBar("mana: $mana | warmProtection: ${warmnessProtectionMultiplicator()} | cold: $coldness")

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

        if (prevLoc != null
            && player!!.location.distanceSquared(prevLoc!!) < .0001
            && player!!.isSneaking
            && mana > 0
        )
            standStillTime++
        else {
            standStillTime = 0
            prevLoc = player!!.location
        }
        if (standStillTime < STAND_STILL_TIME) return

        if (cooldown <= 0) {
            if (player!!.inventory.itemInMainHand.isFrostmourne()) {
                if (manaCheckAndRemove(1000)) {
                    cooldown = MAKE_IT_SNOW_COOLDOWN
                    player!!.world.spawnParticle(
                        Particle.END_ROD, locy, 300,
                        1.0, 1.0, 1.0, 0.1, null, true
                    )
                    player!!.world.playSound(locy, Sound.PARTICLE_SOUL_ESCAPE, 2f, 0.5f)
                    for (j in 1..100) {
                        val x = random.nextGaussian() * 3 + locy.x
                        val y = random.nextGaussian() * 3 + locy.y
                        val z = random.nextGaussian() * 3 + locy.z
                        val loc = Location(player!!.world, x, y, z)
                        if (loc.distanceSquared(locy) < 4) continue
                        val block = loc.block
                        val blockData = block.blockData
                        /*if (block.type != Material.SNOW && !block.isReplaceable && !block.isEmpty) continue
                        val blockBelow = loc.clone().add(0.0, -1.0, 0.0).block
                        val belowBlockData = blockBelow.blockData
                        if (belowBlockData is Snow)
                            if (belowBlockData.layers < belowBlockData.maximumLayers)
                                continue
                        else if (!blockBelow.isSolid) continue

                        val blockData = block.blockData
                        if (blockData is Snow) {
                            blockData.layers = (blockData.layers + 1).coerceAtMost(blockData.maximumLayers)
                            block.blockData = blockData
                        } else block.type = Material.SNOW*/

                        when (block.type) {
                            Material.AIR -> {
                                val blockBelow = loc.clone().add(0.0, -1.0, 0.0).block
                                if (blockBelow.isSolid || (blockBelow.blockData as? Snow)?.layers == 8)
                                    block.type = Material.SNOW
                            }

                            Material.SNOW -> {
                                if (blockData is Snow) {
                                    blockData.layers = (blockData.layers + random.nextInt(1, 4))
                                        .coerceAtMost(blockData.maximumLayers)
                                    block.blockData = blockData
                                }
                            }

                            Material.WATER -> {
                                if (blockData is Levelled && blockData.level == 0 && random.nextDouble() < 0.1)
                                    block.type = Material.ICE
                            }

                            Material.LAVA -> {
                                if (blockData is Levelled && blockData.level == 0 && random.nextDouble() < 0.1)
                                    block.type = Material.OBSIDIAN
                            }

                            Material.FROSTED_ICE -> block.type = Material.ICE

                            else -> {}
                        }
                    }

                    for (entity in locy.getNearbyLivingEntities(8.0)) {
                        if (entity.uniqueId == player!!.uniqueId) continue
                        val add = entity.location.add(0.0, entity.height / 2, 0.0)
                        val cold = (8 - add.distance(locy)) * 15
                        if (cold > 0) applyCold(entity, cold.toInt())
                    }
                }
            }
        } else cooldown--

        // the sun is bad gor him
        // undeads doesn't attack him

    }

    private fun warmnessProtectionMultiplicator(): Double {
        var w = 0
        for (armor in player!!.inventory.armorContents)
            w += armor?.enchantments?.get(Enchantment.FIRE_PROTECTION) ?: 0
        return (1 - w.toDouble() / 32) *
                if (player!!.activePotionEffects.map { it.type }.contains(PotionEffectType.FIRE_RESISTANCE))
                    0.5 else 1.0
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
                coldness -= 500
            }

            DamageType.FALL -> if (isStandingOnColdBlock()) event.isCancelled = true

            else -> {}
        }
    }

    override fun onAttack(event: PrePlayerAttackEntityEvent) {
        val item = player!!.inventory.itemInMainHand
        if (event.willAttack() && (item.isFrostmourne() || item.isEmpty)) {
            val entity = event.attacked
            if (entity is LivingEntity)
                applyCold(entity, 70)
        }
    }

    private fun applyCold(entity: LivingEntity, amount: Int) {
        entity.freezeTicks = (entity.freezeTicks +
                ((amount * 0.5) * (coldnessFromNegative1To1() + 1)).toInt()).coerceAtMost(FROZEN_TICKS_LIMIT)
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