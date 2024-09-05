package uwu.levaltru.warvilore

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

abstract class Actionable {
    open fun onTick(event: ServerTickEndEvent) {}
    open fun onBlockBreak(event: BlockBreakEvent) {}
    open fun onBlockPlace(event: BlockPlaceEvent) {}
    open fun onDamage(event: EntityDamageEvent) {}
    open fun onHeal(event: EntityRegainHealthEvent) {}
    open fun onAction(event: PlayerInteractEvent) {}
    open fun onEating(event: PlayerItemConsumeEvent) {}
    open fun onDeath(event: PlayerDeathEvent) {}
    open fun onBowShooting(event: EntityShootBowEvent) {}
    open fun onAttack(event: PrePlayerAttackEntityEvent) {}
    open fun onJoin(event: PlayerJoinEvent) {}
    open fun onLeave(event: PlayerQuitEvent) {}
}