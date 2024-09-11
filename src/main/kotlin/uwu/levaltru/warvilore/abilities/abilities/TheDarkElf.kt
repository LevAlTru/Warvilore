package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
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

private const val DARKNESS_TIME = 20 * 20

class TheDarkElf(nickname: String) : AbilitiesCore(nickname), EvilAurable {
    override fun onJoin(event: PlayerJoinEvent) {
        player!!.getAttribute(Attribute.GENERIC_SCALE)!!.addModifier(
            AttributeModifier(
                Warvilore.namespace("temp_shortcurse"),
                -0.17,
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
            )
        )
    }

    override fun onTick(event: ServerTickEndEvent) {
        if (abilitiesDisabled) return
        if (player!!.location.block.lightFromBlocks < 2 &&
            player!!.location.block.lightFromSky < 2)
            player!!.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, 15, 1, true, false, true))
    }

    override fun onAttack(event: EntityDamageByEntityEvent) {
        if (abilitiesDisabled) return
        if (event.damageSource.damageType == DamageType.ARROW)
            (event.entity as? LivingEntity)?.addPotionEffect(PotionEffect(PotionEffectType.DARKNESS, DARKNESS_TIME, 0, false, true, true))
    }

    override fun getAboutMe(): List<Component> = listOf(
        text("Твои умения:").color(NamedTextColor.GREEN),
        text(""),
        text("- Ты становишься невидимым в темноте (но не при луне).").color(NamedTextColor.GREEN),
        text(""),
        text("- Твои стрелы дают эффект тьмы.").color(NamedTextColor.GREEN),
        text(""),
        text("- shortь.").color(NamedTextColor.GOLD),
    )

    override fun getEvilAura(): Double = 4.0
}