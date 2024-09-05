package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.damage.DamageType
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import kotlin.math.ceil
import kotlin.time.times

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

    override fun completeCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String>? {
        return when (args.size) {
            1 -> listOf("x")
            2 -> listOf("y")
            3 -> listOf("z")
            4 -> listOf("size")
            else -> listOf("")
        }
    }

    override fun executeCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>) {
        if (sender !is Player) return
        try {
            val x = args[0].toInt()
            val y = args[1].toInt()
            val z = args[2].toInt()
            val size = args[3].toDouble()

            val i = ceil(size).toInt()
            for (x1 in -i..i)
                for (z1 in -i..i)
                    if (x1 * x1 + z1 * z1 < size * size) sender.world.setType(x + x1, y, z + z1, Material.DIAMOND_BLOCK)

        } catch (e: Exception) {
            sender.sendMessage(Component.text("error: $e").color(NamedTextColor.RED))
        }
    }

    override fun getAboutMe(): List<Component> = listOf(text("test ability").color(NamedTextColor.DARK_PURPLE))
}