package uwu.levaltru.warvilore.trashcan

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemRarity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import uwu.levaltru.warvilore.Warvilore

enum class CustomItems(val material: Material, val model: Int, val itemMetaFunction: () -> ItemMeta, val itemName: String? = null, val itemRarity: ItemRarity? = null) {

    // Honey Bottle
    SOUL_BOTTLE(Material.HONEY_BOTTLE, 1, LevsUtils.ItemMetas.soulBottle, null, ItemRarity.UNCOMMON),
    OMINOUS_SOUL_BOTTLE(Material.HONEY_BOTTLE, 2, LevsUtils.ItemMetas.soulBottle, null, ItemRarity.UNCOMMON),

    SOUL_BEER(Material.HONEY_BOTTLE, 3, LevsUtils.ItemMetas.soulBeer, "Пиво Из Душ", ItemRarity.UNCOMMON),
    OMINOUS_SOUL_BEER(Material.HONEY_BOTTLE, 4, LevsUtils.ItemMetas.soulBeer, "Холодное Пиво Из Душ", ItemRarity.UNCOMMON),


    // Bow

    NETHERITE_BOW(Material.BOW, 1, {
        val itemMeta = ItemStack(Material.BOW).itemMeta

        itemMeta.isFireResistant = true
        (itemMeta as Damageable).setMaxDamage(1324)

        itemMeta
    }, "Незеритовый Лук", ItemRarity.UNCOMMON),


    // Repeating Command Block

    SHARD_OF_MORTUUS(Material.REPEATING_COMMAND_BLOCK, 1, LevsUtils.ItemMetas.MortuusAndVictus, "Осколок Mortuus'a", ItemRarity.RARE),
    FRAGMENT_OF_VICTUS(Material.REPEATING_COMMAND_BLOCK, 2, LevsUtils.ItemMetas.MortuusAndVictus, "Фрагмент Victus'a", ItemRarity.RARE),

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

        itemMeta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES)

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