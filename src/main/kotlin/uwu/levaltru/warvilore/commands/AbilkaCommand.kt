package uwu.levaltru.warvilore.commands

import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import uwu.levaltru.warvilore.abilities.AbilitiesCore.Companion.getAbilities

class AbilkaCommand : TabExecutor {

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        (sender as? Player)?.getAbilities()?.executeCommand(sender, command, label, args ?: arrayOf())
        return false
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): List<String>? {
        val abilities = (sender as? Player)?.getAbilities()
        if (abilities != null) return abilities.completeCommand(sender, command, label, args ?: emptyArray<String>())
        return listOf("nulla")
    }

}