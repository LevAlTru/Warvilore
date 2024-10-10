package uwu.levaltru.warvilore.trashcan

import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.inventory.ItemRarity
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.trashcan.LevsUtils.getSoulInTheBottle
import uwu.levaltru.warvilore.trashcan.LevsUtils.setSoulInTheBottle

enum class CustomItems(
    val material: Material,
    val model: Int,
    val itemMeta: ((ItemMeta) -> Unit)?,
    val itemName: String? = null,
    val itemRarity: ItemRarity
) {

    //bowl
    TANKARD(Material.BOWL, 1, { it.setMaxStackSize(16) }, "Пивная Кружка", ItemRarity.COMMON),


    // Honey Bottle
    SOUL_BOTTLE(Material.HONEY_BOTTLE, 1, { LevsUtils.ItemMetas.soulBottle(it) }, null, ItemRarity.UNCOMMON),
    OMINOUS_SOUL_BOTTLE(Material.HONEY_BOTTLE, 2, { LevsUtils.ItemMetas.soulBottle(it) }, null, ItemRarity.UNCOMMON),


    // poisonous potato
    DRANK_SOUL_BEER(Material.POISONOUS_POTATO, 3, { LevsUtils.ItemMetas.soulBeer(it, TANKARD.getAsItem()) }, "Почти Выпитое Пиво Из Душы", ItemRarity.UNCOMMON),
    LIT_SOUL_BEER(
        Material.POISONOUS_POTATO,
        4,
        {
            LevsUtils.ItemMetas.soulBeer(
                it,
                DRANK_SOUL_BEER.getAsItem(ItemStack(Material.AMETHYST_SHARD).itemMeta.also { it2 ->
                    it2.setSoulInTheBottle(it.getSoulInTheBottle())
                    it2.setFood(it2.food.apply { effects = it.food.effects })
                })
            )
        },
        "Подвыпитое Пиво Из Душы",
        ItemRarity.UNCOMMON
    ),
    SOUL_BEER(
        Material.POISONOUS_POTATO, 5,
        {
            LevsUtils.ItemMetas.soulBeer(
                it,
                LIT_SOUL_BEER.getAsItem(ItemStack(Material.AMETHYST_SHARD).itemMeta.also { it2 ->
                    it2.setSoulInTheBottle(it.getSoulInTheBottle())
                    it2.setFood(it2.food.apply { effects = it.food.effects })
                })
            )
        }, "Пиво Из Душы", ItemRarity.UNCOMMON
    ),

    DRANK_OMINOUS_SOUL_BEER(
        Material.POISONOUS_POTATO,
        6,
        { LevsUtils.ItemMetas.soulBeer(it, TANKARD.getAsItem()) },
        "Почти Выпитое Холодное Пиво Из Душы",
        ItemRarity.UNCOMMON
    ),
    LIT_OMINOUS_SOUL_BEER(
        Material.POISONOUS_POTATO,
        7,
        {
            LevsUtils.ItemMetas.soulBeer(
                it,
                DRANK_OMINOUS_SOUL_BEER.getAsItem(ItemStack(Material.AMETHYST_SHARD).itemMeta.also { it2 ->
                    it2.setSoulInTheBottle(it.getSoulInTheBottle())
                    it2.setFood(it2.food.apply { effects = it.food.effects })
                })
            )
        },
        "Подвыпитое Холодное Пиво Из Душы",
        ItemRarity.UNCOMMON
    ),
    OMINOUS_SOUL_BEER(
        Material.POISONOUS_POTATO,
        8,
        {
            LevsUtils.ItemMetas.soulBeer(
                it,
                LIT_OMINOUS_SOUL_BEER.getAsItem(ItemStack(Material.AMETHYST_SHARD).itemMeta.also { it2 ->
                    it2.setSoulInTheBottle(it.getSoulInTheBottle())
                    it2.setFood(it2.food.apply { effects = it.food.effects })
                })
            )
        },
        "Холодное Пиво Из Душ",
        ItemRarity.UNCOMMON
    ),


    // Bow
    NETHERITE_BOW(Material.BOW, 1, {
        {
            it.isFireResistant = true
            (it as Damageable).setMaxDamage(1324)
        }
    }, "Незеритовый Лук", ItemRarity.UNCOMMON),


    // Repeating Command Block
    SHARD_OF_MORTUUS(
        Material.REPEATING_COMMAND_BLOCK,
        1,
        { LevsUtils.ItemMetas.MortuusAndVictus(it) },
        "Осколок Mortuus'a",
        ItemRarity.RARE
    ),
    FRAGMENT_OF_VICTUS(
        Material.REPEATING_COMMAND_BLOCK,
        2,
        { LevsUtils.ItemMetas.MortuusAndVictus(it) },
        "Фрагмент Victus'a",
        ItemRarity.RARE
    ),

    YOUR_REALITY_HAS_COLLAPSED(Material.REPEATING_COMMAND_BLOCK, 3, {
        it.isFireResistant = true
        it.setEnchantmentGlintOverride(true)

        it.addAttributeModifier(
            org.bukkit.attribute.Attribute.GENERIC_ATTACK_SPEED, org.bukkit.attribute.AttributeModifier(
                Warvilore.namespace("your_reality_has_collapsed"),
                -.85,
                org.bukkit.attribute.AttributeModifier.Operation.MULTIPLY_SCALAR_1,
                org.bukkit.inventory.EquipmentSlotGroup.MAINHAND
            )
        )

        it.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ATTRIBUTES)
    }, "Переплетение Судеб", ItemRarity.EPIC),

    THE_RED_RIBBONS(Material.REPEATING_COMMAND_BLOCK, 4, null, "Лента Покаяния", ItemRarity.COMMON),

    ;

    fun getAsItem(preItemMetaFun: ItemMeta = ItemStack(Material.AMETHYST_SHARD).itemMeta): ItemStack {
        val itemStack = ItemStack(this.material)
        val itemMeta = this.itemMeta
        preItemMetaFun.persistentDataContainer[Namespaces.CUSTOM_ITEM.namespace, PersistentDataType.STRING] =
            this.toString()
        preItemMetaFun.setCustomModelData(this.model)
        preItemMetaFun.setRarity(itemRarity)
        itemName?.let { preItemMetaFun.itemName(Component.text(it)) }
        itemMeta?.invoke(preItemMetaFun)
        itemStack.itemMeta = preItemMetaFun

        return itemStack
    }
}