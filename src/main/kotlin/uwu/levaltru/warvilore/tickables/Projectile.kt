package uwu.levaltru.warvilore.tickables

import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.Tickable

abstract class Projectile(var location: Location, var velocity: Vector, val predicate: (Entity) -> Boolean) :
    Tickable() {

    var prevLocation: Location = location.clone()

    override fun tick(): Boolean {
        prevLocation = location.clone()
        location.add(velocity)
        return false
    }

    fun didCollide(): Boolean {
        val world = location.world

        val subtract = location.clone().subtract(prevLocation).toVector()
        val rayTrace = world.rayTrace(
            prevLocation, subtract, subtract.length() + 0.1, FluidCollisionMode.NEVER,
            true, 0.4, predicate
        )

        val hitPosition = rayTrace?.hitPosition
        if (hitPosition != null) {
            location = hitPosition.toLocation(world)
            if (rayTrace.hitEntity != null)
                if (onCollision(location, rayTrace.hitEntity!!))
                    return true
            if (onCollision(location, null)) return true
        }
        return false
    }

    abstract fun onCollision(collisionPlace: Location, entity: Entity?): Boolean

    fun getInBeetweens(stepSize: Double): MutableList<Vector> {
        val toVector = location.clone().subtract(prevLocation).toVector()
        val length = toVector.length()
        val list = mutableListOf<Vector>()
        var d = 0.0
        while (d < length) {
            val step = toVector.clone().normalize().multiply(d)
            list.add(step.add(prevLocation.toVector()))
            d += stepSize
        }
        return list
    }

    companion object {
        fun isInBlockCollision(loc: Location): Boolean =
            loc.block.collisionShape.boundingBoxes.any { it.shift(loc.toBlockLocation()).contains(loc.toVector()) }
    }
}