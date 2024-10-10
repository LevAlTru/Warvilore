package uwu.levaltru.warvilore

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import io.papermc.paper.tag.EntityTags
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.Crafter
import org.bukkit.damage.DamageType
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
import org.bukkit.event.player.*
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.PortalCreateEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffectType
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.abilities.AbilitiesCore.Companion.getAbilities
import uwu.levaltru.warvilore.abilities.AbilitiesCore.Companion.hashMap
import uwu.levaltru.warvilore.abilities.abilities.BoilingAssasin
import uwu.levaltru.warvilore.abilities.abilities.TheBringer
import uwu.levaltru.warvilore.abilities.bases.Undead
import uwu.levaltru.warvilore.abilities.interfaces.EvilAurable
import uwu.levaltru.warvilore.abilities.interfaces.tagInterfaces.CantLeaveSouls
import uwu.levaltru.warvilore.tickables.CollabsePoint
import uwu.levaltru.warvilore.tickables.DeathSpirit
import uwu.levaltru.warvilore.tickables.NetherInfector
import uwu.levaltru.warvilore.tickables.untraditional.NetherEmitter
import uwu.levaltru.warvilore.tickables.untraditional.RemainsOfTheDeads
import uwu.levaltru.warvilore.tickables.untraditional.Zone
import uwu.levaltru.warvilore.trashcan.CustomItems
import uwu.levaltru.warvilore.trashcan.LevsUtils
import uwu.levaltru.warvilore.trashcan.LevsUtils.getAsCustomItem
import uwu.levaltru.warvilore.trashcan.LevsUtils.getAsCustomWeapon
import uwu.levaltru.warvilore.trashcan.LevsUtils.getSoulBound
import uwu.levaltru.warvilore.trashcan.LevsUtils.isSoulBound
import uwu.levaltru.warvilore.trashcan.Namespaces
import java.util.*

private const val DEATH_TICKS_MAX = 20 * 40

class CustomEvents : Listener {

