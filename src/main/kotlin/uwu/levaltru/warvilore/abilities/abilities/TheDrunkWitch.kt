package uwu.levaltru.warvilore.abilities.abilities

import net.kyori.adventure.text.Component
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.abilities.interfaces.CanSeeSouls
import uwu.levaltru.warvilore.abilities.interfaces.EvilAurable

class TheDrunkWitch(string: String) : AbilitiesCore(string), EvilAurable, CanSeeSouls {
    override fun getEvilAura(): Double {
        TODO("DrunkWhich: dark aura is not implemented yet")
    }

    override fun getAboutMe(): List<Component> {
        TODO("Not yet implemented")
    }
}