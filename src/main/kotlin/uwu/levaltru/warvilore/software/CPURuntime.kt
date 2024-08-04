package uwu.levaltru.warvilore.software

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import uwu.levaltru.warvilore.SoftwareBase

class CPURuntime : SoftwareBase() {
    override fun tick(player: Player) {
        val ticksLived = player.ticksLived

        if (ticksLived % 20 == 0) {
            val seconds = (ticksLived / (20)) % 60
            val minutes = (ticksLived / (20 * 60)) % 60
            val hours = (ticksLived / (20 * 60 * 60))

            var string = ""
            if (hours > 0) string += "${hours}h : "
            if (minutes > 0 || hours > 0) string += "${minutes}m : "
            string += "${seconds}s"

            player.sendActionBar(Component.text(string).color(NamedTextColor.RED))
        }
    }

    override val arguments: List<String>
        get() = emptyList()

    override val description: List<TextComponent>
        get() = listOf(
            text("System active time."),
            text("Also it is the first software ever created.")
        )
}