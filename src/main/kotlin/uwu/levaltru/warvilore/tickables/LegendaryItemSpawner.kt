package uwu.levaltru.warvilore.tickables

import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Item
import org.bukkit.entity.LightningStrike
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.Tickable
import uwu.levaltru.warvilore.trashcan.LevsUtils

class LegendaryItemSpawner(val location: Location, val itemStack: ItemStack, val maxAge: Int) : Tickable() {

    override fun tick(): Boolean {

        if (age % 20 == 0)
            location.world.playSound(location, Sound.BLOCK_TRIAL_SPAWNER_ABOUT_TO_SPAWN_ITEM, 4f, .5f)

        if (age + 10 == maxAge)
            location.world.spawn(location, LightningStrike::class.java)
        if (age + 8 < maxAge) {
            location.world.spawnParticle(
                Particle.OMINOUS_SPAWNING,
                location,
                LevsUtils.roundToRandomInt(age.toDouble() / maxAge * 5),
                .1,
                .1,
                .1,
                .25 + age.toDouble() / maxAge * 1.25,
                null,
                true
            )
        }

        if (age++ > maxAge) return spawnItem()
        return false
    }

    fun spawnItem(): Boolean {
        location.world.spawn(location, Item::class.java) {
            it.itemStack = itemStack
            it.velocity = Vector()
            it.setGravity(false)
        }
        location.world.playSound(location, Sound.BLOCK_END_PORTAL_SPAWN, 8f, .7f)
        location.world.spawnParticle(Particle.END_ROD, location, 100, .1, .1, .1, .05, null, true)
        return true
    }

    override fun collapse() {
        spawnItem()
    }
}