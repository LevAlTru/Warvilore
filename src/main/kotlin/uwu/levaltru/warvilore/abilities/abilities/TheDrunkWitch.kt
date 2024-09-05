package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.abilities.interfaces.EvilAurable
import uwu.levaltru.warvilore.abilities.interfaces.tagInterfaces.CanSeeSouls
import uwu.levaltru.warvilore.abilities.interfaces.tagInterfaces.CantLeaveSouls
import uwu.levaltru.warvilore.trashcan.Namespaces

private const val MAX_MANA: Int = 10000

class TheDrunkWitch(string: String) : AbilitiesCore(string), EvilAurable, CanSeeSouls, CantLeaveSouls {
    var mana: Int = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.MANA.namespace,
                PersistentDataType.INTEGER
            ) ?: MAX_MANA
            return field
        }
        set(value) {
            val value1 = value.coerceIn(0, MAX_MANA)
            player?.persistentDataContainer?.set(
                Namespaces.MANA.namespace,
                PersistentDataType.INTEGER, value1
            )
            field = value1
        }

    private var cauldronLoc: Location? = null
        set(value) {
            field = value?.toCenterLocation()
        }

    override fun onTick(event: ServerTickEndEvent) {
        if (player!!.ticksLived % 20 == 0) changeHealth()

        if (cauldronTick()) cauldronLoc = null
    }

    private fun cauldronTick(): Boolean {
        val cauLoc = cauldronLoc ?: return false
        if (mana <= 0) return true
        if (!isValidCauldron(cauldronLoc)) return true
        if (try {
                cauLoc.distanceSquared(player!!.location) > 42.25
            } catch (e: IllegalArgumentException) {
                true
            }
        ) return true

        val x = random.nextDouble(player!!.boundingBox.minX, player!!.boundingBox.maxX)
        val y = random.nextDouble(player!!.boundingBox.minY, player!!.boundingBox.maxY)
        val z = random.nextDouble(player!!.boundingBox.minZ, player!!.boundingBox.maxZ)

        val vec = Vector(cauLoc.x - x, cauLoc.y - y, cauLoc.z - z).normalize()

        if (player!!.ticksLived % 3 == 0) {
            player!!.world.spawnParticle(Particle.SCULK_SOUL, x, y, z, 0, vec.x, vec.y, vec.z, 0.15, null, true)
            mana--
            player!!.sendActionBar(text(mana.toString()).color(NamedTextColor.GOLD))
        }

        cauLoc.world.spawnParticle(
            Particle.TRIAL_SPAWNER_DETECTION_OMINOUS,
            cauLoc,
            1,
            0.2,
            0.2,
            0.2,
            .025,
            null,
            true
        )

        return false
    }

    override fun onAction(event: PlayerInteractEvent) {
        val action = event.action
        if (action.isRightClick) mana += 100
        else if (action.isLeftClick) mana -= 100

        if (isValidCauldron(event.clickedBlock?.location)) cauldronLoc = event.clickedBlock!!.location

        player!!.sendActionBar(text(mana.toString()).color(NamedTextColor.GOLD))
    }

    override fun onHeal(event: EntityRegainHealthEvent) {
        event.amount *= (manaFrom0To1() * 2 - .5).coerceIn(0.0, 1.0)
    }

    private fun changeHealth() {
        val maxHealth = (manaFrom0To1() + 0.4).coerceIn(0.5, 1.0)
        for (modifier in player!!.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.modifiers) {
            if (modifier?.name == "luckofmana") {
                player!!.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.removeModifier(modifier)
                break
            }
        }
        player!!.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.addModifier(
            AttributeModifier(
                Warvilore.namespace("luckofmana"),
                maxHealth - 1,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
            )
        )
        player!!.health = player!!.health.coerceAtMost(player!!.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value)
    }

    private fun manaFrom0To1() = mana.toDouble() / MAX_MANA

    override fun getEvilAura(): Double = manaFrom0To1() * 20.0 + 4.0

    override fun getAboutMe(): List<Component> {
        TODO("Not yet implemented")
    }

    companion object {
        fun isValidCauldron(location: Location?): Boolean {
            if (location == null) return false
            if (when (location.block.type) {
                    Material.CAULDRON, Material.WATER_CAULDRON, Material.LAVA_CAULDRON, Material.POWDER_SNOW_CAULDRON -> true
                    else -> false
                } && location.clone().add(0.0, -1.0, 0.0).block.type == Material.SOUL_FIRE
            ) return true
            return false
        }
    }
}