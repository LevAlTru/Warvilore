package uwu.levaltru.warvilore.trashcan

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import uwu.levaltru.warvilore.trashcan.ItemMetaTrashcan.soul_bottle_itemMeta

enum class CustomItems(val material: Material, val model: Int, val itemMetaFunction: () -> ItemMeta) {

    SOUL_BOTTLE(Material.HONEY_BOTTLE, 1, soul_bottle_itemMeta),
    OMINOUS_SOUL_BOTTLE(Material.HONEY_BOTTLE,2, soul_bottle_itemMeta),

    NETHERITE_BOW(Material.BOW,1, {
        val itemMeta = ItemStack(Material.BOW).itemMeta

        itemMeta.isFireResistant = true
        itemMeta.displayName(net.kyori.adventure.text.Component.text("Незеритовый Лук"))
        (itemMeta as Damageable).setMaxDamage(1324)

        itemMeta
    }),

    SOULLITE_INGOT(Material.NETHERITE_INGOT,1, {
        val itemMeta = ItemStack(Material.NETHERITE_INGOT).itemMeta

        itemMeta.displayName(net.kyori.adventure.text.Component.text("Соллитовый Слиток"))

        itemMeta
    }),

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