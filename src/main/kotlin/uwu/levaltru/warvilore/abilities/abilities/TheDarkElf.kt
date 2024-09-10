package uwu.levaltru.warvilore.abilities.abilities

import net.kyori.adventure.text.Component
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.event.player.PlayerJoinEvent
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.abilities.interfaces.EvilAurable

class TheDarkElf(nickname: String) : AbilitiesCore(nickname), EvilAurable {
    override fun onJoin(event: PlayerJoinEvent) {
        for (modifier in player!!.getAttribute(Attribute.GENERIC_SCALE)!!.modifiers) {
            if (modifier?.name == "shortcurse") {
                player!!.getAttribute(Attribute.GENERIC_SCALE)!!.removeModifier(modifier)
                break
            }
        }
        player!!.getAttribute(Attribute.GENERIC_SCALE)!!.addModifier(
            AttributeModifier(
                Warvilore.namespace("shortcurse"),
                -0.2,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
            )
        )
    }

    override fun getAboutMe(): List<Component> {
        TODO("Not yet implemented")
    }

    override fun getEvilAura(): Double = 4.0
}