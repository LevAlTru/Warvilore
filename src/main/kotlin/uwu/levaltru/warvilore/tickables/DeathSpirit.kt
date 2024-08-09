package uwu.levaltru.warvilore.tickables

import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import uwu.levaltru.warvilore.Tickable
import uwu.levaltru.warvilore.abilities.AbilitiesCore.Companion.getAbilities
import uwu.levaltru.warvilore.abilities.interfaces.CanSeeSouls
import java.util.*

private val random = Random()

class DeathSpirit(val loc: Location, val isOminous: Boolean, var nickname: String?) : Tickable() {

    var maxAge = 20 * 30

    companion object {
        val LIST = LinkedList<DeathSpirit>()
    }

    init {
        LIST.add(this)
    }

    private var nearbyPlayers: List<Player>? = null
    private val particle: Particle = if (isOminous) Particle.TRIAL_SPAWNER_DETECTION_OMINOUS else Particle.TRIAL_SPAWNER_DETECTION

    override fun tick(): Boolean {

        if (nickname == null) return remove()
        if (age % 50  == 0) nearbyPlayers = loc.getNearbyPlayers(128.0).filter { it.getAbilities() is CanSeeSouls }

        if (nearbyPlayers != null) {
            for (player in nearbyPlayers!!) {
                if ((maxAge - age).toDouble() / 100 + 0.1 > random.nextDouble()) {
                    player.spawnParticle(particle, loc, 3, .05, .05, .05, .01, null, true)
                    player.spawnParticle(
                        Particle.TRIAL_SPAWNER_DETECTION, loc, 2,
                        .1, .1, .1, .03, null, true
                    )
                }
                if (age % 4 == 0)
                    player.playSound(loc, Sound.PARTICLE_SOUL_ESCAPE, 0.5f, 1f)
            }
        }

        if (age > maxAge) return remove()
        age++
        return false
    }

    private fun remove(): Boolean {
        LIST.remove(this)
        return true
    }
}