package uwu.levaltru.warvilore.trashcan

import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta

object ItemMetaTrashcan {

    val soul_bottle_itemMeta: () -> ItemMeta = {
        val itemMeta = ItemStack(Material.GLASS_BOTTLE).itemMeta
        val food = itemMeta.food
        food.nutrition = 1
        food.saturation = 3.5f
        food.setCanAlwaysEat(true)
        itemMeta.setFood(food)

        itemMeta
    }

}