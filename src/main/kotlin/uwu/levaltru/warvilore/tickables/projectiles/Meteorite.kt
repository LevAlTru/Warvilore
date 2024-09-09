package uwu.levaltru.warvilore.tickables.projectiles

import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.entity.Entity
import org.bukkit.entity.FallingBlock
import org.bukkit.entity.LivingEntity
import org.bukkit.inventory.InventoryHolder
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.tickables.Projectile
import uwu.levaltru.warvilore.trashcan.LevsUtils
import java.util.*

private val random = Random()

private const val MAX_AGE = 20 * 45

private const val BOOM_RADIUS = 3

class Meteorite(location: Location, velocity: Vector, private var collisionsLeft: Int) :
    Projectile(location, velocity, { it is LivingEntity }) {

    override fun tick(): Boolean {
        super.tick()

        for (point in getInBeetweens(0.2)) {
            location.world.spawnParticle(
                Particle.CAMPFIRE_SIGNAL_SMOKE,
                point.x, point.y, point.z, 1,
                .0, .0, .0, .025, null, true
            )
            location.world.spawnParticle(
                Particle.CAMPFIRE_SIGNAL_SMOKE,
                point.x, point.y, point.z, 1,
                .0, .0, .0, .01, null, true
            )
            location.world.spawnParticle(
                Particle.CAMPFIRE_COSY_SMOKE,
                point.x, point.y, point.z, 3,
                .0, .0, .0, .03, null, true
            )
            location.world.spawnParticle(
                Particle.CAMPFIRE_COSY_SMOKE,
                point.x, point.y, point.z, 2,
                .0, .0, .0, .05, null, true
            )
        }
        location.world.spawnParticle(
            Particle.FLASH, location, 1,
            .0, .0, .0, .0, null, true
        )

        if (!location.chunk.isLoaded) location.chunk.load()
        return didCollide() || age++ > MAX_AGE
    }

    override fun onCollision(collisionPlace: Location, entity: Entity?): Boolean {
        val isFinalHit = collisionsLeft-- <= 0
        for (x in -BOOM_RADIUS..BOOM_RADIUS) {
            for (y in -BOOM_RADIUS..BOOM_RADIUS) {
                for (z in -BOOM_RADIUS..BOOM_RADIUS) {
                    if (x * x + y * y + z * z > BOOM_RADIUS * BOOM_RADIUS)
                        continue
                    val add = location.toCenterLocation().add(
                        x.toDouble(),
                        y.toDouble(),
                        z.toDouble(),
                    )
                    val world = location.world
                    val blockAt = world.getBlockAt(add)
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
        for (i in 1..if (isFinalHit) 50 else 15) {
            val block = randomTrace() ?: continue
            block.breakNaturally()
            val nextInt = random.nextInt(0, 100)
            block.type =
                if (nextInt < 5 && isFinalHit) Material.ANCIENT_DEBRIS
                else if (nextInt < 70) Material.MAGMA_BLOCK
                else Material.OBSIDIAN
        }
        return isFinalHit
    }

    private fun randomTrace() = location.world.rayTraceBlocks(
        location, LevsUtils.getRandomNormalizedVector(),
        BOOM_RADIUS * 2.0, FluidCollisionMode.NEVER, true
    ) {
        !(it.type == Material.OBSIDIAN || it.type == Material.ANCIENT_DEBRIS || it.type == Material.MAGMA_BLOCK)
    }?.hitBlock
}