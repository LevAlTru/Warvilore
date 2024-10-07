package uwu.levaltru.warvilore.trashcan

import com.comphenix.protocol.PacketType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.*
import org.bukkit.Material.*
import org.bukkit.damage.DamageSource
import org.bukkit.entity.Item
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemRarity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.abilities.TheColdestOne
import uwu.levaltru.warvilore.protocolManager
import uwu.levaltru.warvilore.tickables.effect.DeathMarker
import java.util.*
import kotlin.math.*

object LevsUtils {
    fun getRandomNormalizedVector(): Vector {
        var vector: Vector
        val random = Random()
        do {
            vector = Vector(
                random.nextDouble(-1.0, 1.0),
                random.nextDouble(-1.0, 1.0),
                random.nextDouble(-1.0, 1.0)
            )
        } while (vector.lengthSquared() > 1)
        return vector.normalize()
    }

    fun roundToRandomInt(d: Double): Int {
        val d1 = if (d < 0) 1 + (d % 1) else d % 1
        if (d1 == 0.0) return floor(d).toInt()
        return floor(d).toInt() + if (Random().nextDouble() < d1) 1 else 0
    }

    fun ItemMeta.setSoulBoundTo(nickname: String) {
        this.persistentDataContainer.set(Namespaces.SOULBOUND.namespace, PersistentDataType.STRING, nickname)
    }

    fun ItemMeta.isSoulBound(): Boolean {
        return this.persistentDataContainer.has(Namespaces.SOULBOUND.namespace, PersistentDataType.STRING)
    }

    fun ItemMeta.getSoulBound(): String? {
        return this.persistentDataContainer.get(Namespaces.SOULBOUND.namespace, PersistentDataType.STRING)
    }

    fun ItemMeta?.getAsCustomWeapon(): CustomWeapons? {
        val s = this?.persistentDataContainer?.get(Namespaces.CUSTOM_ITEM.namespace, PersistentDataType.STRING)
            ?: return null
        for (entry in CustomWeapons.entries) if (entry.toString() == s) return entry
        return null
    }

    fun ItemMeta?.getAsCustomItem(): CustomItems? {
        val s = this?.persistentDataContainer?.get(Namespaces.CUSTOM_ITEM.namespace, PersistentDataType.STRING)
            ?: return null
        for (entry in CustomItems.entries) if (entry.toString() == s) return entry
        return null
    }

    fun CustomItems?.isSoulInTheBottle(): Boolean = when (this) {
        CustomItems.SOUL_BOTTLE, CustomItems.OMINOUS_SOUL_BOTTLE -> true
        else -> false
    }

    fun ItemMeta?.getSoulInTheBottle(): String? {
        return this?.persistentDataContainer?.get(Namespaces.SOUL_IN_THE_BOTTLE.namespace, PersistentDataType.STRING)
    }

    fun ItemMeta?.setSoulInTheBottle(nick: String?) {
        if (nick == null) return
        this?.persistentDataContainer?.set(
            Namespaces.SOUL_IN_THE_BOTTLE.namespace,
            PersistentDataType.STRING,
            nick
        )
    }

    fun ItemStack?.setSoulInTheBottle(nick: String?) {
        if (nick == null) return
        val itemMeta = this?.itemMeta
        itemMeta?.persistentDataContainer?.set(
            Namespaces.SOUL_IN_THE_BOTTLE.namespace,
            PersistentDataType.STRING,
            nick
        )
        this?.itemMeta = itemMeta
    }

    fun soulBottleOf(ominous: Boolean, nick: String): ItemStack {
        val itemStack =
            if (ominous) CustomItems.OMINOUS_SOUL_BOTTLE.getAsItem() else CustomItems.SOUL_BOTTLE.getAsItem()
        itemStack.setSoulInTheBottle(nick)
        val itemMeta = itemStack.itemMeta
        itemMeta.itemName(Component.text("Склянка с Душой $nick"))
        itemMeta.setRarity(ItemRarity.EPIC)

        itemStack.itemMeta = itemMeta
        return itemStack
    }

