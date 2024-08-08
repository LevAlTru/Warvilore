package uwu.levaltru.warvilore.software

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.SoftwareBase

class Coordinates(string: String) : SoftwareBase(string) {

    override fun tick(player: Player): Boolean {
        val toVector = player.location.toVector()
        var color = NamedTextColor.GOLD
        val worldName = player.world.name
        when (arguments["show"]) {
            "other" -> {
                if (worldName == "world_the_end") {
                    player.sendActionBar(
                        Component.text(
                            "%.2f | %.2f | %.2f"
                                .format(toVector.x, toVector.y, toVector.z)
                        ).color(NamedTextColor.LIGHT_PURPLE)
                    )
                    return false
                }
                when (worldName) {
                    "world_nether" -> {
                        toVector.multiply(Vector(8.0, 1.0, 8.0))
                        color = NamedTextColor.GREEN
                    }
                }
            }

            "current", null -> {
                if (worldName == "world_the_end") {
                    player.sendActionBar(
                        Component.text(
                            "%.2f | %.2f | %.2f"
                                .format(toVector.x, toVector.y, toVector.z)
                        ).color(NamedTextColor.LIGHT_PURPLE)
                    )
                    return false
                }
                when (worldName) {
                    "world_nether" -> {
                        color = NamedTextColor.RED
                    }

                    "world" -> {
                        color = NamedTextColor.GREEN
                    }
                }
            }

            "both" -> {
                if (worldName == "world_the_end") {
                    player.sendActionBar(
                        Component.text(
                            "%.2f | %.2f | %.2f"
                                .format(toVector.x, toVector.y, toVector.z)
                        ).color(NamedTextColor.LIGHT_PURPLE)
                    )
                    return false
                }
                val nether: Vector
                val overworld: Vector

                when (worldName) {
                    "world_nether" -> {
                        nether = toVector.clone()
                        overworld = toVector.clone().multiply(Vector(8.0, 1.0, 8.0))
                    }

                    else -> {
                        nether = toVector.clone().multiply(Vector(0.125, 1.0, 0.125))
                        overworld = toVector.clone()
                    }
                }

                player.sendActionBar(text("%.2f | %.2f | %.2f".format(overworld.x, overworld.y, overworld.z)).color(
                    NamedTextColor.GREEN
                )
                    .append { text(" | ").color(NamedTextColor.GOLD) }
                    .append {
                        text(
                            "%.2f | %.2f | %.2f".format(
                                nether.x,
                                nether.y,
                                nether.z
                            )
                        ).color(NamedTextColor.RED)
                    })
                return false
            }

            else -> {
                player.sendMessage(text("Invalid argument (${arguments["show"]})").color(NamedTextColor.RED))
                return true
            }
        }
        player.sendActionBar(
            Component.text("%.2f | %.2f | %.2f".format(toVector.x, toVector.y, toVector.z)).color(color)
        )
        return false
    }

    override fun possibleArguments(): List<String> = listOf("show:other", "show:current", "show:both")

    override fun description() = listOf(
        text("Shows coordinates like you've pressed F3.").color(NamedTextColor.RED),
        text("Also it is the second software ever created (for arguments testing).").color(NamedTextColor.RED),
    )


}