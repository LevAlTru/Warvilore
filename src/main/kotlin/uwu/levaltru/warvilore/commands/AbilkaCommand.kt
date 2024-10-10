package uwu.levaltru.warvilore.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.AbilitiesCore.Companion.getAbilities

class AbilkaCommand : TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        try {
            (sender as? Player)?.getAbilities()?.executeCommand(sender, command, label, args ?: arrayOf())
        } catch (e: Exception) {
            sender.sendMessage(Component.text(e.toString()).color(NamedTextColor.RED))
            Warvilore.severe(e.toString())
            return false
        }
        return false
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): List<String>? {
        val abilities = (sender as? Player)?.getAbilities()
        if (abilities != null) return try {
            abilities.completeCommand(sender, command, label, args ?: emptyArray<String>())
        } catch (e: Exception) {
            sender.sendMessage(Component.text(e.toString()).color(NamedTextColor.RED))
            Warvilore.severe(e.toString())
            return listOf("ERROR")
        }
        return listOf("nulla")
    }

}