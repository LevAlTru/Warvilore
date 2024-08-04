package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Particle
import org.bukkit.damage.DamageType
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import uwu.levaltru.warvilore.abilities.AbilitiesCore

class TestAbility(nickname: String) : AbilitiesCore(nickname) {
    override fun onTick(event: ServerTickEndEvent) {
        player?.world?.spawnParticle(
            Particle.END_ROD,
            player!!.location,
            1,
            0.3, 0.1, 0.3,
            0.02,
            null, true
        )
    }
    override fun onBlockBreak(event: BlockBreakEvent) {
        event.block.world.spawnParticle(
            Particle.END_ROD,
            event.block.location.toCenterLocation(),
            100,
            0.2, 0.2, 0.2,
            0.1,
            null, true
        )
    }
    override fun onBlockPlace(event: BlockPlaceEvent) {
        event.block.world.spawnParticle(
            Particle.END_ROD,
            event.block.location.toCenterLocation(),
            100,
            0.2, 0.2, 0.2,
            0.1,
            null, true
        )
    }
    override fun onDamage(event: EntityDamageEvent) {
        if (event.damageSource.damageType == DamageType.FALL) {
            player?.world?.spawnParticle(
                Particle.END_ROD,
                player!!.location,
                100,
                0.3, 0.1, 0.3,
                0.05,
                null, true
            )
            event.isCancelled = true
        } else {
            player?.world?.spawnParticle(
                Particle.END_ROD,
                player!!.location.clone().add(0.0, player!!.height / 2, 0.0),
                100,
                0.1, 0.3, 0.1,
                0.1,
                null, true
            )
        }
    }

    override fun getAboutMe(): List<Component> = listOf(text("test ability").color(NamedTextColor.DARK_PURPLE))
}