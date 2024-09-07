package uwu.levaltru.warvilore.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.command.TabExecutor
import org.bukkit.entity.Player
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.abilities.AbilitiesCore.Companion.getAbilities
import uwu.levaltru.warvilore.trashcan.CustomWeapons

class AbilitiesCommand : TabExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {

        val size = args?.size ?: 0

        if (size < 1) return false

        if (args!![0].lowercase() == "getall") {
            sender.sendMessage(Component.text(AbilitiesCore.hashMap.toString()).color(NamedTextColor.YELLOW))
            return true
        }

        if (args[0].lowercase() == "give") {

            if (sender !is Player) {
                sender.sendMessage(Component.text("not a player").color(NamedTextColor.RED))
                return true
            }
            if (args.size < 2) {
                sender.sendMessage(Component.text("not enough arguments").color(NamedTextColor.RED))
                return true
            }
            var soulbound: String? = null
            if (args.size > 2) {
                soulbound = args[2]
            }
            try {
                val valueOf = CustomWeapons.valueOf(args[1])
                if (sender.inventory.addItem(valueOf.giveItem(soulBouder = soulbound)).isEmpty())
                    sender.sendMessage(
                        Component.text("gave $valueOf").color(NamedTextColor.YELLOW)
                    ) else sender.sendMessage(Component.text("not enough space").color(NamedTextColor.RED))
            } catch (e: Exception) {
                sender.sendMessage(Component.text("not found " + args[1]).color(NamedTextColor.RED))
            }
            return true
        }

        if (args[0].lowercase() == "get") {
            val player = Bukkit.getPlayer(args[1])
            if (player == null) {
                sender.sendMessage(Component.text("Player is not found").color(NamedTextColor.RED))
                return true
            }
            sender.sendMessage(
                Component.text("${player.name} is ").color(NamedTextColor.GOLD)
                    .append(Component.text(player.getAbilities()?.let { it::class.simpleName } ?: "none")
                        .color(NamedTextColor.LIGHT_PURPLE))
            )
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
                    sender.sendMessage(
                        Component.text(
                            "Abilities of ${player.name} are already set. To replace them remove them first"
                        ).color(NamedTextColor.RED)
                    )
                    return false
                }
                clazz.constructors[0].newInstance(player.name)
                sender.sendMessage(
                    Component.text("Gave ${player.name} abilities of ${clazz.simpleName}").color(NamedTextColor.GREEN)
                )
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
            1 -> return mutableListOf("set", "get", "getAll", "give")
            2 -> when (args[0].lowercase()) {
                "set", "get" -> return null
                "give" -> return CustomWeapons.entries.map { it.toString() }.filter { it.lowercase().startsWith(args[1].lowercase()) }
            }

            3 -> when (args[0].lowercase()) {
                "set" -> return Warvilore.abilitiesList?.plus("remove")
                    ?.filter { it.lowercase().startsWith(args[2].lowercase()) } ?: listOf("ERROR")

                "get" -> return Warvilore.abilitiesList?.filter { it.lowercase().startsWith(args[2].lowercase()) }
                    ?: listOf("ERROR")

                "give" -> return null
            }

            4 -> when (args[0].lowercase()) {
                "set" -> return listOf("hide")
            }
        }
        return emptyList()
    }
}