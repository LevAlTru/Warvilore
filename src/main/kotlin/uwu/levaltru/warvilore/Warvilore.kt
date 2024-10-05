package uwu.levaltru.warvilore

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketAdapter
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.events.PacketEvent
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.PlayerInfoData
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.RecipeChoice
import org.bukkit.inventory.ShapedRecipe
import org.bukkit.inventory.ShapelessRecipe
import org.bukkit.inventory.SmithingTransformRecipe
import org.bukkit.plugin.java.JavaPlugin
import org.reflections.Reflections
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.commands.AbilitiesCommand
import uwu.levaltru.warvilore.commands.AbilkaCommand
import uwu.levaltru.warvilore.commands.AboutMeCommand
import uwu.levaltru.warvilore.tickables.untraditional.NetherEmitter
import uwu.levaltru.warvilore.tickables.untraditional.RemainsOfTheDeads
import uwu.levaltru.warvilore.trashcan.CustomItems
import uwu.levaltru.warvilore.trashcan.CustomWeapons
import uwu.levaltru.warvilore.trashcan.LevsUtils

const val DeveloperMode = false
val protocolManager: ProtocolManager? = ProtocolLibrary.getProtocolManager()

class Warvilore : JavaPlugin() {

    override fun onEnable() {
        instance = this

        log("Hiiii!!! :3")

        if (DeveloperMode)
            for (i in 1..30)
                severe("DEVELOPER MODE IS TURNED ON!!!!!!")

        log("Events registration...")
        Bukkit.getPluginManager().registerEvents(CustomEvents(), this)

        log("Creating list of abilities for command usage...")
        abilitiesList = emptyList<String>().toMutableList()
        for (clazz in Reflections(Warvilore::class.java.`package`.name + ".abilities.abilities").getSubTypesOf(
            AbilitiesCore::class.java
        )
            .filter { it.packageName.contains("abilities.abilities") }) {
            log(clazz.name)
            abilitiesList!!.add(clazz.simpleName)
        }

        log("Creating list of software for walking computers usage...")
        softwareList = emptyList<String>().toMutableList()
        for (clazz in Reflections(Warvilore::class.java.`package`.name + ".software").getSubTypesOf(SoftwareBase::class.java)) {
            log(clazz.name)
            softwareList!!.add(clazz.simpleName)
        }

        log("Commands registration...")
        registerCommands()

        log("Recipe registration...")

        Bukkit.addRecipe(
            SmithingTransformRecipe(
                namespace("netherite_bow_recipe"),
                CustomItems.NETHERITE_BOW.getAsItem(),
                RecipeChoice.MaterialChoice(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                RecipeChoice.MaterialChoice(Material.BOW),
                RecipeChoice.MaterialChoice(Material.NETHERITE_INGOT),
                true
            )
        )
        Bukkit.addRecipe(
            // checking for important stuff in "CustomEvents" in "onSmithingTableCraft"
            SmithingTransformRecipe(
                namespace("mi_penitencia_recipe"),
                CustomWeapons.MI_PENITENCIA.giveItem(),
                RecipeChoice.MaterialChoice(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                RecipeChoice.MaterialChoice(CustomWeapons.MEA_CULPA.material),
                RecipeChoice.MaterialChoice(CustomItems.THE_RED_RIBBONS.material),
                true
            )
        )
        Bukkit.addRecipe(
            ShapedRecipe(namespace("the_red_ribbons_recipe"), CustomItems.THE_RED_RIBBONS.getAsItem()).shape(
                " NR",
                "NSN",
                "RN ",
            )
                .setIngredient('N', Material.NETHERITE_SCRAP)
                .setIngredient('R', Material.RED_WOOL)
                .setIngredient('S', Material.STRING)
        )
        Bukkit.addRecipe(
            ShapelessRecipe(
                namespace("tankard_recipe"),
                CustomItems.TANKARD.getAsItem().add(3)
            ).addIngredient(Material.BARREL)
        )

        log("Dead Remains loading...")
        RemainsOfTheDeads.load()
        NetherEmitter.load()

        log("Adding Protocol Lib events")
        if (protocolManager != null) {

            protocolManager.addPacketListener(object : PacketAdapter(instance, PacketType.Play.Server.PLAYER_INFO) {
                override fun onPacketSending(event: PacketEvent?) {
                    if (event == null) return

                    val pk = event.packet
//                    log(pk.type.toString() + "  1")
//                    log(pk.strings.values.toString() + "  2")
//                    log(pk.integers.values.toString() + "  3")
//                    log(pk.shorts.values.toString() + "  4")
//                    log(pk.bytes.values.toString() + "  5")
//                    log(pk.playerInfoActions.values.toString() + "  6")
//                    log(pk.playerInfoAction.values.toString() + "  7")
//                    log(pk.playerActions.values.toString() + "  8")
                    val playerInfoDataLists = pk.playerInfoDataLists
//                    log(playerInfoDataLists.values.toString() + "  9")

                    val nickname = playerInfoDataLists?.read(1)?.get(0)?.profile?.name
                    if (
                        pk.playerInfoActions.read(0).contains(EnumWrappers.PlayerInfoAction.ADD_PLAYER)/* ||
                        pk.playerInfoActions.read(0).contains(EnumWrappers.PlayerInfoAction.UPDATE_LATENCY) ||
                        pk.playerInfoActions.read(0).contains(EnumWrappers.PlayerInfoAction.UPDATE_DISPLAY_NAME) ||
                        pk.playerInfoActions.read(0).contains(EnumWrappers.PlayerInfoAction.UPDATE_GAME_MODE)*/
                    ) {
                        if (LevsUtils.Hiddens.isHidden(nickname)) {
                            protocolHashmap[nickname!!] = pk
                        }
                        return
                    }
//                    log("1111111111111111111111")
//                    log(playerInfoDataLists?.values.toString())
//                    log(playerInfoDataLists?.read(1).toString())
//                    log(playerInfoDataLists?.read(1)?.get(0).toString())
//                    log(playerInfoDataLists?.read(1)?.get(0)?.profile.toString())
//                    log(nickname.toString())
                    if (!LevsUtils.Hiddens.isHidden(nickname)) return
//                    log("2222222222222222222222")

                    protocolManager.sendServerPacket(event.player, protocolHashmap[nickname!!])
//                    Bukkit.getScheduler().runTaskLater(instance, Runnable {
//                        log("33333333333333333333333333")
//                        protocolManager.sendServerPacket(event.player, deepClone)
//                    }, 50)
                }

                override fun onPacketReceiving(event: PacketEvent?) {}
            })
        } else severe("Protocol Lib not found")


        log("All done!")

    }

    private fun registerCommands() {
        val abilitiesCommand = getCommand("warvibilities")
        val abilitiesCommandExecutor = AbilitiesCommand()
        abilitiesCommand!!.setExecutor(abilitiesCommandExecutor)
        abilitiesCommand.tabCompleter = abilitiesCommandExecutor

        getCommand("aboutme")!!.setExecutor(AboutMeCommand())

        val abilkaCommand = getCommand("abilka")
        val abilkaCommandExecutor = AbilkaCommand()
        abilkaCommand!!.setExecutor(abilkaCommandExecutor)
        abilkaCommand.tabCompleter = abilkaCommandExecutor
    }

    override fun onDisable() {
        Tickable.clearWithEffects()
        RemainsOfTheDeads.save()
        NetherEmitter.save()
    }

    companion object {
        lateinit var instance: JavaPlugin
        var abilitiesList: MutableList<String>? = null
        var softwareList: MutableList<String>? = null
        val protocolHashmap: HashMap<String, PacketContainer?> = hashMapOf()

        fun log(string: String) {
            instance.logger.info(string)
        }

        fun severe(string: String) {
            instance.logger.severe(string)
        }

        fun namespace(s: String): NamespacedKey = NamespacedKey(instance, s)
    }
}
