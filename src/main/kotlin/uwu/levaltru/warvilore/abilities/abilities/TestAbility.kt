package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.damage.DamageType
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.persistence.PersistentDataType
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.trashcan.Namespaces
import kotlin.math.ceil

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
            1 -> Namespaces.values().map { it.name }.filter { it.lowercase().contains("world") }
            2 -> listOf("value")
            else -> listOf("")
        }
    }

    override fun executeCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>) {
        if (sender !is Player) return
        try {
            val valueOf = Namespaces.valueOf(args[0])
            val double = args[1].toDouble()

            player!!.world.persistentDataContainer.set(valueOf.namespace, PersistentDataType.DOUBLE, double)

            sender.sendMessage(Component.text("set ${valueOf.namespace} to $double").color(NamedTextColor.GREEN))

        } catch (e: Exception) {
            sender.sendMessage(Component.text("error: $e").color(NamedTextColor.RED))
        }
    }

    override fun getAboutMe(): List<Component> = listOf(text("test ability").color(NamedTextColor.DARK_PURPLE))
}