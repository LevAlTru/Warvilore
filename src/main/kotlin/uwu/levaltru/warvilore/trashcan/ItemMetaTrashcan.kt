package uwu.levaltru.warvilore.trashcan

import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import uwu.levaltru.warvilore.abilities.AbilitiesCore.Companion.getAbilities
import uwu.levaltru.warvilore.abilities.abilities.TestAbility
import uwu.levaltru.warvilore.tickables.SoulSucker

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