package uwu.levaltru.warvilore.tickables.untraditional

import io.papermc.paper.math.Position
import org.bukkit.*
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.tickables.NetherInfector
import uwu.levaltru.warvilore.tickables.untraditional.RemainsOfTheDeads.Companion
import uwu.levaltru.warvilore.trashcan.LevsUtils
import java.util.*

class NetherEmitter(val x: Int, val z: Int, val world: World, var maxPower: Int, var intensity: Float) {

    var markedForRemoval = false

    companion object {
        val random = Random()
        val LIST = mutableListOf<NetherEmitter>()
        private var instance: NetherEmitter? = null

        fun Tick() {
            LIST.removeIf { it.tick() }
        }

        fun load() {
            LIST.clear()
            for (world in Bukkit.getWorlds()) {
                val x = world.persistentDataContainer.get(
                    Warvilore.namespace("netheremitter_x"),
                    PersistentDataType.LIST.integers()
                ) ?: return
                val z = world.persistentDataContainer.get(
                    Warvilore.namespace("netheremitter_z"),
                    PersistentDataType.LIST.integers()
                ) ?: return
                val s = world.persistentDataContainer.get(
                    Warvilore.namespace("netheremitter_s"),
                    PersistentDataType.LIST.integers()
                ) ?: return
                val i = world.persistentDataContainer.get(
                    Warvilore.namespace("netheremitter_i"),
                    PersistentDataType.LIST.floats()
                ) ?: return
                for (it in x.indices) {
                    NetherEmitter(x[it], z[it], world, s[it], i[it])
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
                    ss.add(it.maxPower)
                    `is`.add(it.intensity)
                }

                world.persistentDataContainer.set(
                    Warvilore.namespace("netheremitter_x"),
                    PersistentDataType.LIST.integers(), xs
                )
                world.persistentDataContainer.set(
                    Warvilore.namespace("netheremitter_z"),
                    PersistentDataType.LIST.integers(), zs
                )
                world.persistentDataContainer.set(
                    Warvilore.namespace("netheremitter_s"),
                    PersistentDataType.LIST.integers(), ss
                )
                world.persistentDataContainer.set(
                    Warvilore.namespace("netheremitter_i"),
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

        if (markedForRemoval) {
            LIST.remove(this)
            return true
        }

        if (intensity < 0 || maxPower < 0) markedForRemoval = true

        if (intensity > random.nextDouble()) {
            world.rayTraceBlocks(
                (Position.block(x, world.minHeight, z)),
                LevsUtils.getRandomNormalizedVector().multiply(Vector(1.0, 0.001, 1.0)),
                50.0,
                FluidCollisionMode.NEVER,
                true
            ) { it.type == Material.BEDROCK }?.hitBlock?.let {
                NetherInfector(it.location.add(0.0, 1.0, 0.0), Vector(0, 1, 0), LevsUtils.roundToRandomInt(random.nextDouble(.5, 1.0) * maxPower))
            }
        }

        return false
    }

}