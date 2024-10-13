package uwu.levaltru.warvilore.abilities.abilities

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.DeveloperMode
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.tickables.NetherInfector
import uwu.levaltru.warvilore.tickables.TheUpperEmitter
import uwu.levaltru.warvilore.tickables.untraditional.NetherEmitter
import kotlin.math.floor

class TheBringer(n: String) : AbilitiesCore(n) {

    var seeTheThings = false

    override fun executeCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>) {
        when (args[0].lowercase()) {
            "blocks" -> {
                if (!DeveloperMode) {
                    player!!.sendMessage(text("Development Mode is turned off").color(NamedTextColor.RED))
                    return
                }
                var dx = 0
                var dz = 0

                for (material in Material.entries) {
                    if (!material.isBlock) continue
                    dx += 2
                    if (dx > 100) {
                        dx = 0
                        dz += 5
                    }
                    player!!.location.add(Vector(dx, 0, dz))
                        .block.setType(material, false)

                    val block = player!!.location.add(Vector(dx, 3, dz)).block
                    block.setType(material, false)

                    NetherInfector.changeBlock(block.location)

                }

            }

            "spawn" -> {
                NetherInfector(player!!.location, player!!.location.direction, args[1].toInt())
            }

            "toggle" -> {
                seeTheThings = !seeTheThings
                sender.sendMessage(text(seeTheThings.toString()).color(NamedTextColor.GOLD))
            }

            "emitter" -> {
                when (args[1].lowercase()) {
                    "edit" -> {
                        val minBy =
                            NetherEmitter.LIST.minByOrNull { (it.x - player!!.x) * (it.x - player!!.x) + (it.z - player!!.z) * (it.z - player!!.z) }
                        if (minBy == null) {
                            player!!.sendMessage(text("No emitters found").color(NamedTextColor.RED))
                            return
                        }
                        if (minBy.world != player!!.world) {
                            player!!.sendMessage(text("Emitter is in other world").color(NamedTextColor.RED))
                            return
                        }
                        if (args.size < 4) {
                            player!!.sendMessage(text("Emitter stats: intensity=${minBy.intensity}   power=${minBy.maxPower}   x=${minBy.x}   z=${minBy.z}").color(NamedTextColor.GOLD))
                            return
                        }

                        val toFloat = args[2].toFloat()
                        val toInt = args[3].toInt()

                        player!!.sendMessage(text("Old emitter stats: intensity=${minBy.intensity}   power=${minBy.maxPower}   x=${minBy.x}   z=${minBy.z}").color(NamedTextColor.GOLD))
                        player!!.sendMessage(text("New emitter stats: intensity=$toFloat   power=$toInt").color(NamedTextColor.GOLD))

                        minBy.intensity = toFloat
                        minBy.maxPower = toInt
                    }
                    "create" -> {
                        val toFloat = args[2].toFloat()
                        val toInt = args[3].toInt()
                        NetherEmitter(floor(player!!.x).toInt(), floor(player!!.z).toInt(), player!!.world, toInt, toFloat)
                        player!!.sendMessage(text("Emitter stats: intensity=$toFloat   power=$toInt").color(NamedTextColor.GOLD))
                    }
                }
            }

            "upper_emitter" -> {
                TheUpperEmitter(player!!.location, args[2].toDouble(), args[3].toDouble(), args[1].toInt())
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
            1 -> listOf("blocks", "spawn", "emitter", "toggle", "upper_emitter")

            2 -> if (args[0].lowercase() == "spawn" || args[0].lowercase() == "upper_emitter")
                listOf("energy")
            else if (args[0].lowercase() == "emitter")
                listOf("edit", "create")
            else listOf()

            3 -> if (args[0].lowercase() == "emitter" || args[0].lowercase() == "upper_emitter")
                listOf("intensity") else listOf()

            4 -> if (args[0].lowercase() == "emitter")
                listOf("power")
            else if (args[0].lowercase() == "upper_emitter")
                listOf("size")
            else listOf()

            else -> listOf()
        }
    }

    override fun getAboutMe(): List<Component> =
        listOf(text("The one who will bring chaos to the overworld").color(NamedTextColor.LIGHT_PURPLE))
}