package uwu.levaltru.warvilore.tickables.projectiles

import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import uwu.levaltru.warvilore.Tickable
import uwu.levaltru.warvilore.abilities.AbilitiesCore.Companion.getAbilities
import uwu.levaltru.warvilore.abilities.interfaces.EvilAurable
import uwu.levaltru.warvilore.trashcan.LevsUtils
import kotlin.math.PI

class RemoveAbilitiesSphere(val location: Location, size: Double) : Tickable() {

    var markedForRemoval = false
    var size = 16.0

    companion object

    val LIST = mutableListOf<RemoveAbilitiesSphere>()

    init {
        LIST.add(this)
    }

    override fun tick(): Boolean {
        for (i in 1..(PI * size * size).toInt().coerceAtMost(256)) {
            val vector = LevsUtils.getRandomNormalizedVector().multiply(size)
            location.world.spawnParticle(Particle.END_ROD, vector.x, vector.y, vector.z, 1, .0, .0, .0, .01)
        }

        if (age % 10 == 0) for (player in location.getNearbyPlayers(size)) {
            val locy = player.location.add(0.0, player.height / 2, 0.0)
            if (locy.distanceSquared(location) > size * size) continue
            val abilities = player.getAbilities() ?: continue
            if (!abilities.abilitiesDisabled) {
                player.world.spawnParticle(
                    if (abilities is EvilAurable) Particle.TRIAL_SPAWNER_DETECTION_OMINOUS else Particle.TRIAL_SPAWNER_DETECTION,
                    locy, 50, .2, .4, .2, .01
                )
                player.world.playSound(player, Sound.ITEM_FIRECHARGE_USE, 1f, .8f)
                abilities.abilitiesDisabled = true
            }
        }

        if (markedForRemoval) {
            LIST.remove(this)
            return true
        }
        age++
        return false
    }
}