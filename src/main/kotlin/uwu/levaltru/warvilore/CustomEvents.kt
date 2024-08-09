package uwu.levaltru.warvilore

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import io.papermc.paper.tag.EntityTags
import org.bukkit.*
import org.bukkit.entity.AbstractArrow
import org.bukkit.entity.Arrow
import org.bukkit.entity.Player
import org.bukkit.entity.SpectralArrow
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.*
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.persistence.PersistentDataType
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.abilities.AbilitiesCore.Companion.getAbilities
import uwu.levaltru.warvilore.abilities.AbilitiesCore.Companion.hashMap
import uwu.levaltru.warvilore.abilities.abilities.BoilingAssasin
import uwu.levaltru.warvilore.abilities.abilities.Nekomancer
import uwu.levaltru.warvilore.abilities.bases.Undead
import uwu.levaltru.warvilore.abilities.interfaces.EvilAurable
import uwu.levaltru.warvilore.tickables.DeathSpirit
import uwu.levaltru.warvilore.trashcan.LevsUtils
import uwu.levaltru.warvilore.trashcan.LevsUtils.getSoulBound
import uwu.levaltru.warvilore.trashcan.LevsUtils.isSoulBound
import uwu.levaltru.warvilore.trashcan.Namespaces

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
        val player = event.player
        val item = player.inventory.itemInMainHand
        val itemMeta = item.itemMeta

        if (itemMeta != null) {
            if (event.willAttack())
                if (itemMeta.isSoulBound()) {
                    val asCustomItem = LevsUtils.getAsCustomItem(itemMeta)
                    if (itemMeta.getSoulBound() != player.name) {
                        val i =
                            itemMeta.persistentDataContainer[Namespaces.TIMES_BEFORE_BREAK.namespace, PersistentDataType.INTEGER]
                                ?: asCustomItem?.timesBeforeBreak ?: 0
                        if (i <= 0)
                            if (asCustomItem != null) asCustomItem.onBreak(player, event.attacked)
                            else {
                                item.amount = 0
                                player.world.playSound(
                                    player.location,
                                    Sound.ENTITY_ITEM_BREAK,
                                    SoundCategory.MASTER,
                                    1f,
                                    1f
                                )
                            }
                        else {
                            itemMeta.persistentDataContainer[Namespaces.TIMES_BEFORE_BREAK.namespace, PersistentDataType.INTEGER] =
                                i - 1
                            item.itemMeta = itemMeta
                        }
                    } else {
                        itemMeta.persistentDataContainer.set(
                            Namespaces.TIMES_BEFORE_BREAK.namespace,
                            PersistentDataType.INTEGER,
                            asCustomItem?.timesBeforeBreak ?: 1
                        )
                        item.itemMeta = itemMeta
                    }
                }
        }

        player.getAbilities()?.onAttack(event)
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val entity = event.entity

        if (EntityTags.UNDEADS.values.contains(entity.type)) {
            val player = event.damageSource.causingEntity
            if ((player as? Player)?.getAbilities() is Undead) {
                var strings =
                    entity.persistentDataContainer[Namespaces.WHO_HAVE_HIT.namespace, PersistentDataType.LIST.strings()]
                        ?.toMutableList()
                if (strings != null) {
                    if (!strings.contains(player.name))
                        strings.add(player.name)
                }
                else strings = mutableListOf(player.name)
                entity.persistentDataContainer[Namespaces.WHO_HAVE_HIT.namespace, PersistentDataType.LIST.strings()] =
                    strings
            }
        }

        if (entity is Player) entity.getAbilities()?.onDamage(event)
    }

    @EventHandler
    fun onHeal(event: EntityRegainHealthEvent) {
        val entity = event.entity
        if (entity is Player) entity.getAbilities()?.onHeal(event)
    }

    @EventHandler
    fun onAction(event: PlayerInteractEvent) {
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

    @EventHandler
    fun onMobAgro(event: EntityTargetLivingEntityEvent) {
        val entity = event.entity
        val target = event.target

        if (!EntityTags.UNDEADS.values.contains(entity.type)) return

        if (target !is Player) return
        if (target.getAbilities() is Undead) {
            val strings =
                entity.persistentDataContainer[Namespaces.WHO_HAVE_HIT.namespace, PersistentDataType.LIST.strings()]
            if (strings == null || !strings.contains(target.name)) event.isCancelled = true
        }
    }

    @EventHandler
    fun onChunkLoad(event: ChunkLoadEvent) {
        for (entity in event.chunk.entities) {
            if (entity.persistentDataContainer[Namespaces.SHOULD_DESPAWN.namespace, PersistentDataType.BOOLEAN] == true)
                entity.remove()
        }
    }
}