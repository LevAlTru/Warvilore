package uwu.levaltru.warvilore.software

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.SoftwareBase
import kotlin.reflect.jvm.internal.impl.descriptors.Named

class Coordinates(val string: String) : SoftwareBase() {

    override fun tick(player: Player) {
        val name = player.world.name
        val color: TextColor
        val loc = player.location.toVector()
        when (string) {
            "type:portal" -> {
                when (name) {
                    "world" -> {
                        loc.multiply(Vector(0.125, 1.0, 0.125))
                        color = NamedTextColor.RED
                    }

                    "world_nether" -> {
                        loc.multiply(Vector(8.0, 1.0, 8.0))
                        color = NamedTextColor.GREEN
                    }

                    else -> color = NamedTextColor.GOLD
                }
                player.sendActionBar(text("%2d | %2d | %2d".format(loc.x, loc.y, loc.z)).color(color))
            }

            "type:current" -> {
                when (name) {
                    "world" -> color = NamedTextColor.GREEN
                    "world_nether" -> color = NamedTextColor.RED
                    "world_the_end" -> color = NamedTextColor.LIGHT_PURPLE
                    else -> color = NamedTextColor.GOLD
                }
                player.sendActionBar(text("%2d | %2d | %2d".format(loc.x, loc.y, loc.z)).color(color))
            }

            "type:both" -> {
                var locO = loc.clone()
                var locN = loc.clone()
                when (name) {
                    "world" -> locN.multiply(Vector(0.125, 1.0, 0.125))
                    "world_nether" -> locO.multiply(Vector(8.0, 1.0, 8.0))
                    else -> {
                        player.sendActionBar(
                            text(
                                "%2d | %2d | %2d".format(
                                    locO.x,
                                    locO.y,
                                    locO.z
                                )
                            ).color(NamedTextColor.GOLD)
                        )
                        return
                    }
                }
                player.sendActionBar(text("%2d | %2d | %2d".format(locO.x, locO.y, locO.z)).color(NamedTextColor.GREEN)
                    .append { text(" |-| ").color(NamedTextColor.GOLD) }
                    .append { text("%2d | %2d | %2d".format(locN.x, locN.y, locN.z)) }.color(NamedTextColor.RED)
                )
            }
        }
    }

    override val arguments: List<String>
        get() = listOf(
            "type:portal", "type:both", "type:adaptive"
        )

    override val description: List<TextComponent>
        get() = listOf(
            text("Shows coordinates like if you pressed F3."),
            text("Also it is the second software ever created for testing arguments system.")
        )

}