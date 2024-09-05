package uwu.levaltru.warvilore.abilities.bases

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import io.papermc.paper.tag.EntityTags
import org.bukkit.attribute.Attribute
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.inventory.InventoryHolder
import org.bukkit.potion.PotionEffectType
import uwu.levaltru.warvilore.abilities.AbilitiesCore

abstract class Undead(string: String) : AbilitiesCore(string) {

    override fun onTick(event: ServerTickEndEvent) {
        if (player!!.health > player!!.getAttribute(Attribute.GENERIC_MAX_HEALTH)!!.value - 1.5 && player!!.activePotionEffects.map { it.type }.contains(PotionEffectType.REGENERATION)) {
            player!!.damage(0.01)
            player!!.health -= 1.5
        }
        if (player!!.health < 1.5 && player!!.activePotionEffects.map { it.type }.contains(PotionEffectType.POISON))
            player!!.heal(1.5, EntityRegainHealthEvent.RegainReason.CUSTOM)
    }

    override fun onDamage(event: EntityDamageEvent) {
        when (event.damageSource.damageType) {
            DamageType.MAGIC, DamageType.INDIRECT_MAGIC -> {
                event.isCancelled = true
                player!!.heal(event.damage, EntityRegainHealthEvent.RegainReason.CUSTOM)
            }
            DamageType.DROWN -> {
                event.isCancelled = true
                EntityTags.UNDEADS
            }
            DamageType.MOB_ATTACK, DamageType.PLAYER_ATTACK, DamageType.MOB_ATTACK_NO_AGGRO -> {
                val causingEntity = event.damageSource.causingEntity
                if (causingEntity is Player) {
                    val itemMeta = causingEntity.inventory.itemInMainHand.itemMeta
                    if (itemMeta != null) event.damage += itemMeta.getEnchantLevel(Enchantment.SMITE) * 2.5

                } else if (causingEntity is InventoryHolder) causingEntity.inventory.sumOf { (it?.itemMeta?.getEnchantLevel(Enchantment.SMITE) ?: 0) * 2.5 }
            }
        }
    }

    override fun onHeal(event: EntityRegainHealthEvent) {
        when (event.regainReason) {
            EntityRegainHealthEvent.RegainReason.MAGIC, EntityRegainHealthEvent.RegainReason.MAGIC_REGEN -> {
                event.isCancelled = true
                player!!.damage(event.amount, DamageSource.builder(DamageType.GENERIC).build())
            }
            else -> {}
        }
    }
}