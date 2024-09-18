package uwu.levaltru.warvilore.trashcan

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

data class CauldronDataObject(val int: Int) {

    var itemList: List<ItemStack>? = null

    constructor(int: Int, itemList: List<ItemStack>?) : this(int) {
        this.itemList = itemList?.filter { it.type != Material.AIR }
    }
}
