package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataType
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.abilities.interfaces.CanSeeSouls
import uwu.levaltru.warvilore.abilities.interfaces.EvilAurable
import uwu.levaltru.warvilore.trashcan.Namespaces

private const val MAX_MANA: Int = 10000

class TheDrunkWitch(string: String) : AbilitiesCore(string), EvilAurable, CanSeeSouls {
    var mana: Int = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.MANA.namespace,
                PersistentDataType.INTEGER
            ) ?: MAX_MANA
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.MANA.namespace,
                PersistentDataType.INTEGER, value
            )
            field = value
        }

    override fun onTick(event: ServerTickEndEvent) {
        changeHealth()
    }

    override fun onAction(event: PlayerInteractEvent) {

        val action = event.action
        if (action.isRightClick) mana += 100
        else if (action.isLeftClick) mana -= 100

        player!!.sendActionBar(text(mana.toString()).color(NamedTextColor.GOLD))
    }

    override fun onHeal(event: EntityRegainHealthEvent) {
        event.amount *= (manaFrom0To1() * 2 - .5).coerceIn(0.0, 1.0)
    }

    private fun changeHealth() {
        val maxHealth = (manaFrom0To1() + 0.4).coerceIn(0.5, 1.0)
        for (modifier in player!!.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.modifiers) {
            if (modifier?.name == "luckOfMana") {
                player!!.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.removeModifier(modifier)
                break
            }
        }
        player!!.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.addModifier(AttributeModifier("luckOfMana",
            maxHealth - 1, AttributeModifier.Operation.MULTIPLY_SCALAR_1))
        player!!.health = player!!.health.coerceAtMost(player!!.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value)
    }

    private fun manaFrom0To1() = mana.toDouble() / MAX_MANA

    override fun getEvilAura(): Double = manaFrom0To1() * 20.0 + 4.0

    override fun getAboutMe(): List<Component> {
        TODO("Not yet implemented")
    }
}