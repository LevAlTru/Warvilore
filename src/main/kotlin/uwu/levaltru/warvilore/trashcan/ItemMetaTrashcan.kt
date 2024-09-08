package uwu.levaltru.warvilore.trashcan

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

object ItemMetaTrashcan {

    val soul_bottle_itemMeta = {
        val itemMeta = ItemStack(Material.GLASS_BOTTLE).itemMeta
        val food = itemMeta.food
        food.nutrition = 1
        food.saturation = 3.5f
        food.setCanAlwaysEat(true)
        itemMeta.setFood(food)

        itemMeta
    }

}