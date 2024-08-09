package uwu.levaltru.warvilore

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import org.bukkit.entity.Player

abstract class SoftwareBase(val s: String) {

    var arguments: HashMap<String, String> = HashMap()

    init {
        for (s1 in s.split(" ")) {
            val split = s1.split(":")
            if (split.size >= 2)
                arguments[split[0]] = split[1]
        }
    }

    abstract fun description(): List<TextComponent>
    abstract fun possibleArguments(): List<String>

    abstract fun tick(player: Player): Boolean
    open fun onShutDown(player: Player) {}
    open fun onDeath(player: Player) {}

    fun text(text: String) = Component.text(text)
}