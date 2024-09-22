package uwu.levaltru.warvilore.tickables

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import uwu.levaltru.warvilore.Tickable
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.trashcan.CustomItems
import uwu.levaltru.warvilore.trashcan.LevsUtils
import uwu.levaltru.warvilore.trashcan.Namespaces
import kotlin.random.Random

private const val MAX_AGE = 200

class CollabsePoint(val location: Location, val nickname: String) : Tickable() {

    val player = Bukkit.getPlayer(nickname)

    init {
        player?.let {
            it.teleport(location)
            it.addPotionEffect(
                PotionEffect(PotionEffectType.RESISTANCE, MAX_AGE + 20, 4, true, false, false)
            )
            it.health = it.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 20.0
            it.foodLevel = 20
            it.saturation = 20f
            it.persistentDataContainer[Namespaces.TICK_TIME_OF_DEATH.namespace, PersistentDataType.INTEGER] = 0
            try {
                it.getAttribute(Attribute.GENERIC_GRAVITY)?.addModifier(
                    AttributeModifier(
                        Warvilore.namespace("temp_nogravity"),
                        -1.0,
                        AttributeModifier.Operation.MULTIPLY_SCALAR_1
                    )
                )
            } catch (_: Exception) {}
        }
    }

    override fun tick(): Boolean {
        val locy = player?.location?.add(0.0, player.height / 2, 0.0)
        val power = age.toDouble() / MAX_AGE

        player?.let {
            it.velocity = location.toVector().subtract(locy!!.toVector()).multiply(0.05)
        }

        location.world.spawnParticle(
            Particle.END_ROD, location, (power * 10).toInt(), .1, .1, .1, power * .33, null, true
        )

        var d = Random.nextDouble() * power * 33
        while (d > 1 || d > Random.nextDouble()) {
            val rVector = LevsUtils.getRandomNormalizedVector()
            val pLoc = location.clone().add(rVector.clone().multiply(power * 3.0 + 2.5))
            val pVec = rVector.clone().multiply(-1.0)
            location.world.spawnParticle(
                Particle.ITEM,
                pLoc,
                0,
                pVec.x,
                pVec.y,
                pVec.z,
                power * .9 + .1,
                CustomItems.YOUR_REALITY_HAS_COLLAPSED.getAsItem(),
                true
            )
            if (age + 65 > MAX_AGE)
                location.world.spawnParticle(
                    Particle.END_ROD,
                    pLoc,
                    0,
                    pVec.x,
                    pVec.y,
                    pVec.z,
                    (age + 65.0) / MAX_AGE - 1.0,
                    null,
                    true
                )
            d--
        }
        if (age + 65 == MAX_AGE) location.world.playSound(location, Sound.ENTITY_WARDEN_SONIC_CHARGE, 10f, .5f)
        if (age++ > MAX_AGE) {
            LevsUtils.createEvaExplosionWithParticles(location.clone().add(0.0, -8.0, 0.0))
            LevsUtils.Deads.addDied(nickname)
            Bukkit.getOnlinePlayers().forEach { it.sendMessage(Component.text("Судьба $nickname была сведена до атомов.").color(NamedTextColor.RED)) }
            return true
        }
        return false
    }
}