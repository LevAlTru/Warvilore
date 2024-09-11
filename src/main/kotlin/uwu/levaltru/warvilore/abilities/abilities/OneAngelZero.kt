package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.*
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.damage.DamageType
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.entity.TextDisplay
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import uwu.levaltru.warvilore.abilities.abilities.OneAngelZero.HaloTypes
import uwu.levaltru.warvilore.abilities.bases.HatesEvilAura
import uwu.levaltru.warvilore.abilities.interfaces.tagInterfaces.CanSeeSouls
import uwu.levaltru.warvilore.trashcan.Namespaces
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sin

private const val SCALE = 2.25f
private const val COOLDOWN = 20 * 10
private const val DEFAULT_SPEED = 1f
private val DEFAULT_TYPE = HaloTypes.SMOOTH_SQUARE
private val DEFAULT_COLOR = NamedTextColor.GOLD.value()

class OneAngelZero(string: String) : HatesEvilAura(string), CanSeeSouls {

    private val AMBIENT_SOUND
        get() = random.nextInt(20 * 15, 20 * 30)

    private var haloUp: TextDisplay? = null
    private var haloDown: TextDisplay? = null
    private var color: Int = DEFAULT_COLOR
    private var type = DEFAULT_TYPE
    private var speed = DEFAULT_SPEED

    private var cooldown = COOLDOWN
    private var ambientSound = AMBIENT_SOUND

    var shouldWiggle = true

    private var rotation = 0.0f

    override fun onTick(event: ServerTickEndEvent) {
        if (!player!!.isDead) halosTick()
        cooldown--
    }

    private fun halosTick() {
        val locAbovePlayer = player!!.location.add(0.0, player!!.height + 0.2, 0.0).toVector()
        if (shouldWiggle) {
            val doubleAge = player!!.ticksLived.toDouble()
            locAbovePlayer.add(
                Vector(
                    sin(doubleAge * 0.03) * 0.1,
                    sin(doubleAge * 0.04) * 0.03,
                    cos(doubleAge * 0.02) * 0.1
                )
            )
        }
        if ((player!!.world.name == haloUp?.world?.name) && haloUp?.isValid == true)
            moveHalo(locAbovePlayer, haloUp!!, rotation)
        else {
            haloUp?.remove()
            haloUp = player!!.world.spawn(locAbovePlayer.toLocation(player!!.world), TextDisplay::class.java) {
                createHalo(it)
                val itsLoc = it.location
                itsLoc.pitch = -90f
                it.teleport(itsLoc)
            }
        }
        if ((player!!.world.name == haloDown?.world?.name) && haloDown?.isValid == true)
            moveHalo(locAbovePlayer, haloDown!!, rotation + 90f)
        else {
            haloDown?.remove()
            haloDown = player!!.world.spawn(locAbovePlayer.toLocation(player!!.world), TextDisplay::class.java) {
                createHalo(it)
                val itsLoc = it.location
                itsLoc.pitch = 90f
                it.teleport(itsLoc)
            }
        }
        if (ambientSound <= 0) {
            haloUp!!.world.playSound(
                player!!,
                Sound.BLOCK_AMETHYST_BLOCK_CHIME,
                SoundCategory.MASTER,
                0.5f,
                0.8f
            )
            ambientSound = AMBIENT_SOUND
        }
        rotation += speed
        ambientSound--
    }

    private fun moveHalo(locAbovePlayer: Vector, halo: TextDisplay, yaw: Float) {
        val location = halo.location
        val vector = locAbovePlayer.clone().subtract(location.toVector()).multiply(0.2)
        val add = location.add(vector)
        add.pitch = location.pitch
        add.yaw = yaw
        halo.teleport(add)
    }

    private fun createHalo(it: TextDisplay) {
        it.persistentDataContainer[Namespaces.SHOULD_DESPAWN.namespace, PersistentDataType.BOOLEAN] = true
        val itsLoc = it.location
        itsLoc.yaw = rotation
        it.teleport(itsLoc)
        it.teleportDuration = 2
        it.brightness = Display.Brightness(15, 15)
        it.transformation = Transformation(
            Vector3f(-0.01f * SCALE, -0.16f * SCALE, 0f),
            Quaternionf(),
            Vector3f(1f, 1f, 1f).mul(SCALE),
            Quaternionf(),
        )
        it.text(text(type.char).color(TextColor.color(color)))
        it.backgroundColor = Color.fromARGB(0)
    }

