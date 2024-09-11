package uwu.levaltru.warvilore.tickables.projectiles

import org.bukkit.*
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.Entity
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.InventoryHolder
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.tickables.Projectile
import uwu.levaltru.warvilore.trashcan.LevsUtils
import java.util.*
import kotlin.math.ceil

private val random = Random()

private const val MAX_AGE = 20 * 45

//private const val BOOM_RADIUS = 3

class Meteorite(location: Location, velocity: Vector, private var collisionsLeft: Int, private val size: Double) :
    Projectile(location, velocity, { it is LivingEntity }) {

    override fun tick(): Boolean {
        super.tick()
        velocity.y -= 0.01

        val world = location.world
        for (point in getInBeetweens(0.2)) {
            world.spawnParticle(
                Particle.CAMPFIRE_SIGNAL_SMOKE,
                point.x, point.y, point.z, 1,
                .0, .0, .0, .025, null, true
            )
            world.spawnParticle(
                Particle.CAMPFIRE_SIGNAL_SMOKE,
                point.x, point.y, point.z, 1,
                .0, .0, .0, .01, null, true
            )
            world.spawnParticle(
                Particle.CAMPFIRE_COSY_SMOKE,
                point.x, point.y, point.z, 3,
                .0, .0, .0, .03, null, true
            )
            world.spawnParticle(
                Particle.CAMPFIRE_COSY_SMOKE,
                point.x, point.y, point.z, 2,
                .0, .0, .0, .05, null, true
            )
        }
        world.spawnParticle(
            Particle.FLASH, location, 1,
            .0, .0, .0, .0, null, true
        )
        world.playSound(location, Sound.ENTITY_WARDEN_DEATH, 8f, .5f)

        if (!location.chunk.isLoaded) location.chunk.load()
        return didCollide() || age++ > MAX_AGE
    }

    override fun onCollision(collisionPlace: Location, entity: Entity?): Boolean {
        (entity as? LivingEntity)?.damage(100.0)
        if (entity is Player && entity.gameMode != GameMode.SURVIVAL) return false
        val isFinalHit = collisionsLeft-- <= 0
        val boomRadius = collisionsLeft / 2 + 0.8 + size
        val world = location.world
        val damageSource = DamageSource.builder(DamageType.EXPLOSION).withDamageLocation(location).build()
        for (livingEntity in location.getNearbyLivingEntities(boomRadius + 2.0)) {
            if ((livingEntity.location.distanceSquared(location) > (boomRadius * boomRadius + 4.0 * boomRadius + 4.0))) continue
            livingEntity.damage(30.0, damageSource)
            livingEntity.fireTicks = 200
        }
        val int = ceil(boomRadius).toInt()
        for (x in -int..int) {
            for (y in -int..int) {
                for (z in -int..int) {
                    if (x * x + y * y + z * z > boomRadius * boomRadius)
                        continue
                    val add = location.toCenterLocation().add(
                        x.toDouble(),
                        y.toDouble(),
                        z.toDouble(),
                    )
                    val blockAt = world.getBlockAt(add)
                    if (blockAt.type.blastResistance > 10000) continue
                    if (isFinalHit) {
                        blockAt.breakNaturally()
                        val nextInt = random.nextInt(0, 100)
                        blockAt.type =
                            if (nextInt < 3) Material.ANCIENT_DEBRIS
                            else if (nextInt < 10) Material.LAVA
                            else if (nextInt < 20) Material.MAGMA_BLOCK
                            else if (nextInt < 85) Material.OBSIDIAN
                            else Material.CRYING_OBSIDIAN
                        continue
                    }
                    if (blockAt.state !is InventoryHolder && random.nextDouble() < 0.33) {
                        world.spawn(add, FallingBlock::class.java) {
                            it.blockData = world.getBlockData(add)
                            it.velocity = add.clone().subtract(location).toVector().normalize().multiply(3.0)
                        }
                        blockAt.type = Material.AIR
                    } else blockAt.breakNaturally()
                }
            }
        }
        world.playSound(location, Sound.ENTITY_GENERIC_EXPLODE, 12f, 0.5f)
        for (i in 1.. ceil(6.0 * Math.PI * boomRadius * boomRadius).toInt()) {
            val block = randomTrace(int) ?: continue
            if (block.type.blastResistance > 10000) continue
            block.breakNaturally()
            val nextInt = random.nextInt(0, 10)
            block.type = if (nextInt < 7) Material.NETHERRACK else Material.MAGMA_BLOCK
        }
        if (isFinalHit) {
            world.playSound(location, Sound.ENTITY_WARDEN_SONIC_BOOM, 32f, 0.5f)
            return true
        }
        return false
    }

    private fun randomTrace(BOOM_RADIUS: Int) = location.world.rayTraceBlocks(
        location, LevsUtils.getRandomNormalizedVector(),
        BOOM_RADIUS * 2.0, FluidCollisionMode.NEVER, true
    ) {
        !(it.type == Material.OBSIDIAN || it.type == Material.ANCIENT_DEBRIS || it.type == Material.MAGMA_BLOCK)
    }?.hitBlock
}