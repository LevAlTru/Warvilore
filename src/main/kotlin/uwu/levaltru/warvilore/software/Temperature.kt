package uwu.levaltru.warvilore.software

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import uwu.levaltru.warvilore.SoftwareBase
import uwu.levaltru.warvilore.abilities.abilities.TheColdestOne
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

class Temperature(string: String) : SoftwareBase(string) {
    override fun tick(player: Player): Boolean {

        val temp: Double

        when (arguments["mode"]) {
            "raw", null -> {
                temp = player.location.add(0.0, player.height / 2, 0.0).block.temperature
            }
            "formated" -> {
                temp = TheColdestOne.getUnderFormatedTemperature(player.location.add(0.0, player.height / 2, 0.0))
            }
            else -> {
                player.sendMessage(text("Invalid argument (${arguments["show"]})").color(NamedTextColor.RED))
                return true
            }
        }

        val negativeRed = (170 shl 8) or 170
        val negativeBlue = (170 shl 16) or (170 shl 8)

        val d = (temp * 0.75).coerceIn(-1.0, 1.0)

        val r = (255 -
                (1.0 - (d - 1.0).absoluteValue).coerceAtLeast(0.0) * (negativeRed and 0xFF0000 shr 16) -
                (1.0 - (d + 1.0).absoluteValue).coerceAtLeast(0.0) * (negativeBlue and 0xFF0000 shr 16)).roundToInt()
        val g = (255 -
                (1.0 - (d - 1.0).absoluteValue).coerceAtLeast(0.0) * (negativeRed and 0x00FF00 shr 8) -
                (1.0 - (d + 1.0).absoluteValue).coerceAtLeast(0.0) * (negativeBlue and 0x00FF00 shr 8)).roundToInt()
        val b = (255 -
                (1.0 - (d - 1.0).absoluteValue).coerceAtLeast(0.0) * (negativeRed and 0x0000FF) -
                (1.0 - (d + 1.0).absoluteValue).coerceAtLeast(0.0) * (negativeBlue and 0x0000FF)).roundToInt()

        player.sendActionBar(text("%.2f".format(temp)).color(TextColor.color(r, g, b)))

        return false
    }

    override fun possibleArguments(): List<String> = listOf(
        "mode:raw", "mode:formated"
    )

    override fun description(): List<TextComponent> = listOf(
        text("Shows the temperature of the place on which you're standing on.").color(NamedTextColor.LIGHT_PURPLE),
        text("It isn't useful for you, but ").color(NamedTextColor.LIGHT_PURPLE)
            .append { text("The Coldest One").color(NamedTextColor.AQUA) }
            .append { text(" might look for your help.").color(NamedTextColor.LIGHT_PURPLE) },
        text(""),
        text("raw - real temperature given by \"block.temperature\"").color(NamedTextColor.YELLOW),
        text(""),
        text("formated - (rawTemperature - 0.25) * 4 / 3 + sun light * is dimension ultra-hot").color(NamedTextColor.YELLOW),
        text("  (").color(NamedTextColor.GOLD)
            .append { text("The Coldest One\'s").color(NamedTextColor.AQUA)  }
            .append { text(" temperature equation ↑)").color(NamedTextColor.GOLD) }
        )
}