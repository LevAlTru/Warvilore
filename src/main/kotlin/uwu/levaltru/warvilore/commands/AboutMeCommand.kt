package uwu.levaltru.warvilore.commands

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import uwu.levaltru.warvilore.abilities.AbilitiesCore.Companion.getAbilities

class AboutMeCommand : CommandExecutor {
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>?): Boolean {
        if (sender !is Player) {
            sender.sendMessage(Component.text("Ты не игрок.").color(NamedTextColor.RED))
            return true
        }
        val abilities = sender.getAbilities()
        if (abilities == null) {
            sender.sendMessage(Component.text("У вас нет своего origin'a.").color(NamedTextColor.RED))
            return true
        }
        sender.sendMessage("")
        sender.sendMessage(Component.text(">- ${abilities::class.java.simpleName} -<").color(NamedTextColor.LIGHT_PURPLE))
        for (textComponent in abilities.getAboutMe()) {
            sender.sendMessage(textComponent)
        }
        sender.sendMessage("")
        return true
    }
}