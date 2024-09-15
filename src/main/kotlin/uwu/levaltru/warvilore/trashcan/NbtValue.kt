package uwu.levaltru.warvilore.trashcan

import org.bukkit.entity.Player
import org.bukkit.persistence.PersistentDataType
import uwu.levaltru.warvilore.Warvilore
import kotlin.reflect.KProperty

class NbtValue<C : Any>(
    val persistentDataType: PersistentDataType<*, C>,
    namespaces: Namespaces,
    val player: Player,
    val defaultValue: C
) {
    private val namespace = namespaces.namespace

    operator fun getValue(thisRef: Any?, property: KProperty<*>): C {
        return try {
            getValueFromNbt()
        } catch (e: Exception) {
            Warvilore.log(e.toString())
            defaultValue
        }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: C?) {
        if (value != null) player.persistentDataContainer.set(namespace, persistentDataType, value)
        else player.persistentDataContainer.remove(namespace)
    }

    private fun getValueFromNbt() : C = player.persistentDataContainer.get(namespace, persistentDataType) ?: defaultValue

    override fun toString(): String = getValueFromNbt().toString()
}