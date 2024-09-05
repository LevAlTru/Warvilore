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
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.abilities.interfaces.EvilAurable
import uwu.levaltru.warvilore.trashcan.LevsUtils
import java.util.*
import kotlin.math.max

private const val DASH_DURATION = 5
private const val DAMAGE_TICKS_DURATION = 10
private const val COOLDOWN_DURATION = 7 * 20 + 10

class DashingSamurai(nickname: String) : AbilitiesCore(nickname), EvilAurable {

    var dashTicks: Int = 0
    var damageTicks: Int = 0
    var cooldown: Int = 0
    var direction: Vector = Vector()
    val alreadyHit = mutableListOf<UUID>()

    override fun onTick(event: ServerTickEndEvent) {
        if (cooldown > 0) {
            cooldown--
            if (LevsUtils.isSword(player!!.inventory.itemInMainHand.type)) {
                if (cooldown == 0) player!!.sendActionBar(text(""))
                else if (cooldown % 20 == 0 || cooldown == COOLDOWN_DURATION)
                    player!!.sendActionBar(Component.text("${cooldown / 20}s").color(NamedTextColor.RED))
            }
        }
        if (damageTicks > 0) {
            damageTicks--
            val damageSource = DamageSource.builder(DamageType.PLAYER_ATTACK).withDirectEntity(player!!).withCausingEntity(player!!).build()
            val item = player!!.inventory.itemInMainHand
            val itemMeta = item.itemMeta

            val damage = (player!!.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)?.value ?: 0.0) + 3.5 + (itemMeta.enchants[Enchantment.SHARPNESS] ?: 0)

            val hitBox = player!!.boundingBox.expand(1.0)
            alreadyHit.add(player!!.uniqueId)
            for (entity in player!!.location.getNearbyLivingEntities(16.0)) {
                if (alreadyHit.contains(entity.uniqueId)) continue
                if (entity.isDead) continue
                if (!hitBox.overlaps(entity.boundingBox)) continue

                var flexiDamage = damage
                if (EntityTags.UNDEADS.values.contains(entity.type)) flexiDamage += itemMeta.getEnchantLevel(Enchantment.SMITE) * 2.5
                if (entity.type == EntityType.SPIDER || entity.type == EntityType.CAVE_SPIDER) flexiDamage += itemMeta.getEnchantLevel(Enchantment.BANE_OF_ARTHROPODS) * 2.5
                entity.fireTicks = (entity.fireTicks + (itemMeta.enchants[Enchantment.FIRE_ASPECT] ?: 0) * 80).coerceAtMost(max(entity.fireTicks, 160))

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
            player!!.world.spawnParticle(Particle.DUST, locy, 5, .2, .4, .2, 0.0, Particle.DustOptions(Color.RED, 2f), true)
            player!!.fallDistance = 0f
        } else alreadyHit.clear()
        if (dashTicks <= 0) return
        dashTicks--

        player!!.velocity = direction
    }

    override fun onAction(event: PlayerInteractEvent) {
        if (!event.action.isRightClick) return
        if (!LevsUtils.isSword(player!!.inventory.itemInMainHand.type)) return
        val location = player!!.location
        if (cooldown > 0) {
            if (cooldown > COOLDOWN_DURATION - 5) return
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
            player!!.sendActionBar(Component.text("${cooldown / 20}s").color(NamedTextColor.RED))
        }
    }

    override fun onDamage(event: EntityDamageEvent) {
        event.isCancelled = event.damageSource.damageType == DamageType.MOB_ATTACK && damageTicks > 0
    }

    override fun onDeath(event: PlayerDeathEvent) {
        dashTicks = 0
        damageTicks = 0
        cooldown = 10
    }

    override fun getEvilAura(): Double = 6.0

    override fun getAboutMe(): List<Component> = listOf(
        text("Твои навыки:").color(NamedTextColor.GREEN),
        text("").color(NamedTextColor.GREEN),
        text("- У тебя есть рывок, чтобы его активировать тебе надо нажать правой кнопкой мыши по любому мечу.").color(NamedTextColor.GREEN),
        text("  - Рывок наносит урон всем сущностям рядом, урон зависит от урона предмета в руке.").color(NamedTextColor.GREEN),
        text("  - Когда ты находишься в рывке, ты не получаешь урона от падения.").color(NamedTextColor.GREEN),
        text("  - Рывок перезаряжается ${COOLDOWN_DURATION / 20} секунд.").color(NamedTextColor.GOLD),
        text("  - Вектор движения влияет на расстояние рывка (прыгай перед использованием для большей эффективности).").color(NamedTextColor.DARK_GREEN),
    )
}