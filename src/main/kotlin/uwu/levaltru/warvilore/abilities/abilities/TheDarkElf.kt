package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.damage.DamageType
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.abilities.interfaces.EvilAurable
import uwu.levaltru.warvilore.trashcan.Namespaces
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private const val DARKNESS_TIME = 20 * 25
private const val BLIDNESS_TIME = 20 * 5

private const val BUFF_DURATION = 20 * 45

private const val MAX_MANA = 10000

private const val BOOST_COST = 7000

private const val MANA_TO_DAMAGE_CONVERSION = 100

class TheDarkElf(nickname: String) : AbilitiesCore(nickname), EvilAurable {

    var ghostState: Int = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.GHOST_STATE.namespace,
                PersistentDataType.INTEGER
            ) ?: 0
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.GHOST_STATE.namespace,
                PersistentDataType.INTEGER, value
            )
            field = value
        }

    var isShieldActive = false

    var mana: Int = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.MANA.namespace,
                PersistentDataType.INTEGER
            ) ?: MAX_MANA
            return field
        }
        set(value) {
            val vale = if (value <= 0) {
                isShieldActive = false
                player!!.world.spawnParticle(
                    Particle.SCULK_SOUL, player!!.location.add(0.0, player!!.height / 2, 0.0), 50,
                    .3, .4, .3, 0.2
                )
                player!!.world.playSound(player!!, Sound.ENTITY_WARDEN_ATTACK_IMPACT, 1.5f, 0.5f)
                0
            } else value.coerceAtMost(MAX_MANA)
            player?.persistentDataContainer?.set(
                Namespaces.MANA.namespace,
                PersistentDataType.INTEGER, vale
            )
            field = vale
        }

    var ghostDisplay: ItemDisplay? = null

    override fun onJoin(event: PlayerJoinEvent) {
        player!!.getAttribute(Attribute.GENERIC_SCALE)!!.addModifier(
            AttributeModifier(
                Warvilore.namespace("temp_shortcurse"),
                -0.17,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
            )
        )
    }

    override fun onTick(event: ServerTickEndEvent) {
        val player = player ?: return
        if (isShieldActive) {
            mana--
            val box = player.boundingBox
            val x = box.centerX
            val z = box.centerZ
            val dx = box.widthX
            val dz = box.widthZ

            val nextDouble = random.nextDouble(0.0, PI * 2)
            val sin = sin(nextDouble) * dx * 1.3
            val cos = cos(nextDouble) * dz * 1.3
            val vSin = sin(nextDouble - PI / 3 * 2)
            val vCos = cos(nextDouble - PI / 3 * 2)
            val pV = player.velocity

            player.world.spawnParticle(
                Particle.SCULK_SOUL,
                x + sin,
                random.nextDouble(box.minY, box.maxY),
                z + cos,
                0,
                vSin * 0.03 + pV.x,
                pV.y,
                vCos * 0.03 + pV.z,
                1.0,
                null,
                false
            )

        } else mana++
        if (abilitiesDisabled) return
        if (player.ticksLived % 3 == 0) {
            if (ghostState == GhostStates.SHOWN.ordinal && !player.isDead) {
                val spiritLoc = getSpiritLoc()
                if (ghostDisplay?.isValid != true) {
                    summonGhostDisplay(spiritLoc)
                } else {
                    val ghostDisplay = ghostDisplay!!
                    if (ghostDisplay.world != player.world) summonGhostDisplay(spiritLoc)
                    spiritLoc.direction = ghostDisplay.location.subtract(spiritLoc).toVector().multiply(-1)
                    ghostDisplay.teleport(spiritLoc)
                    if (random.nextInt(3) == 0) {
                        player.world.spawnParticle(
                            Particle.TRIAL_SPAWNER_DETECTION_OMINOUS,
                            ghostDisplay.location,
                            1,
                            .2,
                            .2,
                            .2,
                            .05,
                            null,
                            false
                        )
                    }
                }
            } else ghostDisplay?.remove()
        }
        if (player.ticksLived % 20 == 0) {
            if (ghostState == GhostStates.PARTICLES.ordinal) {
                player.world.spawnParticle(
                    Particle.TRIAL_SPAWNER_DETECTION_OMINOUS,
                    getSpiritLoc(),
                    1,
                    .2,
                    .2,
                    .2,
                    .05,
                    null,
                    false
                )
            }
        }

        if (player.location.block.lightFromBlocks < 2 &&
            player.location.block.lightFromSky < 2 &&
            player.location.block.type.isAir
        ) player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 15, 1, true, false, true))
    }

    private fun summonGhostDisplay(spiritLoc: Location) {
        ghostDisplay?.remove()
        ghostDisplay =
            player!!.world.spawn(spiritLoc, ItemDisplay::class.java) {
                it.setItemStack(ItemStack(Material.CHAIN_COMMAND_BLOCK).apply {
                    val itemMeta = this.itemMeta
                    itemMeta.setCustomModelData(9)
                    this.itemMeta = itemMeta
                })
                it.teleportDuration = 5
                it.brightness = Display.Brightness(15, 15)
                it.persistentDataContainer.set(Namespaces.SHOULD_DESPAWN.namespace, PersistentDataType.BOOLEAN, true)
            }
    }

    private fun getSpiritLoc(): Location {
        val doubleAge = player!!.ticksLived.toDouble()
        val location = player!!.location
        location.add(
            sin(doubleAge * 0.01) * 0.7,
            sin(doubleAge * 0.018) * 0.15 + player!!.height / 2,
            cos(doubleAge * 0.012) * 0.7
        )
        return location
    }

    override fun onAttack(event: EntityDamageByEntityEvent) {
        if (abilitiesDisabled) return
        if (event.damageSource.damageType == DamageType.ARROW) {
            (event.entity as? LivingEntity)?.addPotionEffect(
                PotionEffect(
                    PotionEffectType.DARKNESS,
                    DARKNESS_TIME,
                    0,
                    false,
                    true,
                    true
                )
            )
            (event.entity as? LivingEntity)?.addPotionEffect(
                PotionEffect(
                    PotionEffectType.BLINDNESS,
                    BLIDNESS_TIME,
                    0,
                    false,
                    true,
                    true
                )
            )
        }
    }

    override fun onDamage(event: EntityDamageEvent) {
        if (isShieldActive)
            when (event.cause) {
                EntityDamageEvent.DamageCause.MAGIC, EntityDamageEvent.DamageCause.WITHER,
                EntityDamageEvent.DamageCause.PROJECTILE,
                EntityDamageEvent.DamageCause.ENTITY_ATTACK, EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK -> {
                    mana -= (event.damage * MANA_TO_DAMAGE_CONVERSION).toInt()
                    player!!.noDamageTicks = player!!.maximumNoDamageTicks / 3 * 2
                    if (mana > 0) {
                        blockEffects()
                        event.isCancelled = true
                    }
                }
                else -> {}
            }
    }

    override fun onPotionGain(event: EntityPotionEffectEvent) {
        if (!isShieldActive) return
        if (event.action == EntityPotionEffectEvent.Action.ADDED) {
            when (event.cause) {
                EntityPotionEffectEvent.Cause.ATTACK, EntityPotionEffectEvent.Cause.POTION_SPLASH, EntityPotionEffectEvent.Cause.AREA_EFFECT_CLOUD -> {
                    blockEffects()
                    event.isCancelled = true
                }

                else -> {}
            }
        }
    }

    override fun onDeath(event: PlayerDeathEvent) {
        mana = 1
        isShieldActive = false
    }

    override fun onLeave(event: PlayerQuitEvent) {
        ghostDisplay?.remove()
    }

    private fun blockEffects() {
        player!!.world.playSound(player!!, Sound.BLOCK_CONDUIT_ATTACK_TARGET, 1f, 1.5f)
        player!!.world.spawnParticle(
            Particle.SCULK_SOUL, player!!.location.add(0.0, player!!.height / 2, 0.0),
            15, .3, .4, .3, 0.1
        )
    }

    override fun getAboutMe(): List<Component> = listOf(
        text("Твои умения:").color(NamedTextColor.GREEN),
        text(""),
        text("- Ты становишься невидимым в темноте (но не при луне).").color(NamedTextColor.GREEN),
        text(""),
        text("- Твои стрелы дают эффект тьмы.").color(NamedTextColor.GREEN),
        text(""),
        text("- shortь.").color(NamedTextColor.GOLD),
        text(""),
        text("- У тебя есть призрак компаньон!").color(NamedTextColor.LIGHT_PURPLE),
        text("  - Ты можешь показать его через /abilka ghost_state SHOWN.").color(NamedTextColor.GOLD),
        text("  - Он может защитить тебя через /abilka invoke barrier.").color(NamedTextColor.GREEN),
        text("  - Он может наделить тебя и других рядом стоящих игроков энергией на ${ BUFF_DURATION / 20 } секунд (через /abilka invoke buff).").color(NamedTextColor.GREEN),
        text("  - Ману призрака можно проверить через /abilka mana.").color(NamedTextColor.GOLD),
    )

    override fun getEvilAura(): Double = 5.0

    override fun completeCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String>? {
        val temp = when (args.size) {
            1 -> listOf("ghost_state", "invoke", "mana")
            2 ->
                if (args[0].lowercase() == "ghost_state") GhostStates.entries.map { it.toString() }
                else if (args[0].lowercase() == "invoke") listOf("barrier", "buff")
                else listOf()

            else -> emptyList()
        }
        return temp.filter { it.lowercase().startsWith(args[args.lastIndex]) }
    }

    override fun executeCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>) {
        val player = player ?: return
        if (args.isEmpty()) {
            sender.sendMessage(text("Недостаточно аргументов").color(NamedTextColor.RED))
            return
        }
        val a1 = args[0].lowercase()

        when (a1) {
            "mana" -> {
                sender.sendActionBar(
                    text("${mana / 100}").color(NamedTextColor.LIGHT_PURPLE)
                        .append(text("%").color(NamedTextColor.DARK_PURPLE))
                )
                player.playSound(player, Sound.BLOCK_DECORATED_POT_INSERT_FAIL, 1f, 1f)
            }

            "ghost_state" -> {
                if (args.size < 2) {
                    sender.sendMessage(text("Недостаточно аргументов").color(NamedTextColor.RED))
                    return
                }
                val a2 = args[1]
                try {
                    val valueOf = GhostStates.valueOf(a2)
                    if (GhostStates.entries[ghostState] == GhostStates.SHOWN || valueOf == GhostStates.SHOWN) {
                        val spiritLoc = getSpiritLoc()
                        spiritLoc.world.spawnParticle(
                            Particle.TRIAL_SPAWNER_DETECTION_OMINOUS,
                            spiritLoc,
                            25,
                            .2,
                            .2,
                            .2,
                            0.05,
                            null,
                            false
                        )
                        spiritLoc.world.playSound(spiritLoc, Sound.BLOCK_TRIAL_SPAWNER_SPAWN_ITEM, 1f, .5f)
                    }
                    ghostState = valueOf.ordinal
                    sender.sendMessage(text("Сменил режим призрака на \"$a2\"").color(NamedTextColor.GREEN))
                } catch (e: IllegalArgumentException) {
                    sender.sendMessage(text("Неверные аргумент \"$a2\"").color(NamedTextColor.RED))
                }
            }

            "invoke" -> {
                if (args.size < 2) {
                    sender.sendMessage(text("Недостаточно аргументов").color(NamedTextColor.RED))
                    return
                }
                val a2 = args[1].lowercase()
                when (a2) {
                    "barrier" -> {
                        isShieldActive = !isShieldActive
                    }

                    "buff" -> {
                        if (mana < BOOST_COST) {
                            sender.sendActionBar(text("${(BOOST_COST - mana) / 20}s").color(NamedTextColor.RED))
                            player.playSound(player, Sound.BLOCK_DECORATED_POT_INSERT_FAIL, 1f, 1f)
                            return
                        }
                        mana -= BOOST_COST
                        val world = player.world
                        world.playSound(player.location, Sound.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM, 2f, .5f)
                        player.location.getNearbyPlayers(8.0).forEach {
                            world.spawnParticle(
                                Particle.SCULK_SOUL,
                                it.location.add(0.0, it.height / 2, 0.0),
                                10,
                                .3,
                                .4,
                                .3,
                                .1
                            )
                            it.addPotionEffects(
                                listOf(
                                    PotionEffect(PotionEffectType.REGENERATION, BUFF_DURATION, 1, true, true, true),
                                    PotionEffect(PotionEffectType.STRENGTH, BUFF_DURATION, 0, true, true, true),
                                    PotionEffect(PotionEffectType.RESISTANCE, BUFF_DURATION, 0, true, true, true),
                                    PotionEffect(PotionEffectType.SPEED, BUFF_DURATION, 2, true, true, true),
                                    PotionEffect(PotionEffectType.JUMP_BOOST, BUFF_DURATION, 1, true, true, true),
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    enum class GhostStates { HIDDEN, PARTICLES, SHOWN }
}