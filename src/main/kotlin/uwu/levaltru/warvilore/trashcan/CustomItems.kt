package uwu.levaltru.warvilore.trashcan

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.apache.logging.log4j.util.BiConsumer
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import uwu.levaltru.warvilore.trashcan.LevsUtils.isSoulBound
import java.util.*

private const val MEA_CULPA_CUSTOM_MODEL = 1
private const val FROSTMOURNE_CUSTOM_MODEL = 1

enum class CustomItems(
    val timesBeforeBreak: Int?,
    val customModel: Int,
    val material: Material,
    val whenGive: BiConsumer<ItemMeta, Material>,
    val onBreak: BiConsumer<Player, Location>
) {
    FROSTMOURNE(
        3, FROSTMOURNE_CUSTOM_MODEL, Material.NETHERITE_SWORD,
        { itemMeta, material ->
            run {
                for ((a, b) in material.getDefaultAttributeModifiers(EquipmentSlot.HAND).entries()) {
                    val amount = when (a) {
                        Attribute.GENERIC_ATTACK_SPEED -> -3.0
                        Attribute.GENERIC_ATTACK_DAMAGE -> 9.0
                        else -> {
                            b.amount
                        }
                    }
                    itemMeta.addAttributeModifier(
                        a,
                        AttributeModifier(b.uniqueId, b.name, amount, b.operation, b.slotGroup)
                    )
                }
                itemMeta.addAttributeModifier(
                    Attribute.PLAYER_ENTITY_INTERACTION_RANGE,
                    AttributeModifier(
                        UUID.randomUUID(), "theColdestOneSword", 1.5,
                        AttributeModifier.Operation.ADD_NUMBER, EquipmentSlot.HAND
                    )
                )
                itemMeta.setCustomModelData(FROSTMOURNE_CUSTOM_MODEL)
                itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                if (itemMeta.hasItemName()) {
                    itemMeta.itemName(itemMeta.itemName().color(NamedTextColor.DARK_AQUA))
                } else itemMeta.itemName(Component.text("Ледяная Скорбь").color(NamedTextColor.DARK_AQUA))
                (itemMeta as? Damageable)?.damage = 0
            }
        },
        { p, l ->
            {

            }
        },
    ),
    MEA_CULPA(
        7, MEA_CULPA_CUSTOM_MODEL, Material.IRON_SWORD,
        { itemMeta, material ->
            run {
                if (!itemMeta.hasItemName()) itemMeta.itemName(
                    Component.text("Mea Culpa")
                        .color(NamedTextColor.DARK_AQUA)
                )
                else itemMeta.itemName(itemMeta.itemName().color(NamedTextColor.DARK_AQUA))
                (itemMeta as Damageable).damage = 0
                itemMeta.setMaxDamage(250)
            }
        },
        { p, l ->
            {

            }
        },
    );

    fun giveItem(soulBouder: String?): ItemStack {
        return giveItem(1, soulBouder)
    }

    fun giveItem(amount: Int, soulBouder: String?): ItemStack {
        return replaceItem(ItemStack(material, amount), soulBouder)
    }

    fun replaceItem(item: ItemStack, soulBouder: String?): ItemStack {
        val itemMeta = item.itemMeta.apply {
            setCustomModelData(customModel)
            timesBeforeBreak?.let {
                persistentDataContainer.set(
                    Namespaces.TIMES_BEFORE_BREAK.namespace,
                    PersistentDataType.INTEGER,
                    it
                )
            }
            soulBouder?.let {
                persistentDataContainer.set(
                    Namespaces.SOULBOUND.namespace,
                    PersistentDataType.STRING,
                    it
                )
            }
        }
        whenGive.accept(itemMeta, item.type)
        item.itemMeta = itemMeta
        return item
    }

    fun equals(itemStack: ItemStack): Boolean {
        if (material != itemStack.type) return false
        val itemMeta = itemStack.itemMeta
        if (!itemMeta.hasCustomModelData()) return false
        if (customModel != itemMeta.customModelData) return false
        if (!itemMeta.isSoulBound()) return false
        return true
    }
}