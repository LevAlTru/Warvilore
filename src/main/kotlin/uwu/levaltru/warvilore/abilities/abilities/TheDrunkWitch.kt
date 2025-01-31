package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Item
import org.bukkit.event.Event
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.abilities.interfaces.EvilAurable
import uwu.levaltru.warvilore.abilities.interfaces.tagInterfaces.CanSeeSouls
import uwu.levaltru.warvilore.abilities.interfaces.tagInterfaces.CantLeaveSouls
import uwu.levaltru.warvilore.tickables.unmovables.DeathSpirit
import uwu.levaltru.warvilore.tickables.unmovables.MagicCauldron
import uwu.levaltru.warvilore.trashcan.CustomItems
import uwu.levaltru.warvilore.trashcan.LevsUtils
import uwu.levaltru.warvilore.trashcan.LevsUtils.getAsCustomItem
import uwu.levaltru.warvilore.trashcan.LevsUtils.getSoulInTheBottle
import uwu.levaltru.warvilore.trashcan.Namespaces
import kotlin.math.roundToInt

private const val ACTION_COOLDOWN: Int = 3

private const val MAX_MANA: Int = 10000

private const val HOW_LONG_IS_THE_SOULS_STAYS_IN_THE_DRANK_SOULS = 20 * 60 * 10

private const val HEALTH_TO_MANA_CONVERSION_RATE = 50

