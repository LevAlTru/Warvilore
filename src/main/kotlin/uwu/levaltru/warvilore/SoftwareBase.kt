package uwu.levaltru.warvilore

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.entity.Player

abstract class SoftwareBase(vararg s: String) {

    abstract val description: List<TextComponent>
    abstract val arguments: List<String>

    abstract fun tick(player: Player)

    fun text(text: String) = Component.text(text)
}