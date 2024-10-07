package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.damage.DamageType
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.DeveloperMode
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.trashcan.Namespaces
import kotlin.math.ceil
import kotlin.math.floor

private const val MAX_COOLDOWN = 15

class Pekka(nickname: String) : AbilitiesCore(nickname) {

    var actionCooldown = 0
    var isLarge: Boolean = false
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.IS_LARGE.namespace,
                PersistentDataType.BOOLEAN
            ) ?: false
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.IS_LARGE.namespace,
                PersistentDataType.BOOLEAN, value
            )
            field = value
        }

    override fun onDeath(event: PlayerDeathEvent) {
        isLarge = false
    }

    override fun onTick(event: ServerTickEndEvent) {
        if (actionCooldown > 0) actionCooldown--
    }

    fun updateAttributes() {
        val attackSpeed = player!!.getAttribute(Attribute.GENERIC_ATTACK_SPEED)!!
        val attackDamage = player!!.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!
        val mining = player!!.getAttribute(Attribute.PLAYER_BLOCK_BREAK_SPEED)!!
        val stepHeight = player!!.getAttribute(Attribute.GENERIC_STEP_HEIGHT)!!
        val jumpStrength = player!!.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)!!
        val safeFallDistance = player!!.getAttribute(Attribute.GENERIC_SAFE_FALL_DISTANCE)!!
        val speed = player!!.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)!!
        val size = player!!.getAttribute(Attribute.GENERIC_SCALE)!!
        val maxHealth = player!!.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!
        val entityReach = player!!.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE)!!
        val blockReach = player!!.getAttribute(Attribute.PLAYER_BLOCK_INTERACTION_RANGE)!!

        val healthProsent = player!!.health / maxHealth.value
        val namespace = Warvilore.namespace("temp_pekkaboost")

        attackSpeed.removeModifier(namespace)
        attackDamage.removeModifier(namespace)
        mining.removeModifier(namespace)
        stepHeight.removeModifier(namespace)
        jumpStrength.removeModifier(namespace)
        safeFallDistance.removeModifier(namespace)
        speed.removeModifier(namespace)
        size.removeModifier(namespace)
        maxHealth.removeModifier(namespace)
        entityReach.removeModifier(namespace)
        blockReach.removeModifier(namespace)

        if (isLarge) {
            attackSpeed.addModifier(AttributeModifier(namespace, -0.66, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
            attackDamage.addModifier(AttributeModifier(namespace, 0.7, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
            mining.addModifier(AttributeModifier(namespace, 1.0, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
            stepHeight.addModifier(AttributeModifier(namespace, 0.85, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
            jumpStrength.addModifier(AttributeModifier(namespace, 0.4, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
            safeFallDistance.addModifier(
                AttributeModifier(
                    namespace,
                    1.25,
                    AttributeModifier.Operation.MULTIPLY_SCALAR_1
                )
            )
            speed.addModifier(AttributeModifier(namespace, -0.33, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
            size.addModifier(AttributeModifier(namespace, 0.5, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
            maxHealth.addModifier(AttributeModifier(namespace, 0.5, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
            entityReach.addModifier(AttributeModifier(namespace, 0.7, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
            blockReach.addModifier(AttributeModifier(namespace, 0.7, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
        } else {
            attackSpeed.addModifier(AttributeModifier(namespace, -0.33, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
            attackDamage.addModifier(AttributeModifier(namespace, 0.33, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
            speed.addModifier(AttributeModifier(namespace, 0.33, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
            size.addModifier(AttributeModifier(namespace, -0.20, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
        }

        player!!.health = healthProsent * maxHealth.value
    }

    override fun onJoin(event: PlayerJoinEvent) {
        Bukkit.getScheduler().runTask(Warvilore.instance, Runnable { updateAttributes() })
    }

    override fun onDamage(event: EntityDamageEvent) {
        when (event.damageSource.damageType) {
            DamageType.MAGIC, DamageType.INDIRECT_MAGIC, DamageType.WITHER, DamageType.DROWN -> event.isCancelled =
                true
        }
    }

    override fun onPotionGain(event: EntityPotionEffectEvent) {
        if (event.action == EntityPotionEffectEvent.Action.ADDED) {
            when (event.cause) {
                EntityPotionEffectEvent.Cause.ATTACK, EntityPotionEffectEvent.Cause.POTION_SPLASH -> when (event.newEffect?.type) {
                    PotionEffectType.WITHER, PotionEffectType.POISON, PotionEffectType.REGENERATION,
                    PotionEffectType.INSTANT_HEALTH, PotionEffectType.INSTANT_DAMAGE,
                    PotionEffectType.HUNGER, PotionEffectType.NAUSEA
                        -> event.isCancelled = true
                }

                else -> {}
            }
        }
    }

    override fun onHeal(event: EntityRegainHealthEvent) {
        when (event.regainReason) {
            EntityRegainHealthEvent.RegainReason.MAGIC, EntityRegainHealthEvent.RegainReason.MAGIC_REGEN -> event.isCancelled =
                true

            else -> {}
        }
    }

    override fun getAboutMe(): List<Component> = listOf(
        text("- Когда ты прописываешь /abilka toggle, ты переключаешься между большим и мелким состоянием.").color(
            NamedTextColor.GOLD
        ),
        text(""),
        text("  - Когда ты мелкий:").color(NamedTextColor.GOLD),
        text("    - Ты быстрее.").color(NamedTextColor.GREEN),
        text("    - У тебя больше урона.").color(NamedTextColor.GREEN),
        text("    - Ты мелкий.").color(NamedTextColor.GREEN),
        text("    - Но у тебя медленая атака.").color(NamedTextColor.RED),
        text(""),
        text("  - Когда ты большой:").color(NamedTextColor.GOLD),
        text("    - У тебя еще больше урона.").color(NamedTextColor.GREEN),
        text("    - Ты копаешь быстрее.").color(NamedTextColor.GREEN),
        text("    - Ты умешь использовать целые блоки как ступеньки.").color(NamedTextColor.GREEN),
        text("    - Ты прыгаешь выше.").color(NamedTextColor.GREEN),
        text("    - Ты получаешь чуть-чуть меньше урона от падения.").color(NamedTextColor.GREEN),
        text("    - У тебя больше здоровья.").color(NamedTextColor.GREEN),
        text("    - У тебя больше дистанция взаимодейсвия.").color(NamedTextColor.GREEN),
        text("    - Но ты большой.").color(NamedTextColor.RED),
        text("    - Атака еще медленней.").color(NamedTextColor.RED),
        text("    - Ты медленный.").color(NamedTextColor.RED),
        text(""),
        text("Ты ходячие доспехи, поэтому:").color(NamedTextColor.GOLD),
        text("  - Еще ты не восприимчив к типам урона органических существ (отравление, моментальный урон и иссушение).").color(
            NamedTextColor.GREEN
        ),
        text("  - Еще ты не восприимчив к типам регенерации органических существ (регенерация и исцеление).").color(
            NamedTextColor.RED
        ),
    )

    override fun executeCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>) {
        if (actionCooldown > 0) return
        val location = player!!.location
        player!!.world.playSound(
            player!!.location,
            if (isLarge) {
                isLarge = false
                Sound.BLOCK_BEACON_DEACTIVATE
            } else {
                isLarge = true
                Sound.BLOCK_BEACON_ACTIVATE
            }, 2f, .7f
        )
        player!!.world.spawnParticle(
            Particle.WITCH, player!!.location.add(0.0, player!!.height / 2, 0.0),
            300, .4, .7, .4, 1.0, null, true
        )
        actionCooldown = MAX_COOLDOWN
        updateAttributes()

        Bukkit.getScheduler().runTaskLater(Warvilore.instance, Runnable {
            val boundingBox = player!!.boundingBox

            for (x in floor(boundingBox.minX).toInt()..ceil(boundingBox.maxX).toInt())
                for (y in floor(boundingBox.minY).toInt()..ceil(boundingBox.maxY).toInt())
                    for (z in floor(boundingBox.minZ).toInt()..ceil(boundingBox.maxZ).toInt()) {
                        val shift = boundingBox.clone().shift(Vector(-x, -y, -z))
                        if (DeveloperMode) player!!.sendMessage(shift.toString())
                        val collisionShape = player!!.world.getBlockAt(x, y, z).collisionShape
                        if (DeveloperMode) player!!.sendMessage(collisionShape.boundingBoxes.toString())
                        if (collisionShape.overlaps(shift)) {
                            if (DeveloperMode) player!!.sendMessage("ye")
                            isLarge = !isLarge
                            updateAttributes()
                            player!!.teleport(location)
                            return@Runnable
                        }
                    }
        }, 2L)
    }

    override fun completeCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String>? = if (args.size == 1) listOf("toggle") else listOf()
}