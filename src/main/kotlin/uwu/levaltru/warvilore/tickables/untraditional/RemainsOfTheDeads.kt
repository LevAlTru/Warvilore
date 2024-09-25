package uwu.levaltru.warvilore.tickables.untraditional

import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.persistence.PersistentDataType
import uwu.levaltru.warvilore.Warvilore
import java.util.*
import kotlin.math.floor

class RemainsOfTheDeads(val x: Int, val z: Int, val world: World, val size: Int, val intensity: Float) {

    var markedForRemoval = false

    companion object {
        val random = Random()
        val LIST = mutableListOf<RemainsOfTheDeads>()
        private var instance: RemainsOfTheDeads? = null

        fun Tick() {
            LIST.removeIf { it.tick() }
        }

        fun load() {
            LIST.clear()
            for (world in Bukkit.getWorlds()) {
                val x = world.persistentDataContainer.get(
                    Warvilore.namespace("remains_of_a_dead_x"),
                    PersistentDataType.LIST.integers()
                ) ?: return
                val z = world.persistentDataContainer.get(
                    Warvilore.namespace("remains_of_a_dead_z"),
                    PersistentDataType.LIST.integers()
                ) ?: return
                val s = world.persistentDataContainer.get(
                    Warvilore.namespace("remains_of_a_dead_s"),
                    PersistentDataType.LIST.integers()
                ) ?: return
                val i = world.persistentDataContainer.get(
                    Warvilore.namespace("remains_of_a_dead_i"),
                    PersistentDataType.LIST.floats()
                ) ?: return
                for (it in x.indices) {
                    RemainsOfTheDeads(x[it], z[it], world, s[it], i[it])
                }
            }
        }

        fun save() {
            for (world in Bukkit.getWorlds()) {
                val xs = mutableListOf<Int>()
                val zs = mutableListOf<Int>()
                val ss = mutableListOf<Int>()
                val `is` = mutableListOf<Float>()

                for (it in LIST.filter { it.world.uid == world.uid }) {
                    xs.add(it.x)
                    zs.add(it.z)
                    ss.add(it.size)
                    `is`.add(it.intensity)
                }

                world.persistentDataContainer.set(
                    Warvilore.namespace("remains_of_a_dead_x"),
                    PersistentDataType.LIST.integers(), xs
                )
                world.persistentDataContainer.set(
                    Warvilore.namespace("remains_of_a_dead_z"),
                    PersistentDataType.LIST.integers(), zs
                )
                world.persistentDataContainer.set(
                    Warvilore.namespace("remains_of_a_dead_s"),
                    PersistentDataType.LIST.integers(), ss
                )
                world.persistentDataContainer.set(
                    Warvilore.namespace("remains_of_a_dead_i"),
                    PersistentDataType.LIST.floats(), `is`
                )
            }
        }
    }

    init {
        instance = this
        LIST += this
    }

    fun tick(): Boolean {

        var d = (random.nextDouble() * intensity).coerceAtMost(100.0)
        while (d > 1 || (d < random.nextDouble() && d > 0)) {

            val dx = random.nextGaussian() * size
            val dz = random.nextGaussian() * size

            val fx = dx + x
            val fz = dz + z

            val blockFX = floor(fx).toInt()
            val blockFZ = floor((fz)).toInt()

            if (world.isChunkLoaded(blockFX  / 16 - if (blockFX < 0) 1 else 0, blockFZ  / 16 - if (blockFZ < 0) 1 else 0)) {
                val y = world.getHighestBlockAt(blockFX , blockFZ).y + 1.0
                world.spawnParticle(Particle.TRIAL_SPAWNER_DETECTION_OMINOUS, fx, y, fz, 1, .0, .0, .0, .01, null, true)
            }

            d--
        }

        return markedForRemoval
    }

}