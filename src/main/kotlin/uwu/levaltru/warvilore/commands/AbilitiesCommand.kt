package uwu.levaltru.warvilore.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.AbilitiesCore

class AbilitiesCommand : TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {

        val size = args?.size ?: 0

        if (size < 1) return false

        if (args!![0].lowercase() == "getall") {
            sender.sendMessage(Component.text(AbilitiesCore.hashMap.toString()).color(NamedTextColor.YELLOW))
            return true
        }

        if (size < 3) return false

        if (args[0].lowercase() == "set") {

            val player = Bukkit.getPlayer(args[1])
            var clazz: Class<*>? = null
            if (args[2].lowercase() != "remove") {
                try {
                    clazz = Class.forName("uwu.levaltru.warvilore.abilities.abilities." + args[2])
                } catch (e: ClassNotFoundException) {
                    sender.sendMessage(Component.text("The class is not found: " + args[2]).color(NamedTextColor.RED))
                    return true
                }
            }
            if (player == null) {
                sender.sendMessage(Component.text("The player is not found online").color(NamedTextColor.RED))
                return false
            }
            if (args[2].lowercase() == "remove") {
                AbilitiesCore.hashMap.remove(player.name)
                AbilitiesCore.saveAbility(player)
                sender.sendMessage(Component.text("Removed ${player.name}'s abilities").color(NamedTextColor.GREEN))
                if (size < 4 || args[3].lowercase() != "hide")
                    player.sendMessage(Component.text("У вас украли origin").color(NamedTextColor.RED))
                return true
            }
            if (clazz != null) {
                if (AbilitiesCore.hashMap.containsKey(player.name)) {
                    sender.sendMessage(Component.text(
                        "Abilities of ${player.name} are already set. To replace them remove them first").color(NamedTextColor.RED))
                    return false
                }
                clazz.constructors[0].newInstance(player.name)
                sender.sendMessage(Component.text("Gave ${player.name} abilities of ${clazz.simpleName}").color(NamedTextColor.GREEN))
                if (size < 4 || args[3].lowercase() != "hide") {
                    player.sendMessage(
                        Component.text("Вам выдали ${clazz.simpleName} origin.").color(NamedTextColor.GREEN)
                    )
                    player.sendMessage(
                        Component.text("Чтобы посмотреть делали, пропишите /aboutme").color(NamedTextColor.GOLD)
                    )
                }
                AbilitiesCore.saveAbility(player)
            } else {
                sender.sendMessage(Component.text("The class is not found: " + args[2]).color(NamedTextColor.RED))
                return false
            }
        }

        return true
    }

    override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): List<String>? {
        if (args == null) return emptyList()
        when (args.size) {
            1 -> return mutableListOf("set", "getAll")
            2 -> when (args[0].lowercase()) {
                "set" -> return null
            }
            3 -> when (args[0].lowercase()) {
                "set" -> return Warvilore.abilitiesList?.plus("remove")?.filter { it.lowercase().startsWith(args[2].lowercase()) }
            }
            4 -> when (args[0].lowercase()) {
                "set" -> return listOf("hide")
            }
        }
        return emptyList()
    }
}