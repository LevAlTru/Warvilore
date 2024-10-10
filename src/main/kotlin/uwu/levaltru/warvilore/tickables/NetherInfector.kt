package uwu.levaltru.warvilore.tickables

import org.bukkit.*
import org.bukkit.Material.*
import org.bukkit.block.Block
import org.bukkit.block.data.*
import org.bukkit.block.data.type.CopperBulb
import org.bukkit.block.data.type.Door
import org.bukkit.block.data.type.Gate
import org.bukkit.block.data.type.Lantern
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs
import org.bukkit.block.data.type.Switch
import org.bukkit.block.data.type.TrapDoor
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import org.joml.Vector3i
import uwu.levaltru.warvilore.Tickable
import uwu.levaltru.warvilore.trashcan.LevsUtils
import uwu.levaltru.warvilore.trashcan.LevsUtils.damageBypassArmor
import kotlin.random.Random

private val random by lazy { Random }
private const val MAX_TRIES = 16
private const val CHANCE_TO_MAKE_OBSIDIAN_MUL_MAX_TRIES = 12 * MAX_TRIES

class NetherInfector(location: Location, direction: Vector, var energy: Int) : Tickable() {

    val location = location.toCenterLocation()
    var direction = direction.normalize().multiply(.2)

    override fun tick(): Boolean {

        if (age++ % 2 != 0) return false
        if (random.nextInt(10 * 30) == 0) direction = LevsUtils.getRandomNormalizedVector().multiply(.2)

        location.world.spawnParticle(Particle.CRIMSON_SPORE, location, 1, .2, .2, .2, .2, null, false)

        for (player in playerWhoWillSeeBetter)
            if (player.world == location.world && player.location.distanceSquared(location) < 96.0 * 96.0)
                player.spawnParticle(Particle.END_ROD, location, 1, .0, .0, .0, .0, null, true)

        val damageSource = DamageSource.builder(DamageType.HOT_FLOOR).build()
        for (livingEntity in location.getNearbyLivingEntities(1.5)) {
            if ((livingEntity as? Player)?.gameMode == GameMode.CREATIVE || (livingEntity as? Player)?.gameMode == GameMode.SPECTATOR) continue
            livingEntity.damageBypassArmor(6.0, 8.0, damageSource)
            livingEntity.addPotionEffect(PotionEffect(PotionEffectType.WITHER, 115, 4, false, true, true))
        }

        if (location.block.type.isAir) energy /= 2
        if (amethystFun(location)) energy = 0

        a@ for (tries in 1..MAX_TRIES) {
            val moveVector = LevsUtils.getRandomNormalizedVector().add(direction)

            val add = location.clone().add(moveVector)
            val type = add.block.type

            if (type.isAir) {
                if (random.nextInt(CHANCE_TO_MAKE_OBSIDIAN_MUL_MAX_TRIES) == 0) {
                    add.block.type = OBSIDIAN
                    break@a
                }
                continue
            }
            for (offset in LevsUtils.neighboringBlocksLocs()) {
                if (add.clone().add(offset).block.type.isOccluding || tries == 1) {
                    if (changeBlockWithEffects(location.add(moveVector))) {
                        energy -= 15
                        if (random.nextInt(20 * 3) == 0) {
                            energy -= energy / 3
                            NetherInfector(
                                location, moveVector.clone().add(
                                    Vector(
                                        random.nextDouble(-.05, .05),
                                        random.nextDouble(-.05, .05),
                                        random.nextDouble(-.05, .05)
                                    )
                                ), energy
                            )
                        }
                        if (random.nextInt(10) == 0) {
                            NetherInfector(location, LevsUtils.getRandomNormalizedVector(), energy / 10)
                        }
                    }
                    break@a
                }
            }
        }

        if (location.y <= location.world.minHeight) {
            location.add(0.0, 1.0, 0.0)
            direction = Vector(0.0, 0.3, 0.0)
        }
        return energy-- <= 0
    }

