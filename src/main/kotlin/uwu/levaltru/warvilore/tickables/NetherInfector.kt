package uwu.levaltru.warvilore.tickables

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material.*
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import org.bukkit.block.data.Directional
import org.bukkit.block.data.Rotatable
import uwu.levaltru.warvilore.Tickable
import kotlin.random.Random

private val random by lazy { Random }

class NetherInfector(location: Location) : Tickable() {

    val location = location.toCenterLocation()

    override fun tick(): Boolean {

        val nextInt = random.nextInt(0, 6)

        location.add(
            if (nextInt == 0) 1.0 else if (nextInt == 1) -1.0 else 0.0,
            if (nextInt == 2) 1.0 else if (nextInt == 3) -1.0 else 0.0,
            if (nextInt == 4) 1.0 else if (nextInt == 5) -1.0 else 0.0
        )

        return age++ > 256
    }

    companion object {
        fun changeBlock(location: Location): Boolean {
            val b = location.block
            val blockData = b.blockData
            when (b.type) {
                STONE_SWORD, GRANITE, DIORITE, ANDESITE,
                COAL_ORE, IRON_ORE, DIAMOND_ORE, LAPIS_ORE, EMERALD_ORE, REDSTONE_ORE, COPPER_ORE
                -> b.type = SMOOTH_BASALT

                DEEPSLATE, COBBLED_DEEPSLATE, TUFF,
                DEEPSLATE_COAL_ORE, DEEPSLATE_IRON_ORE, DEEPSLATE_DIAMOND_ORE, DEEPSLATE_LAPIS_ORE, DEEPSLATE_EMERALD_ORE, DEEPSLATE_REDSTONE_ORE, DEEPSLATE_COPPER_ORE
                -> b.type = BLACKSTONE

                GOLD_ORE, DEEPSLATE_GOLD_ORE,
                -> b.type = GILDED_BLACKSTONE

                POLISHED_TUFF, POLISHED_ANDESITE, POLISHED_DIORITE, POLISHED_GRANITE, POLISHED_DEEPSLATE,
                -> b.type = POLISHED_BLACKSTONE

                CHISELED_TUFF, CHISELED_DEEPSLATE, CHISELED_TUFF_BRICKS,
                -> b.type = CHISELED_POLISHED_BLACKSTONE

                DIRT, DIRT_PATH, COARSE_DIRT, ROOTED_DIRT,
                -> b.type = NETHERRACK

                GRASS_BLOCK,
                -> b.type = CRIMSON_NYLIUM

                PODZOL, MYCELIUM,
                -> b.type = WARPED_NYLIUM

                MUD, GRAVEL, SUSPICIOUS_GRAVEL, RED_SAND,
                -> b.type = SOUL_SOIL

                SAND, SUSPICIOUS_SAND,
                -> b.type = SOUL_SAND

                OAK_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG, MANGROVE_LOG -> {
                    b.type = CRIMSON_STEM
                    applyRotation(b, blockData)
                    Bukkit.getOnlinePlayers().forEach { it.sendMessage(blockData::class.java.name) }
                }

                BIRCH_LOG, CHERRY_LOG, SPRUCE_LOG -> {
                    b.type = WARPED_STEM
                    applyRotation(b, blockData)
                    Bukkit.getOnlinePlayers().forEach { it.sendMessage(blockData::class.java.name) }
                }

                STRIPPED_OAK_LOG, STRIPPED_JUNGLE_LOG, STRIPPED_ACACIA_LOG, STRIPPED_DARK_OAK_LOG, STRIPPED_MANGROVE_LOG -> {
                    b.type = STRIPPED_CRIMSON_STEM
                    applyRotation(b, blockData)
                    Bukkit.getOnlinePlayers().forEach { it.sendMessage(blockData::class.java.name) }
                }

                STRIPPED_BIRCH_LOG, STRIPPED_CHERRY_LOG, STRIPPED_SPRUCE_LOG -> {
                    b.type = STRIPPED_WARPED_STEM
                    applyRotation(b, blockData)
                    Bukkit.getOnlinePlayers().forEach { it.sendMessage(blockData::class.java.name) }
                }

                STRIPPED_OAK_WOOD, STRIPPED_JUNGLE_WOOD, STRIPPED_ACACIA_WOOD, STRIPPED_DARK_OAK_WOOD, STRIPPED_MANGROVE_WOOD -> {
                    b.type = STRIPPED_CRIMSON_HYPHAE
                    applyRotation(b, blockData)
                    Bukkit.getOnlinePlayers().forEach { it.sendMessage(blockData::class.java.name) }
                }

                STRIPPED_BIRCH_WOOD, STRIPPED_CHERRY_WOOD, STRIPPED_SPRUCE_WOOD -> {
                    b.type = STRIPPED_WARPED_HYPHAE
                    applyRotation(b, blockData)
                    Bukkit.getOnlinePlayers().forEach { it.sendMessage(blockData::class.java.name) }
                }

                OAK_WOOD, JUNGLE_WOOD, ACACIA_WOOD, DARK_OAK_WOOD, MANGROVE_WOOD -> {
                    b.type = CRIMSON_STEM
                    applyRotation(b, blockData)
                    Bukkit.getOnlinePlayers().forEach { it.sendMessage(blockData::class.java.name) }
                }

                BIRCH_WOOD, CHERRY_WOOD, SPRUCE_WOOD -> {
                    b.type = WARPED_STEM
                    applyRotation(b, blockData)
                    Bukkit.getOnlinePlayers().forEach { it.sendMessage(blockData::class.java.name) }
                }

                BAMBOO_BLOCK, STRIPPED_BAMBOO_BLOCK -> {
                    b.type = POLISHED_BASALT
//                    (b.blockData as Directional).facing = (blockData as Directional).facing
                    Bukkit.getOnlinePlayers().forEach { it.sendMessage(blockData::class.java.name) }
                }

                BAMBOO_PLANKS, BAMBOO_MOSAIC -> {
                    b.type = BASALT
//                    b.blockData = (b.blockData as Rotatable).apply { facing = if (random.nextInt(1) == 0) BlockFace.EAST else BlockFace.NORTH }
                }

                else -> return false
            }
            return true
        }

        private fun applyRotation(b: Block, blockData: BlockData) {
            if (blockData !is Rotatable) return
            (b.blockData as Rotatable).rotation = blockData.rotation
        }

        private fun applyDirection(b: Block, blockData: BlockData) {
            if (blockData !is Directional) return
            (b.blockData as Directional).facing = blockData.facing
        }
    }
}