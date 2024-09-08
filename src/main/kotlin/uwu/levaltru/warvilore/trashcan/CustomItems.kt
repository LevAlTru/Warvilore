package uwu.levaltru.warvilore.trashcan

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import uwu.levaltru.warvilore.trashcan.ItemMetaTrashcan.soul_bottle_itemMeta

enum class CustomItems(val material: Material, val model: Int, val itemMetaFunction: () -> ItemMeta) {

    SOUL_BOTTLE(Material.HONEY_BOTTLE, 1, soul_bottle_itemMeta),
    OMINOUS_SOUL_BOTTLE(Material.HONEY_BOTTLE,2, soul_bottle_itemMeta),

    ;

    fun getAsItem(): ItemStack {
        val itemStack = ItemStack(this.material)
        val itemMeta = this.itemMetaFunction()
        itemMeta.persistentDataContainer[Namespaces.CUSTOM_ITEM.namespace, PersistentDataType.STRING] = this.toString()
        itemMeta.setCustomModelData(this.model)
        itemStack.itemMeta = itemMeta

        return itemStack
    }

}