    override fun onJoin(event: PlayerJoinEvent) {
        val data = player!!.persistentDataContainer
        this.color = data[Namespaces.HALO_COLOR.namespace, PersistentDataType.INTEGER] ?: DEFAULT_COLOR
        this.speed = data[Namespaces.HALO_SPEED.namespace, PersistentDataType.FLOAT] ?: DEFAULT_SPEED
        this.shouldWiggle = data[Namespaces.HALO_SHOULD_WIGGLE.namespace, PersistentDataType.BOOLEAN] ?: true
        val s = data[Namespaces.HALO_TYPE.namespace, PersistentDataType.STRING]
        if (s != null) {
            this.type = HaloTypes.valueOf(s)
        } else this.type = DEFAULT_TYPE
    }

    override fun onLeave(event: PlayerQuitEvent) {
        player!!.persistentDataContainer[Namespaces.HALO_COLOR.namespace, PersistentDataType.INTEGER] = color
        player!!.persistentDataContainer[Namespaces.HALO_SPEED.namespace, PersistentDataType.FLOAT] = speed
        player!!.persistentDataContainer[Namespaces.HALO_TYPE.namespace, PersistentDataType.STRING] = type.name
        player!!.persistentDataContainer[Namespaces.HALO_SHOULD_WIGGLE.namespace, PersistentDataType.BOOLEAN] =
            shouldWiggle
        haloUp?.remove()
        haloDown?.remove()
    }

    fun changeHalo(color: Int, speed: Float, type: HaloTypes) {
        if (haloUp == null) return
        if (haloDown == null) return
        haloUp!!.text(text(type.char).color(TextColor.color(color)))
        haloDown!!.text(text(type.char).color(TextColor.color(color)))
        this.color = color
        this.speed = speed
        this.type = type
    }

    override fun onDamage(event: EntityDamageEvent) {
        when (event.damageSource.damageType) {
            DamageType.FALL, DamageType.STALAGMITE -> event.damage *= 0.5
        }
    }

    override fun onAction(event: PlayerInteractEvent) {
        if (!event.action.isLeftClick) return
        if (!player!!.inventory.itemInMainHand.isEmpty) return
        if (!player!!.isSneaking) return
        if (player!!.pitch < 60) return
        if (cooldown > 0 && player!!.gameMode != GameMode.CREATIVE) {
            player!!.sendActionBar(text("${cooldown / 20}s").color(NamedTextColor.RED))
            player!!.playSound(player!!, Sound.BLOCK_DECORATED_POT_INSERT_FAIL, SoundCategory.MASTER, 1f, 1f)
            return
        }
        if (haloUp == null) return

        val location = haloUp!!.location

        location.world.spawnParticle(
            Particle.END_ROD, location, 30,
            .2, 0.0, .2, .1, null, true
        )
        location.world.playSound(location, Sound.BLOCK_TRIAL_SPAWNER_OMINOUS_ACTIVATE, SoundCategory.MASTER, 3f, 0.8f)
        location.world.playSound(location, Sound.BLOCK_TRIAL_SPAWNER_OMINOUS_ACTIVATE, SoundCategory.MASTER, 3f, 1.2f)

        for (p in location.getNearbyPlayers(32.0)) {
            if (p.uniqueId == player!!.uniqueId) continue
            val vec = location.clone().subtract(p.eyeLocation).toVector().normalize()
            val dot = vec.dot(p.location.direction.normalize())
            if (dot > 0.95) {
                val distance = p.eyeLocation.distance(location)
                if (distance > 32.0) continue
                val factor = (32.0 - distance) / 32.0
                p.addPotionEffect(
                    PotionEffect(
                        PotionEffectType.BLINDNESS, max(30, (200 * factor).roundToInt()), 0,
                        true, false, true
                    )
                )
                p.addPotionEffect(
                    PotionEffect(
                        PotionEffectType.DARKNESS, max(80, (600 * factor).roundToInt()), 0,
                        true, false, true
                    )
                )
            }
        }

        if (!player!!.isOnGround) {
            val velocity = player!!.velocity
            player!!.velocity = Vector(velocity.x, (velocity.y + 0.5).coerceAtLeast(0.2), velocity.z)
        }

        cooldown = COOLDOWN
    }

