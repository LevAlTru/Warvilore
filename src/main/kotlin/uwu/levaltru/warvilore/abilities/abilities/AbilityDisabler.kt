package uwu.levaltru.warvilore.abilities.abilities

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.tickables.projectiles.RemoveAbilitiesSphere

class AbilityDisabler(nickname: String) : AbilitiesCore(nickname) {

    override fun executeCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>) {
        
        when (args[0]) {
            "sphere" -> RemoveAbilitiesSphere(player!!.eyeLocation, args[1].toDouble())
            "player" -> {
                val player = Bukkit.getPlayer(args[1])
                if (player?.getAbilities() == null) {
                    sender.sendMessage(text("The player has not any abilities").color(NamedTextColor.RED))
                    return
                } else player.getAbilities()!!.abilitiesDisabled =
                    if (args[2] == "true") true else if (args[2] == "false") false else {
                        sender.sendMessage(text("Not true if false").color(NamedTextColor.RED))
                        return
                    }
            }
        }
        
    }

    override fun completeCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String>? {
        return when (args.size) {
            1 -> listOf("sphere", "player")
            2 -> when (args[0]) {
                    "sphere" -> listOf("size")
                    "player" -> null
                    else -> listOf()
                }
            3 -> listOf("true", "false")
            else -> listOf()
        }
    }

    override fun getAboutMe(): List<Component> = listOf(
        text("disable spheres").color(NamedTextColor.DARK_PURPLE)
    )
}