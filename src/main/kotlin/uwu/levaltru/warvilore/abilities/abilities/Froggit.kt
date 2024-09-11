package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.MagmaCube
import org.bukkit.entity.Slime
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.AbilitiesCore

private const val SMALL_SLIME_DURATION = 20 * 60 * 3
private const val SMALL_HUNGER_REGAIN = 3

private const val LARGE_SLIME_DURATION = 20 * 60 * 8
private const val LARGE_HUNGER_REGAIN = 8

private const val ITEM_DURATION = 20 * 30
private const val ITEM_HUNGER_REGAIN = 2

class Froggit(nickname: String) : AbilitiesCore(nickname) {

    override fun onTick(event: ServerTickEndEvent) {
        if (player!!.isInWaterOrBubbleColumn) {
            player!!.addPotionEffects(
                listOf(
                    PotionEffect(PotionEffectType.CONDUIT_POWER, 15, 0, true, false, true),
                    PotionEffect(PotionEffectType.DOLPHINS_GRACE, 15, 0, true, false, true),
                )
            )
        }
    }

    override fun onJoin(event: PlayerJoinEvent) {
        player!!.getAttribute(Attribute.GENERIC_JUMP_STRENGTH)!!.addModifier(
            AttributeModifier(
                Warvilore.namespace("temp_frogyboost"),
                0.5,
                AttributeModifier.Operation.ADD_SCALAR
            )
        )
        player!!.getAttribute(Attribute.GENERIC_SAFE_FALL_DISTANCE)!!.addModifier(
            AttributeModifier(
                Warvilore.namespace("temp_frogyboost"),
                5.0,
                AttributeModifier.Operation.ADD_NUMBER
            )
        )
    }

    override fun onPreAttack(event: PrePlayerAttackEntityEvent) {
        if (!player!!.inventory.itemInMainHand.isEmpty) return

        val slime = event.attacked
        if (slime is Slime) {
            val effect = if (slime is MagmaCube) PotionEffectType.FIRE_RESISTANCE else PotionEffectType.RESISTANCE
            val locy = slime.location.add(0.0, slime.height / 2, 0.0)
            val createBlockData = if (slime is MagmaCube) Material.LAVA.createBlockData() else Material.SLIME_BLOCK.createBlockData()
            val potionEffect = when (slime.size) {
                0, 1 -> {
                    locy.world.spawnParticle(Particle.BLOCK, locy, 50, .3, .3, .3, .0, createBlockData, true)

                    player!!.saturation += SMALL_HUNGER_REGAIN.toFloat() / 2
                    player!!.foodLevel += SMALL_HUNGER_REGAIN

                    PotionEffect(effect, SMALL_SLIME_DURATION, 0, false, false, true)
                }
                2 -> {
                    locy.world.spawnParticle(Particle.BLOCK, locy, 60, .4, .4, .4, .0, createBlockData, true)

                    player!!.saturation += LARGE_HUNGER_REGAIN.toFloat() / 2
                    player!!.foodLevel += LARGE_HUNGER_REGAIN

                    PotionEffect(effect, LARGE_SLIME_DURATION, 0, false, false, true)
                }
                else -> return
            }
            player!!.addPotionEffect(potionEffect)
            slime.remove()
            locy.world.playSound(locy, Sound.ENTITY_SLIME_DEATH, 1f, 0.5f)
            locy.world.playSound(locy, Sound.ENTITY_FROG_EAT, 1f, 0.5f)
            locy.world.playSound(locy, Sound.ENTITY_FROG_TONGUE, 1f, 0.5f)
        }
    }

    override fun onAction(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_AIR) {
            val item = player!!.inventory.itemInMainHand
            if (item.itemMeta.hasCustomModelData()) return
            when (item.type) {
                Material.SLIME_BALL -> player!!.addPotionEffect(
                    PotionEffect(PotionEffectType.RESISTANCE, ITEM_DURATION, 0, false, false, true)
                )
                Material.MAGMA_CREAM -> player!!.addPotionEffect(
                    PotionEffect(PotionEffectType.FIRE_RESISTANCE, ITEM_DURATION, 0, false, false, true)
                )
                else -> return
            }
            player!!.saturation += ITEM_HUNGER_REGAIN.toFloat() / 2
            player!!.foodLevel += ITEM_HUNGER_REGAIN
            player!!.world.playSound(player!!, Sound.ENTITY_GENERIC_EAT, 1f, 1f)
            item.subtract()
        }
    }

    override fun getAboutMe(): List<Component> = listOf(
        text("Ты лягушка!").color(NamedTextColor.AQUA),
        text("Твои умения:").color(NamedTextColor.GREEN),
        text(""),
        text("- Ты плаваешь быстрее. Также ты умеешь дышать под водой.").color(NamedTextColor.GREEN),
        text(""),
        text("- Ты прыгаешь выше. Также дистанция падения увеличена на 5 блоков.").color(NamedTextColor.GREEN),
        text("  - (ты получаешь урон когда падаешь с 9 блоков и более).").color(NamedTextColor.GREEN),
        text(""),
        text("- Если ты ударишь по мелкому или среднему слизню, ты его съешь.").color(NamedTextColor.GREEN),
        text("  - Если ты съешь магма куба, то ты получишь огнестойкость. Если обычного, сопротивление.").color(NamedTextColor.GREEN),
        text("  - Также ты можешь есть слизь и магмовый крем.").color(NamedTextColor.GREEN),
    )
}