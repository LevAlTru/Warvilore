package uwu.levaltru.warvilore.abilities.abilities

import net.kyori.adventure.text.Component
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.event.player.PlayerJoinEvent
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.AbilitiesCore

class Froggit(nickname: String) : AbilitiesCore(nickname) {

    override fun onJoin(event: PlayerJoinEvent) {
        for (modifier in player!!.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)!!.modifiers) {
            if (modifier?.name == "frogyboost") {
                player!!.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)!!.removeModifier(modifier)
                break
            }
        }
        player!!.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)!!.addModifier(
            AttributeModifier(
                Warvilore.namespace("frogyboost"),
                0.5,
                AttributeModifier.Operation.ADD_SCALAR
            )
        )

        for (modifier in player!!.getAttribute(Attribute.GENERIC_SAFE_FALL_DISTANCE)!!.modifiers) {
            if (modifier?.name == "frogyboost") {
                player!!.getAttribute(Attribute.GENERIC_SAFE_FALL_DISTANCE)!!.removeModifier(modifier)
                break
            }
        }
        player!!.getAttribute(Attribute.GENERIC_SAFE_FALL_DISTANCE)!!.addModifier(
            AttributeModifier(
                Warvilore.namespace("frogyboost"),
                5.0,
                AttributeModifier.Operation.ADD_NUMBER
            )
        )
    }


    override fun getAboutMe(): List<Component> {
        TODO("Not yet implemented")
    }
}