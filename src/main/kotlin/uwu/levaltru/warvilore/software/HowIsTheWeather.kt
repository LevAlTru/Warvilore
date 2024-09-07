package uwu.levaltru.warvilore.software

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import uwu.levaltru.warvilore.SoftwareBase
import uwu.levaltru.warvilore.trashcan.LevsUtils

class HowIsTheWeather(s: String) : SoftwareBase(s) {

    override fun tick(player: Player): Boolean {
        if (player.ticksLived % 20 != 0) return false

        val world = player.world
        val string = when {
            world.hasStorm() && !world.isThundering -> "rain"
            world.hasStorm() && world.isThundering -> "storm"
            else -> "clear"
        }
        player.sendActionBar(Component.text("weather: ").color(NamedTextColor.GOLD).append { text(string).color(NamedTextColor.LIGHT_PURPLE) }
            .append { text("  |  ").color(NamedTextColor.GOLD) }
            .append { text(LevsUtils.toTime(world.weatherDuration)).color(NamedTextColor.LIGHT_PURPLE) })
        return false
    }

    override fun description(): List<TextComponent> =
        listOf(text("Weather Teller.").color(NamedTextColor.LIGHT_PURPLE))

    override fun possibleArguments(): List<String> = listOf()
}