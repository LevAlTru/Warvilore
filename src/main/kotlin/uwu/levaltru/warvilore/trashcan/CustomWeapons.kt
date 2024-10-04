package uwu.levaltru.warvilore.trashcan

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.trashcan.LevsUtils.getSoulBound

enum class CustomWeapons(
    val timesBeforeBreak: Int? = null,
    val customModel: Int,
    val material: Material,
    val whenGive: (ItemMeta) -> Unit = {_ -> },
    val onBreak: (Player, Entity) -> Unit = {_, _ -> },
    val displayName: Component,
) {
    FROSTMOURNE(
        2, 1, Material.NETHERITE_SWORD,
        { itemMeta ->
            run {
                for ((a, b) in Material.NETHERITE_SWORD.getDefaultAttributeModifiers(EquipmentSlot.HAND).entries()) {
                    val amount = when (a) {
                        Attribute.GENERIC_ATTACK_SPEED -> -3.0
                        Attribute.GENERIC_ATTACK_DAMAGE -> 9.0
                        else -> {
                            b.amount
                        }
                    }
                    itemMeta.addAttributeModifier(a, AttributeModifier(b.key, amount, b.operation, b.slotGroup))
                }
                itemMeta.addAttributeModifier(
                    Attribute.PLAYER_ENTITY_INTERACTION_RANGE,
                    AttributeModifier(Warvilore.namespace("theColdestOneSword"), 1.5, AttributeModifier.Operation.ADD_NUMBER, org.bukkit.inventory.EquipmentSlotGroup.HAND)
                )
                itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
                (itemMeta as? Damageable)?.damage = 0
            }
        },
        { p, e ->
            val originalItem = p.inventory.itemInMainHand
            val item = ItemStack(Material.NETHERITE_SWORD)
            val itemMeta = item.itemMeta
            if (itemMeta is Damageable) {
                itemMeta.damage = 2000
                for ((enchant, level) in originalItem.enchantments) itemMeta.addEnchant(enchant, level, true)
                item.itemMeta = itemMeta
            }
            p.inventory.setItemInMainHand(item)

            val pLoc = p.location
            val locy = p.location.add(0.0, p.height / 2, 0.0).add(pLoc.direction.multiply(pLoc.distance(e.location) / 2))
            LevsUtils.frostmourneExplosion(locy, p)
        },
        Component.text("Ледяная Скорбь").color(NamedTextColor.DARK_AQUA),
    ),
    MEA_CULPA(
        2, 1, Material.IRON_SWORD,
        { itemMeta ->
            run {
                if (!itemMeta.hasItemName()) itemMeta.itemName()
                else itemMeta.itemName(itemMeta.itemName().color(NamedTextColor.DARK_AQUA))
                (itemMeta as Damageable).damage = 0
                itemMeta.setMaxDamage(250)
            }
        },
        { p, _ ->
            val locy = p.location.add(0.0, p.height / 2, 0.0).add(p.location.direction)
            locy.world.playSound(locy, org.bukkit.Sound.BLOCK_TRIAL_SPAWNER_SPAWN_ITEM, 3f, 0.5f)
            locy.world.spawnParticle(org.bukkit.Particle.TRIAL_SPAWNER_DETECTION_OMINOUS, locy, 200,
                .1, .1, .1, .1, null, true)
            locy.world.spawnParticle(org.bukkit.Particle.SOUL_FIRE_FLAME, locy, 200,
                .1, .1, .1, .2, null, true)
            p.inventory.setItemInMainHand(ItemStack.empty())
        },
        Component.text("Mea Culpa").color(NamedTextColor.DARK_AQUA),
    ),
    MI_PENITENCIA(
        7, 2, Material.NETHERITE_SWORD,
        { itemMeta ->
            run {
                (itemMeta as Damageable).damage = 0
            }
        },
        { p, _ ->
            val locy = p.location.add(0.0, p.height / 2, 0.0).add(p.location.direction)
            locy.world.playSound(locy, org.bukkit.Sound.BLOCK_TRIAL_SPAWNER_SPAWN_ITEM, 3f, 0.5f)
            locy.world.spawnParticle(org.bukkit.Particle.TRIAL_SPAWNER_DETECTION_OMINOUS, locy, 200,
                .1, .1, .1, .1, null, true)
            locy.world.spawnParticle(org.bukkit.Particle.SOUL_FIRE_FLAME, locy, 200,
                .1, .1, .1, .2, null, true)
            p.inventory.setItemInMainHand(MEA_CULPA.replaceItem(p.inventory.itemInMainHand, p.inventory.itemInMainHand.itemMeta.getSoulBound()))
        },
        Component.text("Mi Penitencia").color(NamedTextColor.DARK_RED),
    ),

    ;


    fun giveItem(amount: Int = 1, soulBouder: String? = null): ItemStack {
        return replaceItem(ItemStack(material, amount), soulBouder)
    }

    fun replaceItem(item2: ItemStack, soulBouder: String?): ItemStack {
        val item = item2.withType(this.material)
        val itemMeta = item.itemMeta.apply {
            setCustomModelData(customModel)
            itemName(this@CustomWeapons.displayName)
            persistentDataContainer[Namespaces.CUSTOM_ITEM.namespace, PersistentDataType.STRING] =
                this@CustomWeapons.toString()
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
        whenGive(itemMeta)
        item.itemMeta = itemMeta
        return item
    }

    fun equals(itemStack: ItemStack): Boolean {
        val itemMeta = itemStack.itemMeta ?: return false
        val s =
            itemMeta.persistentDataContainer[Namespaces.CUSTOM_ITEM.namespace, PersistentDataType.STRING]
                ?: return false
        return CustomWeapons.valueOf(s) == this
    }
}