    fun frostmourneExplosion(locy: Location, p: Player, hitCaster: Boolean = true) {
        val random = Random()
        val world = locy.world
        for (j in 1..25000) {
            val x = random.nextGaussian() * 10 + locy.x
            val y = random.nextGaussian() * 10 + locy.y
            val z = random.nextGaussian() * 10 + locy.z
            val loc = Location(world, x, y, z)
            if (!hitCaster && loc.distanceSquared(locy) < 4.0) continue
            TheColdestOne.snowyfi(loc)
        }

        locy.world.spawnParticle(
            Particle.END_ROD, locy,
            5000, .1, .1, .1, .75, null, true
        )
        locy.world.playSound(locy, Sound.ITEM_TRIDENT_THUNDER, 5f, 0.5f)

        val entities = locy.getNearbyLivingEntities(16.0)
        val damageSource =
            DamageSource.builder(org.bukkit.damage.DamageType.FREEZE).withDirectEntity(p).withCausingEntity(p).build()
        for (entity in entities) {
            val location = entity.location
            if (location.distanceSquared(locy) > 16.0 * 16.0) continue
            if (!hitCaster && entity.uniqueId == p.uniqueId) continue
            entity.damage(15.0, damageSource)
            val vector = location.toVector().subtract(locy.toVector()).multiply(Vector(1, 0, 1))
                .normalize().multiply(1.5).add(Vector(0.0, 0.8, 0.0))
            entity.velocity = vector
            entity.freezeTicks = entity.freezeTicks.coerceAtLeast(1000)
        }
    }

    fun isSword(material: Material): Boolean {
        return when (material) {
            Material.WOODEN_SWORD, Material.STONE_SWORD, Material.IRON_SWORD, Material.GOLDEN_SWORD, Material.DIAMOND_SWORD, Material.NETHERITE_SWORD -> true
            else -> false
        }
    }

    fun toTime(ticksLived: Int): String {
        val seconds = (ticksLived / (20)) % 60
        val minutes = (ticksLived / (20 * 60)) % 60
        val hours = (ticksLived / (20 * 60 * 60))

        var string = ""
        if (hours > 0) string += "${hours}h : "
        if (minutes > 0 || hours > 0) string += "${minutes}m : "
        string += "${seconds}s"
        return string
    }

    fun Material.isMeatOrFish() = this.isMeat() || this.isFish()

    fun Material.isFish() = this.isCookedFish() || this.isRawFish()

    fun Material.isRawFish() = when (this) {
        Material.COD, Material.SALMON, Material.TROPICAL_FISH, Material.PUFFERFISH -> true
        else -> false
    }

    fun Material.isCookedFish() = when (this) {
        Material.COOKED_COD, Material.COOKED_SALMON -> true
        else -> false
    }

    fun Material.isMeat() = this.isCookedMeat() || this.isRawMeat()

    fun Material.isCookedMeat() = when (this) {
        Material.COOKED_BEEF, Material.COOKED_PORKCHOP, Material.COOKED_MUTTON, Material.COOKED_CHICKEN, Material.COOKED_RABBIT, Material.RABBIT_STEW -> true
        else -> false
    }

    fun Material.isRawMeat() = when (this) {
        Material.BEEF, Material.PORKCHOP, Material.MUTTON, Material.CHICKEN, Material.RABBIT, Material.ROTTEN_FLESH -> true
        else -> false
    }

    fun throwItemTo(
        from: Location,
        to: Vector,
        itemStack: ItemStack
    ) {
        val velocity = to.subtract(from.toVector())
            .multiply(Vector(1.0, 0.0, 1.0)).normalize()
            .multiply(.2).add(Vector(0.0, .15, 0.0))

        from.world.spawn(from, Item::class.java) {
            it.itemStack = itemStack
            it.velocity = velocity
        }
    }

    fun createEvaExplosionWithParticles(location: Location) {
        createEvaExplosion(location.clone().add(0.0, -4.0, 0.0), 70.0, .11, 10.0, 7.0)
        DeathMarker(location)
        Bukkit.getScheduler().runTaskLater(Warvilore.instance, Runnable {
            location.world.players.forEach { it.stopSound(SoundCategory.MASTER) }
        }, 2L)
        Bukkit.getScheduler().runTaskLater(Warvilore.instance, Runnable {
            location.world.playSound(location, "levaltru:massive_boom_louder", 1000000f, 1f)
        }, 4L)
    }

