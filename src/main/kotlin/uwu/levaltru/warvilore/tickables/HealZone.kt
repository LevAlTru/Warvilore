package uwu.levaltru.warvilore.tickables

import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.Tickable
import uwu.levaltru.warvilore.trashcan.LevsUtils
import java.util.Random

private const val MAX_AGE = 20 * 25
private const val RADIUS = 6.0

class HealZone(val location: Location, allowed: List<String>) : Tickable() {

    val allowed = allowed.map { it.lowercase() }
    val random = Random()

    override fun tick(): Boolean {
        if (age % 40 == 0) {
            for (player in location.getNearbyPlayers(RADIUS + 1.0)) {
                val locy = player.location.add(0.0, player.height / 2, 0.0)
                if (locy.distanceSquared(location) > RADIUS * RADIUS) continue
                if (!allowed.contains(player.name.lowercase())) continue
                player.addPotionEffects(listOf(
                    PotionEffect(PotionEffectType.RESISTANCE, 50, 0, true, true, true),
                    PotionEffect(PotionEffectType.REGENERATION, 50, 1, true, true, true),
                    PotionEffect(PotionEffectType.STRENGTH, 50, 1, true, true, true),
                ))
                player.world.spawnParticle(Particle.WAX_ON, locy, 3, .2, .4, .2, 1.0, null, true)
            }
        }

        if (age % 50 == 0) location.world.playSound(location, Sound.BLOCK_BEACON_AMBIENT, 2f, 0.7f)
        if (age == MAX_AGE) {
            location.getNearbyPlayers(32.0).forEach { it.stopSound(Sound.BLOCK_BEACON_AMBIENT) }
            location.world.playSound(location, Sound.BLOCK_BEACON_DEACTIVATE, 2f, 0.7f)
        }

        var vector: Vector? = null
        var add: Location? = null

        for (j in 1..2) {
            for (i in 1..8) {
                vector = LevsUtils.getRandomNormalizedVector()
                add = location.clone().add(vector.multiply(RADIUS))
                vector.multiply(-1)
                val b = random.nextDouble() < 0.66
                var d = random.nextDouble(0.1, 1.5)
                if (!b) d /= 80.0
                location.world.spawnParticle(
                    if (b) Particle.WAX_ON else Particle.END_ROD, add.x, add.y, add.z, 0,
                    vector.x, vector.y, vector.z, d, null, true
                )
            }

            vector!!.multiply(-1)
            if (random.nextDouble() < 0.5) add!!.world.spawnParticle(
                Particle.WAX_ON, add!!.x, add.y, add.z, 0,
                vector.x, vector.y, vector.z, 3.0, null, true
            )
            else add!!.world.spawnParticle(
                Particle.END_ROD, add!!.x, add.y, add.z, 0,
                vector.x, vector.y, vector.z, 3.0 / 60, null, true
            )
        }
        if (age % 2 == 0) location.world.spawnParticle(
            Particle.END_ROD, location, 1,
            0.1, 0.1, 0.1, 0.15, null, true
        )

        return age++ > MAX_AGE
    }
}