package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.*
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.trashcan.LevsUtils
import uwu.levaltru.warvilore.trashcan.Namespaces

private const val COOLDOWN: Int = 20 * 80
private const val DURATION: Int = 20 * 20
private const val REGUIRED_PLAYER_AMOUNT = 3

open class Mafiosy(nickname: String) : AbilitiesCore(nickname) {
    //    override fun getEvilAura(): Double {
//        if (isMafiInvisible()) return 16.0
//        return 4.0
//    }

    var cooldown: Int = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.INVISIBILITY_COUNTER.namespace,
                PersistentDataType.INTEGER
            ) ?: 0
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.INVISIBILITY_COUNTER.namespace,
                PersistentDataType.INTEGER, value
            )
            field = value
        }

    override fun onAction(event: PlayerInteractEvent) {
        invisibleFun(event)
    }

    fun invisibleFun(event: PlayerInteractEvent): Boolean {
        if (event.action.name != "RIGHT_CLICK_AIR") return false
        if (!LevsUtils.isSword(player!!.inventory.itemInMainHand.type)) return false
        if (cooldown > 0 && player!!.gameMode != GameMode.CREATIVE) {
            player!!.sendActionBar(Component.text("${cooldown / 20}s").color(NamedTextColor.RED))
            player!!.playSound(player!!.location, Sound.BLOCK_DECORATED_POT_INSERT_FAIL, SoundCategory.MASTER, 1f, 1f)
            return false
        }
        cooldown = COOLDOWN
        effects()
        player!!.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, DURATION, 1, true, false, true))
        Bukkit.getScheduler().runTaskLater(Warvilore.instance, Runnable {
            effects()
        }, (DURATION - 1).toLong())
        return true
    }

    private fun effects() {
        val add = player!!.location.clone().add(0.0, player!!.height / 2, 0.0)
        player!!.world.playSound(add, Sound.ENTITY_WITHER_SHOOT, SoundCategory.PLAYERS, 0.10f, 0.1f)
        player!!.world.spawnParticle(
            Particle.CAMPFIRE_COSY_SMOKE, add,
            25, 0.1, 0.3, 0.1, 0.01, null, true
        )
    }

    override fun onTick(event: ServerTickEndEvent) {
        if (player!!.ticksLived % 100 == 0) {
            var ye = 0
            for (pla in player!!.location.getNearbyPlayers(8.0)) {
                if (pla.location.distanceSquared(player!!.location) < 64.0) {
                    if (pla.getAbilities() is Mafiosy) {
                        ye++
                        if (ye < REGUIRED_PLAYER_AMOUNT) continue
                        player!!.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, 110, 0, true, false, true))
                        break
                    }
                }
            }
        }
        if (cooldown > 0) cooldown--
    }

    fun isMafiInvisible(): Boolean {
        return ((player?.activePotionEffects
            ?.filter { it.type == PotionEffectType.INVISIBILITY }
            ?.maxOfOrNull { it.amplifier }) ?: -1) > 0
    }

    override fun getAboutMe(): List<Component> = listOf(
        text("Твои навыки:").color(NamedTextColor.GREEN),
        text("- При нажатии на любой меч, ты уходишь в ").color(NamedTextColor.GREEN)
            .append { text("true ").style(Style.style(TextDecoration.ITALIC, NamedTextColor.GREEN)) }
            .append { text("невидимость на ${DURATION / 20} секунд.").color(NamedTextColor.GREEN) },
        text("  - Перезаряжается ${COOLDOWN / 20} секунд.").color(NamedTextColor.GOLD)
    )
}