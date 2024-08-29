package uwu.levaltru.warvilore.abilities.bases

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.abilities.interfaces.EvilAurable
import uwu.levaltru.warvilore.trashcan.LevsUtils
import uwu.levaltru.warvilore.trashcan.Namespaces

private const val EVIL_AURA_DETECTION_RADIUS = 32.0

abstract class HatesEvilAura(nickname: String): AbilitiesCore(nickname) {

    var evilAuraSickness: Long = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.EVIL_AURA_SICKNESS.namespace,
                PersistentDataType.LONG
            ) ?: 0
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.EVIL_AURA_SICKNESS.namespace,
                PersistentDataType.LONG, value)
            field = value
        }

    override fun onTick(event: ServerTickEndEvent) {
        if (evilAuraSickness > 0)
            evilAuraSickness -= evilAuraSickness / 1000 + 1
        for (nearbyPlayer in player!!.location.getNearbyPlayers(EVIL_AURA_DETECTION_RADIUS)) {

            val abilities = nearbyPlayer.getAbilities()
            if (abilities !is EvilAurable) continue

            val evilAura = abilities.getEvilAura()
            val distance = player!!.location.distance(nearbyPlayer.location)
            val evilAuraDistance = evilAura - distance
            evilAuraSickness += evilAuraDistance.toInt().coerceAtLeast(0)

            var d =
                ((EVIL_AURA_DETECTION_RADIUS - distance) / 8).coerceIn(0.0, 1.0) * evilAura * evilAura * 0.25 * 0.25

            while (d > 1 || random.nextDouble() < d) {
                var vector: Vector = LevsUtils.getRandomNormalizedVector()
                val add = nearbyPlayer.location.clone().add(0.0, nearbyPlayer.height / 2, 0.0)

                val nextDouble = random.nextDouble(0.2, 1.4)
                add.add(vector.clone().multiply(1.2 + evilAura * 0.9 * nextDouble))
                vector.multiply(-evilAura * nextDouble)

                player!!.spawnParticle(
                    Particle.OMINOUS_SPAWNING,
                    add.x, add.y, add.z, 0,
                    vector.x, vector.y, vector.z, 1.0, null, true
                )
                d--
            }
        }

        if (player!!.ticksLived % 40 == 0) {
            if (evilAuraSickness > 100)
                player!!.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS,
                    100, 0, true, false, true))
            else return
            if (evilAuraSickness > 250)
                player!!.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS,
                    100, 0, true, false, true))
            else return
            if (evilAuraSickness > 500) {
                player!!.addPotionEffects(
                    listOf(
                        PotionEffect(PotionEffectType.SLOWNESS, 100, 1, true, false, true),
                        PotionEffect(PotionEffectType.BLINDNESS, 100, 1, true, false, true))
                )
            }
            else return
            if (evilAuraSickness > 1000)
                player!!.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS,
                    100, 2, true, false, true))
            else return
            if (evilAuraSickness > 1500)
                player!!.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS,
                    100, 3, true, false, true))
            else return
            if (evilAuraSickness > 2000)
                player!!.addPotionEffect(PotionEffect(PotionEffectType.SLOWNESS,
                    100, 4, true, false, true))
            else return
        }
    }

    override fun onDeath(event: PlayerDeathEvent) {
        evilAuraSickness = 0
    }

    fun showEvilAuraPoisoning() {
        player!!.sendActionBar(
            Component.text("${evilAuraSickness / 250}.${(evilAuraSickness / 25) % 10}")
                .style(Style.style(TextDecoration.ITALIC, TextDecoration.BOLD, NamedTextColor.DARK_PURPLE))
        )
        player!!.playSound(player!!.location, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, SoundCategory.MASTER, 1f, 0.1f)
    }
}