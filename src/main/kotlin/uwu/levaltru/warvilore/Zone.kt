package uwu.levaltru.warvilore

import org.bukkit.*
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.LightningStrike
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import uwu.levaltru.warvilore.trashcan.Namespaces
import java.util.*
import kotlin.math.max
import kotlin.math.sqrt

class Zone private constructor() {

    companion object {
        private var instance: Zone? = null

        fun factorOf(x: Number, z: Number, world: World): Double {
            val distance = sqrt((x.toDouble() * x.toDouble() * 256) + (z.toDouble() * z.toDouble() * 256))
            val distanceToZone =
                world.persistentDataContainer.get(Namespaces.WORLD_ZONE_DISTANCE.namespace, PersistentDataType.DOUBLE)
                    ?: return 0.0
            val blur =
                world.persistentDataContainer.get(Namespaces.WORLD_ZONE_BLUR.namespace, PersistentDataType.DOUBLE)
                    ?: 128.0

            return ((distance - distanceToZone) / blur).coerceIn(0.0, 1.0)
        }

        fun getInstance() = instance ?: Zone()
    }

    val random = Random()

    init {
        instance = this
    }

    var chunks = hashMapOf<Chunk, Double>()

    fun tick(): Boolean {
        if ((Bukkit.getCurrentTick() + 5) % 20 == 0) {
            chunks.clear()
            for (world in Bukkit.getWorlds()) {
                for (chunki in world.loadedChunks) {
                    val factorOf = factorOf(chunki.x, chunki.z, chunki.world)
                    if (factorOf > 0) chunks[chunki] = factorOf
                }
            }
        }

        val tempChunks = hashMapOf<Chunk, Double>()
        for ((chunk, factor) in chunks) {
            if (chunk.isLoaded) {
                val world = chunk.world
                tempChunks[chunk] = chunks[chunk] ?: factorOf(chunk.x, chunk.z, world)

                when (world.persistentDataContainer.get(
                    Namespaces.WORLD_ZONE_TYPE.namespace,
                    PersistentDataType.DOUBLE
                )?.toInt()) {
                    1 -> {
                        val location = Location(
                            world,
                            chunk.x * 16 + 8.0,
                            world.minHeight + world.maxHeight.toDouble() / 2,
                            chunk.z * 16 + 8.0
                        )
                        if (random.nextDouble() < 0.05 * factor) {
                            val x = random.nextInt(0, 16)
                            val z = random.nextInt(0, 16)
                            val fx = chunk.x * 16 + x
                            val fz = chunk.z * 16 + z
                            world.spawn(
                                Location(
                                    world,
                                    fx + 0.5,
                                    world.getHighestBlockAt(fx, fz).y + 1.0,
                                    fz + 0.5
                                ),
                                LightningStrike::class.java, SpawnReason.COMMAND
                            ) {
                                it.persistentDataContainer.set(
                                    Namespaces.SHOULD_DESPAWN.namespace,
                                    PersistentDataType.BOOLEAN,
                                    true
                                )
                            }
                            chunk.entities.filterIsInstance<LivingEntity>().forEach {
                                it.addPotionEffect(
                                    PotionEffect(
                                        PotionEffectType.WITHER, 120, 1, true, true, true
                                    )
                                )
                            }
                        }

                        for (segment in world.minHeight..<world.maxHeight step 16) {
                            val nextDouble = random.nextDouble()
                            var d = nextDouble * nextDouble * 15 * factor
                            while (d > 1 || random.nextDouble() < d) {
                                d--

                                val x = random.nextDouble(0.0, 16.0)
                                val y = random.nextDouble(0.0, 16.0)
                                val z = random.nextDouble(0.0, 16.0)
                                val fx = chunk.x * 16 + x
                                val fy = segment + y
                                val fz = chunk.z * 16 + z

                                if (Location(
                                        location.world,
                                        location.x,
                                        segment + 8.0,
                                        location.z
                                    ).getNearbyPlayers(24.0).isNotEmpty()
                                ) world.spawnParticle(
                                    Particle.ELECTRIC_SPARK,
                                    fx,
                                    fy,
                                    fz,
                                    0,
                                    random.nextDouble(0.75, 1.5),
                                    -random.nextDouble(0.5, 1.0),
                                    random.nextDouble(1.0, 3.0),
                                    1.0 * factor
                                )
                            }
                        }
                    }

                    2 -> {
                        val location = Location(
                            world,
                            chunk.x * 16 + 8.0,
                            world.minHeight + world.maxHeight.toDouble() / 2,
                            chunk.z * 16 + 8.0
                        )

                        for (segment in world.minHeight..<world.maxHeight step 16) {
                            val nextDouble = random.nextDouble()
                            var d = nextDouble * nextDouble * 3 * factor
                            while (d > 1 || random.nextDouble() < d) {
                                d--

                                val x = random.nextDouble(0.0, 16.0)
                                val y = random.nextDouble(0.0, 16.0)
                                val z = random.nextDouble(0.0, 16.0)
                                val fx = chunk.x * 16 + x
                                val fy = segment + y
                                val fz = chunk.z * 16 + z

                                if (Location(
                                        location.world,
                                        location.x,
                                        segment + 8.0,
                                        location.z
                                    ).getNearbyPlayers(24.0).isNotEmpty()
                                ) world.spawnParticle(
                                    Particle.FLAME,
                                    fx,
                                    fy,
                                    fz,
                                    0,
                                    random.nextDouble(0.75, 1.5),
                                    -random.nextDouble(0.5, 1.0),
                                    random.nextDouble(1.0, 3.0),
                                    0.1 * factor
                                )

                                val block = chunk.getBlock(x.toInt(), fy.toInt(), z.toInt())
                                if (chunk.getBlock(
                                        x.toInt(),
                                        (fy.toInt() - 1).coerceIn(world.minHeight, world.maxHeight),
                                        z.toInt()
                                    ).isCollidable && block.type == Material.AIR
                                ) block.type = Material.FIRE
                            }
                        }

                        if (random.nextDouble() < 0.05 * factor) {
                            val damageSource = DamageSource.builder(DamageType.IN_FIRE).build()
                            chunk.entities.forEach {
                                it.fireTicks = (it.fireTicks + 30).coerceAtMost(max(it.fireTicks, 100))
                                if (it is LivingEntity) {
                                    it.damage(5.0, damageSource)
                                    it.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 110, 1, true, true, true))
                                }
                            }
                        }
                    }

                    else -> {}
                }
            }
        }
        chunks = tempChunks

        return false
    }
}