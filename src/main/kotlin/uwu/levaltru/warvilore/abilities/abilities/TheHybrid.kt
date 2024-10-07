package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityPotionEffectEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.trashcan.LevsUtils
import uwu.levaltru.warvilore.trashcan.Namespaces
import java.util.UUID
import kotlin.math.roundToInt

private const val MAX_HEAR_DISTANCE = 16.0
private const val MAX_BLAST_POWER = 3.0
private const val BLAST_ITERATIONS = 15
private const val DISTANCE_PER_ITERATION = 10.0
private const val DISTANCE_BEETWEEN_PARTICLES = 0.2

private const val ACTION_COOLDOWN = 10
private const val BLAST_COOLDOWN_SMOLL = 20 * 3
private const val BLAST_COOLDOWN = 20 * 60
private const val REGEN_BOOST_COOLDOWN = 20 * 60 * 15
private const val REQUIRED_STANDING_TIME = 20 * 8

private const val DAMAGE_TO_FOOD_CONVERSION = 0.33

private val SPHERE_CHARS = listOf(
    "\uE560",
    "\uE561",
    "\uE562",
    "\uE563",
    "\uE564",
    "\uE565",
    "\uE566",
    "\uE567",
    "\uE568",
    "\uE569",
    "\uE56A",
    "\uE56B",
    "\uE56C",
    "\uE56D",
)

class TheHybrid(n: String) : AbilitiesCore(n) {

