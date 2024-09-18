package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import io.papermc.paper.tag.EntityTags
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.meta.Damageable
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.trashcan.LevsUtils
import uwu.levaltru.warvilore.trashcan.Namespaces
import java.util.*
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.max

private const val MAX_CHARGES = 3
private const val DASH_DURATION = 5
private const val DAMAGE_TICKS_DURATION = 10
private const val COOLDOWN_DURATION = 8
private const val REFILL_TIME = 20 * 10

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

class DashingSamurai(nickname: String) : AbilitiesCore(nickname) {

    var charges: Int = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.CHARGES.namespace,
                PersistentDataType.INTEGER
            ) ?: MAX_CHARGES
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.CHARGES.namespace,
                PersistentDataType.INTEGER, value
            )
            field = value
        }
    var refill: Int = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.REFILL.namespace,
                PersistentDataType.INTEGER
            ) ?: MAX_CHARGES
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.REFILL.namespace,
                PersistentDataType.INTEGER, value
            )
            field = value
        }

    var dashTicks: Int = 0
    var damageTicks: Int = 0
    var cooldown: Int = 0
    var direction: Vector = Vector()
    val alreadyHit = mutableListOf<UUID>()

    override fun onTick(event: ServerTickEndEvent) {
        if (abilitiesDisabled) return
        if (cooldown > 0) cooldown--

        if ((LevsUtils.isSword(player!!.inventory.itemInMainHand.type) &&
                    refill * (SPHERE_CHARS.size - 1) % REFILL_TIME <= SPHERE_CHARS.size - 1
                    && charges != MAX_CHARGES)
            || refill == 0
        )
            sendActionBar()

        if (charges < MAX_CHARGES || refill == 0) {
            refill++
            if (refill > REFILL_TIME) {
                refill = 0
                charges++
            }
        }

        if (damageTicks > 0) {
            damageTicks--
            val damageSource =
                DamageSource.builder(DamageType.PLAYER_ATTACK).withDirectEntity(player!!).withCausingEntity(player!!)
                    .build()
            val item = player!!.inventory.itemInMainHand
            val itemMeta = item.itemMeta

            val damage = (player!!.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.value
                ?: 0.0) + 3.5 + (itemMeta.enchants[Enchantment.SHARPNESS] ?: 0)

            val hitBox = player!!.boundingBox.expand(1.0)

            for (x in floor(hitBox.minX).toInt()..ceil(hitBox.maxX).toInt())
                for (y in floor(hitBox.minY).toInt()..ceil(hitBox.maxY).toInt())
                    for (z in floor(hitBox.minZ).toInt()..ceil(hitBox.maxZ).toInt()) {
                        val block = player!!.world.getBlockAt(x, y, z)
                        when (block.type) {
                            Material.BAMBOO, Material.TALL_GRASS, Material.SHORT_GRASS, Material.SEAGRASS, Material.TALL_SEAGRASS, Material.COBWEB -> {
                                block.breakNaturally(item, true)
                                item.damage(1, player!!)
                            }

                            else -> {}
                        }
                    }

            alreadyHit.add(player!!.uniqueId)
            for (entity in player!!.location.getNearbyLivingEntities(16.0)) {
                if (alreadyHit.contains(entity.uniqueId)) continue
                if (entity.isDead) continue
                if (!hitBox.overlaps(entity.boundingBox)) continue

                var flexiDamage = damage
                if (EntityTags.UNDEADS.values.contains(entity.type)) flexiDamage += itemMeta.getEnchantLevel(Enchantment.SMITE) * 2.5
                if (entity.type == EntityType.SPIDER || entity.type == EntityType.CAVE_SPIDER) flexiDamage += itemMeta.getEnchantLevel(
                    Enchantment.BANE_OF_ARTHROPODS
                ) * 2.5
                entity.fireTicks = (entity.fireTicks + (itemMeta.enchants[Enchantment.FIRE_ASPECT]
                    ?: 0) * 80).coerceAtMost(max(entity.fireTicks, 160))

                val boundingBox = entity.boundingBox
                val xz = (boundingBox.maxX - boundingBox.minX) / 3
                val y = (boundingBox.maxY - boundingBox.minY) / 3
                entity.location.add(0.0, entity.height / 2, 0.0).let {
                    entity.world.spawnParticle(Particle.CRIT, it, 25, xz, y, xz, 0.3, null, true)
                    entity.world.spawnParticle(Particle.SWEEP_ATTACK, it, 1, .2, .2, .2, 0.3, null, true)
                }
                entity.world.playSound(entity, Sound.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.MASTER, 1f, 1f)

                entity.damage(damage, damageSource)
                player!!.damageItemStack(EquipmentSlot.HAND, 1)
                alreadyHit.add(entity.uniqueId)
            }


            val locy = player!!.location.add(0.0, player!!.height / 2, 0.0)
            player!!.world.spawnParticle(
                Particle.DUST,
                locy,
                5,
                .2,
                .4,
                .2,
                0.0,
                Particle.DustOptions(Color.RED, 2f),
                true
            )
            player!!.fallDistance = 0f
        } else alreadyHit.clear()

        if (dashTicks > 0) {
            dashTicks--
            player!!.velocity = direction
        }
    }

    private fun sendActionBar() {
        player!!.sendActionBar(
            text(
                if (charges != MAX_CHARGES) {
                    ("${SPHERE_CHARS.last()} ".repeat(charges) +
                            "${SPHERE_CHARS[refill * (SPHERE_CHARS.size - 1) / REFILL_TIME]} " +
                            "${SPHERE_CHARS.first()} ".repeat((MAX_CHARGES - charges - 1).coerceAtLeast(0)))
                        .removeSuffix(" ")
                } else "${SPHERE_CHARS.last()} ${SPHERE_CHARS.last()} ${SPHERE_CHARS.last()}"
            ).color(NamedTextColor.GOLD)
        )
    }

    override fun onAction(event: PlayerInteractEvent) {
        if (abilitiesDisabled) return
        if (!event.action.isRightClick) return
        if (!LevsUtils.isSword(player!!.inventory.itemInMainHand.type)) return
        val location = player!!.location
        if (cooldown > 0) return
        if (charges <= 0) {
            player!!.playSound(location, Sound.BLOCK_DECORATED_POT_INSERT_FAIL, SoundCategory.MASTER, 1f, 1f)
            return
        }

        location.world.playSound(location, Sound.BLOCK_TRIAL_SPAWNER_SPAWN_ITEM, SoundCategory.MASTER, 1.5f, 0.5f)
        location.world.playSound(location, Sound.BLOCK_TRIAL_SPAWNER_OMINOUS_ACTIVATE, SoundCategory.MASTER, 1.5f, 2f)

        dashTicks = DASH_DURATION
        damageTicks = DAMAGE_TICKS_DURATION
        direction = location.direction.multiply(1).add(player!!.velocity.multiply(0.33))
        if (player!!.gameMode != GameMode.CREATIVE) {
            cooldown = COOLDOWN_DURATION
            charges--
            sendActionBar()
        }
    }

    override fun onDamage(event: EntityDamageEvent) {
        event.isCancelled = event.damageSource.damageType == DamageType.MOB_ATTACK && damageTicks > 0
    }

    override fun onDeath(event: PlayerDeathEvent) {
        dashTicks = 0
        damageTicks = 0
        cooldown = 0
        charges = MAX_CHARGES
        refill = 0
    }

    override fun getAboutMe(): List<Component> = listOf(
        text("Твои навыки:").color(NamedTextColor.GREEN),
        text("").color(NamedTextColor.GREEN),
        text("- У тебя есть рывок, чтобы его активировать тебе надо нажать правой кнопкой мыши по любому мечу.").color(
            NamedTextColor.GREEN
        ),
        text("  - Рывок наносит урон всем сущностям рядом, урон зависит от урона предмета в руке.").color(NamedTextColor.GREEN),
        text("  - Когда ты находишься в рывке, ты не получаешь урона от падения.").color(NamedTextColor.GREEN),
        text("  - У тебя $MAX_CHARGES заряда рывка.").color(NamedTextColor.GOLD),
        text("  - Один заряд перезаряжается ${REFILL_TIME / 20} секунд.").color(NamedTextColor.GOLD),
        text("  - Вектор движения влияет на расстояние рывка (прыгай перед использованием для большей эффективности).").color(
            NamedTextColor.DARK_GREEN
        ),
    )
}