    fun createEvaExplosion(
        location: Location,
        size: Double,
        minus: Double,
        multi: Double,
        randomThing: Double
    ) {
        var airBlocks = 0
        var i = 0
        while (airBlocks < 10) {
            var sawNoBlocks = true
            for (dx in 0..i) {
                val log = evaFormula(dx.toDouble(), i.toDouble(), size, minus, multi)

                val a = floor(log).toInt()

                if (i > 500) return

                for (r in 0..7) {
                    val mulX = when (r) {
                        0 -> dx
                        1 -> dx
                        2 -> -dx
                        3 -> -dx
                        4 -> i
                        5 -> i
                        6 -> -i
                        7 -> -i
                        else -> dx
                    }
                    val mulZ = when (r) {
                        0 -> i
                        1 -> -i
                        2 -> i
                        3 -> -i
                        4 -> dx
                        5 -> -dx
                        6 -> dx
                        7 -> -dx
                        else -> i
                    }
                    val randomT = kotlin.random.Random.nextDouble(0.0, 1.0)
                    val max = max(
                        a + (randomT * randomT * randomT * randomThing).roundToInt(),
                        location.world.minHeight - location.blockY
                    )
                    val loci = location.clone().add(mulX.toDouble(), max.toDouble(), mulZ.toDouble())
                    var reset = 0
                    var solidBlocksInARow = 0
                    while (reset < 50) {
                        if (loci.block.type.isAir) {
                            solidBlocksInARow = 0
                            reset++
                        } else {
                            solidBlocksInARow++
                            reset = 0
                            sawNoBlocks = false
                            loci.block.setType(Material.AIR, solidBlocksInARow < randomThing * 1.5)
                        }

                        loci.add(0.0, 1.0, 0.0)
                    }
                }
            }

            if (sawNoBlocks) airBlocks++
            else airBlocks = 0
            i++
        }
        val entities = location.world.entities
        for (entity in entities) {
            val entityLoc = entity.location
            val vector = entityLoc.toVector().subtract(location.toVector())
            val evaFormula = evaFormula(vector.x, vector.z, size, minus, multi)
            if (evaFormula - vector.y > 8.0) continue
            vector.normalize()
            while (vector.dot(Vector(0.0, 1.0, 0.0)) < .1) {
                vector.y += 1.0
                vector.normalize()
            }
            val bigVector = vector.clone().multiply(i)
            val add = location.clone().add(bigVector)
            while (!add.block.type.isAir) add.add(0.0, 25.0, 0.0)
            add.yaw = entityLoc.yaw
            add.pitch = entityLoc.pitch
            entity.teleport(add)
            val multiply = vector.clone().multiply(Vector(7.0, 4.0, 7.0))
            multiply.y = multiply.y.coerceAtLeast(2.0)
            Bukkit.getScheduler()
                .runTaskLater(
                    Warvilore.instance,
                    Runnable {
                        if (entity is Player) {
                            addInfiniteSlowfall(entity)
                        }
                        entity.velocity = multiply
                    },
                    5L
                )
        }
    }

    private fun evaFormula(
        dx: Double,
        dz: Double,
        size: Double,
        minus: Double,
        multi: Double
    ): Double {
        val xz = sqrt(dx * dx + dz * dz)
        val xz1 = (xz / size) + 1
        val d = log2((xz / size) - minus) * multi + (xz1 * xz1 * xz1 * xz1)
        return if (d.isNaN()) Double.NEGATIVE_INFINITY else d
    }

    fun addInfiniteSlowfall(it: Player) {
        it.addPotionEffect(PotionEffect(PotionEffectType.SLOW_FALLING, 50, 1, true, false, true))
    }

    fun neighboringBlocksLocs() = listOf(
        Vector(0, 0, 1),
        Vector(0, 0, -1),
        Vector(0, 1, 0),
        Vector(0, -1, 0),
        Vector(1, 0, 0),
        Vector(-1, 0, 0),
    )

    fun isGlass(material: Material?): Boolean {
        return when (material) {
            GLASS -> true
            else -> isColoredGlass(material)
        }
    }

    fun isColoredGlass(material: Material?): Boolean {
        return when (material) {
            RED_STAINED_GLASS, ORANGE_STAINED_GLASS, YELLOW_STAINED_GLASS, GREEN_STAINED_GLASS, LIME_STAINED_GLASS, BLUE_STAINED_GLASS, CYAN_STAINED_GLASS, LIGHT_BLUE_STAINED_GLASS,
            BROWN_STAINED_GLASS, BLACK_STAINED_GLASS, GRAY_STAINED_GLASS, LIGHT_GRAY_STAINED_GLASS, WHITE_STAINED_GLASS, PURPLE_STAINED_GLASS, MAGENTA_STAINED_GLASS, PINK_STAINED_GLASS
                -> true
            else -> false
        }
    }

    object Deads {
        fun hasDied(nickname: String): Boolean {
            val nickname = nickname.lowercase()
            val get = getDiedList()
            return get?.contains(nickname) ?: false
        }

        fun removeDied(nickname: String): Boolean {
            val nickname = nickname.lowercase()
            val diedList = getDiedList()
            val remove = diedList?.remove(nickname)
            setDiedList(diedList)
            return remove ?: false
        }

