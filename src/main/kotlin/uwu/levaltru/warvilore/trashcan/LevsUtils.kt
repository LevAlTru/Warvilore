package uwu.levaltru.warvilore.trashcan

import org.bukkit.Location
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import uwu.levaltru.warvilore.trashcan.LevsUtils.getSoulBound
import java.util.*
import javax.annotation.meta.TypeQualifierNickname
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

}