package uwu.levaltru.warvilore.tickables.effect

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Item
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.Tickable
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.abilities.abilities.TheHolyOne
import uwu.levaltru.warvilore.trashcan.CustomItems
import uwu.levaltru.warvilore.trashcan.LevsUtils
import java.util.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val REVIVAL_POINT = 965

class ReturnTommy(val location: Location, val shard: String, val fragment: String) : Tickable() {

    var shardItem: Item? = null
    var fragmentItem: Item? = null
    val random = Random()

    override fun tick(): Boolean {

        val procent = age.toDouble() / REVIVAL_POINT

        val procentCubed = procent * procent * procent
        val rotatePos = Vector(
            sin(procentCubed * 400),
            0.0,
            cos(procentCubed * 400)
        ).multiply((1 - (procentCubed * procent * procent)) * 6 + 0.2)

        val ambientParticles = LevsUtils.roundToRandomInt(procentCubed * 40)

        shardItem?.let {
            location.world.spawnParticle(
                Particle.TRIAL_SPAWNER_DETECTION_OMINOUS,
                it.location.add(0.0, 0.2, 0.0),
                1,
                .1,
                .1,
                .1,
                0.03
            )

            if (ambientParticles > 0)
                location.world.spawnParticle(
                    Particle.TRIAL_SPAWNER_DETECTION_OMINOUS,
                    it.location.add(0.0, 0.2, 0.0),
                    ambientParticles,
                    20.0,
                    20.0,
                    20.0,
                    0.1,
                    null,
                    true
                )

            val desiredLocation = location.clone().add(rotatePos)

            it.velocity = desiredLocation.clone().subtract(it.location).toVector().multiply(0.1)
        }

        fragmentItem?.let {
            location.world.spawnParticle(
                Particle.TRIAL_SPAWNER_DETECTION,
                it.location.add(0.0, 0.2, 0.0),
                1,
                .1,
                .1,
                .1,
                0.03
            )

            if (ambientParticles > 0)
                location.world.spawnParticle(
                    Particle.TRIAL_SPAWNER_DETECTION,
                    it.location.add(0.0, 0.2, 0.0),
                    ambientParticles,
                    20.0,
                    20.0,
                    20.0,
                    0.1,
                    null,
                    true
                )

            val desiredLocation = location.clone().subtract(rotatePos)

            it.velocity = desiredLocation.clone().subtract(it.location).toVector().multiply(0.1)
        }

        val shardPlayer = Bukkit.getPlayer(shard)
        val fragmentPlayer = Bukkit.getPlayer(fragment)

        val roundToRandomInt = LevsUtils.roundToRandomInt(procentCubed * 30 + .2)
        if (roundToRandomInt > 0) location.world.spawnParticle(
            Particle.END_ROD, location,
            roundToRandomInt, procentCubed * 2, procentCubed * 2, procentCubed * 2, procent * .3, null, true
        )

        if (age == 0) {
            location.world.playSound(location, "levaltru:the_revival", 24f, 1f)
        } else if (age == 40) {
            shardPlayer?.getAttribute(Attribute.GENERIC_GRAVITY)?.addModifier(
                AttributeModifier(
                    Warvilore.namespace("temp_lose_gravity"), -1.05, AttributeModifier.Operation.ADD_SCALAR
                )
            )
            fragmentPlayer?.getAttribute(Attribute.GENERIC_GRAVITY)?.addModifier(
                AttributeModifier(
                    Warvilore.namespace("temp_lose_gravity"), -1.05, AttributeModifier.Operation.ADD_SCALAR
                )
            )
        } else if (age == 100) {
            shardItem = destroy(true)
        } else if (age == 130) {
            fragmentItem = destroy(false)
        } else if (age > 450) {
            val localProsent = (age - 450).toDouble() / (REVIVAL_POINT - 450)

            var d = LevsUtils.roundToRandomInt(localProsent * 50.0)
            while (d > 0) {

                val add = location.clone()
                    .add(random.nextGaussian() * 20, random.nextGaussian() * 20, random.nextGaussian() * 20)
                location.world.spawnParticle(Particle.END_ROD, add, 0, 0.0, 1.0, 0.0, 1 * localProsent, null, true)

                d--
            }
        }

        shardPlayer?.sendActionBar(Component.text(age.toString()).color(NamedTextColor.GOLD))

        age++

        if (age >= REVIVAL_POINT) {
            shardItem?.remove()
            fragmentItem?.remove()

            if (fragmentPlayer != null) {
                fragmentPlayer.teleport(location.clone().subtract(0.0, fragmentPlayer.height / 2, 0.0))
                fragmentPlayer.gameMode = GameMode.SURVIVAL
                LevsUtils.addInfiniteSlowfall(fragmentPlayer)
                val locy = fragmentPlayer.location.add(0.0, fragmentPlayer.height / 2, 0.0)

                locy.world.spawnParticle(Particle.END_ROD, locy, 1000, .2, .4, .2, .6, null, true)
                locy.world.spawnParticle(
                    Particle.TRIAL_SPAWNER_DETECTION_OMINOUS,
                    locy,
                    200,
                    .2,
                    .4,
                    .2,
                    .2,
                    null,
                    true
                )
                locy.world.spawnParticle(Particle.SOUL_FIRE_FLAME, locy, 300, .2, .4, .2, .2, null, true)
            }

            val enderChestItems = shardPlayer?.enderChest?.contents?.filterNotNull()
            if (enderChestItems != null) {

                Bukkit.getScheduler().runTaskLater(Warvilore.instance, Runnable {
                    for ((i, itemStack) in enderChestItems.withIndex()) {

                        val d = i.toDouble() / enderChestItems.size * 2 * PI + 0.7

                        LegendaryItemSpawner(
                            location.clone().add(
                                sin(d) * enderChestItems.size * 1.5,
                                0.0,
                                cos(d) * enderChestItems.size * 1.5
                            ), itemStack, 300 + i * 7
                        )
                    }
                }, 300L)

            }

            return true
        }
        return false
    }

    private fun destroy(isShard: Boolean): Item {
        val player = Bukkit.getPlayer(if (isShard) shard else fragment)
        return location.world.spawn(
            if (player != null) {
                val locy = player.location.add(0.0, player.height / 2, 0.0)
                player.gameMode = GameMode.SPECTATOR
                if (isShard) AbilitiesCore.hashMap.remove(player.name)
                else TheHolyOne(player.name)
                player.getAttribute(Attribute.GENERIC_GRAVITY)?.removeModifier(Warvilore.namespace("temp_lose_gravity"))
                locy.world.spawnParticle(
                    if (isShard) Particle.TRIAL_SPAWNER_DETECTION else Particle.TRIAL_SPAWNER_DETECTION_OMINOUS,
                    locy,
                    200,
                    .2,
                    .4,
                    .2,
                    .1,
                    null,
                    true
                )
                locy.world.playSound(locy, Sound.PARTICLE_SOUL_ESCAPE, 3f, .5f)
                locy
            } else {
                location
            }, Item::class.java
        ) {
            it.itemStack = (if (isShard) CustomItems.SHARD_OF_MORTUUS else CustomItems.FRAGMENT_OF_VICTUS).getAsItem()
            it.velocity = Vector(0, 0, 0)
            it.pickupDelay = Int.MAX_VALUE
        }
    }
}