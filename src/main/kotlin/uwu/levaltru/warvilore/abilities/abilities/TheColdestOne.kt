package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.*
import org.bukkit.block.data.Levelled
import org.bukkit.block.data.type.Snow
import org.bukkit.damage.DamageType
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Item
import org.bukkit.entity.LivingEntity
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.potion.PotionType
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.abilities.bases.Undead
import uwu.levaltru.warvilore.abilities.interfaces.EvilAurable
import uwu.levaltru.warvilore.trashcan.CustomItems
import uwu.levaltru.warvilore.trashcan.CustomWeapons
import uwu.levaltru.warvilore.trashcan.LevsUtils
import uwu.levaltru.warvilore.trashcan.LevsUtils.getAsCustomItem
import uwu.levaltru.warvilore.trashcan.LevsUtils.getSoulInTheBottle
import uwu.levaltru.warvilore.trashcan.Namespaces
import java.util.*
import kotlin.math.PI
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.math.sin

private const val MAX_COLDNESS = 15_000

private const val STAND_STILL_TIME = 20
private const val BEFORE_SWORD_TIMES = 25

private const val FROZEN_TICKS_LIMIT = 500

private const val OMINOUS_SOUL_BOTTLE_MANA_REGEN = MAX_COLDNESS / 10

class TheColdestOne(string: String) : Undead(string), EvilAurable {
    var coldness: Int = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.COLDNESS.namespace,
                PersistentDataType.INTEGER
            ) ?: MAX_COLDNESS
            return field
        }
        set(value) {
            val coerceIn = value.coerceIn(0, MAX_COLDNESS)
            player?.persistentDataContainer?.set(
                Namespaces.COLDNESS.namespace,
                PersistentDataType.INTEGER, coerceIn
            )
            field = coerceIn
        }
    var prevLoc: Location? = null
    var standStillTime = 0
    var cooldown = 0
    var beforeSword = 0

    override fun onTick(event: ServerTickEndEvent) {
        super.onTick(event)

        if (player!!.freezeTicks > 0) player!!.freezeTicks = 0

        if (random.nextInt(0, MAX_COLDNESS / 3) > coldness && random.nextInt(0, 20) == 0)
            player!!.fireTicks = (player!!.fireTicks + 50).coerceAtMost(200)
        if (random.nextInt(0, MAX_COLDNESS / 4) > coldness && random.nextInt(0, 10) == 0)
            player!!.addPotionEffect(
                PotionEffect(
                    PotionEffectType.DARKNESS,
                    50, 0, true, false, true
                )
            )
        if (random.nextInt(0, MAX_COLDNESS / 6) > coldness && random.nextInt(0, 5) == 0)
            player!!.addPotionEffect(
                PotionEffect(
                    PotionEffectType.BLINDNESS,
                    100, 0, true, false, true
                )
            )

        val location = player!!.location
        val locy = location.add(0.0, player!!.height / 2, 0.0)
        val d = getFormatedTemperature(locy)
        val i = LevsUtils.roundToRandomInt(d)
        coldness = (coldness - i).coerceIn(0, MAX_COLDNESS)

        if (isStandingOnColdBlock()) {
            val i1 = (coldness * 5 / MAX_COLDNESS - 2).coerceAtMost(2)
            if (i1 >= 0)
                player!!.addPotionEffect(
                    PotionEffect(
                        PotionEffectType.SPEED,
                        10, i1, true, false, true
                    )
                )
        }

        if (prevLoc != null
            && prevLoc!!.world == location.world
            && player!!.location.distanceSquared(prevLoc!!) < .0001
            && player!!.isSneaking
        )
            standStillTime++
        else {
            standStillTime = 0
            prevLoc = player!!.location
        }
        if (standStillTime < STAND_STILL_TIME) return

        if (cooldown <= 0) {
            val item = player!!.inventory.itemInMainHand
            if (item.isFrostmourne() || item.type == Material.NETHERITE_SWORD) {
                if (coldness > MAX_COLDNESS / 2) {
                    cooldown = ((MAX_COLDNESS - coldness).toDouble() / MAX_COLDNESS * 64.0).toInt().coerceAtLeast(8)
                    coldness -= 50
                    player!!.world.spawnParticle(
                        Particle.END_ROD, locy, 300,
                        1.0, 1.0, 1.0, 0.1, null, true
                    )
                    player!!.world.playSound(locy, Sound.PARTICLE_SOUL_ESCAPE, 2f, 0.5f)
                    for (j in 1..100) {
                        val x = random.nextGaussian() * 3 + locy.x
                        val y = random.nextGaussian() * 3 + locy.y
                        val z = random.nextGaussian() * 3 + locy.z
                        val loc = Location(player!!.world, x, y, z)
                        if (loc.distanceSquared(locy) < 4) continue

                        snowyfi(loc)
                    }

                    for (entity in locy.getNearbyLivingEntities(8.0)) {
                        if (entity.uniqueId == player!!.uniqueId) continue
                        val add = entity.location.add(0.0, entity.height / 2, 0.0)
                        val cold = (8 - add.distance(locy)) * 15
                        if (cold > 0) applyCold(entity, cold.toInt())
                    }

                    if (!item.isFrostmourne()) {
                        if (beforeSword < BEFORE_SWORD_TIMES) beforeSword++
                        else {
                            coldness = (coldness - MAX_COLDNESS / 2).coerceAtLeast(0)
                            beforeSword = 0
                            player!!.inventory.setItemInMainHand(
                                CustomWeapons.FROSTMOURNE.replaceItem(
                                    item,
                                    player!!.name
                                )
                            )
                            LevsUtils.frostmourneExplosion(locy, player!!, false)
                        }
                    } else beforeSword = 0
                }
            } else beforeSword = 0
        } else cooldown--
    }


    private fun getFormatedTemperature(locy: Location): Double {
        val temperature = locy.block.temperature
        var d = if (locy.world.isUltraWarm) 5.0
        else (temperature - 0.25) * 4 / 3
        if (temperature > 0.25) d *= warmnessProtectionMultiplicator()
        if (!locy.world.isFixedTime)
            d += (sin((locy.world.time * PI) / 12000) * 0.2 + 0.05).coerceAtLeast(0.0) *
                    (locy.block.lightFromSky.toDouble() / 15)
        return d // depends on: daylight and if the world is hot
    }

    //
    //
    //

    private fun warmnessProtectionMultiplicator(): Double {
        var w = 0
        for (armor in player!!.inventory.armorContents)
            w += armor?.enchantments?.get(Enchantment.FIRE_PROTECTION) ?: 0
        return (1 - w.toDouble() / 32) *
                if (player!!.activePotionEffects.map { it.type }.contains(PotionEffectType.FIRE_RESISTANCE))
                    0.5 else 1.0
    }

    private fun coldnessFrom0To1() = (coldness.toDouble() / MAX_COLDNESS)

    override fun onAction(event: PlayerInteractEvent) {
        if (event.action.name != "RIGHT_CLICK_AIR") return
        val item = player!!.inventory.itemInMainHand
        when (item.type) {
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD,
            Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD -> {}
            else -> return
        }

        val temp = getFormatedTemperature(player!!.location.add(0.0, player!!.height / 2, 0.0))

        val negativeRed = (170 shl 8) or 170
        val negativeBlue = (170 shl 16) or (170 shl 8)

        var string = ""
        var d = temp.absoluteValue * 2.5
        val char = if (temp < 0) "↓" else "↑"
        var colori: TextColor? = null
        if (d < 10) {
            while (d > 0) {
                string += char
                d--
            }
            d = d * 2 + 1
            if (temp < 0) d *= -1
            if (string.isEmpty()) {
                string = "~"
                d = 0.0
            }

            val r = (255 -
                    (1.0 - (d - 1.0).absoluteValue).coerceAtLeast(0.0) * (negativeRed and 0xFF0000 shr 16) -
                    (1.0 - (d + 1.0).absoluteValue).coerceAtLeast(0.0) * (negativeBlue and 0xFF0000 shr 16))
                .roundToInt()
            val g = (255 -
                    (1.0 - (d - 1.0).absoluteValue).coerceAtLeast(0.0) * (negativeRed and 0x00FF00 shr 8) -
                    (1.0 - (d + 1.0).absoluteValue).coerceAtLeast(0.0) * (negativeBlue and 0x00FF00 shr 8))
                .roundToInt()
            val b = (255 -
                    (1.0 - (d - 1.0).absoluteValue).coerceAtLeast(0.0) * (negativeRed and 0x0000FF) -
                    (1.0 - (d + 1.0).absoluteValue).coerceAtLeast(0.0) * (negativeBlue and 0x0000FF))
                .roundToInt()

            colori = TextColor.color(r, g, b)
        } else {
            string = "$char⚠$char"
            colori = if (temp < 0) NamedTextColor.BLUE else NamedTextColor.RED
        }

        val i = coldness * 100 / MAX_COLDNESS
//        val i1 = (coldness * 10000 / MAX_COLDNESS) % 100
        val color = if (temp < 0) NamedTextColor.BLUE else NamedTextColor.RED
        player!!.sendActionBar(
            text("$i"/*.$i1"*/).color(NamedTextColor.BLUE).append { text("%").color(NamedTextColor.DARK_BLUE) }
                .append { text(" < ").color(color) }
                .append { text("$string").color(colori!!) }
                .append { text(" >").color(color) })

        player!!.playSound(player!!, Sound.BLOCK_TRIAL_SPAWNER_SPAWN_ITEM_BEGIN, SoundCategory.MASTER, 0.33f, 1.2f)
    }

    override fun onBlockBreak(event: BlockBreakEvent) {
        if (player!!.gameMode == GameMode.CREATIVE) return
        val item = player!!.inventory.itemInMainHand
        if (item.isEmpty || item.isFrostmourne() || materialIsColdBlock(item.type)) {
            val block = event.block
            if (materialIsColdBlock(block.type)) {
                if (block.type.isItem) {
                    val itemStack = ItemStack(block.type)
                    if (block.blockData is Snow) itemStack.amount = (block.blockData as Snow).layers
                    event.isDropItems = false
                    block.type = Material.AIR
                    block.world.spawn(block.location.toCenterLocation(), Item::class.java) {
                        it.itemStack = itemStack
                        it.velocity = Vector(random.nextGaussian() * 0.1, 0.1, random.nextGaussian() * 0.1)
                    }
                }
            }
        }
    }

    override fun onDeath(event: PlayerDeathEvent) {
        coldness = (MAX_COLDNESS * 0.75).roundToInt()
    }

    override fun onDamage(event: EntityDamageEvent) {
        super.onDamage(event)
        when (event.damageSource.damageType) {
            DamageType.FIREBALL, DamageType.UNATTRIBUTED_FIREBALL,
            DamageType.IN_FIRE, DamageType.ON_FIRE,
            DamageType.LAVA -> {
                event.damage *= 3
                coldness -= 500
            }

            DamageType.FALL -> if (isStandingOnColdBlock()) event.isCancelled = true

            else -> {}
        }
    }

    override fun onAttack(event: PrePlayerAttackEntityEvent) {
        val item = player!!.inventory.itemInMainHand
        if (event.willAttack() && (item.isFrostmourne() || item.isEmpty)) {
            val entity = event.attacked
            if (entity is LivingEntity)
                applyCold(entity, 70)
        }
    }

    override fun onHeal(event: EntityRegainHealthEvent) {
        event.amount *= (coldness.toDouble() * 4 / MAX_COLDNESS - 1).coerceIn(0.0, 1.0)
        super.onHeal(event)
    }

    private fun applyCold(entity: LivingEntity, amount: Int) {
        entity.freezeTicks = (entity.freezeTicks +
                (amount * coldnessFrom0To1()).toInt()).coerceAtMost(FROZEN_TICKS_LIMIT)
    }

    fun isStandingOnColdBlock(): Boolean {
        val boundingBox = player!!.boundingBox
        val location = player!!.location
        val min = boundingBox.min
        val max = boundingBox.max
        for (x in min.blockX..max.blockX) {
            for (y in min.blockY - 1..max.blockY) {
                for (z in min.blockZ..max.blockZ) {
                    val add = Location(location.world, x.toDouble(), y.toDouble(), z.toDouble())
                    if (materialIsColdBlock(add.block.type)) return true
                }
            }
        }
        return false
    }

    private fun materialIsColdBlock(material: Material): Boolean {
        return materialIsIce(material) || materialIsSnow(material)
    }

    private fun materialIsIce(material: Material): Boolean {
        return when (material) {
            Material.ICE, Material.FROSTED_ICE, Material.PACKED_ICE, Material.BLUE_ICE -> true
            else -> false
        }
    }

    private fun materialIsSnow(material: Material): Boolean {
        return when (material) {
            Material.SNOW, Material.SNOW_BLOCK, Material.POWDER_SNOW -> true
            else -> false
        }
    }

    override fun onEating(event: PlayerItemConsumeEvent) {
        val itemMeta = event.item.itemMeta
        if (itemMeta.getAsCustomItem() == CustomItems.OMINOUS_SOUL_BOTTLE) {
            if (itemMeta.getSoulInTheBottle()?.lowercase() == player!!.name.lowercase()) return
            coldness += OMINOUS_SOUL_BOTTLE_MANA_REGEN
            player!!.addPotionEffects(listOf(
                PotionEffect(PotionEffectType.POISON, 210, 1, false, true, true),
                PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 0, false, true, true),
                PotionEffect(PotionEffectType.SATURATION, 3, 0, false, true, true),
            ))
            player!!.world.spawnParticle(Particle.SCRAPE, player!!.location.add(0.0, player!!.height / 2, 0.0), 10, .2, .4, .2, 1.0, null, false)
            player!!.world.playSound(player!!.location, Sound.ENTITY_ALLAY_ITEM_GIVEN, 1f, 0.7f)
            player!!.sendActionBar(text((coldness * 100 / MAX_COLDNESS).toString()).color(NamedTextColor.BLUE).append {
                text("%").color(NamedTextColor.DARK_BLUE)
            })
        }
    }

    override fun getEvilAura(): Double = 5.0 * coldnessFrom0To1()

    override fun getAboutMe(): List<Component> = listOf(
        text("Ты Король Холода Лич.").color(NamedTextColor.AQUA),
        text("Твой файл один из самых больших (гордись этим).").color(NamedTextColor.AQUA),
        text(""),
        text("Твои умения:").color(NamedTextColor.GREEN),
        text(""),
        text("- Ты нежить. Это значит что другая нежить не будет тебя атаковать если её не провоцировать. " +
                "Также это значит что на некоторые типы урона тебе просто все ровно, парочку даже восполняют тебе здоровье.").color(NamedTextColor.GREEN),
        text(""),
        text("- Снег и лед это материалы по которым ты двигаешься с увеличенной скоростью. Также это материалы которые обнуляют твой урон от падения (даже снежные ковры)."
        ).color(NamedTextColor.GREEN),
        text(""),
        text("- Добыча снега или льда голой рукой ").color(NamedTextColor.GREEN)
            .append { text("Ледяной Скорбью, ").style(Style.style(TextDecoration.ITALIC, NamedTextColor.DARK_AQUA)) }
            .append { text("осуществляется с эффектом шелкового касания.").color(NamedTextColor.GREEN) },
        text(""),
        text("- Когда ты на шифте, и у тебя в руках незеритовый меч или ").color(NamedTextColor.GREEN)
            .append { text("Ледяная Скорбь. ").style(Style.style(TextDecoration.ITALIC, NamedTextColor.DARK_AQUA)) }
            .append { text("Ты создаешь холод вокруг себя. Если у тебя в руках незеритовый меч, то спустя какое-то время он превратится в ").color(NamedTextColor.GREEN) }
            .append { text("Ледяную Скорбь. ").style(Style.style(TextDecoration.ITALIC, NamedTextColor.DARK_AQUA)) },
        text("  - Небольшая рекомендация: делай это когда у тебя много холода, так как этот процесс забирает много твоих сил. Еще ты можешь загореться.").color(NamedTextColor.GOLD),
        text(""),
        text("- Когда ты нажимаешь на любой меч, тебе показывается температура и шкала твоего холода").color(NamedTextColor.GREEN),
        text("  - Стрелка вниз (↓) обозначает что тут холодно (aka тебе хорошо). Стрелка вверх (↑) обозначает что тут жарко (aka тебе плохо). " +
                "Цвет в стрелках является как дополнительным индикатором холода / жары (aka красная стрелка вниз горячее чем синяя стрелка вниз но тебе и там и там хорошо).").color(NamedTextColor.YELLOW),
        text(""),
        text(""),
        text("Твои минусы:").color(NamedTextColor.RED),
        text(""),
        text("- У тебя есть шкала холода.").color(NamedTextColor.RED),
        text("  - Она накапливает холод по формуле снизу ↓").color(NamedTextColor.GOLD),
        text("    ((t - 0.25) * 4 / 3) * ((1 - p / 32) / f) ").color(NamedTextColor.YELLOW),
        text("    t - Температура (зависит от биома и высоты);").color(NamedTextColor.YELLOW),
        text("    p - Огнеупорность на броне;").color(NamedTextColor.YELLOW),
        text("    f - если есть огнестойкость = 2, если нет = 1").color(NamedTextColor.YELLOW),
        text(""),
        text("- Урон от огня утроен, и твоя шкала холода уменьшается когда ты получаешь урон от огня.").color(NamedTextColor.RED),
        text(""),
        text("- Эффективность твоих умений зависит от шкалы холода.").color(NamedTextColor.RED),
        text(""),
        text("- Если шкала холода будет низкой, ты получаешь негативные эффекты.").color(NamedTextColor.RED),
        text(""),
        text("- Ты нежить. Будь осторожен с Небесной Карой").color(NamedTextColor.RED),
    )

    companion object {
        fun ItemStack.isFrostmourne(): Boolean =
            CustomWeapons.FROSTMOURNE.equals(this)

        fun snowyfi(loc: Location) {
            val block = loc.block
            val blockData = block.blockData
            val random = Random()
            when (block.type) {
                Material.AIR -> {
                    val blockBelow = loc.clone().add(0.0, -1.0, 0.0).block
                    if (blockBelow.isSolid || (blockBelow.blockData as? Snow)?.layers == 8)
                        block.type = Material.SNOW
                }

                Material.SNOW -> {
                    if (blockData is Snow) {
                        blockData.layers = (blockData.layers + random.nextInt(1, 4))
                            .coerceAtMost(blockData.maximumLayers)
                        block.blockData = blockData
                    }
                }

                Material.WATER -> {
                    if (blockData is Levelled && blockData.level == 0 && random.nextDouble() < 0.1)
                        block.type = Material.ICE
                }

                Material.LAVA -> {
                    if (blockData is Levelled && blockData.level == 0 && random.nextDouble() < 0.1)
                        block.type = Material.OBSIDIAN
                }

                Material.FROSTED_ICE -> block.type = Material.ICE


                Material.SHORT_GRASS,
                Material.TALL_GRASS,
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
                Material.PEONY -> {
                    block.type = Material.AIR
                    snowyfi(block.location)
                }

                else -> {}
            }
        }

        fun getUnderFormatedTemperature(locy: Location): Double {
            val temperature = locy.block.temperature
            var d = if (locy.world.isUltraWarm) 5.0
            else (temperature - 0.25) * 4 / 3
            if (!locy.world.isFixedTime)
                d += (sin((locy.world.time * PI) / 12000) * 0.2 + 0.05).coerceAtLeast(0.0) *
                        (locy.block.lightFromSky.toDouble() / 15)
            return d
        }
    }
}