    @EventHandler
    fun onTick(event: ServerTickEndEvent) {
        Tickable.Tick()
        Zone.getInstance().tick()
        RemainsOfTheDeads.Tick()
        NetherEmitter.Tick()
        if (event.tickNumber % 200 == 0) {
            NetherInfector.playerWhoWillSeeBetter.clear()
            NetherInfector.playerWhoWillSeeBetter.addAll(Bukkit.getOnlinePlayers().filter { (it.getAbilities() as? TheBringer)?.seeTheThings == true })
        }
        for (player in Bukkit.getOnlinePlayers()) {
            if ((player.getPotionEffect(PotionEffectType.SLOW_FALLING)?.amplifier ?: -1) > 0) {
                if (
                    player.isOnGround ||
                    player.isInWaterOrBubbleColumn ||
                    player.isFlying
                )

                    player.removePotionEffect(PotionEffectType.SLOW_FALLING)
                else LevsUtils.addInfiniteSlowfall(player)
            }
            val abilities = player.getAbilities()
            val i = player.persistentDataContainer[Namespaces.TICK_TIME_OF_DEATH.namespace, PersistentDataType.INTEGER]
            val trialParticle =
                if (abilities is EvilAurable) Particle.TRIAL_SPAWNER_DETECTION_OMINOUS else Particle.TRIAL_SPAWNER_DETECTION
            if (i != null && i > 0) {
                player.persistentDataContainer[Namespaces.TICK_TIME_OF_DEATH.namespace, PersistentDataType.INTEGER] =
                    i - 1
                player.world.spawnParticle(
                    trialParticle,
                    player.location.add(0.0, player.height / 2, 0.0), 1,
                    .2, .4, .2, .033, null, true
                )
                if (player.ticksLived % 8 == 0)
                    player.world.playSound(
                        player.location, Sound.BLOCK_TRIAL_SPAWNER_AMBIENT_OMINOUS,
                        SoundCategory.MASTER, 1f, 0.5f
                    )
            }
            if (player.ticksLived % 20 == 0) {
                val i1 = player.persistentDataContainer[Namespaces.LIVES_REMAIN.namespace, PersistentDataType.INTEGER]
                if (i1 != null) {
                    val boundingBox = player.boundingBox
                    val random = Random()
                    val loce = Location(
                        player.world,
                        random.nextDouble(boundingBox.minX, boundingBox.maxX),
                        random.nextDouble(boundingBox.minY, boundingBox.maxY),
                        random.nextDouble(boundingBox.minZ, boundingBox.maxZ),
                    )
                    loce.world.spawnParticle(
                        trialParticle, loce, 1, 0.0, 0.0, 0.0, 0.1
                    )
                }
            }
            abilities?.let {
                it.player = player
                try {
                    it.onTick(event)
                } catch (e: Exception) {
                    e.printStackTrace()
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
                    val asCustomItem = itemMeta.getAsCustomWeapon()
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

        val player = (event.entity as? Player)
        if (damager is Player) {
            if (damager.inventory.itemInMainHand.itemMeta.getAsCustomItem() == CustomItems.YOUR_REALITY_HAS_COLLAPSED ||
                damager.inventory.itemInOffHand.itemMeta.getAsCustomItem() == CustomItems.YOUR_REALITY_HAS_COLLAPSED
            ) {
                player?.persistentDataContainer?.set(
                    Namespaces.TICK_TIME_OF_DEATH.namespace,
                    PersistentDataType.INTEGER,
                    DEATH_TICKS_MAX
                )
                player?.persistentDataContainer?.set(
                    Namespaces.LAST_HIT_WITH_DEATH.namespace,
                    PersistentDataType.STRING,
                    damager.name
                )
            }
        }

        if (damager is Projectile) damager = Bukkit.getEntity(damager.ownerUniqueId ?: return) ?: return
        (damager as? Player)?.getAbilities()?.onAttack(event)
    }

    @EventHandler
    fun onDamage(event: EntityDamageEvent) {
        val entity = event.entity

        if (event.damageSource.damageType == DamageType.OUT_OF_WORLD) {
            if (entity.world.name == "world") {
                event.isCancelled = true
                if (entity.location.y < -450) {
                    entity.world.playSound(entity.location, Sound.BLOCK_END_PORTAL_SPAWN, 28f, .5f)
                    entity.teleport(
                        Location(
                            Bukkit.getWorld("world_nether") ?: entity.world,
                            entity.location.x,
                            500.0,
                            entity.location.z
                        )
                    )
                    if (entity is Player) LevsUtils.addInfiniteSlowfall(entity)
                    Bukkit.getScheduler().runTaskLater(Warvilore.instance, Runnable {
                        entity.world.playSound(entity.location, Sound.BLOCK_END_PORTAL_SPAWN, 28f, .5f)
                    }, 10L)
                }
                return
            }
        }

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
        if (DeveloperMode && player.name.lowercase() == "levaltru")
            for (i in 1..10)
                player.sendMessage(Component.text("Development Mode is turned on").color(NamedTextColor.RED))
        if (LevsUtils.Hiddens.isHidden(player.name)) {
            event.joinMessage(null)
            LevsUtils.Hiddens.hidePlayerPacket(player.uniqueId, 2)
        }
        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            if (LevsUtils.Hiddens.isHidden(onlinePlayer.name)) {
                LevsUtils.Hiddens.hidePlayerPacket(onlinePlayer.uniqueId, 50)
            }
        }
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
        if (LevsUtils.Hiddens.isHidden(player.name)) event.quitMessage(null)
        if ((player.persistentDataContainer[Namespaces.TICK_TIME_OF_DEATH.namespace, PersistentDataType.INTEGER]
                ?: 0) > 0
        ) {
            player.health = 0.0
            CollabsePoint(player.location.add(0.0, 1.5, 0.0), player.name)
        }
        val abilities = player.getAbilities()
        abilities?.onLeave(event)
        AbilitiesCore.saveAbility(player)
        hashMap.remove(player.name)
    }

    @EventHandler
    fun onDeath(event: PlayerDeathEvent) {
        val player = event.player

        if (LevsUtils.Hiddens.isHidden(player.name))
            event.deathMessage(null)

        val lives = player.persistentDataContainer[Namespaces.LIVES_REMAIN.namespace, PersistentDataType.INTEGER]
        if (lives != null) {
            player.world.playSound(player.location, Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, 4f, .7f)
            val isEvil = player.getAbilities() is EvilAurable
            player.world.spawnParticle(
                if (isEvil) Particle.TRIAL_SPAWNER_DETECTION_OMINOUS else Particle.TRIAL_SPAWNER_DETECTION,
                player.location.add(0.0, player.height / 2, 0.0),
                300,
                .3,
                .3,
                .3,
                .3,
                null,
                true
            )
            player.world.spawnParticle(
                if (isEvil) Particle.SOUL_FIRE_FLAME else Particle.FLAME,
                player.location.add(0.0, player.height / 2, 0.0),
                100,
                .3,
                .3,
                .3,
                .3,
                null,
                true
            )

            if (lives > 1) {
                player.persistentDataContainer[Namespaces.LIVES_REMAIN.namespace, PersistentDataType.INTEGER] =
                    lives - 1
            } else {
                player.persistentDataContainer.remove(Namespaces.LIVES_REMAIN.namespace)
                LevsUtils.Deads.addDied(player.name)
                Bukkit.getOnlinePlayers().forEach {
                    it.sendMessage(Component.text("${player.name} потерял все свои жизни").color(NamedTextColor.RED))
                    it.playSound(it, Sound.BLOCK_END_PORTAL_SPAWN, 128f, .7f)
                }
            }
            return
        }

        if ((player.persistentDataContainer[Namespaces.TICK_TIME_OF_DEATH.namespace, PersistentDataType.INTEGER]
                ?: 0) > 0
        ) {
            val s = player.persistentDataContainer[Namespaces.LAST_HIT_WITH_DEATH.namespace, PersistentDataType.STRING]
            if (s != null) {
                Bukkit.getPlayer(s)?.let {
                    for (itemStack in it.inventory) {
                        if (itemStack?.itemMeta.getAsCustomItem() == CustomItems.YOUR_REALITY_HAS_COLLAPSED) {
                            itemStack.subtract()
                            CollabsePoint(player.location.add(0.0, 1.5, 0.0), player.name)
                            event.isCancelled = true
                            return
                        }
                    }
                    player.persistentDataContainer[Namespaces.TICK_TIME_OF_DEATH.namespace, PersistentDataType.INTEGER] =
                        0
                }
            }
        }
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
    fun onPotionGain(event: EntityPotionEffectEvent) {
        (event.entity as? Player)?.getAbilities()?.onPotionGain(event)
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

    @EventHandler
    fun onPlayerTriesToConnect(event: PlayerLoginEvent) {
        if (LevsUtils.Deads.hasDied(event.player.name))
            event.disallow(
                PlayerLoginEvent.Result.KICK_BANNED,
                Component.text("У вас не осталось жизней.").color(NamedTextColor.RED)
            )
    }
}