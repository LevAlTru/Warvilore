package uwu.levaltru.warvilore.trashcan

import org.bukkit.Bukkit
import org.bukkit.persistence.PersistentDataType
import uwu.levaltru.warvilore.Warvilore

object ServerVariables {
    enum class ListStrings {
        CURSED_PLAYERS;

        fun get() =
            Bukkit.getWorld("world")!!.persistentDataContainer.get(
                Warvilore.namespace("public_data_${this.name}"), PersistentDataType.LIST.strings()
            )

        fun set(list: List<String>) {
            Bukkit.getWorld("world")!!.persistentDataContainer.set(
                Warvilore.namespace("public_data_${this.name}"), PersistentDataType.LIST.strings(), list
            )
        }
    }
}