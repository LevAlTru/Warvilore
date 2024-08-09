package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.Item
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.SoftwareBase
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.trashcan.Namespaces
import kotlin.math.min

private const val MAX_CHARGE = 6000

class WalkingComputer(string: String) : AbilitiesCore(string) {

    var charge: Int = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.CHARGE.namespace,
                PersistentDataType.INTEGER
            ) ?: 0
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.CHARGE.namespace,
                PersistentDataType.INTEGER, value
            )
            field = value
        }
    private var activeSoftware: SoftwareBase? = null

    override fun onTick(event: ServerTickEndEvent) {
        val locy = player!!.location.add(0.0, player!!.height / 2, 0.0)
        if (charge > 0) {
            if (random.nextDouble() < 0.3)
                player!!.world.spawnParticle(
                    Particle.ELECTRIC_SPARK, locy, 1, .2, .4, .2, .3,
                    null, false
                )
            player!!.addPotionEffects(
                listOf(
                    PotionEffect(PotionEffectType.STRENGTH, 10, 1, true, false, true),
                    PotionEffect(PotionEffectType.HASTE, 10, 1, true, false, true),
                    PotionEffect(PotionEffectType.SPEED, 10, 1, true, false, true),
                    PotionEffect(PotionEffectType.JUMP_BOOST, 10, 1, true, false, true),
                )
            )
            charge--
        }
        if (activeSoftware?.tick(player!!) == true) {
            activeSoftware?.onShutDown(player!!)
            changeSoftware(null)
        }
        if (player!!.isInWaterOrBubbleColumn
            && player!!.noDamageTicks <= 8
            && !player!!.isDead
            && player!!.gameMode != GameMode.CREATIVE
        ) {
            if (charge <= 0) {
                player!!.damage(0.01)
                player!!.health -= player!!.health.coerceAtMost(0.98)
                player!!.world.spawnParticle(
                    Particle.ELECTRIC_SPARK, locy,
                    15, .2, .3, .2, .3, null, false
                )
            } else {
                player!!.world.spawnParticle(
                    Particle.ELECTRIC_SPARK, locy, 150,
                    .2, .3, .2, 2.0, null, true
                )
                player!!.world.spawnParticle(
                    Particle.END_ROD, locy, 100,
                    .2, .3, .2, .7, null, true
                )
                player!!.world.playSound(
                    player!!.location,
                    Sound.BLOCK_BEACON_DEACTIVATE,
                    SoundCategory.MASTER,
                    5f,
                    0.5f
                )
                player!!.world.playSound(
                    player!!.location,
                    Sound.BLOCK_TRIAL_SPAWNER_BREAK,
                    SoundCategory.MASTER,
                    3f,
                    0.5f
                )
                for (itemStack in player!!.inventory) {
                    if (itemStack == null || itemStack.isEmpty) continue
                    player!!.world.spawn(locy, Item::class.java) {
                        it.itemStack = itemStack
                        it.velocity = Vector(
                            random.nextGaussian() * .3,
                            random.nextGaussian() * .3 + .3,
                            random.nextGaussian() * .3
                        )
                    }
                }
                player!!.inventory.clear()
                val source = DamageSource.builder(DamageType.LIGHTNING_BOLT).withDirectEntity(player!!).build()
                player!!.damage(100.0, source)
                player!!.health = 0.0
                charge = 0
                electricExplosion(50.0)
            }
        }
    }

    private fun electricExplosion(power: Double) {
        val entities = player!!.location.getNearbyLivingEntities(power)
        val source = DamageSource.builder(DamageType.LIGHTNING_BOLT).withDirectEntity(player!!).build()
        val size = entities.size
        for (e in entities) {
            if (e.uniqueId == player!!.uniqueId) continue
            if (!e.isInWaterOrBubbleColumn) continue
            if (e.location.distanceSquared(player!!.location) > 16.0 * 16.0) continue
            e.damage(power, source)
            val locy = e.location.add(0.0, e.height / 2, 0.0)
            e.world.playSound(
                player!!.location,
                Sound.BLOCK_TRIAL_SPAWNER_BREAK,
                SoundCategory.MASTER,
                min(2f, 4f / size),
                0.5f
            )
            e.world.spawnParticle(
                Particle.ELECTRIC_SPARK, locy, min(150, 450 / size),
                .3, .3, .3, 2.0, null, true
            )
            e.world.spawnParticle(
                Particle.END_ROD, locy, min(100, 300 / size),
                .3, .3, .3, .7, null, true
            )
        }
    }

    override fun onDamage(event: EntityDamageEvent) {
        when (event.damageSource.damageType) {
            DamageType.LIGHTNING_BOLT -> {
                charge = MAX_CHARGE
                event.damage = 0.01
                player!!.addPotionEffect(
                    PotionEffect(PotionEffectType.BLINDNESS, 25, 0, true, false, true)
                )
            }
        }
    }

    override fun onJoin(event: PlayerJoinEvent) {
        loadSoftware()
    }

    override fun onLeave(event: PlayerQuitEvent) {
        activeSoftware?.onShutDown(player!!)
        saveSoftware(activeSoftware)
    }

    override fun onDeath(event: PlayerDeathEvent) {
        charge = 0
    }

    fun changeSoftware(software: SoftwareBase?) {
        activeSoftware?.onShutDown(player!!)
        activeSoftware = software
        saveSoftware(software)
    }

    private fun loadSoftware() {
        val data = player!!.persistentDataContainer

        if (data.has(Namespaces.SOFTWARE_SAVE_PLACE.namespace)) {
            val s = data[Namespaces.SOFTWARE_SAVE_PLACE.namespace, PersistentDataType.STRING]
            if (s == null || s == "null") return
            try {
                changeSoftware(
                    Class.forName(Warvilore::class.java.`package`.name + ".software." + s)
                        .constructors[0].newInstance(data[Namespaces.SOFTWARE_SAVE_ARGS_PLACE.namespace, PersistentDataType.STRING])
                            as? SoftwareBase ?: return
                )
            } catch (e: ClassNotFoundException) {
            }
        }
    }

    private fun saveSoftware(software: SoftwareBase?) {
        val data = player!!.persistentDataContainer
        if (software != null) {
            data[Namespaces.SOFTWARE_SAVE_PLACE.namespace, PersistentDataType.STRING] = software::class.java.simpleName
            data[Namespaces.SOFTWARE_SAVE_ARGS_PLACE.namespace, PersistentDataType.STRING] =
                software.arguments.map { "${it.key}:${it.value}" }.joinToString(separator = " ")
        } else {
            data[Namespaces.SOFTWARE_SAVE_PLACE.namespace, PersistentDataType.STRING] = "null"
            data[Namespaces.SOFTWARE_SAVE_ARGS_PLACE.namespace, PersistentDataType.STRING] = "null"
        }
    }

    override fun getAboutMe(): List<Component> = listOf(
        text("Твои умения:").color(NamedTextColor.GREEN),
        text(""),
        text("- Ты компьютер. У тебя есть возможность использовать различный софт через команду /software.").color(
            NamedTextColor.GREEN
        ),
        text(""),
        text(
            "- Когда по тебе бьет молния ты перезаряжаешься. " +
                    "У тебя появляются дополнительные эффекты на 5 минут."
        ).color(NamedTextColor.GREEN),
        text(""),
        text("Твои минусы:").color(NamedTextColor.RED),
        text(""),
        text("- Ты компьютер. Ты боишься воды (но не дождя).").color(NamedTextColor.RED),
        text(
            "  - Когда ты перезаряжен, ты взрываешься при контакте с водой. " +
                    "Также это наносит колоссальный крон тебе и тем кто касается воды около тебя."
        ).color(NamedTextColor.RED),
    )
}