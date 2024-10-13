package uwu.levaltru.warvilore.abilities.abilities

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import org.bukkit.damage.DamageType
import org.bukkit.event.entity.EntityDamageEvent
import uwu.levaltru.warvilore.trashcan.LevsUtils
import kotlin.math.roundToInt

class KittyWitheredCat(nickname: String) : KittyCat(nickname) {
    override fun onDamage(event: EntityDamageEvent) {
        super.onDamage(event)
        if (!event.isCancelled) {
            when(event.damageSource.damageType) {
                DamageType.HOT_FLOOR, DamageType.WITHER -> event.isCancelled = true
            }
        }
    }

    override fun getAboutMe(): List<Component> = super.getAboutMe().toMutableList().apply {

        val string = "  - Ты не получаешь урона от магма блоков и иссушения."

        addAll(listOf(
            text(""),
            LevsUtils.rainbowText("- Заражение незера", NamedTextColor.RED, NamedTextColor.GOLD),
            LevsUtils.rainbowText(string, NamedTextColor.RED, NamedTextColor.GREEN),
        ))
    }
}