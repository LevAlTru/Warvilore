package uwu.levaltru.warvilore.tickables.effect

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Display
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f
import uwu.levaltru.warvilore.Tickable
import uwu.levaltru.warvilore.trashcan.Namespaces
import kotlin.random.Random

private const val MAX_AGE = 200

private const val STATE_STEP = 45
private const val FIRST_STEP_DURATION = 1

private const val START_SIZE = 20.0
private const val END_SIZE = 30.0

private const val FINAL_STATE_NUMBER = 8

class DeathMarker(val location: Location) : Tickable() {

    private var display: ItemDisplay? = null

    override fun tick(): Boolean {
        val zeroToOneAge = age.toDouble() / overAge()
        val lerp = (START_SIZE * (1 - zeroToOneAge) + END_SIZE * zeroToOneAge).toFloat()
        if (display == null) {
            location.yaw = Random.nextFloat() * 360f - 180f
            location.pitch = 0f
            display = location.world.spawn(location, ItemDisplay::class.java) {
                it.persistentDataContainer.set(Namespaces.SHOULD_DESPAWN.namespace, PersistentDataType.BOOLEAN, true)
                it.setItemStack(function())
                it.interpolationDuration = 7
                it.transformation = Transformation(
                    Vector3f(),
                    Quaternionf(),
                    Vector3f(lerp),
                    Quaternionf(),
                )
                it.brightness = Display.Brightness(15, 15)
            }
        } else {
            display!!.teleportDuration = 3
            display!!.teleport(display!!.location.add(0.0, 0.05, 0.0))
            display!!.setItemStack(function())
            display!!.transformation = Transformation(
                Vector3f(),
                Quaternionf(),
                Vector3f(lerp),
                Quaternionf(),
            )
        }
        if (age++ > overAge()) {
            display?.remove()
            return true
        }
        return false
    }

    private fun overAge() = STATE_STEP * (FINAL_STATE_NUMBER + FIRST_STEP_DURATION + 1)

    private fun getCustomModelData() = (age / STATE_STEP - FIRST_STEP_DURATION).coerceAtLeast(1)

    private fun function(): ItemStack {
        val itemStack = ItemStack(Material.CHAIN_COMMAND_BLOCK)
        val itemMeta = itemStack.itemMeta

        itemMeta.setCustomModelData(getCustomModelData())

        itemStack.itemMeta = itemMeta
        return itemStack
    }
}