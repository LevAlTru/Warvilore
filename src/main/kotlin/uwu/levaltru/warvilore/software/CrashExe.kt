package uwu.levaltru.warvilore.software

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.title.Title
import org.bukkit.Sound
import org.bukkit.entity.Player
import uwu.levaltru.warvilore.SoftwareBase
import uwu.levaltru.warvilore.abilities.AbilitiesCore.Companion.getAbilities
import uwu.levaltru.warvilore.abilities.abilities.WalkingComputer
import java.time.Duration

class CrashExe(string: String) : SoftwareBase(string) {

    var i1 = 64 * 5 + 4

    override fun tick(player: Player): Boolean {
        var string = ""
        val i = i1 / 5
        if (i shr 5 and 1 == 1) string += "| " else string += ". "
        if (i shr 4 and 1 == 1) string += "| " else string += ". "
        if (i shr 3 and 1 == 1) string += "| " else string += ". "
        if (i shr 2 and 1 == 1) string += "| " else string += ". "
        if (i shr 1 and 1 == 1) string += "| " else string += ". "
        if (i shr 0 and 1 == 1) string += "|" else string += "."
        player.showTitle(Title.title(text(if ((i1 / 2) % 2 == 0) "$i" else "").color(NamedTextColor.RED),
            text(string).style(Style.style(TextDecoration.BOLD, NamedTextColor.RED)),
            Title.Times.times(Duration.ZERO, Duration.ofSeconds(1L), Duration.ZERO)))
        if ((i1 / 2) % 2 == 0) player.world.playSound(player, Sound.BLOCK_DISPENSER_FAIL, 1f, 1.5f)
        if (i1-- <= 0 || player.isDead) {
            val abilities = player.getAbilities()
            if (abilities is WalkingComputer) {
                abilities.killButPretty()
                if (player.isInWaterOrBubbleColumn) abilities.electricExplosion(30.0)
                return true
            }
            player.health = 0.0
            return true
        }
        return false
    }

    override fun onShutDown(player: Player) {
        player.showTitle(Title.title(text(""), text("")))
    }

    override fun possibleArguments(): List<String> = listOf()

    override fun description(): List<TextComponent> = listOf(text("=3").color(NamedTextColor.DARK_RED))

}