    companion object {
        fun changeBlockWithEffects(location: Location): Boolean {
            if (changeBlock(location)) {
                location.world.playSound(location, Sound.BLOCK_SCULK_SENSOR_CLICKING_STOP, 1f, .5f)
                return true
            }
            return false
        }

        fun changeBlock(location: Location): Boolean {
            var bool = true
            val b = location.block
            val blockData = b.blockData
            when (b.type) {
                RED_CONCRETE, ORANGE_CONCRETE, YELLOW_CONCRETE, GREEN_CONCRETE, LIME_CONCRETE, BLUE_CONCRETE, CYAN_CONCRETE, LIGHT_BLUE_CONCRETE,
                BROWN_CONCRETE, BLACK_CONCRETE, GRAY_CONCRETE, LIGHT_GRAY_CONCRETE, WHITE_CONCRETE, PURPLE_CONCRETE, MAGENTA_CONCRETE, PINK_CONCRETE,

                RED_GLAZED_TERRACOTTA, ORANGE_GLAZED_TERRACOTTA, YELLOW_GLAZED_TERRACOTTA, GREEN_GLAZED_TERRACOTTA, LIME_GLAZED_TERRACOTTA, BLUE_GLAZED_TERRACOTTA, CYAN_GLAZED_TERRACOTTA, LIGHT_BLUE_GLAZED_TERRACOTTA,
                BROWN_GLAZED_TERRACOTTA, BLACK_GLAZED_TERRACOTTA, GRAY_GLAZED_TERRACOTTA, LIGHT_GRAY_GLAZED_TERRACOTTA, WHITE_GLAZED_TERRACOTTA, PURPLE_GLAZED_TERRACOTTA, MAGENTA_GLAZED_TERRACOTTA, PINK_GLAZED_TERRACOTTA,

                STONE_SWORD, INFESTED_STONE, GRANITE, DIORITE, ANDESITE, STONE,
                    -> b.type = SMOOTH_BASALT

                INFESTED_COBBLESTONE, INFESTED_DEEPSLATE, COBBLESTONE, MOSSY_COBBLESTONE,
                DEEPSLATE, COBBLED_DEEPSLATE, TUFF,
                MUD_BRICKS, PACKED_MUD,
                    -> b.type = BLACKSTONE

                SANDSTONE_SLAB, SMOOTH_SANDSTONE_SLAB, CUT_SANDSTONE_SLAB, BRICK_SLAB, DEEPSLATE_TILE_SLAB -> {
                    b.type = NETHER_BRICK_SLAB
                    applySlabbing(b, blockData)
                }

                RED_SANDSTONE_SLAB, SMOOTH_RED_SANDSTONE_SLAB, CUT_RED_SANDSTONE_SLAB -> {
                    b.type = RED_NETHER_BRICK_SLAB
                    applySlabbing(b, blockData)
                }

                DEEPSLATE_COAL_ORE, DEEPSLATE_IRON_ORE, DEEPSLATE_DIAMOND_ORE, DEEPSLATE_LAPIS_ORE, DEEPSLATE_EMERALD_ORE, DEEPSLATE_REDSTONE_ORE, DEEPSLATE_COPPER_ORE, DEEPSLATE_GOLD_ORE,
                COAL_ORE, IRON_ORE, DIAMOND_ORE, LAPIS_ORE, EMERALD_ORE, REDSTONE_ORE, COPPER_ORE, GOLD_ORE,
                    -> b.type = GILDED_BLACKSTONE

                POLISHED_TUFF, POLISHED_ANDESITE, POLISHED_DIORITE, POLISHED_GRANITE, POLISHED_DEEPSLATE,
                    -> b.type = POLISHED_BLACKSTONE

                CHISELED_TUFF, CHISELED_TUFF_BRICKS,
                CHISELED_DEEPSLATE,
                CHISELED_STONE_BRICKS, INFESTED_CHISELED_STONE_BRICKS,
                SMOOTH_STONE,
                    -> b.type = CHISELED_POLISHED_BLACKSTONE

                DEEPSLATE_BRICKS,
                CRACKED_DEEPSLATE_BRICKS,
                TUFF_BRICKS,
                STONE_BRICKS, INFESTED_STONE_BRICKS,
                MOSSY_STONE_BRICKS, INFESTED_MOSSY_STONE_BRICKS,
                CRACKED_STONE_BRICKS, INFESTED_CRACKED_STONE_BRICKS,
                    -> b.type = POLISHED_BLACKSTONE_BRICKS

                POLISHED_TUFF_WALL, POLISHED_DEEPSLATE_WALL,
                    -> b.type = POLISHED_BLACKSTONE_WALL

                TUFF_BRICK_WALL, MOSSY_STONE_BRICK_WALL, STONE_BRICK_WALL, MUD_BRICK_WALL, DEEPSLATE_BRICK_WALL,
                    -> b.type = POLISHED_BLACKSTONE_BRICK_WALL

                TUFF_WALL, ANDESITE_WALL, GRANITE_WALL, DIORITE_WALL, COBBLESTONE_WALL, MOSSY_COBBLESTONE_WALL, COBBLED_DEEPSLATE_WALL,
                    -> b.type = BLACKSTONE_WALL

                POLISHED_TUFF_SLAB, POLISHED_DEEPSLATE_SLAB, COBBLED_DEEPSLATE_SLAB, POLISHED_ANDESITE_SLAB, POLISHED_DIORITE_SLAB, POLISHED_GRANITE_SLAB, SMOOTH_STONE_SLAB -> {
                    b.type = POLISHED_BLACKSTONE_SLAB
                    applySlabbing(b, blockData)
                }

                TUFF_BRICK_SLAB, MOSSY_STONE_BRICK_SLAB, STONE_BRICK_SLAB, MUD_BRICK_SLAB, DEEPSLATE_BRICK_SLAB -> {
                    b.type = POLISHED_BLACKSTONE_BRICK_SLAB
                    applySlabbing(b, blockData)
                }

                TUFF_SLAB, ANDESITE_SLAB, GRANITE_SLAB, DIORITE_SLAB, COBBLESTONE_SLAB, MOSSY_COBBLESTONE_SLAB, STONE_SLAB -> {
                    b.type = BLACKSTONE_SLAB
                    applySlabbing(b, blockData)
                }

                POLISHED_TUFF_STAIRS, POLISHED_DEEPSLATE_STAIRS, COBBLED_DEEPSLATE_STAIRS, POLISHED_ANDESITE_STAIRS, POLISHED_DIORITE_STAIRS, POLISHED_GRANITE_STAIRS -> {
                    b.type = POLISHED_BLACKSTONE_STAIRS
                    applyStairring(b, blockData)
                }

                TUFF_BRICK_STAIRS, MOSSY_STONE_BRICK_STAIRS, STONE_BRICK_STAIRS, MUD_BRICK_STAIRS, DEEPSLATE_BRICK_STAIRS -> {
                    b.type = POLISHED_BLACKSTONE_BRICK_STAIRS
                    applyStairring(b, blockData)
                }

                TUFF_STAIRS, ANDESITE_STAIRS, GRANITE_STAIRS, DIORITE_STAIRS, COBBLESTONE_STAIRS, MOSSY_COBBLESTONE_STAIRS, STONE_STAIRS -> {
                    b.type = BLACKSTONE_STAIRS
                    applyStairring(b, blockData)
                }

                DIRT, ROOTED_DIRT,
                    -> b.type = NETHERRACK

                GRASS_BLOCK -> {
                    b.type = CRIMSON_NYLIUM
                    if (b.location.add(0.0, 1.0, 0.0).block.type.isAir) {
                        val nextInt = random.nextInt(10)
                        if (nextInt < 6)
                            b.location.add(0.0, 1.0, 0.0).block.type =
                                if (nextInt < 5) CRIMSON_ROOTS else CRIMSON_FUNGUS
                    }
                }

                PODZOL, MYCELIUM -> {
                    b.type = WARPED_NYLIUM
                    if (b.location.add(0.0, 1.0, 0.0).block.type.isAir) {
                        val nextInt = random.nextInt(10)
                        if (nextInt < 7)
                            b.location.add(0.0, 1.0, 0.0).block.type =
                                if (nextInt < 5) WARPED_ROOTS else if (nextInt < 6) WARPED_FUNGUS else NETHER_SPROUTS
                    }
                }

                HAY_BLOCK, SCAFFOLDING,
                BRAIN_CORAL_BLOCK, BUBBLE_CORAL_BLOCK, FIRE_CORAL_BLOCK, TUBE_CORAL_BLOCK, HORN_CORAL_BLOCK,
                DEAD_BRAIN_CORAL_BLOCK, DEAD_BUBBLE_CORAL_BLOCK, DEAD_FIRE_CORAL_BLOCK, DEAD_TUBE_CORAL_BLOCK, DEAD_HORN_CORAL_BLOCK,
                RED_WOOL, WHITE_WOOL, BLUE_WOOL, YELLOW_WOOL, PURPLE_WOOL, PINK_WOOL, ORANGE_WOOL, MAGENTA_WOOL, LIME_WOOL, LIGHT_GRAY_WOOL, LIGHT_BLUE_WOOL, GREEN_WOOL, GRAY_WOOL, CYAN_WOOL, BROWN_WOOL, BLACK_WOOL,
                MUD, GRAVEL, SUSPICIOUS_GRAVEL, RED_SAND, COARSE_DIRT, CLAY, MUDDY_MANGROVE_ROOTS,
                SCULK, SCULK_SENSOR, CALIBRATED_SCULK_SENSOR, SCULK_CATALYST, SCULK_SHRIEKER,
                    -> b.type = SOUL_SOIL


                RED_STAINED_GLASS, ORANGE_STAINED_GLASS, YELLOW_STAINED_GLASS, GREEN_STAINED_GLASS, LIME_STAINED_GLASS, BLUE_STAINED_GLASS, CYAN_STAINED_GLASS, LIGHT_BLUE_STAINED_GLASS,
                BROWN_STAINED_GLASS, BLACK_STAINED_GLASS, GRAY_STAINED_GLASS, LIGHT_GRAY_STAINED_GLASS, WHITE_STAINED_GLASS, PURPLE_STAINED_GLASS, MAGENTA_STAINED_GLASS, PINK_STAINED_GLASS, GLASS,
                RED_STAINED_GLASS_PANE, ORANGE_STAINED_GLASS_PANE, YELLOW_STAINED_GLASS_PANE, GREEN_STAINED_GLASS_PANE, LIME_STAINED_GLASS_PANE, BLUE_STAINED_GLASS_PANE, CYAN_STAINED_GLASS_PANE, LIGHT_BLUE_STAINED_GLASS_PANE,
                BROWN_STAINED_GLASS_PANE, BLACK_STAINED_GLASS_PANE, GRAY_STAINED_GLASS_PANE, LIGHT_GRAY_STAINED_GLASS_PANE, WHITE_STAINED_GLASS_PANE, PURPLE_STAINED_GLASS_PANE, MAGENTA_STAINED_GLASS_PANE, PINK_STAINED_GLASS_PANE, GLASS_PANE,
                SAND, SUSPICIOUS_SAND, DIRT_PATH,
                    -> b.type = SOUL_SAND

                FARMLAND -> {
                    changeBlock(b.location.clone().add(0.0, 1.0, 0.0))
                    b.type = SOUL_SAND
                }

                POTATOES, CARROTS, ATTACHED_MELON_STEM, ATTACHED_PUMPKIN_STEM, MELON_STEM, PUMPKIN_STEM, BEETROOTS -> {
                    b.type = NETHER_WART
                    b.blockData = (b.blockData as Ageable).apply {
                        age = (blockData as? Ageable)?.age?.coerceAtMost(maximumAge) ?: maximumAge
                    }
                }

                SANDSTONE, SMOOTH_SANDSTONE, CUT_SANDSTONE, DEEPSLATE_TILES, CRACKED_DEEPSLATE_TILES, BRICKS,
                    -> b.type = NETHER_BRICKS

                SANDSTONE_STAIRS, SMOOTH_SANDSTONE_STAIRS, BRICK_STAIRS, DEEPSLATE_TILE_STAIRS -> {
                    b.type = NETHER_BRICK_STAIRS
                    applyStairring(b, blockData)
                }

                RED_SANDSTONE_STAIRS, SMOOTH_RED_SANDSTONE_STAIRS -> {
                    b.type = RED_NETHER_BRICK_STAIRS
                    applyStairring(b, blockData)
                }

                CHISELED_SANDSTONE -> b.type = CHISELED_NETHER_BRICKS

                RED_SANDSTONE, SMOOTH_RED_SANDSTONE, CUT_RED_SANDSTONE, CHISELED_RED_SANDSTONE
                    -> b.type = RED_NETHER_BRICKS

                SANDSTONE_WALL, DEEPSLATE_TILE_WALL, BRICK_WALL
                    -> b.type = NETHER_BRICK_WALL

                RED_SANDSTONE_WALL -> b.type = RED_NETHER_BRICK_WALL

                OAK_LOG, JUNGLE_LOG, ACACIA_LOG, DARK_OAK_LOG, MANGROVE_LOG -> {
                    b.type = CRIMSON_STEM
                    applyOrientation(b, blockData)
                }

                BIRCH_LOG, CHERRY_LOG, SPRUCE_LOG -> {
                    b.type = WARPED_STEM
                    applyOrientation(b, blockData)
                }

                OAK_SAPLING, JUNGLE_SAPLING, ACACIA_SAPLING, DARK_OAK_SAPLING, MANGROVE_PROPAGULE
                    -> b.type = CRIMSON_FUNGUS


                BIRCH_SAPLING, CHERRY_SAPLING, SPRUCE_SAPLING
                    -> b.type = WARPED_FUNGUS


                OAK_PRESSURE_PLATE, JUNGLE_PRESSURE_PLATE, ACACIA_PRESSURE_PLATE, DARK_OAK_PRESSURE_PLATE, MANGROVE_PRESSURE_PLATE -> {
                    b.type = CRIMSON_PRESSURE_PLATE
                    b.blockData = (b.blockData as Powerable).apply { isPowered = (blockData as Powerable).isPowered }
                }

                BIRCH_PRESSURE_PLATE, CHERRY_PRESSURE_PLATE, SPRUCE_PRESSURE_PLATE, BAMBOO_PRESSURE_PLATE -> {
                    b.type = WARPED_PRESSURE_PLATE
                    b.blockData = (b.blockData as Powerable).apply { isPowered = (blockData as Powerable).isPowered }
                }

                STONE_PRESSURE_PLATE -> {
                    b.type = POLISHED_BLACKSTONE_PRESSURE_PLATE
                    b.blockData = (b.blockData as Powerable).apply { isPowered = (blockData as Powerable).isPowered }
                }

                OAK_STAIRS, JUNGLE_STAIRS, ACACIA_STAIRS, DARK_OAK_STAIRS, MANGROVE_STAIRS -> {
                    b.type = CRIMSON_STAIRS
                    applyStairring(b, blockData)
                }

                BIRCH_STAIRS, CHERRY_STAIRS, SPRUCE_STAIRS, BAMBOO_STAIRS, BAMBOO_MOSAIC_STAIRS -> {
                    b.type = WARPED_STAIRS
                    applyStairring(b, blockData)
                }

                OAK_SIGN, JUNGLE_SIGN, ACACIA_SIGN, DARK_OAK_SIGN, MANGROVE_SIGN,
                OAK_WALL_SIGN, JUNGLE_WALL_SIGN, ACACIA_WALL_SIGN, DARK_OAK_WALL_SIGN, MANGROVE_WALL_SIGN,
                    -> b.blockData = b.state.apply { type = CRIMSON_SIGN }.blockData


                BIRCH_SIGN, CHERRY_SIGN, SPRUCE_SIGN, BAMBOO_SIGN,
                BIRCH_WALL_SIGN, CHERRY_WALL_SIGN, SPRUCE_WALL_SIGN, BAMBOO_WALL_SIGN,
                    -> b.blockData = b.state.apply { type = WARPED_SIGN }.blockData

                OAK_WALL_HANGING_SIGN, JUNGLE_WALL_HANGING_SIGN, ACACIA_WALL_HANGING_SIGN, DARK_OAK_WALL_HANGING_SIGN, MANGROVE_WALL_HANGING_SIGN,
                OAK_HANGING_SIGN, JUNGLE_HANGING_SIGN, ACACIA_HANGING_SIGN, DARK_OAK_HANGING_SIGN, MANGROVE_HANGING_SIGN,
                    -> b.blockData = b.state.apply { type = CRIMSON_HANGING_SIGN }.blockData

                BIRCH_HANGING_SIGN, CHERRY_HANGING_SIGN, SPRUCE_HANGING_SIGN, BAMBOO_HANGING_SIGN,
                BIRCH_WALL_HANGING_SIGN, CHERRY_WALL_HANGING_SIGN, SPRUCE_WALL_HANGING_SIGN, BAMBOO_WALL_HANGING_SIGN,
                    -> b.blockData = b.state.apply { type = WARPED_HANGING_SIGN }.blockData

                OAK_DOOR, JUNGLE_DOOR, ACACIA_DOOR, DARK_OAK_DOOR, MANGROVE_DOOR -> {
                    applyDooring(b, blockData, CRIMSON_DOOR)
                }

                BIRCH_DOOR, CHERRY_DOOR, SPRUCE_DOOR, BAMBOO_DOOR -> {
                    applyDooring(b, blockData, WARPED_DOOR)
                }

                OAK_TRAPDOOR, JUNGLE_TRAPDOOR, ACACIA_TRAPDOOR, DARK_OAK_TRAPDOOR, MANGROVE_TRAPDOOR -> {
                    b.type = CRIMSON_TRAPDOOR
                    applyTrapdooring(b, blockData)
                }

                BIRCH_TRAPDOOR, CHERRY_TRAPDOOR, SPRUCE_TRAPDOOR, BAMBOO_TRAPDOOR -> {
                    b.type = WARPED_TRAPDOOR
                    applyTrapdooring(b, blockData)
                }

                OAK_SLAB, JUNGLE_SLAB, ACACIA_SLAB, DARK_OAK_SLAB, MANGROVE_SLAB -> {
                    b.type = CRIMSON_SLAB
                    applySlabbing(b, blockData)
                }

                BIRCH_SLAB, CHERRY_SLAB, SPRUCE_SLAB, BAMBOO_SLAB, BAMBOO_MOSAIC_SLAB -> {
                    b.type = WARPED_SLAB
                    applySlabbing(b, blockData)
                }

                OAK_FENCE_GATE, JUNGLE_FENCE_GATE, ACACIA_FENCE_GATE, DARK_OAK_FENCE_GATE, MANGROVE_FENCE_GATE -> {
                    b.type = CRIMSON_FENCE_GATE
                    b.blockData = (b.blockData as Gate).apply {
                        (blockData as Gate).let {
                            isInWall = it.isInWall
                            isOpen = it.isOpen
                            isPowered = it.isPowered
                        }
                    }
                }

                BIRCH_FENCE_GATE, CHERRY_FENCE_GATE, SPRUCE_FENCE_GATE, BAMBOO_FENCE_GATE -> {
                    b.type = WARPED_FENCE_GATE
                    b.blockData = (b.blockData as Gate).apply {
                        (blockData as Gate).let {
                            isInWall = it.isInWall
                            isOpen = it.isOpen
                            isPowered = it.isPowered
                        }
                    }
                }

                OAK_BUTTON, JUNGLE_BUTTON, ACACIA_BUTTON, DARK_OAK_BUTTON, MANGROVE_BUTTON -> {
                    b.type = CRIMSON_BUTTON
                    b.blockData = (b.blockData as Switch).apply {
                        (blockData as Switch).let {
                            isPowered = blockData.isPowered
                            facing = blockData.facing
                            attachedFace = blockData.attachedFace
                        }
                    }
                }

                BIRCH_BUTTON, CHERRY_BUTTON, SPRUCE_BUTTON, BAMBOO_BUTTON -> {
                    b.type = WARPED_BUTTON
                    b.blockData = (b.blockData as Switch).apply {
                        (blockData as Switch).let {
                            isPowered = blockData.isPowered
                            facing = blockData.facing
                            attachedFace = blockData.attachedFace
                        }
                    }
                }

                STONE_BUTTON -> {
                    b.type = POLISHED_BLACKSTONE_BUTTON
                    b.blockData = (b.blockData as Switch).apply {
                        (blockData as Switch).let {
                            isPowered = blockData.isPowered
                            facing = blockData.facing
                            attachedFace = blockData.attachedFace
                        }
                    }
                }

                OAK_FENCE, JUNGLE_FENCE, ACACIA_FENCE, DARK_OAK_FENCE, MANGROVE_FENCE, MANGROVE_ROOTS
                    -> b.type = CRIMSON_FENCE

                BIRCH_FENCE, CHERRY_FENCE, SPRUCE_FENCE, BAMBOO_FENCE,
                    -> b.type = WARPED_FENCE

                STRIPPED_OAK_LOG, STRIPPED_JUNGLE_LOG, STRIPPED_ACACIA_LOG, STRIPPED_DARK_OAK_LOG, STRIPPED_MANGROVE_LOG -> {
                    b.type = STRIPPED_CRIMSON_STEM
                    applyOrientation(b, blockData)
                }

                STRIPPED_BIRCH_LOG, STRIPPED_CHERRY_LOG, STRIPPED_SPRUCE_LOG -> {
                    b.type = STRIPPED_WARPED_STEM
                    applyOrientation(b, blockData)
                }

                STRIPPED_OAK_WOOD, STRIPPED_JUNGLE_WOOD, STRIPPED_ACACIA_WOOD, STRIPPED_DARK_OAK_WOOD, STRIPPED_MANGROVE_WOOD -> {
                    b.type = STRIPPED_CRIMSON_HYPHAE
                    applyOrientation(b, blockData)
                }

                STRIPPED_BIRCH_WOOD, STRIPPED_CHERRY_WOOD, STRIPPED_SPRUCE_WOOD -> {
                    b.type = STRIPPED_WARPED_HYPHAE
                    applyOrientation(b, blockData)
                }

                OAK_WOOD, JUNGLE_WOOD, ACACIA_WOOD, DARK_OAK_WOOD, MANGROVE_WOOD -> {
                    b.type = CRIMSON_HYPHAE
                    applyOrientation(b, blockData)
                }

                BIRCH_WOOD, CHERRY_WOOD, SPRUCE_WOOD -> {
                    b.type = WARPED_HYPHAE
                    applyOrientation(b, blockData)
                }

                BAMBOO_BLOCK, STRIPPED_BAMBOO_BLOCK -> {
                    b.type = POLISHED_BASALT
                    applyOrientation(b, blockData)
                }

                RED_TERRACOTTA, ORANGE_TERRACOTTA, YELLOW_TERRACOTTA, GREEN_TERRACOTTA, LIME_TERRACOTTA, BLUE_TERRACOTTA, CYAN_TERRACOTTA, LIGHT_BLUE_TERRACOTTA,
                BROWN_TERRACOTTA, BLACK_TERRACOTTA, GRAY_TERRACOTTA, LIGHT_GRAY_TERRACOTTA, WHITE_TERRACOTTA, PURPLE_TERRACOTTA, MAGENTA_TERRACOTTA, PINK_TERRACOTTA, TERRACOTTA,

                RED_CONCRETE_POWDER, ORANGE_CONCRETE_POWDER, YELLOW_CONCRETE_POWDER, GREEN_CONCRETE_POWDER, LIME_CONCRETE_POWDER, BLUE_CONCRETE_POWDER, CYAN_CONCRETE_POWDER, LIGHT_BLUE_CONCRETE_POWDER,
                BROWN_CONCRETE_POWDER, BLACK_CONCRETE_POWDER, GRAY_CONCRETE_POWDER, LIGHT_GRAY_CONCRETE_POWDER, WHITE_CONCRETE_POWDER, PURPLE_CONCRETE_POWDER, MAGENTA_CONCRETE_POWDER, PINK_CONCRETE_POWDER,
                BAMBOO_PLANKS, BAMBOO_MOSAIC -> {
                    b.type = BASALT
                    b.blockData =
                        (b.blockData as Orientable).apply { axis = if (random.nextInt(2) == 0) Axis.X else Axis.Z }
                }

                OAK_PLANKS, JUNGLE_PLANKS, ACACIA_PLANKS, DARK_OAK_PLANKS, MANGROVE_PLANKS
                    -> b.type = CRIMSON_PLANKS

                BIRCH_PLANKS, CHERRY_PLANKS, SPRUCE_PLANKS
                    -> b.type = WARPED_PLANKS

                OAK_LEAVES, JUNGLE_LEAVES, ACACIA_LEAVES, DARK_OAK_LEAVES, MANGROVE_LEAVES
                    -> b.type = NETHER_WART_BLOCK

                BIRCH_LEAVES, CHERRY_LEAVES, SPRUCE_LEAVES, AZALEA_LEAVES, FLOWERING_AZALEA_LEAVES,
                    -> b.type = WARPED_WART_BLOCK

                WET_SPONGE -> b.type = SPONGE

                OBSIDIAN -> b.type = CRYING_OBSIDIAN
                CRYING_OBSIDIAN -> b.type = MAGMA_BLOCK
                MAGMA_BLOCK -> {

                    val things = listOf(
                        Vector3i(0, 0, 1),
                        Vector3i(0, 0, -1),
                        Vector3i(0, 1, 0),
                        Vector3i(0, -1, 0),
                        Vector3i(1, 0, 0),
                        Vector3i(-1, 0, 0),
                    )

                    for (thing in things) {
                        val block = location.clone().add(Vector(thing.x, thing.y, thing.z)).block
                        when (block.type) {
                            WATER, SEAGRASS, TALL_SEAGRASS, BUBBLE_COLUMN, KELP_PLANT, KELP
                                -> block.type = OBSIDIAN

                            else -> {
                                val localBlockData = block.blockData
                                if (localBlockData is Waterlogged) localBlockData.isWaterlogged = false
                            }
                        }
                    }

                    b.type = LAVA
                }

                WALL_TORCH -> {
                    b.type = SOUL_WALL_TORCH
                    b.blockData = (b.blockData as Directional).apply { facing = (blockData as Directional).facing }
                }

                TORCH -> b.type = SOUL_TORCH

                CAMPFIRE -> b.type = SOUL_CAMPFIRE

                LANTERN -> {
                    b.type = SOUL_LANTERN
                    b.blockData = (b.blockData as Lantern).apply { isHanging = (b.blockData as Lantern).isHanging }
                }

                BLUE_ICE -> b.type = PACKED_ICE
                PACKED_ICE -> b.type = ICE

                CACTUS -> applyToSameBlockAboveAndBelow(b.location, { it.setType(BONE_BLOCK, false) })

                AZALEA, FLOWERING_AZALEA
                    -> b.type = STRIPPED_WARPED_HYPHAE

                MOSS_BLOCK -> b.type = WARPED_WART_BLOCK

                BAMBOO, BAMBOO_SAPLING -> applyToSameBlockAboveAndBelow(
                    b.location,
                    { it.setType(TWISTING_VINES, it.location.add(0.0, 1.0, 0.0).block.type.isAir) })

                SMALL_DRIPLEAF -> {
                    val type = if (random.nextInt(2) == 0) WARPED_FENCE else CRIMSON_FENCE
                    applyToSameBlockAboveAndBelow(
                        b.location,
                        { it.setType(type, it.location.add(0.0, 1.0, 0.0).block.type.isAir) })
                }

                BIG_DRIPLEAF_STEM, BIG_DRIPLEAF -> {
                    val type = if (random.nextInt(2) == 0) WARPED_FENCE else CRIMSON_FENCE
                    applyToSameBlockAboveAndBelow(
                        b.location,
                        { it.setType(type, false) },
                        { it.type == BIG_DRIPLEAF_STEM || it.type == BIG_DRIPLEAF })
                }

                HANGING_ROOTS -> b.type = WEEPING_VINES

                SUGAR_CANE -> applyToSameBlockAboveAndBelow(
                    b.location,
                    { it.setType(TWISTING_VINES_PLANT, it.location.add(0.0, 1.0, 0.0).block.type.isAir) })

                CAVE_VINES, CAVE_VINES_PLANT -> {
                    applyToSameBlockAboveAndBelow(
                        b.location,
                        { it.setType(WEEPING_VINES_PLANT, it.location.add(0.0, -1.0, 0.0).block.type.isAir) },
                        { it.type == CAVE_VINES || it.type == CAVE_VINES_PLANT })
                }

                POPPY, DANDELION, BLUE_ORCHID, ALLIUM, AZURE_BLUET, RED_TULIP, PINK_TULIP, WHITE_TULIP, ORANGE_TULIP, OXEYE_DAISY, CORNFLOWER, LILY_OF_THE_VALLEY, TORCHFLOWER,
                    -> b.type = WITHER_ROSE


                POTTED_OAK_SAPLING, POTTED_JUNGLE_SAPLING, POTTED_ACACIA_SAPLING, POTTED_DARK_OAK_SAPLING, POTTED_MANGROVE_PROPAGULE,
                POTTED_BIRCH_SAPLING, POTTED_CHERRY_SAPLING, POTTED_SPRUCE_SAPLING,

                POTTED_FERN, POTTED_CACTUS, POTTED_DEAD_BUSH,

                POTTED_POPPY, POTTED_DANDELION, POTTED_BLUE_ORCHID, POTTED_ALLIUM, POTTED_AZURE_BLUET, POTTED_RED_TULIP,
                POTTED_PINK_TULIP, POTTED_WHITE_TULIP, POTTED_ORANGE_TULIP, POTTED_OXEYE_DAISY, POTTED_CORNFLOWER, POTTED_LILY_OF_THE_VALLEY, POTTED_TORCHFLOWER,
                    -> b.type = POTTED_WITHER_ROSE

//                RED_MUSHROOM -> b.type = CRIMSON_FUNGUS
//                BROWN_MUSHROOM -> b.type = WARPED_FUNGUS

                WATER, SEAGRASS, TALL_SEAGRASS, BUBBLE_COLUMN, KELP_PLANT, KELP, LADDER
                    -> b.type = OBSIDIAN

                VINE -> applyToSameBlockAboveAndBelow(b.location, { it.setType(WARPED_FENCE, false) })

                COPPER_BLOCK, WAXED_COPPER_BLOCK -> b.type = EXPOSED_COPPER
                EXPOSED_COPPER, WAXED_EXPOSED_COPPER -> b.type = WEATHERED_COPPER
                WEATHERED_COPPER, WAXED_WEATHERED_COPPER, WAXED_OXIDIZED_COPPER -> b.type = OXIDIZED_COPPER

                COPPER_BULB, WAXED_COPPER_BULB -> {
                    b.type = EXPOSED_COPPER_BULB
                    b.blockData = (b.blockData as CopperBulb).apply {
                        isPowered = (blockData as CopperBulb).isPowered
                        isLit = blockData.isLit
                    }
                }

                EXPOSED_COPPER_BULB, WAXED_EXPOSED_COPPER_BULB -> {
                    b.type = WEATHERED_COPPER_BULB
                    b.blockData = (b.blockData as CopperBulb).apply {
                        isPowered = (blockData as CopperBulb).isPowered
                        isLit = blockData.isLit
                    }
                }

                WEATHERED_COPPER_BULB, WAXED_WEATHERED_COPPER_BULB, WAXED_OXIDIZED_COPPER_BULB -> {
                    b.type = OXIDIZED_COPPER_BULB
                    b.blockData = (b.blockData as CopperBulb).apply {
                        isPowered = (blockData as CopperBulb).isPowered
                        isLit = blockData.isLit
                    }
                }

                COPPER_DOOR, WAXED_COPPER_DOOR -> applyDooring(b, blockData, EXPOSED_COPPER_DOOR)

                EXPOSED_COPPER_DOOR, WAXED_EXPOSED_COPPER_DOOR -> applyDooring(b, blockData, WEATHERED_COPPER_DOOR)

                WEATHERED_COPPER_DOOR, WAXED_WEATHERED_COPPER_DOOR, WAXED_OXIDIZED_COPPER_DOOR -> applyDooring(
                    b,
                    blockData,
                    OXIDIZED_COPPER_DOOR
                )


                COPPER_TRAPDOOR, WAXED_COPPER_TRAPDOOR -> {
                    b.type = EXPOSED_COPPER_TRAPDOOR
                    applyTrapdooring(b, blockData)
                }

                EXPOSED_COPPER_TRAPDOOR, WAXED_EXPOSED_COPPER_TRAPDOOR -> {
                    b.type = WEATHERED_COPPER_TRAPDOOR
                    applyTrapdooring(b, blockData)
                }

                WEATHERED_COPPER_TRAPDOOR, WAXED_WEATHERED_COPPER_TRAPDOOR, WAXED_OXIDIZED_COPPER_TRAPDOOR -> {
                    b.type = OXIDIZED_COPPER_TRAPDOOR
                    applyTrapdooring(b, blockData)
                }

                CUT_COPPER_SLAB, WAXED_CUT_COPPER_SLAB -> {
                    b.type = EXPOSED_CUT_COPPER_SLAB
                    applySlabbing(b, blockData)
                }

                EXPOSED_CUT_COPPER_SLAB, WAXED_EXPOSED_CUT_COPPER_SLAB -> {
                    b.type = WEATHERED_CUT_COPPER_SLAB
                    applySlabbing(b, blockData)
                }

                WEATHERED_CUT_COPPER_SLAB, WAXED_WEATHERED_CUT_COPPER_SLAB, WAXED_OXIDIZED_CUT_COPPER_SLAB -> {
                    b.type = OXIDIZED_CUT_COPPER_SLAB
                    applySlabbing(b, blockData)
                }

                CUT_COPPER_STAIRS, WAXED_CUT_COPPER_STAIRS -> {
                    b.type = EXPOSED_CUT_COPPER_STAIRS
                    applyStairring(b, blockData)
                }

                EXPOSED_CUT_COPPER_STAIRS, WAXED_EXPOSED_CUT_COPPER_STAIRS -> {
                    b.type = WEATHERED_CUT_COPPER_STAIRS
                    applyStairring(b, blockData)
                }

                WEATHERED_CUT_COPPER_STAIRS, WAXED_WEATHERED_CUT_COPPER_STAIRS, WAXED_OXIDIZED_CUT_COPPER_STAIRS -> {
                    b.type = OXIDIZED_CUT_COPPER_STAIRS
                    applyStairring(b, blockData)
                }

                CUT_COPPER, WAXED_CUT_COPPER -> b.type = EXPOSED_CUT_COPPER
                EXPOSED_CUT_COPPER, WAXED_EXPOSED_CUT_COPPER -> b.type = WEATHERED_CUT_COPPER
                WEATHERED_CUT_COPPER, WAXED_WEATHERED_CUT_COPPER, WAXED_OXIDIZED_CUT_COPPER -> b.type =
                    OXIDIZED_CUT_COPPER

                COPPER_GRATE, WAXED_COPPER_GRATE -> b.type = EXPOSED_COPPER_GRATE
                EXPOSED_COPPER_GRATE, WAXED_EXPOSED_COPPER_GRATE -> b.type = WEATHERED_COPPER_GRATE
                WEATHERED_COPPER_GRATE, WAXED_WEATHERED_COPPER_GRATE, WAXED_OXIDIZED_COPPER_GRATE -> b.type =
                    OXIDIZED_COPPER_GRATE

                CHISELED_COPPER, WAXED_CHISELED_COPPER -> b.type = EXPOSED_CHISELED_COPPER
                EXPOSED_CHISELED_COPPER, WAXED_EXPOSED_CHISELED_COPPER -> b.type = WEATHERED_CHISELED_COPPER
                WEATHERED_CHISELED_COPPER, WAXED_WEATHERED_CHISELED_COPPER, WAXED_OXIDIZED_CHISELED_COPPER -> b.type =
                    OXIDIZED_CHISELED_COPPER

                TNT -> {
                    b.type = AIR
                    b.world.createExplosion(b.location.toCenterLocation(), 8f, true)
                    val toCenterLocation = b.location.toCenterLocation()
                    for (_i_ in 1..100) {
                        val vector = LevsUtils.getRandomNormalizedVector()
                        b.world.rayTraceBlocks(toCenterLocation, vector, 50.0)?.hitBlock?.let {
                            NetherInfector(it.location.toCenterLocation(), vector, 500)
                        }
                    }
                }

                WARPED_FENCE, CRIMSON_FENCE -> {
                    val type = b.location.add(0.0, 1.0, 0.0).block.type
                    val b150 = b.y - 150
                    bool = false
                    if ((type.isAir || type == LAVA) && (b150 <= b.world.minHeight || b.world.getBlockAt(b.x, b150, b.z).type != b.type)) {
                        b.location.add(0.0, 1.0, 0.0).block.type = b.type
                        bool = true
                    }
                }


                BRAIN_CORAL, BUBBLE_CORAL, FIRE_CORAL, TUBE_CORAL, HORN_CORAL,
                BRAIN_CORAL_FAN, BUBBLE_CORAL_FAN, FIRE_CORAL_FAN, TUBE_CORAL_FAN, HORN_CORAL_FAN,
                BRAIN_CORAL_WALL_FAN, BUBBLE_CORAL_WALL_FAN, FIRE_CORAL_WALL_FAN, TUBE_CORAL_WALL_FAN, HORN_CORAL_WALL_FAN,

                DEAD_BRAIN_CORAL, DEAD_BUBBLE_CORAL, DEAD_FIRE_CORAL, DEAD_TUBE_CORAL, DEAD_HORN_CORAL,
                DEAD_BRAIN_CORAL_FAN, DEAD_BUBBLE_CORAL_FAN, DEAD_FIRE_CORAL_FAN, DEAD_TUBE_CORAL_FAN, DEAD_HORN_CORAL_FAN,
                DEAD_BRAIN_CORAL_WALL_FAN, DEAD_BUBBLE_CORAL_WALL_FAN, DEAD_FIRE_CORAL_WALL_FAN, DEAD_TUBE_CORAL_WALL_FAN, DEAD_HORN_CORAL_WALL_FAN,

                SCULK_VEIN,

                PINK_PETALS, MOSS_CARPET,

                ICE, SNOW, SNOW_BLOCK, POWDER_SNOW,

                COBWEB, TALL_GRASS, SHORT_GRASS, DEAD_BUSH, LARGE_FERN, FERN, PEONY, ROSE_BUSH, LILAC, SUNFLOWER,

                RED_CARPET, WHITE_CARPET, BLUE_CARPET, YELLOW_CARPET, PURPLE_CARPET, PINK_CARPET, ORANGE_CARPET, MAGENTA_CARPET,
                LIME_CARPET, LIGHT_GRAY_CARPET, LIGHT_BLUE_CARPET, GREEN_CARPET, GRAY_CARPET, CYAN_CARPET, BROWN_CARPET, BLACK_CARPET,
                    -> b.type = AIR

                else -> bool = false
            }
            if ((b.blockData as? Waterlogged)?.isWaterlogged == true) b.blockData =
                (b.blockData as Waterlogged).apply { isWaterlogged = false }
            return bool
        }

        private fun applyDooring(b: Block, blockData: BlockData, material: Material) {
            applyToSameBlockAboveAndBelow(b.location, {
                it.setType(material, false)
                it.blockData = (it.blockData as Door).apply {
                    (blockData as Door).let { it2 ->
                        this.isOpen = it2.isOpen
                        this.facing = it2.facing
                        this.isPowered = it2.isPowered
                        this.hinge = it2.hinge
                        this.half = it2.half
                    }
                }
            })
        }

        private fun applyTrapdooring(b: Block, blockData: BlockData) {
            b.blockData = (b.blockData as TrapDoor).apply {
                (blockData as TrapDoor).let { it2 ->
                    this.isOpen = it2.isOpen
                    this.facing = it2.facing
                    this.isPowered = it2.isPowered
                    this.half = it2.half
                }
            }
        }

        private fun applyOrientation(b: Block, blockData: BlockData) {
            b.blockData = (b.blockData as Orientable).apply {
                axis = (blockData as Orientable).axis
            }
        }

        private fun applySlabbing(b: Block, blockData: BlockData) {
            b.blockData = (b.blockData as Slab).apply {
                type = (blockData as Slab).type
            }
        }

        private fun applyStairring(b: Block, blockData: BlockData) {
            b.blockData = (b.blockData as Stairs).apply {
                shape = (blockData as Stairs).shape
                facing = blockData.facing
                half = blockData.half
            }
        }

        private fun applyToSameBlockAboveAndBelow(
            location: Location,
            func: (Block) -> Unit,
            continueFunc: ((Block) -> Boolean)? = null
        ) {
            val type = location.block.type
            var loci = location.clone()
            while (continueFunc?.invoke(loci.block) ?: (loci.block.type == type)) {
                func(loci.block)
                loci.add(0.0, 1.0, 0.0)
            }
            loci = location.clone().add(0.0, -1.0, 0.0)
            while (continueFunc?.invoke(loci.block) ?: (loci.block.type == type)) {
                func(loci.block)
                loci.add(0.0, -1.0, 0.0)
            }
        }

        private fun amethystFun(location: Location): Boolean {
            when (location.block.type) {
                AMETHYST_BLOCK, AMETHYST_CLUSTER, BUDDING_AMETHYST, SMALL_AMETHYST_BUD, MEDIUM_AMETHYST_BUD, LARGE_AMETHYST_BUD -> {
                    val centerLoc = location.toCenterLocation()
                    val world = location.world
                    if (random.nextInt(250) == 0) {
                        location.block.type = OBSIDIAN
                        world.playSound(centerLoc, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1f, .5f)
                        return false
                    } else {
                        world.spawnParticle(Particle.OMINOUS_SPAWNING, centerLoc, 30, .0, .0, .0, 0.75)
                        world.spawnParticle(Particle.END_ROD, centerLoc, 12, .0, .0, .0, 0.05)
                        world.playSound(centerLoc, Sound.BLOCK_AMETHYST_BLOCK_RESONATE, 1f, .5f)
                    }
                    return true
                }

                else -> return false
            }
        }

        val playerWhoWillSeeBetter = mutableListOf<Player>()
    }
}