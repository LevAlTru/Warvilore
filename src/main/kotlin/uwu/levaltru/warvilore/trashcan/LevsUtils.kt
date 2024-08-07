package uwu.levaltru.warvilore.trashcan

import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.damage.DamageSource
import org.bukkit.entity.Player
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.abilities.abilities.TheColdestOne
import java.util.*
import kotlin.math.floor

object LevsUtils {
    fun getRandomNormalizedVector(): Vector {
        var vector: Vector
        val random = Random()
        do {
            vector = Vector(
                random.nextDouble(-1.0, 1.0),
                random.nextDouble(-1.0, 1.0),
                random.nextDouble(-1.0, 1.0)
            )
        } while (vector.lengthSquared() > 1)
        return vector.normalize()
    }

    fun roundToRandomInt(d: Double): Int {
        val d1 = if (d < 0) 1 + (d % 1) else d % 1
        if (d1 == 0.0) return floor(d).toInt()
        return floor(d).toInt() + if (Random().nextDouble() < d1) 1 else 0
    }

    fun ItemMeta.setSoulBoundTo(nickname: String) {
        this.persistentDataContainer.set(Namespaces.SOULBOUND.namespace, PersistentDataType.STRING, nickname)
    }

    fun ItemMeta.isSoulBound(): Boolean {
        return this.persistentDataContainer.has(Namespaces.SOULBOUND.namespace, PersistentDataType.STRING)
    }

    fun ItemMeta.getSoulBound(): String? {
        return this.persistentDataContainer.get(Namespaces.SOULBOUND.namespace, PersistentDataType.STRING)
    }

    fun getAsCustomItem(itemMeta: ItemMeta): CustomItems? {
        val s =
            itemMeta.persistentDataContainer[Namespaces.CUSTOM_ITEM.namespace, PersistentDataType.STRING] ?: return null
        return CustomItems.valueOf(s)
    }

    fun frostmourneExplosion(locy: Location, p: Player, hitCaster: Boolean = true) {
        val random = Random()
        val world = locy.world
        for (j in 1..25000) {
            val x = random.nextGaussian() * 10 + locy.x
            val y = random.nextGaussian() * 10 + locy.y
            val z = random.nextGaussian() * 10 + locy.z
            val loc = Location(world, x, y, z)
            if (!hitCaster && loc.distanceSquared(locy) < 4.0) continue
            TheColdestOne.snowyfi(loc)
        }

        locy.world.spawnParticle(
            Particle.END_ROD, locy,
            5000, .1, .1, .1, .75, null, true
        )
        locy.world.playSound(locy, org.bukkit.Sound.ITEM_TRIDENT_THUNDER, 5f, 0.5f)

        val entities = locy.getNearbyLivingEntities(16.0)
        val damageSource = DamageSource.builder(org.bukkit.damage.DamageType.FREEZE).withDirectEntity(p).build()
        for (entity in entities) {
            val location = entity.location
            if (location.distanceSquared(locy) > 16.0 * 16.0) continue
            if (!hitCaster && entity.uniqueId == p.uniqueId) continue
            entity.damage(15.0, damageSource)
            val vector = location.toVector().subtract(locy.toVector()).multiply(Vector(1, 0, 1))
                .normalize().multiply(1.5).add(Vector(0.0, 0.8, 0.0))
            entity.velocity = vector
            entity.freezeTicks = entity.freezeTicks.coerceAtLeast(1000)
        }
    }

    fun myMod(d: Double, m: Double): Double = if (d < 0) (d % m + m) % m else d % m

}