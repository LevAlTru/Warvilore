package uwu.levaltru.warvilore.tickables.projectiles

import org.bukkit.*
import org.bukkit.Particle.DustOptions
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.tickables.Projectile
import java.util.*
import kotlin.math.*

private const val DETAIL_LEVEL = 32
private const val STEP_SIZE = 0.1
private const val MAX_AGE = 50

private const val BLADE_SIZE = 0.7

class BloodySlice(location: Location, velocity: Vector, val owner: UUID) :
    Projectile(location, velocity, { it.uniqueId != owner && it is LivingEntity }) {

    val rotation = atan2(velocity.z, velocity.x)
    val sin = sin(rotation)
    val cos = cos(rotation)
    val rotateY = -velocity.y * PI / 3
    val set = HashSet<UUID>(64)

    init {
        set.add(owner)
    }

    override fun tick(): Boolean {
        velocity.multiply(0.9)
        super.tick()

        val scale = 1 - (age.toDouble() / MAX_AGE).pow(2).coerceAtMost(1.0)

        if (age % 3 == 0) location.world.playSound(
            location,
            Sound.BLOCK_TRIAL_SPAWNER_SPAWN_ITEM_BEGIN,
            (0.5 * scale).toFloat(),
            0.5f
        )

        val point = location.clone().add(velocity.clone().multiply(0.5))
        val entities = point.getNearbyLivingEntities(velocity.length())
        val damageSource = DamageSource.builder(DamageType.PLAYER_ATTACK)
        val player = Bukkit.getPlayer(owner)
        player?.let { damageSource.withDirectEntity(player).withCausingEntity(player) }
        val damageSourceBuilt = damageSource.build()

        for (vec in getInBeetweens(STEP_SIZE)) {

            for (livingEntity in entities.filter {
                it.boundingBox.expand(1.0)
                    .contains(vec.clone().add(velocity.clone().normalize().multiply(BLADE_SIZE)))
            }) {
                val uuid = livingEntity.uniqueId
                if (set.contains(uuid)) continue
                set.add(uuid)
                livingEntity.damage(15.0 * scale, damageSourceBuilt)

                val vector = velocity.clone().rotateAroundY(90.0)                         // a stupid way to get
                val subtract = livingEntity.location.toVector().subtract(location.toVector())  // "is entity on the
                vector.multiply(Vector(1, 0, 1)).normalize()                                 // right side of this
                val dot = vector.dot(subtract.normalize())                                   // vector on it is on
                if (dot < 0) vector.multiply(-1)                                                // the left?"

                vector.multiply(0.7 * scale).add(Vector(0.0, 0.7 * scale, 0.0))
                livingEntity.velocity = vector
            }

            val world = location.world
            if (isInBlockCollision(vec.toLocation(world))) return true
            var d = 0.0 /*+ PI - PI * scale*/
            while (d < PI
            /** scale*/
            ) {
                val xz = sin(d + rotateY)
                val y = cos(d + rotateY) * BLADE_SIZE
                val x = xz * cos * BLADE_SIZE/* * scale*/
                val z = xz * sin * BLADE_SIZE/* * scale*/

                world.spawnParticle(
                    Particle.DUST, vec.toLocation(world).add(x, y, z), 1,
                    0.0, 0.0, 0.0, 0.0,
                    DustOptions(Color.RED, ((0.5 + (1 - sin(d)) * 0.6 * scale) * scale).toFloat()), true
                )

                d += PI / DETAIL_LEVEL
            }
        }

        if (age > MAX_AGE) return true
        age++
        return isInBlockCollision(location)
    }

    override fun onCollision(collisionPlace: Location, entity: Entity?) {}
}