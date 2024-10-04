package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.*
import org.bukkit.damage.DamageSource
import org.bukkit.damage.DamageType
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.tickables.projectiles.BloodySlice
import uwu.levaltru.warvilore.trashcan.CustomWeapons
import uwu.levaltru.warvilore.trashcan.LevsUtils.getAsCustomItem
import uwu.levaltru.warvilore.trashcan.LevsUtils.getAsCustomWeapon
import uwu.levaltru.warvilore.trashcan.Namespaces
import kotlin.math.max

private const val RIGHT_CLICK_TIMES = 6

private const val PRAY_COOLDOWN = 20 * 45
private const val BLOOD_SLICE_COOLDOWN = 15
private const val PRAY_SCORE = 20

class TheHolyOne(string: String) : AbilitiesCore(string) {

    var timeBeforeNextPray: Int = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.BEFORE_NEXT_PRAY.namespace,
                PersistentDataType.INTEGER
            ) ?: 0
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.BEFORE_NEXT_PRAY.namespace,
                PersistentDataType.INTEGER, value
            )
            field = value
        }

    var bloodSliceCooldown: Int = 0
    var burningScore = 0
    var rightClickTimes = 0
    var prevLoc: Location? = null

    override fun onTick(event: ServerTickEndEvent) {

        val location = player!!.location
        if (bloodSliceCooldown > 0) bloodSliceCooldown--

        if (abilitiesDisabled) return

        if (prevLoc == null
            || ((timeBeforeNextPray > 0 && player!!.gameMode != GameMode.CREATIVE) && burningScore <= 0)
            || location.distanceSquared(prevLoc!!) > 0.0001
            || !player!!.isSneaking
        ) {
            if (timeBeforeNextPray > 1 && timeBeforeNextPray % 20 == 0)
                player!!.sendActionBar(text("${timeBeforeNextPray / 20}s").color(NamedTextColor.RED))
            else if (timeBeforeNextPray == 1) player!!.sendActionBar(text(""))

            prevLoc = location.clone()
            rightClickTimes = 0
            timeBeforeNextPray -= timeBeforeNextPray.coerceAtMost(1)
            burningScore = 0
            return
        }

        if (!isBurning()) return

        if (player!!.ticksLived % 4 != 0) return

        burningScore++
        timeBeforeNextPray = burningScore * PRAY_COOLDOWN / PRAY_SCORE

        val world = player!!.world
        world.playSound(location, Sound.ITEM_FIRECHARGE_USE, 0.5f, 0.8f)
        val locy = location.clone().add(0.0, player!!.height / 2, 0.0)
        world.spawnParticle(
            Particle.SOUL_FIRE_FLAME, locy, 24,
            .2, .4, .2, .03, null, true
        )


        val damageSource = DamageSource.builder(DamageType.IN_FIRE).withDirectEntity(player!!).withCausingEntity(player!!).build()
        val nearbyLivingEntities = location.getNearbyLivingEntities(10.0)
        if (burningScore >= PRAY_SCORE) {
            for (livingEntity in nearbyLivingEntities.filter { it.location.distanceSquared(location) < 100.0 }) {
                if (livingEntity.uniqueId == player!!.uniqueId) continue
                livingEntity.damage(10.0, damageSource)
                livingEntity.fireTicks = max(livingEntity.fireTicks, 200)
                livingEntity.velocity = livingEntity.location.subtract(locy).toVector()
                    .multiply(Vector(1, 0, 1)).normalize().multiply(0.7).add(Vector(0.0, 0.5, 0.0))
            }
            world.spawnParticle(
                Particle.SOUL_FIRE_FLAME, locy, 1000,
                .2, .4, .2, .5, null, true
            )
            world.spawnParticle(
                Particle.TRIAL_SPAWNER_DETECTION_OMINOUS, locy, 2000,
                .2, .4, .2, .5, null, true
            )
            world.playSound(location, Sound.ENTITY_WARDEN_SONIC_BOOM, 3f, 0.7f)
            world.playSound(location, Sound.ITEM_TRIDENT_THUNDER, 3f, 0.7f)
            burningScore = 0
            val item = player!!.inventory.itemInMainHand
            if (item.type == Material.IRON_SWORD) {
                val itemMeta = item.itemMeta
                if (!itemMeta.hasCustomModelData() || item.isMeaCulpa()) {
                    player!!.inventory.setItemInMainHand(CustomWeapons.MEA_CULPA.replaceItem(item, player!!.name))
                    heartAttack(true)
                }
            }
        }

        pipa@ for (i in 1..25) {
            var x: Double
            var z: Double
            do {
                x = random.nextDouble(-7.0, 7.0)
                z = random.nextDouble(-7.0, 7.0)
                val d = x * x + z * z
            } while (d > 7 * 7 || d < 3 * 3)
            val add = location.clone().toCenterLocation().add(x, -0.3, z)
            for (j in 1..4)
                if (j == 4) continue@pipa
                else if (isCube(add)) add.add(0.0, 1.0, 0.0)
                else if (!isCube(add) && !isCube(add.clone().add(0.0, -1.0, 0.0))) add.add(0.0, -1.0, 0.0)
                else break
            for (entity in nearbyLivingEntities.filter { it.location.distanceSquared(add) < 4.0 }) {
                entity.damage(4.0, damageSource)
                entity.velocity = Vector(x, 0.0, z).normalize().multiply(0.2).add(Vector(0.0, 0.1, 0.0))
                entity.fireTicks = (entity.fireTicks + 50).coerceAtMost(200)
            }
            if (random.nextDouble() < 0.05)
                world.setType(add, Material.FIRE)
            if (add.block.type == Material.SNOW) world.setType(add, Material.AIR)
            world.spawnParticle(
                Particle.SOUL_FIRE_FLAME, add, 20,
                0.5, 0.0, 0.5, 0.05, null, true
            )
            world.spawnParticle(
                Particle.TRIAL_SPAWNER_DETECTION_OMINOUS, add, 10,
                0.5, 0.0, 0.5, 0.03, null, true
            )
        }
    }

    private fun isCube(add: Location): Boolean {
        val block = add.block
        return !block.isPassable && block.boundingBox.volume > 0.99
    }

    override fun onAction(event: PlayerInteractEvent) {
        val itemInMainHand = player!!.inventory.itemInMainHand
        if ((itemInMainHand.isMeaCulpa())
            && !player!!.isSneaking &&
            (bloodSliceCooldown <= 0 || player!!.gameMode == GameMode.CREATIVE)
            && event.action.isRightClick
        ) {
            BloodySlice(player!!.eyeLocation, player!!.location.direction.multiply(1.5), player!!.uniqueId)
            heartAttack(false)
            player!!.world.playSound(
                player!!.location,
                Sound.BLOCK_TRIAL_SPAWNER_SPAWN_ITEM,
                SoundCategory.MASTER,
                1f,
                0.8f
            )
            bloodSliceCooldown = BLOOD_SLICE_COOLDOWN
        } else if ((itemInMainHand.type == Material.IRON_SWORD || itemInMainHand.isMiPenitencia())
            && event.action.isRightClick
            && player!!.pitch > 20.0
            && player!!.isSneaking
        ) rightClickTimes++
    }

    override fun onDeath(event: PlayerDeathEvent) {
        bloodSliceCooldown = 0
        timeBeforeNextPray = 0
    }

    private fun isBurning(): Boolean = rightClickTimes >= RIGHT_CLICK_TIMES

    fun ItemStack?.isMeaCulpa(): Boolean = this?.itemMeta?.getAsCustomWeapon() == CustomWeapons.MEA_CULPA
    fun ItemStack?.isMiPenitencia(): Boolean = this?.itemMeta?.getAsCustomWeapon() == CustomWeapons.MI_PENITENCIA

    private fun heartAttack(effects: Boolean) {
        if (player!!.gameMode != GameMode.CREATIVE) {
            val damageSource = DamageSource.builder(DamageType.GENERIC).withDirectEntity(player!!).withCausingEntity(player!!).build()
            player!!.damage(0.01, damageSource)
            player!!.health -= player!!.health.coerceAtMost(6.99)
        }
        if (effects)
            player!!.addPotionEffects(
            listOf(
                PotionEffect(PotionEffectType.BLINDNESS, 100, 0, true, false, true),
                PotionEffect(PotionEffectType.SLOWNESS, 250, 2, true, false, true),
                PotionEffect(PotionEffectType.SLOWNESS, 175, 3, true, false, true),
                PotionEffect(PotionEffectType.SLOWNESS, 100, 4, true, false, true),
            )
        )
    }

    override fun getAboutMe(): List<Component> = listOf(
        text("С тобой бог. Для молитвы богу тебе надо присесть и наживать по земле с железным мечем в руках. Пока ты в присяди, ").color(NamedTextColor.GREEN)
            .append { text("синий огонь ").color(NamedTextColor.DARK_AQUA) }
            .append {
                text("твоей вины опутает тебя и окресность вокгур. Он будет тебя защищать до конца молитвы. Если ты будешь молится достаточно долго, то твой железный мечь будет награжден новой формой.")
                    .color(NamedTextColor.GREEN)
            },
        text("Его имя ").color(NamedTextColor.GREEN)
            .append { text("Mea Culpa. ").style(Style.style(TextDecoration.ITALIC, NamedTextColor.DARK_AQUA)) }
            .append { text("Этот меч позволит тебе выпускать ").color(NamedTextColor.GREEN) }
            .append { text("Лезвие Крови. ").color(NamedTextColor.RED) },
        text("Но будь осторожен! После каждой молитвы дьявол захочет тебе помешать! ").color(NamedTextColor.GOLD)
            .append { text("Ты почуствешь себя плохо, и ты даже потеряешь часть своих жизней. ").color(NamedTextColor.RED) }
            .append { text("Также это будет происходить когда ты используешь ").color(NamedTextColor.GOLD) }
            .append { text("Лезвие Крови ").color(NamedTextColor.RED) }
            .append { text("(потому что selfharm это больно u know?).").color(NamedTextColor.LIGHT_PURPLE) }
    )
}