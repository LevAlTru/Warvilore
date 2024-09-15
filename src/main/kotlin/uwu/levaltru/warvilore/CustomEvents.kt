package uwu.levaltru.warvilore

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import io.papermc.paper.tag.EntityTags
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.attribute.Attribute
import org.bukkit.block.Crafter
import org.bukkit.entity.*
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockGrowEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.CrafterCraftEvent
import org.bukkit.event.entity.*
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.PortalCreateEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.abilities.AbilitiesCore.Companion.getAbilities
import uwu.levaltru.warvilore.abilities.AbilitiesCore.Companion.hashMap
import uwu.levaltru.warvilore.abilities.abilities.BoilingAssasin
import uwu.levaltru.warvilore.abilities.abilities.TheColdestOne.Companion.isFrostmourne
import uwu.levaltru.warvilore.abilities.bases.Undead
import uwu.levaltru.warvilore.abilities.interfaces.EvilAurable
import uwu.levaltru.warvilore.abilities.interfaces.tagInterfaces.CantLeaveSouls
import uwu.levaltru.warvilore.tickables.DeathSpirit
import uwu.levaltru.warvilore.trashcan.CustomItems
import uwu.levaltru.warvilore.trashcan.LevsUtils
import uwu.levaltru.warvilore.trashcan.LevsUtils.getAsCustomItem
import uwu.levaltru.warvilore.trashcan.LevsUtils.getSoulBound
import uwu.levaltru.warvilore.trashcan.LevsUtils.isSoulBound
import uwu.levaltru.warvilore.trashcan.Namespaces

class CustomEvents : Listener {

    @EventHandler
    fun onTick(event: ServerTickEndEvent?) {
        Tickable.Tick()
        Zone.getInstance().tick()
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
    fun onPreAttack(event: PrePlayerAttackEntityEvent) {
        val player = event.player
        val item = player.inventory.itemInMainHand
        val itemMeta = item.itemMeta

        if (itemMeta != null) {
            if (event.willAttack())
                if (itemMeta.isSoulBound()) {
                    val asCustomItem = LevsUtils.getAsCustomWeapon(itemMeta)
                    if (itemMeta.getSoulBound() != player.name) {
                        val i =
                            itemMeta.persistentDataContainer[Namespaces.TIMES_BEFORE_BREAK.namespace, PersistentDataType.INTEGER]
                                ?: asCustomItem?.timesBeforeBreak
                        if (i != null) {
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

        player.getAbilities()?.onPreAttack(event)
    }

    @EventHandler
    fun onAttack(event: EntityDamageByEntityEvent) {
        var damager = event.damager
        if (damager is Projectile) damager = Bukkit.getEntity(damager.ownerUniqueId ?: return) ?: return
        (damager as? Player)?.getAbilities()?.onAttack(event)
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
                } else strings = mutableListOf(player.name)
                entity.persistentDataContainer[Namespaces.WHO_HAVE_HIT.namespace, PersistentDataType.LIST.strings()] =
                    strings
            }
        }

        if (entity is Player) entity.getAbilities()?.onDamage(event)
    }

    @EventHandler
    fun onKill(event: EntityDeathEvent) {
        val killer = event.damageSource.causingEntity ?: return
        if (killer is Player) killer.getAbilities()?.onKill(event)
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
        if (entity is Player) {
            entity.getAbilities()?.onBowShooting(event)
            if (event.bow?.itemMeta?.getAsCustomItem() == CustomItems.NETHERITE_BOW) {
                event.projectile.velocity = event.projectile.velocity.multiply(1.5)
            }
        }
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
        for (attribute in Attribute.entries) {
            val modifiers = player.getAttribute(attribute)?.modifiers
            if (modifiers != null) {
                for (modifier in modifiers) {
                    if (modifier.name.contains("temp"))
                        player.getAttribute(attribute)!!.removeModifier(modifier.key)
                }
            }
        }
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
        if (abilities is CantLeaveSouls) return
        if (!event.isCancelled) DeathSpirit(
            player.location.add(0.0, player.height / 2, 0.0),
            (abilities is EvilAurable),
            player.name
        )
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
    fun onMobSpawn(event: EntitySpawnEvent) {
        event.isCancelled = event.entity.entitySpawnReason == CreatureSpawnEvent.SpawnReason.NATURAL &&
                Zone.factorOf(event.location.blockX / 16, event.location.blockZ / 16, event.location.world) > 0.01
    }

    @EventHandler
    fun onPortalThing(event: PortalCreateEvent) {
        event.isCancelled = (event.world.persistentDataContainer.get(
            Namespaces.WORLD_ARE_PORTAL_ALLOWED.namespace,
            PersistentDataType.DOUBLE
        ) ?: 0) == 0
    }

    @EventHandler
    fun onItemCrafting(event: PrepareItemCraftEvent) {
        if (event.inventory.any { it?.itemMeta?.getAsCustomItem() != null }) {
            event.inventory.setItem(0, ItemStack.empty())
        }
    }

    @EventHandler
    fun onItemCrafting(event: CraftItemEvent) {
        event.isCancelled = event.inventory.any { it?.itemMeta?.getAsCustomItem() != null }
    }

    @EventHandler
    fun onCrafterItemCrafting(event: CrafterCraftEvent) {
        event.isCancelled =
            (event.block.getState(false) as Crafter).inventory.any { it?.itemMeta?.getAsCustomItem() != null }
    }

    @EventHandler
    fun onChunkLoad(event: ChunkLoadEvent) {
        val b = event.isNewChunk && Zone.factorOf(event.chunk.x, event.chunk.z, event.world) > 0.01
        for (entity in event.chunk.entities) {
            if (b && entity is LivingEntity) entity.remove()
            if (entity.persistentDataContainer[Namespaces.SHOULD_DESPAWN.namespace, PersistentDataType.BOOLEAN] == true)
                entity.remove()
        }
    }

    @EventHandler
    fun onPlantGrowing(event: BlockGrowEvent) {
        val chunk = event.block.location.chunk
        val factorOf = Zone.factorOf(chunk.x, chunk.z, chunk.world)
        event.isCancelled = factorOf > Math.random()
    }
}