        fun addDied(nickname: String): Boolean {
            Bukkit.getPlayer(nickname)?.let {
                it.health = 0.0
                it.kick(Component.text("Вы умерли.").color(NamedTextColor.RED))
            }
            val nickname = nickname.lowercase()
            val diedList = getDiedList()
            if (diedList == null) {
                setDiedList(listOf(nickname))
                return true
            }
            if (diedList.contains(nickname)) return false
            diedList.add(nickname)
            setDiedList(diedList)
            return true
        }

        fun getDiedList() = Bukkit.getWorlds()[0].persistentDataContainer.get(
            Namespaces.PUBLIC_DEAD_PEOPLE.namespace,
            PersistentDataType.LIST.strings()
        )?.toMutableList()

        fun setDiedList(list: List<String>?) {
            if (list == null) {
                Bukkit.getWorlds()[0].persistentDataContainer.remove(Namespaces.PUBLIC_DEAD_PEOPLE.namespace)
                return
            }
            Bukkit.getWorlds()[0].persistentDataContainer.set(
                Namespaces.PUBLIC_DEAD_PEOPLE.namespace,
                PersistentDataType.LIST.strings(), list
            )
        }
    }

    object Hiddens {
        fun isHidden(nickname: String?): Boolean {
            if (nickname == null) return false
            val nickname = nickname.lowercase()
            val get = getHiddenList()
            return get?.contains(nickname) ?: false
        }

        fun removeHidden(nickname: String): Boolean {
            val nickname = nickname.lowercase()
            val hiddenList = getHiddenList()
            val remove = hiddenList?.remove(nickname)
            setHiddenList(hiddenList)
            return remove ?: false
        }

        fun addHidden(nickname: String): Boolean {
            Bukkit.getPlayer(nickname)?.let { hidePlayerPacket(it.uniqueId, 2) }
            val nickname = nickname.lowercase()
            val hiddenList = getHiddenList()
            if (hiddenList == null) {
                setHiddenList(listOf(nickname))
                return true
            }
            if (hiddenList.contains(nickname)) return false
            hiddenList.add(nickname)
            setHiddenList(hiddenList)
            return true
        }

        fun getHiddenList() = Bukkit.getWorlds()[0].persistentDataContainer.get(
            Namespaces.PUBLIC_HIDDEN_PEOPLE.namespace,
            PersistentDataType.LIST.strings()
        )?.toMutableList()

        fun setHiddenList(list: List<String>?) {
            if (list == null) {
                Bukkit.getWorlds()[0].persistentDataContainer.remove(Namespaces.PUBLIC_HIDDEN_PEOPLE.namespace)
                return
            }
            Bukkit.getWorlds()[0].persistentDataContainer.set(
                Namespaces.PUBLIC_HIDDEN_PEOPLE.namespace,
                PersistentDataType.LIST.strings(), list
            )
        }

        fun hidePlayerPacket(uuid: UUID, afterTicks: Long) {
            hidePlayerPacket(listOf(uuid), afterTicks)
        }

        fun hidePlayerPacket(uuids: List<UUID>, afterTicks: Long) {
            if (protocolManager == null) return
            Bukkit.getScheduler().runTaskLater(Warvilore.instance, Runnable {
                val packet = protocolManager.createPacket(PacketType.Play.Server.PLAYER_INFO_REMOVE)
                packet.uuidLists.write(0, uuids)
                for (player in Bukkit.getOnlinePlayers()) {
                    if (uuids.contains(player.uniqueId)) continue
                    protocolManager.sendServerPacket(player, packet)
                }
            }, afterTicks)
        }
    }

    object ItemMetas {

        fun soulBottle(itemMeta: ItemMeta) {
            val food = itemMeta.food
            food.nutrition = 1
            food.saturation = 3.5f
            food.setCanAlwaysEat(true)
            itemMeta.setFood(food)
        }

        fun soulBeer(itemMeta: ItemMeta, usingConvertsTo: ItemStack) {
            val food = itemMeta.food
            food.nutrition = 5
            food.saturation = 6.5f
            food.setCanAlwaysEat(true)
            val soul = itemMeta.getSoulInTheBottle()
            food.usingConvertsTo = usingConvertsTo.also { it.setSoulInTheBottle(soul) }
            itemMeta.setFood(food)

            itemMeta.itemName(itemMeta.itemName().append(Component.text(" $soul")))
            itemMeta.setMaxStackSize(1)
        }

        fun MortuusAndVictus(itemMeta: ItemMeta) {
            itemMeta.isFireResistant = true
        }

    }

//    fun myMod(d: Double, m: Double): Double = if (d < 0) (d % m + m) % m else d % m

}