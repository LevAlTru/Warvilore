package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.*
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.abilities.interfaces.EvilAurable
import uwu.levaltru.warvilore.abilities.interfaces.tagInterfaces.CanSeeSouls
import uwu.levaltru.warvilore.abilities.interfaces.tagInterfaces.CantLeaveSouls
import uwu.levaltru.warvilore.tickables.DeathSpirit
import uwu.levaltru.warvilore.trashcan.LevsUtils
import uwu.levaltru.warvilore.trashcan.Namespaces

private const val MAX_MANA = 6000

private const val RADIUS = 2.5

private const val CIRCLE_OF_REVIVAL = 20
private const val READY_TO_REVIVE = 220

private const val REVIVAL_COST = 1000
private const val GOLDEN_APPLE_MANA = 600

private const val HOW_OFTEN_GET_SICK_MIN = 20 * 60 * 3
private const val HOW_OFTEN_GET_SICK_MAX = 20 * 60 * 5

class Nekomancer(string: String) : AbilitiesCore(string), EvilAurable, CanSeeSouls, CantLeaveSouls {

    var prevLoc: Location? = null
    var standStillTime = 0
    var mana: Int = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.MANA.namespace,
                PersistentDataType.INTEGER
            ) ?: MAX_MANA
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.MANA.namespace,
                PersistentDataType.INTEGER, value
            )
            field = value
        }
    var timeBeforeNextStroke: Int = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.BEFORE_NEXT_STROKE.namespace,
                PersistentDataType.INTEGER
            ) ?: HOW_OFTEN_GET_SICK_MAX
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.BEFORE_NEXT_STROKE.namespace,
                PersistentDataType.INTEGER, value
            )
            field = value
        }

    override fun onTick(event: ServerTickEndEvent) {
        if (mana < MAX_MANA && player!!.ticksLived % 20 == 0)
            mana++

        if (timeBeforeNextStroke <= 0) {
            manaDeficiency()
            timeBeforeNextStroke = (random.nextInt(HOW_OFTEN_GET_SICK_MIN, HOW_OFTEN_GET_SICK_MAX))
        } else timeBeforeNextStroke--

        if (prevLoc != null
            && prevLoc!!.world == player!!.location.world
            && (player!!.location.world == prevLoc?.world && player!!.location.distanceSquared(prevLoc!!) < .0001)
            && player!!.isSneaking
            && (mana > REVIVAL_COST || standStillTime > 10)
            && mana > 0
        )
            standStillTime++
        else {
            if (circleOfRevival()) {
                player!!.location.getNearbyPlayers(32.0).forEach { it.stopSound(Sound.BLOCK_BEACON_AMBIENT) }
                player!!.world.playSound(player!!.location, Sound.BLOCK_BEACON_DEACTIVATE, 1f, 0.7f)
                manaDeficiency()
            }
            standStillTime = 0
            prevLoc = player!!.location
        }

        if (!circleOfRevival()) return
        mana--
        timeBeforeNextStroke -= 10
        if ((standStillTime - CIRCLE_OF_REVIVAL) % 50 == 0)
            player!!.world.playSound(player!!.location, Sound.BLOCK_BEACON_AMBIENT, 1f, 0.7f)
        val locy = player!!.location.clone().add(0.0, player!!.height / 2, 0.0)
        var vector: Vector? = null
        var add: Location? = null
        val world = player!!.world

        for (i in 1..10) {
            vector = LevsUtils.getRandomNormalizedVector()
            add = locy.clone().add(vector.multiply(RADIUS))
            vector.multiply(-1)
            val b = random.nextDouble() < 0.66
            var d = random.nextDouble(0.1, 1.5)
            if (!b) d /= 80.0
            world.spawnParticle(
                if (b) Particle.SCRAPE else Particle.END_ROD, add.x, add.y, add.z, 0,
                vector.x, vector.y, vector.z, d, null, true
            )
        }

        vector!!.multiply(-1)
        if (random.nextDouble() < 0.5) world.spawnParticle(
            Particle.SCRAPE, add!!.x, add.y, add.z, 0,
            vector.x, vector.y, vector.z, 3.0, null, true
        )
        else world.spawnParticle(
            Particle.END_ROD, add!!.x, add.y, add.z, 0,
            vector.x, vector.y, vector.z, 3.0 / 60, null, true
        )

        for (nearbyPlayer in locy.getNearbyPlayers(RADIUS + 0.2)) {
            val location = nearbyPlayer.location.add(0.0, nearbyPlayer.height / 2, 0.0)
            if (location.distance(locy) > RADIUS + 0.2) continue
            if (standStillTime % 40 == 0)
                nearbyPlayer.addPotionEffect(
                    PotionEffect(
                        PotionEffectType.REGENERATION, 45, 2,
                        true, true, true
                    )
                )
            val particle = if (nearbyPlayer.isEvil()) Particle.SCRAPE else Particle.WAX_ON
            world.spawnParticle(particle, location, 1, .2, .4, .2, 3.0, null, true)
        }

        val spiritsNearby = getDeathSpiritsNearby(locy)
        if (spiritsNearby.isEmpty()) {
            displayMana()
            return
        }
        if (standStillTime - 50 < READY_TO_REVIVE) displayCharge()
        else displayMana()

        if (standStillTime == (READY_TO_REVIVE - 65))
            world.playSound(player!!.location, Sound.ENTITY_WARDEN_SONIC_CHARGE, 1.5f, 0.5f)

        for (deathSpirit in spiritsNearby) {
            val deadPlayer = Bukkit.getPlayer(deathSpirit.nickname!!)
            deathSpirit.maxAge++
            if (deadPlayer?.isDead != false) continue
            deathSpirit.loc.world.spawnParticle(Particle.END_ROD, deathSpirit.loc, 1, .1, .1, .1, .02)
            deadPlayer.world.spawnParticle(
                if (deadPlayer.isEvil()) Particle.TRIAL_SPAWNER_DETECTION_OMINOUS else Particle.TRIAL_SPAWNER_DETECTION,
                deadPlayer.location.clone().add(0.0, deadPlayer.height / 2, 0.0), 2,
                0.2, 0.4, 0.2, .05, null, true
            )
            if (deadPlayer.ticksLived % 8 == 0)
                deadPlayer.world.playSound(
                    deadPlayer.location, Sound.BLOCK_TRIAL_SPAWNER_AMBIENT_OMINOUS,
                    SoundCategory.MASTER, 1f, 0.5f
                )
            if (standStillTime == (READY_TO_REVIVE - 65))
                deadPlayer.world.playSound(deadPlayer.location, Sound.BLOCK_TRIAL_SPAWNER_AMBIENT_OMINOUS, 2.5f, 0.5f)
        }
    }

    private fun displayCharge() {
        player!!.sendActionBar(
            Component.text(
                "${
                    ((standStillTime - CIRCLE_OF_REVIVAL) * 100 / (READY_TO_REVIVE - CIRCLE_OF_REVIVAL))
                        .coerceAtMost(100)
                }%"
            ).style(Style.style(TextDecoration.BOLD, TextDecoration.ITALIC, NamedTextColor.GOLD))
        )
    }

    override fun onAction(event: PlayerInteractEvent) {
        if (player == null) return
        if (!event.action.isLeftClick) return
        when (player!!.inventory.itemInMainHand.type) {
            Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE -> displayMana()
            else -> {}
        }
        when (player!!.inventory.itemInOffHand.type) {
            Material.GOLDEN_APPLE, Material.ENCHANTED_GOLDEN_APPLE -> displayMana()
            else -> {}
        }
        if (!readyToRevive()) return
        if (player!!.pitch < 50.0) return

        val location = player!!.location
        val spiritsNearby = getDeathSpiritsNearby(location.clone().add(0.0, player!!.height / 2, 0.0))
        for (spirit in spiritsNearby) {
            val loc = spirit.loc
            val world = loc.world
            val deadPlayer = Bukkit.getPlayer(spirit.nickname!!)
            if (deadPlayer == null || deadPlayer.isDead) {
                world.spawnParticle(Particle.END_ROD, loc, 50, .1, .1, .1, .1, null, true)
                world.playSound(loc, Sound.ENTITY_ALLAY_DEATH, SoundCategory.MASTER, 1f, 0.5f)
                spirit.nickname = null
                continue
            }

            for (stack in deadPlayer.inventory) {
                if (stack?.isEmpty != false) continue
                world.spawn(deadPlayer.location, Item::class.java) {
                    it.itemStack = stack
                    it.velocity = Vector(random.nextGaussian() * 0.3, 0.2, random.nextGaussian() * 0.3)
                }
                stack.amount = 0
            }
            deadPlayer.location.getNearbyPlayers(32.0)
                .forEach { it.stopSound(Sound.BLOCK_TRIAL_SPAWNER_OMINOUS_ACTIVATE) }
            effects(deadPlayer)
            deadPlayer.teleport(loc)
            spirit.nickname = null
            effects(deadPlayer)
            deadPlayer.velocity = spawnRandomVector()
        }
        if (spiritsNearby.isNotEmpty()) {
            val world = player!!.world
            world.spawnParticle(
                Particle.TRIAL_SPAWNER_DETECTION, location.add(.0, .2, .0),
                200, 1.0, 0.1, 1.0, 0.05, null, true
            )
            location.getNearbyPlayers(32.0).forEach { it.stopSound(Sound.BLOCK_BEACON_AMBIENT) }
            world.playSound(location, Sound.ENTITY_WARDEN_SONIC_BOOM, 1.0f, 0.8f)
            player!!.velocity = spawnRandomVector()
            standStillTime = 0
            mana -= (spiritsNearby.size * REVIVAL_COST).coerceAtMost(mana)
            manaDeficiency()
            displayMana()
        }
    }

    private fun manaDeficiency() {
        if (mana < 4500) {
            player!!.addPotionEffect(PotionEffect(PotionEffectType.NAUSEA, 30 * 20, 0, true, false, true))
            player!!.playSound(player!!.location, Sound.BLOCK_TRIAL_SPAWNER_SPAWN_MOB, 0.33f, 0.5f)
        } else return
        if (mana < 3000) player!!.addPotionEffect(
            PotionEffect(
                PotionEffectType.DARKNESS,
                15 * 20,
                0,
                true,
                false,
                true
            )
        )
        else return
        if (mana < 1500) player!!.addPotionEffect(
            PotionEffect(
                PotionEffectType.BLINDNESS,
                30 * 20,
                0,
                true,
                false,
                true
            )
        )
        else return
        if (mana < 500) player!!.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 20 * 20, 0, true, false, true))
        else return
    }

    override fun onEating(event: PlayerItemConsumeEvent) {
        when (event.item.type) {
            Material.GOLDEN_APPLE -> {
                player!!.world.spawnParticle(
                    Particle.SCRAPE, player!!.location.add(0.0, player!!.height / 2, 0.0),
                    10, .2, .4, .2, 2.0, null, true
                )
                player!!.world.playSound(player!!.location, Sound.ENTITY_ALLAY_ITEM_GIVEN, 1f, 0.7f)
                mana = (mana + GOLDEN_APPLE_MANA).coerceAtMost(MAX_MANA)
                displayMana()
            }

            Material.ENCHANTED_GOLDEN_APPLE -> {
                player!!.world.spawnParticle(
                    Particle.SCRAPE, player!!.location.add(0.0, player!!.height / 2, 0.0),
                    100, .2, .4, .2, 2.0, null, true
                )
                player!!.world.playSound(player!!.location, Sound.ENTITY_ALLAY_ITEM_GIVEN, 1f, 0.7f)
                mana = MAX_MANA
                displayMana()
            }

            else -> {}
        }
    }

    private fun displayMana() {
        val i = mana * 1000 / MAX_MANA
        player!!.sendActionBar(
            Component.text(
                "${i / 10}.${i % 10}%"
            ).style(Style.style(TextDecoration.BOLD, TextDecoration.ITALIC, NamedTextColor.BLUE))
        )
    }

    private fun spawnRandomVector(): Vector {
        return Vector(random.nextGaussian() * 0.2, random.nextDouble(0.5, 0.7), random.nextGaussian() * 0.2)
    }

    private fun effects(deadPlayer: Player) {
        val particle =
            if (deadPlayer.isEvil()) Particle.TRIAL_SPAWNER_DETECTION_OMINOUS else Particle.TRIAL_SPAWNER_DETECTION
        val add = deadPlayer.location.add(0.0, deadPlayer.height / 2, 0.0)
        add.world.spawnParticle(particle, add, 250, .2, .4, .2, .1, null, true)
        add.world.playSound(add, Sound.BLOCK_TRIAL_SPAWNER_SPAWN_ITEM, SoundCategory.MASTER, 1f, 0.5f)
    }

    private fun circleOfRevival(): Boolean {
        return standStillTime >= CIRCLE_OF_REVIVAL
    }

    private fun readyToRevive(): Boolean {
        return standStillTime >= READY_TO_REVIVE
    }

    private fun getDeathSpiritsNearby(locy: Location): List<DeathSpirit> {
        return DeathSpirit.LIST.filter { locy.distanceSquared(it.loc) < RADIUS * RADIUS + 0.2 }
    }

    override fun getEvilAura(): Double {
        val d = mana.toDouble() / MAX_MANA
        if (readyToRevive()) return 16.0 * d
        if (circleOfRevival()) return 12.0 * d
        return 4.0 * d
    }

    override fun getAboutMe(): List<Component> = listOf(
        text("Твои навыки:").color(NamedTextColor.LIGHT_PURPLE),
        text("- Когда ты на шифте, у тебя появляется круг регенерации для тебя и твоей команды").color(NamedTextColor.GREEN),
        text("- Ты видишь души.").color(NamedTextColor.GREEN),
        text(
            "- Твой круг регенерации умеет заряжаться. Дуги в твоем круге задерживаются в этом мире на подольше. " +
                    "Когда он полностью заряжен и ты бьешь рукой по полу, игроки чьи души были в твоем кругу телепортируются на точку их души."
        ).color(NamedTextColor.GREEN),
        text("- - Но для этого нужно чтобы игрок возродился и был в сети! ").color(NamedTextColor.GOLD)
            .append { text("Иначе их душа разрушится моментально!").color(NamedTextColor.RED) },
        text("- Также у тебя есть мана. Чтобы восстановить твою ману ты можешь ждать, или съесть золотое или золотое зачарованное яблоко").color(
            NamedTextColor.AQUA
        ),
        text("- - Еще если ты ударишь яблоком по воздуху, ты увидишь свою ману").color(NamedTextColor.AQUA),
    )
}