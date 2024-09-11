package uwu.levaltru.warvilore.abilities.abilities

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import net.kyori.adventure.text.Component
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.damage.DamageType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.abilities.interfaces.EvilAurable

private const val DARKNESS_TIME = 20 * 30

class TheDarkElf(nickname: String) : AbilitiesCore(nickname), EvilAurable {
    override fun onJoin(event: PlayerJoinEvent) {
        player!!.getAttribute(Attribute.GENERIC_SCALE)!!.addModifier(
            AttributeModifier(
                Warvilore.namespace("temp_shortcurse"),
                -0.2,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
            )
        )
    }

    override fun onAttack(event: EntityDamageByEntityEvent) {
        if (event.damageSource.damageType == DamageType.ARROW) {
            (event.entity as? LivingEntity)?.addPotionEffect(
                PotionEffect(PotionEffectType.DARKNESS, DARKNESS_TIME, 0, false, true, true)
            )
        }
    }

    override fun getAboutMe(): List<Component> {
        TODO("Not yet implemented")
    }

    override fun getEvilAura(): Double = 4.0
}