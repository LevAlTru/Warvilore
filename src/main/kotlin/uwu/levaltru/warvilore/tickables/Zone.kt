package uwu.levaltru.warvilore.tickables

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.entity.LightningStrike
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason
import org.bukkit.persistence.PersistentDataType
import uwu.levaltru.warvilore.Tickable
import uwu.levaltru.warvilore.trashcan.Namespaces
import java.util.*
import kotlin.math.sqrt

class Zone private constructor() : Tickable() {

    companion object {
        private var instance: Zone? = null

        private fun factorOf(x: Number, z: Number) =
            sqrt((x.toDouble() * x.toDouble() * 256) + (z.toDouble() * z.toDouble() * 256))

        fun getInstance() = instance ?: Zone()
    }

    val random = Random()

    init {
        instance = this
    }

    var chunks = hashMapOf<Chunk, Double>()

    override fun tick(): Boolean {

        if ((Bukkit.getCurrentTick() + 5) % 20 == 0) {
            chunks.clear()
            for (world in Bukkit.getWorlds()) {
                for (chunki in world.loadedChunks) {
                    chunks[chunki] = factorOf(chunki.x, chunki.z)
                }
            }
        }

        val tempChunks = hashMapOf<Chunk, Double>()
        for ((chunk, factor) in chunks) {
            if (chunk.isLoaded) {
                chunk.entities.filterIsInstance<Player>().forEach { it.sendMessage(Component.text(factor)) }
                tempChunks[chunk] = chunks[chunk] ?: factorOf(chunk.x, chunk.z)
                if (random.nextDouble() <
                    (chunk.world.persistentDataContainer[Namespaces.WORLD_ZONE_SPEED.namespace, PersistentDataType.DOUBLE] ?: 0.05)
                ) {
                    val x = random.nextInt(0, 16)
                    val z = random.nextInt(0, 16)
                    val fx = chunk.x * 16 + x
                    val fz = chunk.z * 16 + z
                    chunk.world.spawn(
                        Location(chunk.world, fx + 0.5, chunk.world.getHighestBlockAt(fx, fz).y + 1.0, fz + 0.5),
                        LightningStrike::class.java, SpawnReason.COMMAND
                    ) {
                        it.persistentDataContainer.set(
                            Namespaces.SHOULD_DESPAWN.namespace,
                            PersistentDataType.BOOLEAN,
                            true
                        )
                    }
                }
            }
        }
        chunks = tempChunks

        return false
    }
}