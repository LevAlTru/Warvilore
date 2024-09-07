package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.GameMode
import org.bukkit.Sound
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.abilities.interfaces.EvilAurable
import uwu.levaltru.warvilore.tickables.HealZone
import uwu.levaltru.warvilore.trashcan.LevsUtils
import uwu.levaltru.warvilore.trashcan.Namespaces

private const val COOLDOWN = 60 * 20

class TheZoner(s: String) : AbilitiesCore(s), EvilAurable {

    var cooldown: Int = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.COOLDOWN.namespace,
                PersistentDataType.INTEGER
            ) ?: 0
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.COOLDOWN.namespace,
                PersistentDataType.INTEGER, value
            )
            field = value
        }
    var allowedList: List<String> = listOf()
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.ALLOWED_NICKNAMES.namespace,
                PersistentDataType.LIST.strings()
            ) ?: listOf()
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.ALLOWED_NICKNAMES.namespace,
                PersistentDataType.LIST.strings(), value
            )
            field = value
        }

    override fun onTick(event: ServerTickEndEvent) {
        if (cooldown % 20 == 0 && cooldown > 0 && LevsUtils.isSword(player!!.inventory.itemInMainHand.type))
            player!!.sendActionBar(text("${cooldown / 20}s").color(NamedTextColor.RED))
        else if (cooldown == 1) player!!.sendActionBar(text(""))
        cooldown--
    }

    override fun onAction(event: PlayerInteractEvent) {
        if (player!!.pitch < 80) return
        if (!player!!.isSneaking) return
        if (!LevsUtils.isSword(player!!.inventory.itemInMainHand.type)) return
        if (cooldown > 0 && player!!.gameMode != GameMode.CREATIVE) return

        cooldown = COOLDOWN
        val locy = player!!.location.add(0.0, player!!.height / 2, 0.0)
        player!!.world.playSound(locy, Sound.BLOCK_ENDER_CHEST_OPEN, 1.5f, 0.5f)
        HealZone(locy, allowedList.plus(player!!.name))
    }

    override fun executeCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>) {
        when (args[0]) {
            "list" -> {
                sender.sendMessage(text("Союзники: $allowedList").color(NamedTextColor.GOLD))
            }

            "add" -> {
                if (args.size < 2) {
                    sender.sendMessage(text("Не достаточно аргументов.").color(NamedTextColor.RED))
                    return
                }
                val nick = args[1]
                if (allowedList.map { it.lowercase() }.contains(nick.lowercase())) {
                    sender.sendMessage(text("'$nick' уже в списке.").color(NamedTextColor.RED))
                    return
                }
                if (args[1].lowercase() == player!!.name.lowercase()) {
                    sender.sendMessage(text("Ты уже союзник себя <3").color(NamedTextColor.LIGHT_PURPLE))
                    return
                }
                val add = allowedList.toMutableList()
                if (add.add(nick)) {
                    allowedList = add.toList()
                    sender.sendMessage(text("Добавил '$nick' в союзники.").color(NamedTextColor.GREEN))
                } else sender.sendMessage(
                    text("не смог добавить '$nick' в список союзников по неизвестной причине.").color(
                        NamedTextColor.RED
                    )
                )
            }

            "remove" -> {
                if (args.size < 2) {
                    sender.sendMessage(text("Не достаточно аргументов.").color(NamedTextColor.RED))
                    return
                }
                val nick = args[1]
                if (args[1].lowercase() == player!!.name.lowercase()) {
                    sender.sendMessage(text("зачем???").color(NamedTextColor.LIGHT_PURPLE))
                    return
                }
                val remove = allowedList.toMutableList()
                if (remove.remove(nick)) {
                    allowedList = remove.toList()
                    sender.sendMessage(text("Убрал '$nick' из союзников.").color(NamedTextColor.GREEN))
                } else sender.sendMessage(text("'$nick' не найден в списке союзников.").color(NamedTextColor.RED))
            }

            else -> sender.sendMessage(text("Неправильные аргументы.").color(NamedTextColor.RED))
        }
    }

    override fun completeCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String>? {
        return when (args.size) {
            1 -> listOf("add", "remove", "list").filter { it.startsWith(args[0]) }
            2 -> when (args[0]) {
                "add" -> null
                "remove" -> allowedList.filter { it.startsWith(args[1]) }
                else -> listOf()
            }

            else -> listOf()
        }
    }

    override fun onDeath(event: PlayerDeathEvent) {
        cooldown = 0
    }

    override fun getEvilAura(): Double = 8.0

    override fun getAboutMe(): List<Component> = listOf(
        text("Твои навыки:").color(NamedTextColor.GREEN),
        text(""),
        text("- Когда ты на шифте и нажимаешь в низ с мечем в руках, ты создаешь зону с позитивными эффектами для себя и союзников.").color(NamedTextColor.GREEN),
        text("  - Союзников можно выбирать через /abilka.").color(NamedTextColor.GREEN),
        text("  - Позитивные эффекты: Сила 2, Регенерация 2, Сопротивление 1.").color(NamedTextColor.GREEN),
        text("  - Зона перезаряжается 60 секунд и длится 25 секунд.").color(NamedTextColor.GOLD),
    )

}