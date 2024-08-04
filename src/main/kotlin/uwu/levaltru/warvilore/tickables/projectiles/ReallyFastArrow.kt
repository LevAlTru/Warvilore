package uwu.levaltru.warvilore.tickables.projectiles

import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.block.data.Waterlogged
import org.bukkit.entity.Damageable
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.tickables.Projectile
import java.util.*
import kotlin.math.max

private const val MAX_AGE = 20 * 60

private const val STEP_SIZE = 0.1
private val random = java.util.Random()

class ReallyFastArrow(location: Location, velocity: Vector, val owner: UUID,
                      val damage: Double, val knockback: Int, var fire: Boolean, val effects: List<PotionEffect>) :
    Projectile(location, velocity, {it.uniqueId != owner && it is Damageable}) {

    override fun tick(): Boolean {

        super.tick()
        velocity.add(Vector(0.0, -0.025, 0.0))

        if (!location.isChunkLoaded) return true

        val world = location.world

        for (vec in getInBeetweens(STEP_SIZE)) {
            val toLocation = vec.toLocation(world)
            var particle = Particle.SMALL_GUST
            val block = toLocation.block
            if (isInWater(block)) {
                particle = Particle.BUBBLE_COLUMN_UP
                velocity.multiply(0.9)
                fire = false
            } else if (block.type == Material.LAVA) {
                particle = Particle.FLAME
                velocity.multiply(0.9)
                fire = true
            } else if (block.type == Material.FIRE || block.type == Material.SOUL_FIRE) {
                particle = Particle.FLAME
                fire = true
            } else if (random.nextDouble() < STEP_SIZE )
                world.playSound(toLocation, Sound.ENTITY_BREEZE_IDLE_GROUND, SoundCategory.MASTER, 0.1f, 2f)
            world.spawnParticle(
                particle, vec.x, vec.y, vec.z,
                1, 0.0, 0.0, 0.0, 0.0, null, true
            )
            if (fire && random.nextDouble() < 0.3)
                world.spawnParticle(
                    Particle.SMALL_FLAME, vec.x, vec.y, vec.z,
                    1, 0.0, 0.0, 0.0, 0.02, null, true
                )
        }

        if (age > MAX_AGE) return true
        age++
        return didCollide()
    }

    override fun onCollision(collisionPlace: Location, entity: Entity?) {
        val world = location.world

        world.playSound(location, Sound.ENTITY_ARROW_HIT, SoundCategory.MASTER, 1f, 2f)
        if (effects.isNotEmpty()) {
            val filter = location.getNearbyLivingEntities(4.0)
                .filter { location.distance(it.location.add(0.0, it.height / 2, 0.0)) < 3.0 }.toMutableList()
            if (entity is LivingEntity)
                filter.add(entity)
            for (livingEntity in filter) livingEntity.addPotionEffects(effects)
            for (effect in effects)
                world.spawnParticle(Particle.ENTITY_EFFECT, location, 500 / effects.size,
                1.0, 1.0, 1.0, 1.0, effect.type.color, true)
            world.playSound(location, Sound.BLOCK_BREWING_STAND_BREW, SoundCategory.MASTER, 1f, 1.2f)
            world.playSound(location, Sound.BLOCK_GLASS_BREAK, SoundCategory.MASTER, 0.5f, 1.1f)
        }
        val player = Bukkit.getPlayer(owner)
        (entity as? Damageable)?.damage(damage, player)
        if (knockback > 0) (entity as? Damageable)?.velocity =
            velocity.clone().normalize().multiply(Vector(0.6 * knockback, 0.0, 0.6 * knockback)).add(Vector(0.0, 0.33, 0.0))
        if (fire) entity?.let { it.fireTicks = max(100, it.fireTicks) }
        if (entity is Player) player?.playSound(player, Sound.ENTITY_ARROW_HIT_PLAYER, SoundCategory.MASTER, 0.5f, 1f)
    }

    companion object {
        fun isInWater(block: Block): Boolean {
            return when (block.type) {
                Material.WATER, Material.KELP_PLANT, Material.SEAGRASS, Material.TALL_SEAGRASS -> true
                else -> (block.blockData as? Waterlogged)?.isWaterlogged ?: false
            }
        }
    }

}