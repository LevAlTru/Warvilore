package uwu.levaltru.warvilore.abilities.abilities

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.abilities.interfaces.EvilAurable
import uwu.levaltru.warvilore.trashcan.Namespaces

class TestAbility(nickname: String) : AbilitiesCore(nickname), EvilAurable {
    override fun completeCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String>? {
        return when (args.size) {
            1 -> Namespaces.values().map { it.name }.filter { it.lowercase().contains("world") }
            2 -> listOf("value")
            else -> listOf("")
        }
    }

    override fun executeCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>) {
        if (sender !is Player) return
        try {
            val valueOf = Namespaces.valueOf(args[0])
            val double = args[1].toDouble()

            player!!.world.persistentDataContainer.set(valueOf.namespace, PersistentDataType.DOUBLE, double)

            sender.sendMessage(Component.text("set ${valueOf.namespace} to $double").color(NamedTextColor.GREEN))

        } catch (e: Exception) {
            sender.sendMessage(Component.text("error: $e").color(NamedTextColor.RED))
        }
    }

    override fun getAboutMe(): List<Component> = listOf(text("test ability").color(NamedTextColor.DARK_PURPLE))
    override fun getEvilAura(): Double = 5.0
}