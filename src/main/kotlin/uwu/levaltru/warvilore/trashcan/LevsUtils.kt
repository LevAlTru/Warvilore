package uwu.levaltru.warvilore.trashcan

import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import java.util.*
import kotlin.math.floor

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

    fun getAsCustomItem(itemMeta: ItemMeta): CustomItems? {
        val s = itemMeta.persistentDataContainer[Namespaces.CUSTOM_ITEM.namespace, PersistentDataType.STRING] ?: return null
        return CustomItems.valueOf(s)
    }

}