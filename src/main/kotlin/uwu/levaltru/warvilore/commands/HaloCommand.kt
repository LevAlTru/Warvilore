package uwu.levaltru.warvilore.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabCompleter
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import uwu.levaltru.warvilore.abilities.AbilitiesCore.Companion.getAbilities
import uwu.levaltru.warvilore.abilities.abilities.OneAngelZero
import java.lang.NumberFormatException

class HaloCommand : TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        val abilities = (sender as? Player)?.getAbilities()
        if (abilities !is OneAngelZero) {
            sender.sendMessage(Component.text("=[").color(NamedTextColor.RED))
            return true
        }

        if ((args?.size ?: -1) < 3) {
            sender.sendMessage(Component.text("Not enough arguments").color(NamedTextColor.RED))
            return true
        }

        val speed = args!![0].toFloatOrNull()
        if (speed == null) {
            sender.sendMessage(Component.text("<speed> should be a " +
                    "float number (3.14 as an example) not \"${args[1]}\"").color(NamedTextColor.RED))
            return true
        }
        val color = try { Integer.parseInt(args[1], 16) } catch (e: NumberFormatException) {
            sender.sendMessage(Component.text("<colorHexadecimal> should be a " +
                    "hexadecimal number not \"${args[1]}\"").color(NamedTextColor.RED))
            return true
        }
        var type: OneAngelZero.HaloTypes? = null
        for (thing in OneAngelZero.HaloTypes.entries) {
            if (thing.string == args[2]) {
                type = thing
                break
            }
        }
        if (type == null) {
            sender.sendMessage(Component.text("Invalid type of \"${args[2]}\"").color(NamedTextColor.RED))
            return true
        }

        abilities.changeHalo(color, speed, type)
        sender.sendMessage(Component.text("Changed halo to: ").color(NamedTextColor.GREEN)
            .append { Component.text(type.string).color(TextColor.color(color)) })

        if (args.size >= 4) {
            if (args[3].lowercase() == "true") {
                abilities.shouldWiggle = true
                sender.sendMessage(
                    Component.text("Changed shouldWiggle to: ").color(NamedTextColor.GREEN)
                        .append { Component.text("true").color(NamedTextColor.LIGHT_PURPLE) }
                )
            } else if (args[3].lowercase() == "false") {
                abilities.shouldWiggle = false
                sender.sendMessage(
                    Component.text("Changed shouldWiggle to: ").color(NamedTextColor.GREEN)
                        .append { Component.text("false").color(NamedTextColor.LIGHT_PURPLE) }
                )
            } else {
                Component.text("shouldWiggle can be only true of false not \"${args[3]}\"").color(NamedTextColor.RED)
            }
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String> {
        val abilities = (sender as? Player)?.getAbilities()
        if (abilities !is OneAngelZero) return listOf("ERROR_ILLEGAL_ACCESS")
        return when (args.size) {
            1 -> listOf("<speed>")
            2 -> listOf("<colorHexadecimal>")
            3 -> OneAngelZero.HaloTypes.entries.map { it.string }.filter { it.startsWith(args[2]) }
            4 -> listOf("true", "false")
            else -> listOf()
        }
    }
}