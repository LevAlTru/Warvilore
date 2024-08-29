package uwu.levaltru.warvilore.abilities.abilities

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType

private const val REGUIRED_PLAYER_AMOUNT = 3

class MafiosyGreater(nickname: String) : MafiosyLesser(nickname) {
//    override fun getEvilAura(): Double {
//        if (isMafiInvisible()) return 20.0
//        return 6.0
//    }

    override fun onAction(event: PlayerInteractEvent) {
        if (invisibleFun(event)) {
            var i = 0
            val nearbyPlayers = player!!.location.getNearbyPlayers(16.0)
            for (player1 in nearbyPlayers) {
                if (player1.uniqueId == player!!.uniqueId) continue
                if (player1?.getAbilities() is MafiosyLesser) i++
                if (i >= REGUIRED_PLAYER_AMOUNT) {
                    for (it in nearbyPlayers) {
                        if (it.getAbilities() !is MafiosyLesser) continue
                        it.addPotionEffect(PotionEffect(PotionEffectType.RESISTANCE, 30 * 20, 0, true, true, true))
                        val world = it.world
                        val locy = it.location.add(0.0, it.height / 2, 0.0)
                        world.playSound(locy, Sound.BLOCK_BEACON_POWER_SELECT, 0.5f, 1.5f)
                        world.spawnParticle(Particle.FIREWORK, locy, 30, .2, .4, .2, .05, null, true)
                    }
                    break
                }
            }
        }
    }

    override fun getAboutMe(): List<Component> {
        val add = super.getAboutMe().toMutableList()
        add.add(text("  - Также если вокруг тебя есть еще $REGUIRED_PLAYER_AMOUNT мафиози, ты даешь себе и мафиози вокруг тебя сопротивление (-20% урона).").color(NamedTextColor.GREEN))
        return add
    }
}