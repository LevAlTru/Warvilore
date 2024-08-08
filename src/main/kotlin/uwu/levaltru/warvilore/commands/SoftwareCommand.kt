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
        if (clazz != null) newInstance =
            clazz.constructors[0].newInstance(args.copyOfRange(1, args.size).joinToString(separator = " ")) as SoftwareBase
        if (args.any { it.lowercase() == "help" }) {
            newInstance?.description()?.forEach { sender.sendMessage(it) }
                ?: sender.sendMessage(Component.text("null").color(NamedTextColor.LIGHT_PURPLE))
            return false
        }

        abilities.changeSoftware(newInstance)

        sender.sendMessage(Component.text("Set active software to: ").color(NamedTextColor.GREEN)
            .append {
                Component.text(if (clazz != null) clazz.simpleName else "null").color(NamedTextColor.LIGHT_PURPLE)
            }
            .append { if (args.size > 1 && newInstance != null) {
                Component.text(" with arguments: ").color(NamedTextColor.GREEN)
                    .append { Component.text(
                        newInstance.arguments.map { "${it.key}:${it.value}" }.joinToString(separator = " ")
                    ).color(NamedTextColor.LIGHT_PURPLE) }
            } else Component.text("") })
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
            return Warvilore.softwareList?.plus("null")?.filter { it.lowercase().startsWith(args[0].lowercase()) }
        } else if ((args?.size ?: -1) > 1) {
            val clazz: Class<*>
            if (args!![0].lowercase() != "null") {
                try {
                    clazz = Class.forName("uwu.levaltru.warvilore.software." + args[0])
                } catch (e: ClassNotFoundException) {
                    return listOf("ERROR_NULL")
                }
            } else return listOf()
            val newInstance = clazz?.constructors?.get(0)
                ?.newInstance(args.copyOfRange(1, args.size).joinToString { " " }) as? SoftwareBase ?: return listOf("ERROR_NULL")
            val lastArg = args[args.size - 1]
            if (lastArg.contains(":")) {
                return newInstance.possibleArguments().filter { it.startsWith(lastArg) }
            } else {
                val map = newInstance.possibleArguments().map { it.split(":")[0] }
                val list = mutableListOf<String>()
                for (i in map.indices) {
                    if (i == 0) list.add(map[i] + ":")
                    else if (map[i] != map[i - 1]) list.add(map[i] + ":")
                }
                if (args.size == 2) list.add("help")
                return list
            }
        }
        return listOf("ERROR_CHECK_FAIL")
    }
}