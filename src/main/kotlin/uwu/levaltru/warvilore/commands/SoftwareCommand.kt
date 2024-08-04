package uwu.levaltru.warvilore.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import uwu.levaltru.warvilore.SoftwareBase
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.AbilitiesCore.Companion.getAbilities
import uwu.levaltru.warvilore.abilities.abilities.WalkingComputer

class SoftwareCommand : TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        val abilities = (sender as? Player)?.getAbilities()
        if (abilities !is WalkingComputer) {
            sender.sendMessage(Component.text("=[").color(NamedTextColor.RED))
            return true
        }

        if ((args?.size ?: -1) < 1) {
            sender.sendMessage(Component.text("Not enough arguments").color(NamedTextColor.RED))
            return true
        }

        var clazz: Class<*>? = null
        if (args!![0].lowercase() != "null") {
            try {
                clazz = Class.forName("uwu.levaltru.warvilore.software." + args[0])
            } catch (e: ClassNotFoundException) {
                sender.sendMessage(Component.text("The class is not found: " + args[0]).color(NamedTextColor.RED))
                return true
            }
        }
        var newInstance: SoftwareBase? = null
        if (clazz != null)
            newInstance = clazz.constructors[0].newInstance() as SoftwareBase

        abilities.changeSoftware(newInstance)

        sender.sendMessage(Component.text("Set active software to: ").color(NamedTextColor.GREEN)
            .append {
                Component.text(if (clazz != null) clazz.simpleName else "null").color(NamedTextColor.LIGHT_PURPLE)
            })
        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): List<String>? {
        if ((sender as? Player)?.getAbilities() !is WalkingComputer) return listOf()
        if (args?.size == 1) {
            return Warvilore.softwareList?.plus("null")
        } else if ((args?.size ?: -1) > 1) {
            var clazz: Class<*>? = null
            if (args!![0].lowercase() != "null") {
                try {
                    clazz = Class.forName("uwu.levaltru.warvilore.software." + args[0])
                } catch (e: ClassNotFoundException) {
                    return listOf("ERROR_NULL")
                }
            }
            val newInstance = clazz?.constructors?.get(0)?.newInstance() as? SoftwareBase
                ?: return listOf("ERROR_NULL")
            val arguments = newInstance.arguments
            val lastArgument = args[args.size - 1]
            if (lastArgument.contains(":"))
                return arguments.filter { it.startsWith(lastArgument) }
            val map = arguments.map { it.split(":")[0] }
            val map2 = mutableListOf<String>()
            for (i in map.indices)
                if (i == 0 || map[i] != map[i - 1]) map2.add(map[i])
            return map2
        }
        return listOf("ERROR_CHECK_FAIL")
    }
}