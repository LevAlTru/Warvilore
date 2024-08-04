package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Ageable
import org.bukkit.block.data.type.Sapling
import org.bukkit.entity.Item
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.bases.HatesEvilAura
import javax.inject.Named

class TheOneWhoLikesTrees(nickname: String) : HatesEvilAura(nickname) {

    override fun onTick(event: ServerTickEndEvent) {

        if (player == null) return

        super.onTick(event)

        val randLoc = player!!.location.clone().add(
            random.nextGaussian(0.0, 3.0),
            random.nextGaussian(0.0, 3.0),
            random.nextGaussian(0.0, 3.0)
        )
        val block = player!!.world.getBlockAt(randLoc)
        val blockData = block.blockData
        if (blockData !is Ageable) {
            if (blockData is Sapling) {
                block.applyBoneMeal(BlockFace.UP)
            }
            return
        }
        if (blockData.age < blockData.maximumAge) {
            blockData.age++
            block.blockData = blockData
            block.world.spawnParticle(
                Particle.HAPPY_VILLAGER, block.location.toCenterLocation(),
                2, 0.3, 0.3, 0.3, 0.0, null, false
            )
        }
    }

    override fun onBlockBreak(event: BlockBreakEvent) {
        val block = event.block
        val data = block.blockData
        if (data !is Ageable) return
        if (player!!.isSneaking) return

        event.isDropItems = false
        val drops = block.drops.toList()
        val materialNeeded = when (data.material) {
            Material.POTATOES -> Material.POTATO
            Material.CARROTS -> Material.CARROT
            Material.BEETROOTS -> Material.BEETROOT_SEEDS
            Material.WHEAT -> Material.WHEAT_SEEDS
            Material.TORCHFLOWER_CROP -> Material.TORCHFLOWER_SEEDS
            else -> null
        }
        for (i in drops.indices) {
            if (drops[i].type == materialNeeded) {
                drops[i].subtract()
                data.age = 0
                Bukkit.getScheduler().runTaskLater(Warvilore.instance, Runnable {
                    val blocky = block.location.block
                    blocky.type = data.material
                    blocky.blockData = data
                }, 2L)
                break
            }
        }

        for (drop in drops) {
            val spawn = block.world.spawn(
                block.location.toCenterLocation().add(0.0, -0.2, 0.0),
                Item::class.java
            ) { it.itemStack = drop }
            spawn.velocity = Vector(random.nextGaussian() * 0.1, 0.1, random.nextGaussian() * 0.1)
        }


    }

    override fun getAboutMe(): List<Component> = listOf(
        text("Твои плюсы:").color(NamedTextColor.DARK_GREEN),
        text("- Когда ты стоишь около агрокультур или саженцов, они растут быстрее.")
            .color(NamedTextColor.GREEN),
        text(
            "- Когда ты вскапываешь агрокультуры, и ты не на шифте, они автоматически садятся обратно."
        ).color(NamedTextColor.GREEN),
        text("Твои минусы:").color(NamedTextColor.DARK_RED),
        text("- Тебе становится плохо когда ты находишся рядом с ").color(NamedTextColor.RED)
            .append {
                text("темными людьми.").style(Style.style(TextDecoration.ITALIC, NamedTextColor.RED))
            },
        text("- - Чтобы проверить наскольно тебе попа, нажми по воздуху с любым цветком в руках.")
            .color(NamedTextColor.LIGHT_PURPLE),
    )

}