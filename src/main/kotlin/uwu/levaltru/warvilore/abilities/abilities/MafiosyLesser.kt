package uwu.levaltru.warvilore.abilities.abilities

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import uwu.levaltru.warvilore.abilities.bases.MafiosyBase

class MafiosyLesser(nickname: String) : MafiosyBase(nickname, 20 * 60 * 6, 15 * 20) {
//    override fun getEvilAura(): Double {
//        if (isMafiInvisible()) return 16.0
//        return 4.0
//    }

    override fun getAboutMe(): List<Component> = listOf(
        text("Твои навыки:").color(NamedTextColor.GREEN),
        text("- При нажатии на любой меч, ты уходишь в ").color(NamedTextColor.GREEN).append {
            text("true ").style(Style.style(TextDecoration.ITALIC, NamedTextColor.GREEN))
        }.append {
            text("невидимость на 15 секунд.").color(NamedTextColor.GREEN)
        },
        text("- - Перезаряжается 6 минут.").color(NamedTextColor.GOLD)
    )
}