class TheDrunkWitch(string: String) : AbilitiesCore(string), EvilAurable, CanSeeSouls, CantLeaveSouls {
    var mana: Int = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.MANA.namespace,
                PersistentDataType.INTEGER
            ) ?: MAX_MANA
            return field
        }
        set(value) {
            val value1 = value.coerceIn(0, MAX_MANA)
            player?.persistentDataContainer?.set(
                Namespaces.MANA.namespace,
                PersistentDataType.INTEGER, value1
            )
            field = value1
        }
    var drankSouls: List<String> = listOf()
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.DRANK_SOULS.namespace,
                PersistentDataType.LIST.strings()
            ) ?: listOf()
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.DRANK_SOULS.namespace,
                PersistentDataType.LIST.strings(), value
            )
            field = value
        }
    var actionCooldown = 0

    override fun onTick(event: ServerTickEndEvent) {
        tickDrankSouls()
        if (actionCooldown > 0) actionCooldown--
        if (player!!.ticksLived % 20 == 0) changeHealth()
    }

    override fun onAction(event: PlayerInteractEvent) {
        if (actionCooldown > 0) return
        actionCooldown = ACTION_COOLDOWN
        val item = player!!.inventory.itemInMainHand
        if (event.action.isRightClick) {
            val loc = event.clickedBlock?.location?.toCenterLocation() ?: return
            val neatestCauldron = MagicCauldron.getNeatestCauldron(loc)
            if (item.isEmpty) {
                if (MagicCauldron.isValidCauldron(loc)) {
                    if (player!!.isSneaking) {
                        neatestCauldron ?: return
                        if (neatestCauldron.location.distance(loc) > .2) return
                        val itemStack = neatestCauldron.items.lastOrNull() ?: return
                        neatestCauldron.items.removeLast()

                        LevsUtils.throwItemTo(
                            neatestCauldron.location.clone().add(0.0, 0.7, 0.0),
                            player!!.location.toVector(),
                            itemStack
                        )
                        neatestCauldron.splash()

                    } else MagicCauldron(loc)
                }
            } else if (item.type == Material.AMETHYST_SHARD) {
                neatestCauldron ?: return
                if (loc.distanceSquared(neatestCauldron.location) < .2) {
                    val i = neatestCauldron.findRecipeNode()?.manaCost
                    if (i == null) {
                        neatestCauldron.ejectItems()
                        return
                    }
                    val a = mana - i
                    if (a < 0) {
                        val manaFromHealth = ((player!!.health - 0.1) * HEALTH_TO_MANA_CONVERSION_RATE)
                        if (a < -manaFromHealth) {
                            player!!.sendActionBar(text("Тебе не хватает ${-(a + manaFromHealth.roundToInt()) / 100}% маны").color(NamedTextColor.RED))
                            player!!.world.playSound(player!!, Sound.ENTITY_PLAYER_BREATH, 1f, .5f)
                            player!!.addPotionEffects(
                                listOf(
                                    PotionEffect(PotionEffectType.BLINDNESS, 30, 0, true, false, true),
                                    PotionEffect(PotionEffectType.SLOWNESS, 35, 0, true, false, true),
                                    PotionEffect(PotionEffectType.SLOWNESS, 25, 1, true, false, true),
                                    PotionEffect(PotionEffectType.SLOWNESS, 15, 2, true, false, true),
                                )
                            )
                            return
                        }
                        player!!.damage(0.01)
                        val health = a / HEALTH_TO_MANA_CONVERSION_RATE
                        player!!.health = (player!!.health + health).coerceAtLeast(0.1)
                    }
                    mana -= neatestCauldron.activate(player!!) ?: return
                    player!!.world.playSound(player!!, Sound.BLOCK_AMETHYST_BLOCK_STEP, 1f, 1f)
                    item.subtract()
                    event.setUseItemInHand(Event.Result.ALLOW)
                }
            }
        }
        if (item.itemMeta?.hasCustomModelData() != true && item.type == Material.GLASS_BOTTLE) {
            for (spirit in DeathSpirit.LIST) {
                val spiritLoc = spirit.loc
                val eyeLocation = player!!.eyeLocation
                try {
                    val d = player!!.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE)?.value ?: 5.0
                    if (spiritLoc.distanceSquared(eyeLocation) > d * d) continue
                } catch (e: Exception) {
                    continue
                }
                if (spiritLoc.toVector().subtract(eyeLocation.toVector()).normalize()
                        .dot(eyeLocation.direction) < 0.95
                ) continue

                eyeLocation.world.playSound(eyeLocation, Sound.BLOCK_RESPAWN_ANCHOR_CHARGE, 1f, 0.9f)
                item.subtract()
                val bottleOf = LevsUtils.soulBottleOf(spirit.isOminous, spirit.nickname!!)
                spirit.remove()
                if (player!!.inventory.addItem(bottleOf).isNotEmpty())
                    player!!.world.spawn(player!!.eyeLocation, Item::class.java) {
                        it.itemStack = bottleOf
                        it.velocity = eyeLocation.direction.multiply(0.5)
                    }
            }
        }
    }

    override fun onHeal(event: EntityRegainHealthEvent) {
        event.amount *= (manaFrom0To1() * 2 - .5).coerceIn(0.0, 1.0)
    }

    private fun changeHealth() {
        val maxHealth = (manaFrom0To1() + 0.4).coerceIn(0.5, 1.0)
        for (modifier in player!!.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.modifiers) {
            if (modifier?.name == "temp_lackofmana") {
                player!!.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.removeModifier(modifier)
                break
            }
        }
        player!!.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.addModifier(
            AttributeModifier(
                Warvilore.namespace("temp_lackofmana"),
                maxHealth - 1,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
            )
        )
        player!!.health = player!!.health.coerceAtMost(player!!.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value)
    }

    override fun onEating(event: PlayerItemConsumeEvent) {
        val itemMeta = event.item.itemMeta
        val asCustomItem = itemMeta.getAsCustomItem()
        when (asCustomItem) {
            CustomItems.OMINOUS_SOUL_BOTTLE, CustomItems.SOUL_BOTTLE ->
                onSoulItemCunsumtion(
                    HOW_LONG_IS_THE_SOULS_STAYS_IN_THE_DRANK_SOULS,
                    asCustomItem == CustomItems.OMINOUS_SOUL_BOTTLE,
                    itemMeta.getSoulInTheBottle(),
                    MAX_MANA / 16
                )


            CustomItems.SOUL_BEER, CustomItems.LIT_SOUL_BEER, CustomItems.DRANK_SOUL_BEER,
            CustomItems.OMINOUS_SOUL_BEER, CustomItems.LIT_OMINOUS_SOUL_BEER, CustomItems.DRANK_OMINOUS_SOUL_BEER ->
                onSoulItemCunsumtion(
                    HOW_LONG_IS_THE_SOULS_STAYS_IN_THE_DRANK_SOULS / 3,
                    when (asCustomItem) {
                        CustomItems.OMINOUS_SOUL_BEER, CustomItems.LIT_OMINOUS_SOUL_BEER, CustomItems.DRANK_OMINOUS_SOUL_BEER -> true
                        else -> false
                    },
                    itemMeta.getSoulInTheBottle(),
                    MAX_MANA / 12
                )

            else -> {
            }
        }
    }

    private fun onSoulItemCunsumtion(
        howLongStays: Int,
        isOminous: Boolean,
        nickname: String?,
        manaRegened: Int
    ) {
        if (hasInDrankSouls(nickname)) {
            player!!.addPotionEffects(
                listOf(
                    PotionEffect(PotionEffectType.NAUSEA, 210, 1, false, true, true),
                    PotionEffect(PotionEffectType.SATURATION, 3, 0, false, true, true),
                )
            )
            player!!.world.spawnParticle(
                if (isOminous) Particle.SCRAPE else Particle.WAX_ON,
                player!!.location.add(0.0, player!!.height / 2, 0.0),
                4,
                .2,
                .4,
                .2,
                1.0,
                null,
                false
            )
            return
        }
        addDrankSoul(nickname, howLongStays)
        player!!.addPotionEffects(
            listOf(
                PotionEffect(PotionEffectType.REGENERATION, 210, 1, false, true, true),
                PotionEffect(PotionEffectType.INSTANT_HEALTH, 1, 0, false, true, true),
                PotionEffect(PotionEffectType.SATURATION, 3, 0, false, true, true),
            )
        )
        player!!.world.spawnParticle(
            if (isOminous) Particle.SCRAPE else Particle.WAX_ON,
            player!!.location.add(0.0, player!!.height / 2, 0.0),
            10,
            .2,
            .4,
            .2,
            1.0,
            null,
            false
        )
        player!!.world.playSound(player!!.location, Sound.ENTITY_ALLAY_ITEM_GIVEN, 1f, 0.7f)
        mana += manaRegened
        sendManaBar()
    }

    private fun sendManaBar() {
        player!!.sendActionBar(text((mana * 100 / MAX_MANA).toString()).color(NamedTextColor.GOLD).append {
            text("%").color(NamedTextColor.RED)
        })
    }

    private fun manaFrom0To1() = mana.toDouble() / MAX_MANA

    private fun tickDrankSouls() {
        val list = drankSouls.toMutableList()
        val newList = mutableListOf<String>()
        for (s in list) {
            val split = s.split(":")
            val int = split[1].toIntOrNull() ?: continue
            if (int <= 0) continue
            val nick = split[0]
            newList += ("$nick:${int - 1}")
        }
        drankSouls = newList
    }

    private fun hasInDrankSouls(nick: String?): Boolean {
        val nick = nick?.lowercase() ?: return false
        val list = drankSouls.toMutableList()
        for (element in list) {
            if (element.split(":")[0] == nick) {
                return true
            }
        }
        return false
    }

    private fun addDrankSoul(nick: String?, cooldown: Int) {
        val nick = nick?.lowercase() ?: return
        val list = drankSouls.toMutableList()
        for (element in list) {
            if (element.split(":")[0] == nick) {
                list.remove(element)
                break
            }
        }
        list.add("$nick:$cooldown")
        drankSouls = list
    }

    private fun removeDrankSoul(nick: String): Boolean {
        val nick = nick.lowercase()
        val list = drankSouls.toMutableList()
        for (element in list) {
            if (element.split(":")[0] == nick) {
                list.remove(element)
                return true
            }
        }
        return false
    }

    override fun getEvilAura(): Double = manaFrom0To1() * 5.0

    override fun getAboutMe(): List<Component> {
        TODO("Not yet implemented")
    }
}