    var blastCooldown: Int = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.BLAST_COOLDOWN.namespace,
                PersistentDataType.INTEGER
            ) ?: BLAST_COOLDOWN
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.BLAST_COOLDOWN.namespace,
                PersistentDataType.INTEGER, value
            )
            field = value
        }
    var regenBoostCooldown: Int = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.REGEN_BOOST_COOLDOWN.namespace,
                PersistentDataType.INTEGER
            ) ?: BLAST_COOLDOWN
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.REGEN_BOOST_COOLDOWN.namespace,
                PersistentDataType.INTEGER, value
            )
            field = value
        }

    var heartLocation: Location? = null
    var actionCooldown = 0
    var standStillTime = 0
    var standLoc: Location? = null

    override fun onTick(event: ServerTickEndEvent) {

        val player = player ?: return

        if (actionCooldown > 0) actionCooldown--
        if (blastCooldown > 0) blastCooldown--
        if (regenBoostCooldown > 0) regenBoostCooldown--

        val locy = player.location.add(0.0, player.height / 2, 0.0)


        if (standLoc != null && standLoc!!.world == player.world && player.location.distanceSquared(standLoc!!) < 0.01) {
            standStillTime++

            val progress = (standStillTime.toDouble() / REQUIRED_STANDING_TIME).coerceAtMost(1.0)
            val d = random.nextDouble() * 3 * progress
            for (_i_ in 0..<LevsUtils.roundToRandomInt(d)) {
                val dVec = LevsUtils.getRandomNormalizedVector().multiply(random.nextDouble(.7, 1.0))

                val add = locy.clone().add(dVec.clone().multiply(2.3 * progress))

                val vVec = dVec.clone().multiply(-1)
                player.world.spawnParticle(
                    Particle.DRAGON_BREATH,
                    add,
                    0,
                    vVec.x,
                    vVec.y,
                    vVec.z,
                    0.06 * progress,
                    null,
                    true
                )
            }

            if (standStillTime % 8 == 0)
                player.world.playSound(player, Sound.BLOCK_CONDUIT_AMBIENT_SHORT, (3.0 * progress).toFloat(), .7f)

            if (standStillTime < REQUIRED_STANDING_TIME)
                player.sendActionBar(
                    text(
                        SPHERE_CHARS[(progress * SPHERE_CHARS.size).toInt().coerceIn(0, SPHERE_CHARS.lastIndex)]
                    ).color(NamedTextColor.RED)
                )
            else if (standStillTime == REQUIRED_STANDING_TIME) player.sendActionBar(
                text(SPHERE_CHARS.last()).color(
                    NamedTextColor.GREEN
                )
            )

        } else {
            standStillTime = 0
            standLoc = null
        }

        if (abilitiesDisabled) {
            collapseHeart()
            return
        }

        val nnHeartLoc = heartLocation

        if (nnHeartLoc != null) {
            val distance = locy.distance(nnHeartLoc)

            if (player.ticksLived % 3 == 0) {
                player.sendActionBar(
                    text(
                        SPHERE_CHARS[(MAX_HEAR_DISTANCE - distance).roundToInt().coerceIn(0, SPHERE_CHARS.lastIndex)]
                    ).color(NamedTextColor.GOLD)
                )

                val box = player.boundingBox

                val loci = Vector(
                    random.nextDouble(box.minX, box.maxX),
                    random.nextDouble(box.minY, box.maxY),
                    random.nextDouble(box.minZ, box.maxZ),
                )

                val randomHeartLoc = nnHeartLoc.toVector().add(
                    Vector(
                        random.nextDouble(-.45, .45),
                        random.nextDouble(-.45, .45),
                        random.nextDouble(-.45, .45)
                    )
                )
                loci.subtract(randomHeartLoc)
                player.world.spawnParticle(Particle.WITCH, nnHeartLoc, 1, .7, .7, .7, 1.0, null, true)
                player.world.spawnParticle(
                    Particle.OMINOUS_SPAWNING,
                    randomHeartLoc.x,
                    randomHeartLoc.y,
                    randomHeartLoc.z,
                    0,
                    loci.x,
                    loci.y,
                    loci.z,
                    1.0,
                    null,
                    true
                )
            }

            if (player.ticksLived % 12 == 0)
                player.world.playSound(player, Sound.BLOCK_CONDUIT_AMBIENT_SHORT, 1.5f, .5f)

            if (player.ticksLived % 200 == 0) {
                player.saturation = (player.saturation - 1.5f).coerceAtLeast(0f)
                player.foodLevel = (player.foodLevel - 1).coerceAtLeast(0)
            }

            if (nnHeartLoc.block.type != Material.CRYING_OBSIDIAN) collapseHeart()
            for (offset in LevsUtils.neighboringBlocksLocs()) {
                if (!LevsUtils.isColoredGlass(nnHeartLoc.clone().add(offset).block.type)) {
                    collapseHeart()
                    break
                }
            }
            if (nnHeartLoc.world != player.world) collapseHeart()
            if (distance > MAX_HEAR_DISTANCE) collapseHeart()
            if (standStillTime > 0) collapseHeart()
        }
    }

    override fun onAction(event: PlayerInteractEvent) {
        if (actionCooldown > 0) return
        actionCooldown = ACTION_COOLDOWN

        if (player!!.isSneaking && player!!.inventory.itemInOffHand.isEmpty && player!!.inventory.itemInMainHand.isEmpty) {
            val clickedBlock = event.clickedBlock
            if (event.action.isRightClick && LevsUtils.isColoredGlass(clickedBlock?.type)) {
                for (offset in LevsUtils.neighboringBlocksLocs()) {
                    val add = clickedBlock!!.location.add(offset)
                    if (add.block.type == Material.CRYING_OBSIDIAN) {
                        for (offset2 in LevsUtils.neighboringBlocksLocs())
                            if (!LevsUtils.isColoredGlass(add.clone().add(offset2).block.type)) return

                        if (heartLocation != null) {
                            if (heartLocation!!.blockX == add.blockX &&
                                heartLocation!!.blockY == add.blockY &&
                                heartLocation!!.blockZ == add.blockZ
                            ) {
                                collapseHeart()
                                return
                            }
                            collapseHeart()
                        }

                        add.world.playSound(add, Sound.BLOCK_VAULT_ACTIVATE, 2.5f, .5f)
                        heartLocation = add.toCenterLocation()
                    }
                }
            } else if (event.action.isLeftClick && standStillTime >= REQUIRED_STANDING_TIME) {
                player!!.world.playSound(player!!, Sound.BLOCK_CONDUIT_DEACTIVATE, 4f, .5f)
                player!!.velocity = player!!.location.direction.multiply(-.8)
                blastCooldown = BLAST_COOLDOWN
                standStillTime = 0
                standLoc = null

                val collidedEntity = hashSetOf(player!!.uniqueId)
                for (i in 1..BLAST_ITERATIONS) {
                    val d = i * DISTANCE_PER_ITERATION
                    val rayTrace = player!!.world.rayTrace(
                        player!!.eyeLocation,
                        player!!.location.direction,
                        d,
                        FluidCollisionMode.NEVER,
                        true,
                        .33,
                        { !collidedEntity.contains(it.uniqueId) && it is LivingEntity }
                    )

                    val hitEntity = (rayTrace?.hitEntity as? LivingEntity)
                    if (hitEntity != null) {
                        hitEntity.health = (hitEntity.health - 8.0).coerceAtLeast(0.01)
                        val damageSource = DamageSource.builder(DamageType.EXPLOSION)
                            .withCausingEntity(player!!)
                            .withDirectEntity(player!!).build()
                        hitEntity.damage(5.0, damageSource)
                        collidedEntity.add(hitEntity.uniqueId)
                    }

                    val hitPosition = rayTrace?.hitPosition
                    if (hitPosition != null)
                        player!!.world.createExplosion(
                            hitPosition.x,
                            hitPosition.y,
                            hitPosition.z,
                            ((BLAST_ITERATIONS - i) * MAX_BLAST_POWER / BLAST_ITERATIONS).toFloat(),
                            false,
                            true,
                            player
                        )

                    if (i == BLAST_ITERATIONS) {
                        val subtract = hitPosition?.clone()?.subtract(player!!.eyeLocation.toVector())
                            ?: player!!.location.direction.multiply(BLAST_ITERATIONS * DISTANCE_PER_ITERATION)

                        val distance = player!!.eyeLocation.toVector().distance(subtract)
                        var d1 = 0.7
                        while (d1 < distance) {
                            val add = player!!.eyeLocation.add(subtract.normalize().multiply(d1))
                            player!!.world.spawnParticle(Particle.DRAGON_BREATH, add, 1, .0, .0, .0, .03, null, true)
                            if (random.nextDouble() < DISTANCE_BEETWEEN_PARTICLES / 2)
                                player!!.world.playSound(
                                    add,
                                    Sound.ENTITY_BREEZE_IDLE_GROUND,
                                    SoundCategory.MASTER,
                                    0.1f,
                                    2f
                                )
                            d1 += DISTANCE_BEETWEEN_PARTICLES
                        }
                    }
                }
            }
        }
    }

    override fun onDamage(event: EntityDamageEvent) {
        if (heartLocation != null) {
            when (event.damageSource.damageType) {
                DamageType.FALL,
                DamageType.OUT_OF_WORLD, DamageType.OUTSIDE_BORDER,
                DamageType.MAGIC, DamageType.WITHER -> {
                }

                else -> {
                    player!!.saturation =
                        (player!!.saturation - event.damage * (DAMAGE_TO_FOOD_CONVERSION * 1.5)).toFloat()
                            .coerceAtLeast(0f)
                    val i =
                        LevsUtils.roundToRandomInt(player!!.foodLevel - event.damage * DAMAGE_TO_FOOD_CONVERSION)
                    if (i > 0) player!!.foodLevel = i
                    else {
                        player!!.foodLevel = 0
                        player!!.saturation = 0f
                        player!!.world.spawnParticle(
                            Particle.DRAGON_BREATH, player!!.location.add(0.0, player!!.height / 2, 0.0), 100,
                            .3, .4, .3, 0.07
                        )
                        player!!.world.playSound(player!!, Sound.ENTITY_WARDEN_ATTACK_IMPACT, 1.5f, 0.5f)
                        collapseHeart()
                        return
                    }

                    player!!.world.playSound(player!!, Sound.BLOCK_CONDUIT_ATTACK_TARGET, 1f, 1.5f)
                    player!!.world.spawnParticle(
                        Particle.FALLING_OBSIDIAN_TEAR, player!!.location.add(0.0, player!!.height / 2, 0.0),
                        15, .3, .4, .3, 0.0
                    )
                    player!!.noDamageTicks = player!!.maximumNoDamageTicks / 3 * 2
                    event.isCancelled = true
                }
            }
        }
    }

    override fun onDeath(event: PlayerDeathEvent) {
        collapseHeart()
        if (regenBoostCooldown <= 0) {
            event.isCancelled = true
            regenBoost()
            player!!.saturation = 0f
            player!!.foodLevel = 0
            return
        }
    }

    private fun regenBoost() {
        player!!.health = player!!.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value / 2
        player!!.addPotionEffect(PotionEffect(PotionEffectType.REGENERATION, 200, 1, true, true, true))
        player!!.world.playSound(player!!, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 2f, .5f)
        player!!.world.spawnParticle(
            Particle.FALLING_OBSIDIAN_TEAR, player!!.location.add(0.0, player!!.height / 2, 0.0),
            200, .3, .4, .3, 0.0
        )
        regenBoostCooldown = REGEN_BOOST_COOLDOWN
    }

    override fun onHeal(event: EntityRegainHealthEvent) {
        event.isCancelled = heartLocation != null
    }

    override fun onPotionGain(event: EntityPotionEffectEvent) {
        if (heartLocation == null) return
        if (event.action == EntityPotionEffectEvent.Action.ADDED) {
            when (event.cause) {
                EntityPotionEffectEvent.Cause.ATTACK, EntityPotionEffectEvent.Cause.POTION_SPLASH -> event.isCancelled =
                    true

                else -> {}
            }
        }
    }

    private fun collapseHeart() {
        val nnHeartLoc = heartLocation ?: return

        nnHeartLoc.world.playSound(nnHeartLoc, Sound.BLOCK_VAULT_DEACTIVATE, 2.5f, .5f)
        nnHeartLoc.world.spawnParticle(Particle.FALLING_OBSIDIAN_TEAR, nnHeartLoc, 200, .7, .7, .7, 0.0)

        heartLocation = null
    }

    override fun getAboutMe(): List<Component> = listOf(
        text("Твои умения:").color(NamedTextColor.GREEN),
        text(""),
        text("- Безграничность. При постройке и активации своего сердца ты получаешь:").color(NamedTextColor.GREEN),
        text("  - Полную неуязвимость ко всем типам урона, кроме падения и эффектов отравления и иссушения.").color(NamedTextColor.GREEN),
        text("    - Но ты голодаешь. И голодаешь сильнее когда получаешь урон.").color(NamedTextColor.RED),
        text("  - Зелья брошеные на тебя со стороны, не накладывают на тебя эффекты.").color(NamedTextColor.GREEN),
        text("  - Но если ты отойдешь от сердца, его разрушут, или ты проголодаешься, то эффекты пропадут.").color(NamedTextColor.RED),
        text("  - Стройка: Плачущий обсидиан по центру и 6 цветного стекла вокруг.").color(NamedTextColor.GOLD),
        text("  - Активация: Присесть и нажать по стеклу.").color(NamedTextColor.GOLD),
        text(""),
        text("- Разрушение:").color(NamedTextColor.GREEN),
        text("  - При прописывании /abilka boom, ты начинаешь заряжать снаряд. Когда он полностью заряжен, ты можешь его запустить если будешь на шифте и нажмешь лкм.").color(NamedTextColor.GREEN),
        text(""),
        text("- При смерти, ты от неё спасаешься.").color(NamedTextColor.GREEN),
        text("  - Но ты можешь это делать только раз в ${REGEN_BOOST_COOLDOWN / 1200} минут.").color(NamedTextColor.RED),
        text("  - Когда будет следующий раз можно узнать with /abilka regen_boost_cooldown.").color(NamedTextColor.GOLD),
    )

    override fun executeCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>) {
        when (args[0].lowercase()) {
            "boom" -> {
                if (abilitiesDisabled) sender.sendMessage(text("Abilities are disabled"))
                if (standStillTime > 0) return
                if (blastCooldown > 0 && player!!.gameMode != GameMode.CREATIVE) {
                    sender.sendActionBar(text("${blastCooldown / 20}s").color(NamedTextColor.RED))
                    player!!.playSound(player!!, Sound.BLOCK_DECORATED_POT_INSERT_FAIL, 1f, 1f)
                    return
                }
                blastCooldown = BLAST_COOLDOWN_SMOLL

                standLoc = player!!.location.clone()
            }

            "regen_boost_cooldown" -> {
                sender.sendActionBar(text("${regenBoostCooldown / 20}s").color(if (regenBoostCooldown > 0) NamedTextColor.GOLD else NamedTextColor.GREEN))
                player!!.playSound(player!!, Sound.BLOCK_DECORATED_POT_INSERT_FAIL, 1f, 1f)
            }

            else -> sender.sendMessage(text("Invalid argument `${args[0]}`").color(NamedTextColor.RED))
        }
    }

    override fun completeCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String>? = if (args.size == 1) listOf("boom", "regen_boost_cooldown") else listOf()
}