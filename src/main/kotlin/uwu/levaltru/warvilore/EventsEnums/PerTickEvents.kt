package uwu.levaltru.warvilore.EventsEnums

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import org.bukkit.Bukkit
import uwu.levaltru.warvilore.Tickable
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.AbilitiesCore.Companion.getAbilities
import uwu.levaltru.warvilore.tickables.untraditional.NetherEmitter
import uwu.levaltru.warvilore.tickables.untraditional.RemainsOfTheDeads
import uwu.levaltru.warvilore.tickables.untraditional.Zone

enum class PerTickEvents(val execute: (ServerTickEndEvent) -> Unit) {

    TICKABLES_TICK({
        Tickable.Tick()
        NetherEmitter.Tick()
        Zone.getInstance().tick()
        RemainsOfTheDeads.Tick()
    }),

    ABILITIES_TICK({
        for (player in Bukkit.getOnlinePlayers()) {
            val abilities = player.getAbilities() ?: continue
            abilities.player = player
            try {
                abilities.onTick(it)
            } catch (e: Exception) {
                Warvilore.severe(e.toString())
                Warvilore.severe(e.message)
            }
        }
    }),

    INFINITE_SLOWFALL_EFFECT({
        for (player in Bukkit.getOnlinePlayers())
            if ((player.getPotionEffect(org.bukkit.potion.PotionEffectType.SLOW_FALLING)?.amplifier ?: -1) > 0) {
                if (
                    player.isOnGround ||
                    player.isInWaterOrBubbleColumn ||
                    player.isFlying
                ) player.removePotionEffect(org.bukkit.potion.PotionEffectType.SLOW_FALLING)
                else uwu.levaltru.warvilore.trashcan.LevsUtils.addInfiniteSlowfall(player)
            }
    }),

    THE_KNOT_OF_LIFE_HIT_EFFECT({
        for (player in Bukkit.getOnlinePlayers()) {
            val i =
                player.persistentDataContainer[uwu.levaltru.warvilore.trashcan.Namespaces.TICK_TIME_OF_DEATH.namespace, org.bukkit.persistence.PersistentDataType.INTEGER]
            val trialParticle =
                if (player.getAbilities() is uwu.levaltru.warvilore.abilities.interfaces.EvilAurable)
                    org.bukkit.Particle.TRIAL_SPAWNER_DETECTION_OMINOUS
                else org.bukkit.Particle.TRIAL_SPAWNER_DETECTION
            if (i != null && i > 0) {
                player.persistentDataContainer[uwu.levaltru.warvilore.trashcan.Namespaces.TICK_TIME_OF_DEATH.namespace, org.bukkit.persistence.PersistentDataType.INTEGER] =
                    i - 1
                player.world.spawnParticle(
                    trialParticle,
                    player.location.add(0.0, player.height / 2, 0.0), 1,
                    .2, .4, .2, .033, null, true
                )
                if (player.ticksLived % 8 == 0)
                    player.world.playSound(
                        player.location, org.bukkit.Sound.BLOCK_TRIAL_SPAWNER_AMBIENT_OMINOUS,
                        org.bukkit.SoundCategory.MASTER, 1f, 0.5f
                    )
            }
        }
    }),

    MORTALITY_CURSE({
        for (player in Bukkit.getOnlinePlayers()) {
            if (player.ticksLived % 20 == 0) {
                val i1 =
                    player.persistentDataContainer[uwu.levaltru.warvilore.trashcan.Namespaces.LIVES_REMAIN.namespace, org.bukkit.persistence.PersistentDataType.INTEGER]
                if (i1 != null) {
                    val trialParticle =
                        if (player.getAbilities() is uwu.levaltru.warvilore.abilities.interfaces.EvilAurable)
                            org.bukkit.Particle.TRIAL_SPAWNER_DETECTION_OMINOUS
                        else org.bukkit.Particle.TRIAL_SPAWNER_DETECTION
                    val boundingBox = player.boundingBox
                    val random = java.util.Random()
                    val loce = org.bukkit.Location(
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
        }
    }),

    UPDATE_NETHER_INFESTOR_THE_ONES_WHO_SEE_LIST({
        if (it.tickNumber % 100 == 0) {
            uwu.levaltru.warvilore.tickables.NetherInfector.playerWhoWillSeeBetter.clear()
            uwu.levaltru.warvilore.tickables.NetherInfector.playerWhoWillSeeBetter.addAll(
                Bukkit.getOnlinePlayers()
                    .filter { (it.getAbilities() as? uwu.levaltru.warvilore.abilities.abilities.TheBringer)?.seeTheThings == true }
            )
        }
    })

}