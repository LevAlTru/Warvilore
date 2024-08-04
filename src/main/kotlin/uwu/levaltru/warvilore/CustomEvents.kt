package uwu.levaltru.warvilore

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import org.bukkit.Bukkit
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.entity.SpectralArrow
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.*
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.meta.Damageable
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.abilities.AbilitiesCore.Companion.getAbilities
import uwu.levaltru.warvilore.abilities.AbilitiesCore.Companion.hashMap
import uwu.levaltru.warvilore.abilities.abilities.BoilingAssasin
import uwu.levaltru.warvilore.abilities.abilities.Nekomancer
import uwu.levaltru.warvilore.abilities.interfaces.EvilAurable
import uwu.levaltru.warvilore.tickables.DeathSpirit
import uwu.levaltru.warvilore.trashcan.LevsUtils.getSoulBound
import uwu.levaltru.warvilore.trashcan.LevsUtils.isSoulBound

class CustomEvents : Listener {

    @EventHandler
    fun onTick(event: ServerTickEndEvent?) {
        Tickable.Tick()
        for (player in Bukkit.getOnlinePlayers()) {
            event?.let {
                player.getAbilities()?.let {
                    it.player = player
                    try {
                        it.onTick(event)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    @EventHandler
    fun onBlockBreakEvent(event: BlockBreakEvent) {
        event.player.getAbilities()?.onBlockBreak(event)
    }

    @EventHandler
    fun onBlockPlace(event: BlockPlaceEvent) {
        event.player.getAbilities()?.onBlockPlace(event)
    }

    @EventHandler
    fun onAttack(event: PrePlayerAttackEntityEvent) {
        event.player.getAbilities()?.onAttack(event)
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val entity = event.entity
        if (entity is Player) entity.getAbilities()?.onDamage(event)
    }

    @EventHandler
    fun onHeal(event: EntityRegainHealthEvent) {
        val entity = event.entity
        if (entity is Player) entity.getAbilities()?.onHeal(event)
    }

    @EventHandler
    fun onAction(event: PlayerInteractEvent) {
        val item = event.item
        val itemMeta = item?.itemMeta
        if (event.action.isLeftClick)
            if (itemMeta?.isSoulBound() == true) {
                if (itemMeta.getSoulBound() != event.player.name) {
//                    (itemMeta as? Damageable)?.damage = (itemMeta as? Damageable)?.maxDamage
                    return
                }
            }
        event.player.getAbilities()?.onAction(event)
    }

    @EventHandler
    fun onEating(event: PlayerItemConsumeEvent) {
        event.player.getAbilities()?.onEating(event)
    }

    @EventHandler
    fun onBowShooting(event: EntityShootBowEvent) {
        val entity = event.entity
        if (entity is Player) entity.getAbilities()?.onBowShooting(event)
    }

    @EventHandler
    fun onArrowSpawn(event: EntitySpawnEvent) {
        val arrow = event.entity
        if (arrow !is Arrow && arrow !is SpectralArrow) return
        val id = (arrow as AbstractArrow).ownerUniqueId ?: return
        val abilities = Bukkit.getPlayer(id)?.getAbilities()
        if (abilities is BoilingAssasin) if (abilities.abilitiesWork()) arrow.remove()
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player
        AbilitiesCore.loadAbility(player)
        val abilities = player.getAbilities()
        abilities?.player = player
        abilities?.onJoin(event)
    }

    @EventHandler
    fun onLeave(event: PlayerQuitEvent) {
        val player = event.player
        val abilities = player.getAbilities()
        abilities?.onLeave(event)
        AbilitiesCore.saveAbility(player)
        hashMap.remove(player.name)
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.player
        val abilities = player.getAbilities()
        abilities?.onDeath(event)
        if (abilities is Nekomancer) return
        DeathSpirit(player.location.add(0.0, player.height / 2, 0.0), (abilities is EvilAurable), player.name)
    }
}