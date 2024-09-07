package uwu.levaltru.warvilore.software

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import uwu.levaltru.warvilore.SoftwareBase
import uwu.levaltru.warvilore.trashcan.LevsUtils

class CPURuntime(val string: String) : SoftwareBase(string) {
    override fun tick(player: Player): Boolean {
        val ticksLived = player.ticksLived

        if (ticksLived % 20 == 0) {
            var string = LevsUtils.toTime(ticksLived)

            player.sendActionBar(Component.text(string).color(NamedTextColor.RED))
        }
        return false
    }

    override fun possibleArguments() = listOf<String>()

    override fun description() = listOf(
        text("System active time.").color(NamedTextColor.LIGHT_PURPLE),
        text("Also it is the first software ever created.").color(NamedTextColor.LIGHT_PURPLE),
    )
}