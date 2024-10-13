package uwu.levaltru.warvilore.tickables

import org.bukkit.*
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import uwu.levaltru.warvilore.Tickable
import uwu.levaltru.warvilore.trashcan.LevsUtils
import uwu.levaltru.warvilore.trashcan.Namespaces
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val MIN_CUSTOM_MODEL_DATA = 10
private const val MAX_CUSTOM_MODEL_DATA = 15

private const val MAX_IN_AIR_COUNTER = 700

class TheUpperEmitter(location: Location, val intensity: Double, val size: Double, val energy: Int) : Tickable() {

    val disapierHeight = location.y
    val location = location.apply {
        y = world.minHeight - 3.0
        pitch = 90f
        yaw = 0f
    }
    var itemDisplay: ItemDisplay? = null
    var inAirCounter = 0
    val random = Random()

    override fun tick(): Boolean {
        location.yaw += .33f
        location.add(0.0, 0.06, 0.0)
        if (location.isChunkLoaded) {
            if (itemDisplay?.isValid != true) createDisplay()
            if (age % 8 == 0) {
                itemDisplay?.teleport(location)
                location.world.playSound(
                    location,
                    Sound.BLOCK_TRIAL_SPAWNER_AMBIENT_OMINOUS,
                    .2f * size.toFloat() * ((inAirCounter.toFloat() * 3f / MAX_IN_AIR_COUNTER).coerceAtMost(1f)),
                    .7f
                )
            }
        }
        val b = location.y > disapierHeight

        val type = location.block.type
        inAirCounter =
            if (!type.isAir || b) (inAirCounter - 1).coerceAtLeast(0)
            else (inAirCounter + 1).coerceAtMost(MAX_IN_AIR_COUNTER)
        changeState(((inAirCounter.toDouble() * (MAX_CUSTOM_MODEL_DATA - MIN_CUSTOM_MODEL_DATA + 1)) / MAX_IN_AIR_COUNTER).toInt())

        for (d in 0..<LevsUtils.roundToRandomInt(intensity)) {
            val ranVal = random.nextDouble(PI * 2.0)
            val add = location.clone().add(sin(ranVal) * size, 0.0, cos(ranVal) * size)

            for (nearbyPlayer in add.getNearbyPlayers(48.0)) {
                nearbyPlayer.spawnParticle(Particle.SOUL_FIRE_FLAME, add, 30, .5, .1, .5, .1, null, true)
                nearbyPlayer.spawnParticle(Particle.OMINOUS_SPAWNING, add, 30, .1, .1, .1, 10.0, null, true)
                nearbyPlayer.playSound(add, Sound.BLOCK_END_PORTAL_FRAME_FILL, 2f, .5f)
            }

            val vec = LevsUtils.getRandomNormalizedVector()
            add.world.rayTraceBlocks(add, vec, 256.0, FluidCollisionMode.ALWAYS, false)?.hitBlock?.let {
                NetherInfector(it.location.toCenterLocation(), vec, energy)
            }
        }

        age++
        return b && inAirCounter <= 0
    }

    override fun collapse() {
        itemDisplay?.remove()
    }

    private fun createDisplay() {
        itemDisplay = location.world.spawn(location, ItemDisplay::class.java) {
            it.setItemStack(ItemStack(Material.CHAIN_COMMAND_BLOCK).apply {
                this.itemMeta = this.itemMeta.apply {
                    setCustomModelData(MIN_CUSTOM_MODEL_DATA)
                }
            })
            it.teleportDuration = 10
            it.interpolationDuration = 10
            it.persistentDataContainer.set(Namespaces.SHOULD_DESPAWN.namespace, PersistentDataType.BOOLEAN, true)
            val float = (2.125 * size).toFloat()
            it.transformation = Transformation(
                Vector3f(), Quaternionf(), Vector3f(
                    float, float, .1f
                ), Quaternionf()
            )
            it.brightness = Display.Brightness(15, 15)
            it.viewRange = 5000f
        }
    }

    private fun changeState(int: Int) {
        val s = (int + MIN_CUSTOM_MODEL_DATA).coerceIn(MIN_CUSTOM_MODEL_DATA, MAX_CUSTOM_MODEL_DATA)
        itemDisplay?.setItemStack(ItemStack(Material.CHAIN_COMMAND_BLOCK).apply {
            itemMeta = itemMeta.apply { setCustomModelData(s) }
        })
    }
}