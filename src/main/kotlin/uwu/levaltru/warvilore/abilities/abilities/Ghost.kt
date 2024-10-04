package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.damage.DamageType
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.persistence.PersistentDataType
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.abilities.interfaces.tagInterfaces.CantLeaveSouls
import uwu.levaltru.warvilore.trashcan.Namespaces

class Ghost(nickname: String) : AbilitiesCore(nickname), CantLeaveSouls {

    var particlesMode: Int = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.PARTICLES_MODE.namespace,
                PersistentDataType.INTEGER
            ) ?: 0
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.PARTICLES_MODE.namespace,
                PersistentDataType.INTEGER, value
            )
            field = value
        }
    var isColorYellow: Boolean = false
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.ARE_PARTICLES_YELLOW.namespace,
                PersistentDataType.BOOLEAN
            ) ?: false
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.ARE_PARTICLES_YELLOW.namespace,
                PersistentDataType.BOOLEAN, value
            )
            field = value
        }

    override fun onTick(event: ServerTickEndEvent) {

        if (!particleModeConditionsMet()) return

        if (player!!.ticksLived % 7 == 0) {
            val box = player!!.boundingBox
            val x = random.nextDouble(box.minX, box.maxX)
            val y = random.nextDouble(box.minY, box.maxY)
            val z = random.nextDouble(box.minZ, box.maxZ)
            player!!.world.spawnParticle(
                if (isColorYellow) Particle.TRIAL_SPAWNER_DETECTION else Particle.TRIAL_SPAWNER_DETECTION_OMINOUS,
                x, y, z, 1, .0, .0, .0, .02, null, true
            )
            player!!.foodLevel = 20
            player!!.saturation = 20f
        }
    }

    override fun onDeath(event: PlayerDeathEvent) {
        event.isCancelled = true
        effects(true)
        player!!.gameMode = GameMode.SPECTATOR
        player!!.health = player!!.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value
    }

    override fun onDamage(event: EntityDamageEvent) {
        event.isCancelled = when (event.damageSource.damageType) {
            DamageType.FALL, DamageType.IN_WALL -> true
            else -> false
        }
    }

    private fun particleModeConditionsMet(): Boolean {
        return when (particlesMode) {
            ParticlesState.NEVER.int -> false
            ParticlesState.ALWAYS.int -> true
            ParticlesState.SURVIVAL.int -> player!!.gameMode == GameMode.SURVIVAL
            ParticlesState.SPECTATOR.int -> player!!.gameMode == GameMode.SPECTATOR
            else -> false
        }
    }

    override fun completeCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String>? {
        return when (args.size) {
            1 -> listOf("v", "p")
            2 -> if (args[0].lowercase() == "p") ParticlesState.entries.map { it.name }
                .plus("CHANGE_COLOR") else listOf()

            else -> listOf()
        }
    }

    override fun executeCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>) {
        if (args.isEmpty()) {
            player!!.sendMessage(text("Ошибка: недостаточно аргументов.").color(NamedTextColor.RED))
            return
        }
        when (args[0]) {
            "v" -> {
                if (player!!.gameMode == GameMode.SURVIVAL) {
                    player!!.gameMode = GameMode.SPECTATOR
                } else if (player!!.gameMode == GameMode.SPECTATOR) {
                    player!!.gameMode = GameMode.SURVIVAL
                } else {
                    player!!.sendMessage(
                        text("Ошибка: вы находитесь в режиме креатива или в режиме приключений.")
                            .color(NamedTextColor.RED)
                    )
                    return
                }
                effects(false)
            }

            "p" -> {
                if (args.size < 2) {
                    player!!.sendMessage(text("Ошибка: недостаточно аргументов.").color(NamedTextColor.RED))
                    return
                }

                if (args[1].lowercase() == "change_color") {
                    isColorYellow = !isColorYellow
                    return
                }
                for (entry in ParticlesState.entries) {
                    if (entry.name == args[1]) {
                        particlesMode = entry.int
                        return
                    }
                }
                player!!.sendMessage(text("Ошибка: нету такого режима.").color(NamedTextColor.RED))
            }
        }
    }

    private fun effects(deathParticles: Boolean) {
        player!!.world.spawnParticle(
            if (deathParticles) Particle.SCULK_SOUL else { if (isColorYellow) Particle.TRIAL_SPAWNER_DETECTION else Particle.TRIAL_SPAWNER_DETECTION_OMINOUS },
            player!!.location.add(0.0, player!!.height / 2, 0.0),
            100,
            .2,
            .3,
            .2,
            .1,
            null,
            true
        )
        player!!.world.playSound(player!!.location, Sound.BLOCK_TRIAL_SPAWNER_OMINOUS_ACTIVATE, 2f, .5f)
        if (deathParticles)
            player!!.world.playSound(player!!.location, Sound.ENTITY_WARDEN_ATTACK_IMPACT, 2f, .5f)
    }

    override fun getAboutMe(): List<Component> = listOf(
        text("You're dead lol...").color(NamedTextColor.LIGHT_PURPLE),
        text(""),
        text("- А еще ты не получаешь урон от падения").color(NamedTextColor.GREEN),
        text(""),
        text("- /abilka для настойки частиц и \"ваниша\"").color(NamedTextColor.GREEN),
    )

    enum class ParticlesState(val int: Int) {
        NEVER(0),
        ALWAYS(1),
        SPECTATOR(2),
        SURVIVAL(3),
    }
}