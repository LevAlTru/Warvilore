package uwu.levaltru.warvilore.abilities.abilities

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.tickables.projectiles.Meteorite

class MeteorSpawner(nickname: String) : AbilitiesCore(nickname) {

    var castingSpeed = 1.0
    var castingCollisionTimes = 5

    override fun onAction(event: PlayerInteractEvent) {
        if (castingSpeed > 0 && castingCollisionTimes > 0) {
            val direction = player!!.eyeLocation.direction
            Meteorite(
                player!!.eyeLocation.add(direction),
                direction.clone().multiply(castingSpeed),
                castingCollisionTimes
            )
        }
    }

    override fun executeCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>) {
        if (args.size <= 2) {
            castingSpeed = args[0].toDouble()
            castingCollisionTimes = args[1].toInt()
            return
        }
        Meteorite(
            Location(
                (sender as Player).world,
                args[0].toDouble(),
                args[1].toDouble(),
                args[2].toDouble(),
            ),
            Vector(args[3].toDouble(), args[4].toDouble(), args[5].toDouble()),
            args[6].toInt()
        )
    }

    override fun completeCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String>? {
        return when (args.size) {
            1 -> listOf("x", "speed")
            2 -> listOf("y", "collisionTimes")
            3 -> listOf("z")
            4 -> listOf("dx")
            5 -> listOf("dy")
            6 -> listOf("dz")
            7 -> listOf("collsionTimes")
            else -> listOf("")
        }
    }

    override fun getAboutMe(): List<Component> = listOf(
        text("meteor spawner").color(NamedTextColor.DARK_PURPLE)
    )
}