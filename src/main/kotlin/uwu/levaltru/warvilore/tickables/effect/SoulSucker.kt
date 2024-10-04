package uwu.levaltru.warvilore.tickables.effect

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.Tickable
import uwu.levaltru.warvilore.trashcan.CustomItems
import uwu.levaltru.warvilore.trashcan.LevsUtils
import uwu.levaltru.warvilore.trashcan.Namespaces
import java.util.*

private const val DISTANCE_FROM_SUCKER = 12.0

private const val IDLE_STATE_DURATION = 100

private const val RITUAL_DURATION = 20 * 45 + IDLE_STATE_DURATION

private const val REMAINING_LIVES_MAX = 6

class SoulSucker(val location: Location, val player: Player, val dark: Boolean) : Tickable() {
    override fun tick(): Boolean {
        val particle = if (dark) Particle.TRIAL_SPAWNER_DETECTION_OMINOUS else Particle.TRIAL_SPAWNER_DETECTION
        if (!player.isValid || player.isDead) return deactivate(particle)
        if (player.persistentDataContainer.has(Namespaces.LIVES_REMAIN.namespace) && dark) return deactivate(particle)

        if (age % 2 == 0) {
            val rVec = LevsUtils.getRandomNormalizedVector()
            val pVel = rVec.clone().multiply(-1)
            location.world.spawnParticle(
                if (dark) Particle.SCRAPE else Particle.WAX_ON, location.clone().add(rVec.clone().multiply(.5)),
                0, pVel.x, pVel.y, pVel.z, 1.0
            )
        }

        if (age > IDLE_STATE_DURATION) {
            if (player.ticksLived % 8 == 0)
                player.world.playSound(
                    player.location, Sound.BLOCK_TRIAL_SPAWNER_AMBIENT_OMINOUS,
                    SoundCategory.MASTER, 1f, 0.5f
                )
            val locy = player.location.add(0.0, player.height / 2, 0.0)
            if (age % 60 == 0) {
                player.foodLevel -= 1
                player.saturation -= 1f
            }
            val toVec = location.toVector().subtract(locy.toVector()).multiply(Vector(1.0, 0.2, 1.0))

            val box = player.boundingBox
            val locyClone = Location(
                locy.world,
                Random().nextDouble(box.minX, box.maxX),
                Random().nextDouble(box.minY, box.maxY),
                Random().nextDouble(box.minZ, box.maxZ)
            )
            locy.world.spawnParticle(
                particle, locyClone, 0,
                toVec.x, toVec.y, toVec.z, .05
            )
            location.world.spawnParticle(particle, location, 2, .05, .2, .05, 0.0)
            if (player.location.distanceSquared(location) > DISTANCE_FROM_SUCKER * DISTANCE_FROM_SUCKER) {
                return deactivate(particle)
            } else if (age > RITUAL_DURATION) {
                location.world.spawnParticle(particle, location, 30, .2, .2, .2, .03, null, true)
                location.world.playSound(location, Sound.BLOCK_END_PORTAL_SPAWN, 3f, .87f)

                location.world.spawn(location, Item::class.java) {
                    it.itemStack =
                        (if (dark) CustomItems.SHARD_OF_MORTUUS else CustomItems.FRAGMENT_OF_VICTUS).getAsItem()
                    it.setGravity(false)
                    it.velocity = Vector()
                }

                locy.world.spawnParticle(
                    if (dark) Particle.TRIAL_SPAWNER_DETECTION_OMINOUS else Particle.TRIAL_SPAWNER_DETECTION,
                    locy,
                    50,
                    .2,
                    .4,
                    .2,
                    .1,
                    null,
                    true
                )
                player.damage(5.0)
                player.persistentDataContainer[Namespaces.LIVES_REMAIN.namespace, org.bukkit.persistence.PersistentDataType.INTEGER] =
                    REMAINING_LIVES_MAX
                player.sendMessage(Component.text("Ты чуствуешь как твоя душа не выдержит больше ${REMAINING_LIVES_MAX - 1} смертей").color(NamedTextColor.RED))
                return true
            }
        }

        age++
        return false
    }

    private fun deactivate(particle: Particle): Boolean {
        location.world.spawnParticle(particle, location, IDLE_STATE_DURATION, .2, .2, .2, .25, null, true)
        location.world.playSound(location, Sound.BLOCK_BEACON_DEACTIVATE, 3f, .5f)
        return true
    }
}