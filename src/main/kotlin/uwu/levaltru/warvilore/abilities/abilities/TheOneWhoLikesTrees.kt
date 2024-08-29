package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Ageable
import org.bukkit.block.data.type.Sapling
import org.bukkit.entity.Entity
import org.bukkit.entity.Item
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.bases.HatesEvilAura

class TheOneWhoLikesTrees(nickname: String) : HatesEvilAura(nickname) {

    override fun onTick(event: ServerTickEndEvent) {
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

    override fun onAction(event: PlayerInteractEvent) {
        val action = event.action
        if (action.name != "RIGHT_CLICK_AIR") return
        when (player!!.inventory.itemInMainHand.type) {
            Material.DANDELION,
            Material.POPPY,
            Material.BLUE_ORCHID,
            Material.ALLIUM,
            Material.AZURE_BLUET,
            Material.RED_TULIP,
            Material.ORANGE_TULIP,
            Material.WHITE_TULIP,
            Material.PINK_TULIP,
            Material.OXEYE_DAISY,
            Material.CORNFLOWER,
            Material.LILY_OF_THE_VALLEY,
            Material.TORCHFLOWER,
            Material.PINK_PETALS,
            Material.LILAC,
            Material.ROSE_BUSH,
            Material.PEONY,
            Material.PITCHER_PLANT -> showEvilAuraPoisoning()

            Material.NETHERITE_SWORD,
            Material.WOODEN_SWORD,
            Material.GOLDEN_SWORD,
            Material.IRON_SWORD,
            Material.STONE_SWORD,
            Material.DIAMOND_SWORD -> {

                TODO("make a three")

            }

            else -> return
        }
    }

    override fun getAboutMe(): List<Component> = listOf(
        text("Твои умения:").color(NamedTextColor.GREEN),
        text("- Когда ты стоишь около агрокультур или саженцов, они растут быстрее.").color(NamedTextColor.GREEN),
        text("- Когда ты вскапываешь агрокультуры, и ты не на шифте, они автоматически садятся обратно.").color(NamedTextColor.GREEN),
        text("- Тебе становится плохо когда ты находишся рядом с ").color(NamedTextColor.RED).append { text("темными людьми.").style(Style.style(TextDecoration.ITALIC, NamedTextColor.RED)) },
        text("  - Чтобы проверить наскольно тебе попа, нажми по воздуху с любым цветком в руках.").color(NamedTextColor.LIGHT_PURPLE),
        text("- Когда ты нажимаешь на живое существо, ты создаешь дерево на его месте.").color(NamedTextColor.GREEN),
        text("  - Существо должен стоять на месте где может вырастить дерево.").color(NamedTextColor.GOLD),
        text("  - Также если дереву будет недостаточно места, оно разрушит блоки.").color(NamedTextColor.GOLD),
    )

}