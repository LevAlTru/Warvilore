package uwu.levaltru.warvilore.abilities.abilities

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import uwu.levaltru.warvilore.abilities.bases.MafiosyBase

class MafiosyGreater(nickname: String) : MafiosyBase(nickname, 20 * 60 * 4, 20 * 20) {
//    override fun getEvilAura(): Double {
//        if (isMafiInvisible()) return 20.0
//        return 6.0
//    }

    override fun getAboutMe(): List<Component> = listOf(
        text("Твои навыки:").color(NamedTextColor.GREEN),
        text("- При нажатии на любой меч, ты уходишь в ").color(NamedTextColor.GREEN).append {
            text("true ").style(Style.style(TextDecoration.ITALIC, NamedTextColor.GREEN))
        }.append {
            text("невидимость на 20 секунд.").color(NamedTextColor.GREEN)
        },
        text("- - Перезаряжается 4 минуты.").color(NamedTextColor.GOLD),
    )
}