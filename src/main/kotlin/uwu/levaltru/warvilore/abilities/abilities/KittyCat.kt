package uwu.levaltru.warvilore.abilities.abilities

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.block.data.Ageable
import org.bukkit.damage.DamageType
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.inventory.meta.components.FoodComponent
import org.bukkit.inventory.meta.components.FoodComponent.FoodEffect
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.trashcan.LevsUtils.isMeatOrFish

class KittyCat(nickname: String) : AbilitiesCore(nickname) {

    override fun onJoin(event: PlayerJoinEvent) {
        for (modifier in player!!.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)!!.modifiers) {
            if (modifier?.name == "kittyboost") {
                player!!.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)!!.removeModifier(modifier)
                break
            }
        }
        player!!.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)!!.addModifier(
            AttributeModifier(
                Warvilore.namespace("kittyboost"),
                0.5,
                AttributeModifier.Operation.ADD_SCALAR
            )
        )
    }

    override fun onAttack(event: PrePlayerAttackEntityEvent) {
        if (!event.willAttack()) return
        for (modifier in player!!.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.modifiers) {
            if (modifier?.name == "kittyboost") {
                player!!.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.removeModifier(modifier)
                break
            }
        }
        if (player!!.inventory.itemInMainHand.isEmpty) {
            val add = player!!.eyeLocation.add(0.0, -0.66, 0.0).add(player!!.location.direction)
            player!!.world.spawnParticle(
                Particle.SWEEP_ATTACK,
                add,
                1,
                0.0,
                0.0,
                0.0,
                0.0,
                null,
                true
            )
            player!!.world.playSound(add, Sound.ENTITY_PLAYER_ATTACK_SWEEP, 1f, 1f)
            player!!.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)!!.addModifier(
                AttributeModifier(
                    Warvilore.namespace("kittyboost"),
                    5.0,
                    AttributeModifier.Operation.ADD_NUMBER
                )
            )
        }

    }

    override fun onDamage(event: EntityDamageEvent) {
        event.isCancelled = event.damageSource.damageType == DamageType.FALL
    }

    override fun onEating(event: PlayerItemConsumeEvent) {
        val item = event.item.type
        if (item.isMeatOrFish() || item == Material.MILK_BUCKET || item == Material.POTION || item == Material.OMINOUS_BOTTLE) return
        player!!.addPotionEffects(
            listOf(
                PotionEffect(PotionEffectType.NAUSEA, 90 * 20, 0, false, true, true),
                PotionEffect(PotionEffectType.POISON, 30 * 20, 0, false, true, true),
                PotionEffect(PotionEffectType.HUNGER, 45 * 20, 0, false, true, true)
            )
        )
    }

    override fun getAboutMe(): List<Component> = listOf(
        text("Твои навыки:").color(NamedTextColor.GREEN),
        text(""),
        text("- Ты прыгаешь выше.").color(NamedTextColor.GREEN),
        text(""),
        text("- Ты не получаешь урона от падения.").color(NamedTextColor.GREEN),
        text(""),
        text("- Когда у тебя нет предмета в руке, ты бьешь сильнее").color(NamedTextColor.GREEN),
        text(""),
        text("- Ты не можешь есть фрукты или овощи. Ты можешь есть только мясо и рыбу").color(NamedTextColor.RED),
    )
}