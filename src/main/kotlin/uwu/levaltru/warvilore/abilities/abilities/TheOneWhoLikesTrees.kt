package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.*
import org.bukkit.attribute.Attribute
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Ageable
import org.bukkit.block.data.type.Sapling
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.entity.Item
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.BoundingBox
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.bases.HatesEvilAura
import uwu.levaltru.warvilore.trashcan.LevsUtils
import uwu.levaltru.warvilore.trashcan.Namespaces
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

private const val TREE_COOLDOWN = 20 * 20

class TheOneWhoLikesTrees(nickname: String) : HatesEvilAura(nickname) {

    var treeCooldown: Int = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.TREE_COOLDOWN.namespace,
                PersistentDataType.INTEGER
            ) ?: 0
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.TREE_COOLDOWN.namespace,
                PersistentDataType.INTEGER, value
            )
            field = value
        }

    override fun onTick(event: ServerTickEndEvent) {
        super.onTick(event)

        if (treeCooldown % 20 == 0 && treeCooldown > 0 && LevsUtils.isSword(player!!.inventory.itemInMainHand.type))
            player!!.sendActionBar(text("${treeCooldown / 20}s").color(NamedTextColor.RED))
        else if (treeCooldown == 1) player!!.sendActionBar(text(""))
        treeCooldown--

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
            Material.PITCHER_PLANT -> {
                if (action.name != "RIGHT_CLICK_AIR") return
                showEvilAuraPoisoning()
            }

            Material.NETHERITE_SWORD,
            Material.WOODEN_SWORD,
            Material.GOLDEN_SWORD,
            Material.IRON_SWORD,
            Material.STONE_SWORD,
            Material.DIAMOND_SWORD -> {
                if (!action.isRightClick) return
                if (player!!.world.isUltraWarm) return
                if (treeCooldown > 0 && player!!.gameMode != GameMode.CREATIVE) return
                val i1 = ceil(player!!.getAttribute(Attribute.PLAYER_ENTITY_INTERACTION_RANGE)?.value ?: 0.0).toInt() + 1
                val traceResult = player!!.rayTraceEntities(i1)
                if (traceResult != null) {
                    val hitEntity = traceResult.hitEntity
                    if (hitEntity != null) {
                        if (treeStuff(hitEntity.location)) return
                    }
                }
                if (player!!.pitch > 80 && player!!.isSneaking) {
                    if (treeStuff(player!!.location.add(.0, .1, .0))) return
                }
            }

            else -> return
        }
    }

    private fun treeStuff(location: Location): Boolean {
        val d1 = random.nextInt(3, 6)
        val damageSource = DamageSource.builder(DamageType.FALL).withDirectEntity(player!!)
            .withCausingEntity(player!!).build()
        for (iiiiii in 1..2) {
            if (!isTreeSupportive(location.add(0.0, -1.0, 0.0).block.type)) continue
            val centralLoc = location.toCenterLocation().add(0.0, 1.0, 0.0)
            for (i2 in centralLoc.blockY..<centralLoc.blockY + d1) {
                if (centralLoc.world.getBlockAt(centralLoc.blockX, i2, centralLoc.blockZ).type.blastResistance > 500) return false
            }
            for (x1 in -1..1) {
                for (y1 in 0..1) {
                    for (z1 in -1..1) {
                        if (y1 == 1) if (x1 == 0 && z1 == 0) continue
                        else if (abs(x1) == 1 && abs(z1) == 1) continue
                        if (centralLoc.world.getBlockAt(centralLoc.blockX + x1, centralLoc.blockY + y1 + d1, centralLoc.blockZ + z1).type.isBlockTreeThing().not()) return false
                    }
                }
            }

            for (entity in centralLoc.getNearbyLivingEntities(16.0)) {
                val treeTop = centralLoc.y + d1 + 1
                if (BoundingBox(
                        centralLoc.x - 1,
                        centralLoc.y - 1,
                        centralLoc.z - 1,
                        centralLoc.x + 1,
                        treeTop,
                        centralLoc.z + 1
                    )
                        .overlaps(entity.boundingBox)
                ) {
                    entity.teleport(
                        Location(
                            entity.world,
                            entity.x,
                            treeTop,
                            entity.z,
                            entity.yaw,
                            entity.pitch
                        )
                    )
                    Bukkit.getScheduler().runTaskLater(Warvilore.instance, Runnable {
                        entity.damage(15.0, damageSource)
                        entity.velocity = Vector(
                            random.nextGaussian() * 0.3,
                            random.nextDouble(1.2, 1.5),
                            random.nextGaussian() * 0.3
                        )
                    }, 1L)
                }
            }

            centralLoc.world.spawnParticle(
                Particle.BLOCK,
                centralLoc.x,
                (centralLoc.blockY + d1 / 2).toDouble(),
                centralLoc.z, 2000, 1.0, 1.6, 1.0, 0.0, Material.FLOWERING_AZALEA_LEAVES.createBlockData(), true
            )
            centralLoc.world.playSound(centralLoc, Sound.BLOCK_AZALEA_LEAVES_BREAK, 2f, 0.5f)
            centralLoc.world.playSound(centralLoc, Sound.ENTITY_PLAYER_ATTACK_CRIT, 2f, 0.5f)

            location.add(0.0, 1.0, 0.0)
            val stemLoc = location.clone().add(0.0, -1.0, 0.0)
            for (iiii in 1..d1) {
                stemLoc.add(0.0, 1.0, 0.0)
                stemLoc.world.getBlockAt(stemLoc).breakNaturally()
                stemLoc.world.setType(stemLoc, Material.OAK_LOG)
            }

            val xInt = floor(stemLoc.x).toInt()
            val yInt = floor(stemLoc.y).toInt()
            val zInt = floor(stemLoc.z).toInt()
            for (x in -1..1) {
                for (y in 0..1) {
                    for (z in -1..1) {
                        if (y == 0) {
                            if (x == 0 && z == 0) continue
                        } else if (abs(x) == 1 && abs(z) == 1) continue
                        val material =
                            if (random.nextDouble() < 0.25) Material.FLOWERING_AZALEA_LEAVES else Material.AZALEA_LEAVES
                        stemLoc.world.getBlockAt(x + xInt, y + yInt, z + zInt).breakNaturally()
                        stemLoc.world.setType(x + xInt, y + yInt, z + zInt, material)
                    }
                }
            }
            treeCooldown = TREE_COOLDOWN
            return true
        }
        return false
    }

    override fun onEating(event: PlayerItemConsumeEvent) {
        if (event.item.type == Material.MILK_BUCKET) evilAuraSickness = 0
    }

    override fun onDeath(event: PlayerDeathEvent) {
        super.onDeath(event)
        treeCooldown = 0
    }

    override fun getAboutMe(): List<Component> = listOf(
        text("Твои умения:").color(NamedTextColor.GREEN),
        text("- Когда ты стоишь около агрокультур или саженцов, они растут быстрее.").color(NamedTextColor.GREEN),
        text("- Когда ты вскапываешь агрокультуры, и ты не на шифте, они автоматически садятся обратно.").color(
            NamedTextColor.GREEN
        ),
        text("- Тебе становится плохо когда ты находишся рядом с ").color(NamedTextColor.RED)
            .append { text("темными людьми.").style(Style.style(TextDecoration.ITALIC, NamedTextColor.RED)) },
        text("  - Чтобы проверить наскольно тебе попа, нажми по воздуху с любым цветком в руках.").color(NamedTextColor.LIGHT_PURPLE),
        text("- Ты можешь создать дерево.").color(NamedTextColor.GREEN),
        text("  - Когда ты на шифте и нажимаешь с мичем вниз, ты создаешь дерево которое подкидывает тебя вверх.").color(NamedTextColor.GREEN),
        text("  - Когда ты нажимаешь на живое существо, ты создаешь дерево на его месте.").color(NamedTextColor.GREEN),
        text("  - Но чтобы дерево выросло, тебе нужна почва на месте создания дерева.").color(NamedTextColor.GOLD),
    )

    companion object {
        fun isTreeSupportive(material: Material): Boolean {
            if (!material.isBlock) return false
            return when (material) {
                Material.MOSS_BLOCK,
                Material.GRASS_BLOCK,
                Material.DIRT_PATH,
                Material.DIRT,
                Material.COARSE_DIRT,
                Material.ROOTED_DIRT,
                Material.FARMLAND,
                Material.MYCELIUM,
                Material.PODZOL,
                Material.MUD -> true

                else -> false
            }
        }

        fun Material.isBlockTreeThing(): Boolean {
            if (!this.isBlock) return false
            return !this.isCollidable || isTreeSupportive(this) || when (this) {
                Material.SPRUCE_LEAVES,
                Material.OAK_LEAVES,
                Material.MANGROVE_LEAVES,
                Material.JUNGLE_LEAVES,
                Material.CHERRY_LEAVES,
                Material.ACACIA_LEAVES,
                Material.BIRCH_LEAVES,
                Material.DARK_OAK_LEAVES,
                Material.FLOWERING_AZALEA_LEAVES,
                Material.AZALEA_LEAVES,

                Material.OAK_LOG,
                Material.BIRCH_LOG,
                Material.ACACIA_LOG,
                Material.MANGROVE_LOG,
                Material.DARK_OAK_LOG,
                Material.SPRUCE_LOG,
                Material.CHERRY_LOG,
                Material.JUNGLE_LOG -> true

                else -> false
            }
        }
    }

}