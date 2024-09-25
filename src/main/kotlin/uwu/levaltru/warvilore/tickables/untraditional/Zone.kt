package uwu.levaltru.warvilore.tickables.untraditional

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
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.sqrt

private const val HOW_CLOSE_PLAYER_SHOULD_BE = 12 * 16.0

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
                    ?: HOW_CLOSE_PLAYER_SHOULD_BE

            return ((distance - distanceToZone) / blur)/*.coerceIn(0.0, 1.0)*/
        }

        fun getInstance() = instance ?: Zone()
    }

    val random = Random()

    init {
        instance = this
    }

    private var chunks = hashMapOf<Chunk, Double>()

    fun tick() {

        val player = Bukkit.getPlayer("LevAlTru")

        for (world in Bukkit.getWorlds()) {
            var distance =
                world.persistentDataContainer.get(Namespaces.WORLD_ZONE_DISTANCE.namespace, PersistentDataType.DOUBLE)
            val appDistance = world.persistentDataContainer.get(
                Namespaces.WORLD_ZONE_APPROACHING_DISTANCE.namespace,
                PersistentDataType.DOUBLE
            )
            val speedDistance = world.persistentDataContainer.get(
                Namespaces.WORLD_ZONE_SPEED.namespace,
                PersistentDataType.DOUBLE
            )?.absoluteValue
            var blur =
                world.persistentDataContainer.get(Namespaces.WORLD_ZONE_BLUR.namespace, PersistentDataType.DOUBLE)
            val appBlur = world.persistentDataContainer.get(
                Namespaces.WORLD_ZONE_APPROACHING_BLUR.namespace,
                PersistentDataType.DOUBLE
            )
            val speedBlur = world.persistentDataContainer.get(
                Namespaces.WORLD_ZONE_BLUR_SPEED.namespace,
                PersistentDataType.DOUBLE
            )?.absoluteValue

            if (distance != null && appDistance != null && speedDistance != null) {
                distance += (appDistance - distance).coerceIn(-speedDistance, speedDistance)
                world.persistentDataContainer.set(
                    Namespaces.WORLD_ZONE_DISTANCE.namespace,
                    PersistentDataType.DOUBLE,
                    distance
                )
            }

            if (distance?.isNaN() != false) distance = appDistance
            if (blur?.isNaN() != false) blur = appBlur

            if (blur != null && appBlur != null && speedBlur != null) {
                blur += (appBlur - blur).coerceIn(-speedBlur, speedBlur)
                world.persistentDataContainer.set(
                    Namespaces.WORLD_ZONE_BLUR.namespace,
                    PersistentDataType.DOUBLE,
                    blur
                )
            }
            if (Bukkit.getCurrentTick() % 30 == 5) {
//                chunks.clear()
                for (chunki in world.loadedChunks) {
//                    val nearbyPlayers = Location(
//                        world,
//                        chunki.z * 16 + 8.0,
//                        world.minHeight + world.maxHeight / 2.0,
//                        chunki.x * 16 + 8.0
//                    ).getNearbyPlayers(HOW_CLOSE_PLAYER_SHOULD_BE, 4096.0, HOW_CLOSE_PLAYER_SHOULD_BE)
//
//                    if (nearbyPlayers.isEmpty()) continue

                    val factorOf = factorOf(chunki.x, chunki.z, world)
                    if (factorOf > 0) chunks[chunki] = factorOf
                    else chunks.remove(chunki)
                }
            }
        }

        val tempChunks = hashMapOf<Chunk, Double>()

        for ((chunk, factor) in chunks) {
            val world = chunk.world
//            if (DeveloperMode) world.players.forEach { it.sendMessage("chunk $factor") }
            if (chunk.isLoaded) {

                tempChunks[chunk] = factor

                when (world.persistentDataContainer.get(
                    Namespaces.WORLD_ZONE_TYPE.namespace,
                    PersistentDataType.DOUBLE
                )?.toInt()) {
                    1 -> {
//                        if (DeveloperMode) world.players.forEach { it.sendMessage("world 1") }
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
                                    it.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 170, 4, true, true, true))
                                }
                            }
                        }
                    }

                    else -> {
//                        if (DeveloperMode) world.players.forEach { it.sendMessage("world else") }
                        val location = Location(
                            world,
                            chunk.x * 16 + 8.0,
                            world.minHeight + world.maxHeight.toDouble() / 2,
                            chunk.z * 16 + 8.0
                        )
                        if (random.nextDouble() < 0.05 * factor) {
//                            if (!chunk.isEntitiesLoaded) continue
                            if (random.nextDouble() < .33) {
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
                            }
                            val damageSource = DamageSource.builder(DamageType.WITHER).build()
                            chunk.entities.filterIsInstance<LivingEntity>().forEach {
                                it.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 170, 4, true, true, true))
                                it.damage(5.0, damageSource)
                            }
                        }

                        for (segment in world.minHeight..<world.maxHeight step 16) {
                            val nextDouble = random.nextDouble()
                            var d = nextDouble * nextDouble * 45 * factor
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
                }
            }
        }

        chunks = tempChunks

    }
}