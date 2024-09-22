package uwu.levaltru.warvilore.trashcan

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemRarity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.trashcan.ItemMetaTrashcan.soul_bottle_itemMeta

enum class CustomItems(val material: Material, val model: Int, val itemMetaFunction: () -> ItemMeta, val itemName: String? = null, val itemRarity: ItemRarity? = null) {

    SOUL_BOTTLE(Material.HONEY_BOTTLE, 1, soul_bottle_itemMeta, null, ItemRarity.UNCOMMON),
    OMINOUS_SOUL_BOTTLE(Material.HONEY_BOTTLE, 2, soul_bottle_itemMeta, null, ItemRarity.UNCOMMON),

    NETHERITE_BOW(Material.BOW, 1, {
        val itemMeta = ItemStack(Material.BOW).itemMeta

        itemMeta.isFireResistant = true
        (itemMeta as Damageable).setMaxDamage(1324)

        itemMeta
    }, "Незеритовый Лук", ItemRarity.UNCOMMON),

    SHARD_OF_MORTUUS(Material.REPEATING_COMMAND_BLOCK, 1, { ItemStack(Material.AMETHYST_SHARD).itemMeta }, "Осколок Mortuus'a", ItemRarity.RARE),
    FRAGMENT_OF_VICTUS(Material.REPEATING_COMMAND_BLOCK, 2, { ItemStack(Material.AMETHYST_SHARD).itemMeta }, "Фрагмент Victus'a", ItemRarity.RARE),

    YOUR_REALITY_HAS_COLLAPSED(Material.REPEATING_COMMAND_BLOCK, 3, {
        val itemMeta = ItemStack(Material.WOODEN_SWORD).itemMeta

        itemMeta.isFireResistant = true
        itemMeta.setEnchantmentGlintOverride(true)

        itemMeta.addAttributeModifier(
            org.bukkit.attribute.Attribute.GENERIC_ATTACK_SPEED, org.bukkit.attribute.AttributeModifier(
                Warvilore.namespace("your_reality_has_collapsed"),
                -.85,
                org.bukkit.attribute.AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                org.bukkit.inventory.EquipmentSlotGroup.MAINHAND
            )
        )

        itemMeta
    }, "The Knot Of Life", ItemRarity.EPIC),


    ;

    fun getAsItem(): ItemStack {
        val itemStack = ItemStack(this.material)
        val itemMeta = this.itemMetaFunction()
        itemMeta.persistentDataContainer[Namespaces.CUSTOM_ITEM.namespace, PersistentDataType.STRING] = this.toString()
        itemMeta.setCustomModelData(this.model)
        this.itemName?.let { itemMeta.itemName(Component.text(it)) }
        this.itemRarity?.let { itemMeta.setRarity(it) }
        itemStack.itemMeta = itemMeta

        return itemStack
    }

}