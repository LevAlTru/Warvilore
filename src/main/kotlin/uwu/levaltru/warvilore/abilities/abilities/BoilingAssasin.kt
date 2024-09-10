package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.tickables.projectiles.ReallyFastArrow

class BoilingAssasin(string: String) : AbilitiesCore(string) {

    override fun onTick(event: ServerTickEndEvent) {
        if (!abilitiesWork()) return
        player!!.addPotionEffects(listOf(
            PotionEffect(PotionEffectType.INVISIBILITY, 10, 1, true, true, true),
            PotionEffect(PotionEffectType.SPEED, 10, 1, true, true, true),
        ))
    }

    override fun onBowShooting(event: EntityShootBowEvent) {
        if (!abilitiesWork()) return

        val bow = event.bow
        val enchantments = bow?.enchantments
        var damage = event.force.toDouble() * 3.4
        damage += (enchantments?.get(Enchantment.POWER) ?: 0) * 3 * event.force.toDouble()
        damage *= 0.6
        val knockback = enchantments?.get(Enchantment.PUNCH) ?: 0
        val fire = enchantments?.keys?.contains(Enchantment.FLAME) ?: false

        val effectDevided8 = mutableListOf<PotionEffect>()
        val arrow = event.consumable
        if (arrow?.type == Material.SPECTRAL_ARROW)
            effectDevided8.add(PotionEffect(PotionEffectType.GLOWING,
            200, 0, false, true, true))
        else {
            val itemMeta = arrow?.itemMeta
            if (itemMeta is PotionMeta) {
                val potionEffects = itemMeta.basePotionType?.potionEffects
                if (potionEffects != null)
                    for (effect in potionEffects) {
                        effectDevided8.add(
                            PotionEffect(
                                effect.type, effect.duration / 8 + 1, effect.amplifier,
                                false, true, true
                            )
                        )
                    }
            }
        }

        val direction = player!!.location.direction
        ReallyFastArrow(player!!.eyeLocation.add(direction.clone().multiply(0.33)),
            direction.multiply(1.66 * event.force), player!!.uniqueId, damage, knockback, fire, effectDevided8)
        bow?.damage(1, player!!)
    }

    fun abilitiesWork(): Boolean {
        if (!isHeldingABow()) return false
        if (player == null) return false
        for (stack in player!!.inventory.armorContents)
            if (stack != null) return false
        return true
    }

    private fun isHeldingABow(): Boolean {
        val inventory = player!!.inventory
        if (inventory.itemInOffHand.type == Material.BOW || inventory.itemInMainHand.type == Material.BOW) return true
        return false
    }

    override fun getAboutMe(): List<Component> = listOf(
        text("За твои долгие года жизни бытья наемником ты научился:").color(NamedTextColor.GREEN),
        text("- Бежать эффективнее когда ты держишь лук.").color(NamedTextColor.GREEN),
        text("- Уходить в невидимость когда ты держишь лук.").color(NamedTextColor.GREEN),
        text("- Стрелять стрелами в разы быстрее.").color(NamedTextColor.GREEN),
        text("  - Иза этого стрелы с эффектами разносят эффекты вокруг.").color(NamedTextColor.GREEN),
        text("- Но иза того что лук требует каждую частичку твоего тела, ты не можешь себя сковывать броней, если хочешь стрелять во всю силу").color(NamedTextColor.RED)
    )

}