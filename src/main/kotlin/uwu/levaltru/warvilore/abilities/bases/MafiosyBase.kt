package uwu.levaltru.warvilore.abilities.bases

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.trashcan.Namespaces

abstract class MafiosyBase(nickname: String, private val COOLDOWN: Int, private val DURATION: Int) : AbilitiesCore(nickname)/*, EvilAurable*/ {

    var cooldown: Int = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.INVISIBILITY_COUNTER.namespace,
                PersistentDataType.INTEGER
            ) ?: 0
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.INVISIBILITY_COUNTER.namespace,
                PersistentDataType.INTEGER, value)
            field = value
        }

    override fun onAction(event: PlayerInteractEvent) {

        if (event.action.name != "RIGHT_CLICK_AIR") return
        when (player!!.inventory.itemInMainHand.type) {
            Material.WOODEN_SWORD,
            Material.STONE_SWORD,
            Material.IRON_SWORD,
            Material.GOLDEN_SWORD,
            Material.DIAMOND_SWORD,
            Material.NETHERITE_SWORD -> {}
            else -> return
        }
        if (cooldown > 0 && player!!.gameMode != GameMode.CREATIVE) {
            player!!.sendActionBar(Component.text("${cooldown / 20}s").color(NamedTextColor.RED))
            player!!.playSound(player!!.location, Sound.BLOCK_DECORATED_POT_INSERT_FAIL, SoundCategory.MASTER, 1f, 1f)
            return
        }
        cooldown = COOLDOWN
        effects()
        player!!.addPotionEffect(
            PotionEffect(PotionEffectType.INVISIBILITY, DURATION, 1, true, false, true)
        )
        Bukkit.getScheduler().runTaskLater(Warvilore.instance, Runnable {
            effects()
        }, (DURATION - 1).toLong())

    }

    private fun effects() {
        val add = player!!.location.clone().add(0.0, player!!.height / 2, 0.0)
        player!!.world.playSound(add, Sound.ENTITY_WITHER_SHOOT, SoundCategory.PLAYERS, 0.10f, 0.1f)
        player!!.world.spawnParticle(
            Particle.CAMPFIRE_COSY_SMOKE, add,
            25, 0.1, 0.3, 0.1, 0.01, null, true
        )
    }

    override fun onTick(event: ServerTickEndEvent) {
        if (cooldown > 0) cooldown--
    }

    fun isMafiInvisible(): Boolean {
        return ((player?.activePotionEffects
            ?.filter { it.type == PotionEffectType.INVISIBILITY }
            ?.maxOfOrNull { it.amplifier }) ?: -1) > 0
    }
}