    override fun onDeath(event: PlayerDeathEvent) {
        haloUp?.remove()
        haloDown?.remove()
    }


    override fun executeCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>) {
        val abilities = (sender as? Player)?.getAbilities()
        if (abilities !is OneAngelZero) {
            sender.sendMessage(Component.text("=[").color(NamedTextColor.RED))
            return
        }

        if (args.size < 3) {
            sender.sendMessage(Component.text("Not enough arguments").color(NamedTextColor.RED))
            return
        }

        val speed = args[0].toFloatOrNull()
        if (speed == null) {
            sender.sendMessage(Component.text("<speed> should be a " +
                    "float number (3.14 as an example) not \"${args[1]}\"").color(NamedTextColor.RED))
            return
        }
        val color = try { Integer.parseInt(args[1], 16) } catch (e: NumberFormatException) {
            sender.sendMessage(Component.text("<colorHexadecimal> should be a " +
                    "hexadecimal number not \"${args[1]}\"").color(NamedTextColor.RED))
            return
        }
        var type: HaloTypes? = null
        for (thing in OneAngelZero.HaloTypes.entries) {
            if (thing.string == args[2]) {
                type = thing
                break
            }
        }
        if (type == null) {
            sender.sendMessage(Component.text("Invalid type of \"${args[2]}\"").color(NamedTextColor.RED))
            return
        }

        abilities.changeHalo(color, speed, type)
        sender.sendMessage(Component.text("Changed halo to: ").color(NamedTextColor.GREEN)
            .append { Component.text(type.string).color(TextColor.color(color)) })

        if (args.size >= 4) {
            if (args[3].lowercase() == "true") {
                abilities.shouldWiggle = true
                sender.sendMessage(
                    Component.text("Changed shouldWiggle to: ").color(NamedTextColor.GREEN)
                        .append { Component.text("true").color(NamedTextColor.LIGHT_PURPLE) }
                )
            } else if (args[3].lowercase() == "false") {
                abilities.shouldWiggle = false
                sender.sendMessage(
                    Component.text("Changed shouldWiggle to: ").color(NamedTextColor.GREEN)
                        .append { Component.text("false").color(NamedTextColor.LIGHT_PURPLE) }
                )
            } else Component.text("shouldWiggle can be only true of false not \"${args[3]}\"").color(NamedTextColor.RED)
        }
    }

    override fun completeCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String> {
        val abilities = (sender as? Player)?.getAbilities()
        if (abilities !is OneAngelZero) return listOf("ERROR_ILLEGAL_ACCESS")
        return when (args.size) {
            1 -> listOf("<speed>")
            2 -> listOf("<colorHexadecimal>")
            3 -> OneAngelZero.HaloTypes.entries.map { it.string }.filter { it.startsWith(args[2]) }
            4 -> listOf("true", "false")
            else -> listOf()
        }
    }

    override fun getAboutMe(): List<Component> = listOf(
        text("Твои умения:").color(NamedTextColor.LIGHT_PURPLE),
        text(""),
        text("- Твой урон от падения снижен вдвое").color(NamedTextColor.GREEN),
        text(""),
        text(
            "- Когда ты на шифте, и у тебя нет ничего в руках, и ты смотришь в низ, ты испускаешь яркую волну света которая может ослепить игроков рядом. " +
                    "Также эта волна света дает тебе скачок вверх если ты в воздухе. Также этот скачок всегда дает импульс вверх достаточный чтобы не разбиться.").color(NamedTextColor.GREEN),
        text(""),
        text("- У тебя есть нимб который ты можешь настраивать через /abilka.").color(NamedTextColor.YELLOW),
        text("  - Хоть ты можешь его настроить, ты не можешь его скрыть.").color(NamedTextColor.RED),
    )

    enum class HaloTypes(val string: String, val char: String) {
        SQUARE("square", "\uE550"),
        SMOOTH_SQUARE("smooth_square", "\uE551"),
        CIRCLE("circle", "\uE552"),
        CROSS("cross", "\uE553"),
        VORTEX_LARGER("vortex_larger", "\uE554"),
        VORTEX("vortex", "\uE555"),
    }
}

