package uwu.levaltru.warvilore

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.inventory.*
import org.bukkit.plugin.java.JavaPlugin
import org.reflections.Reflections
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.commands.AbilitiesCommand
import uwu.levaltru.warvilore.commands.AbilkaCommand
import uwu.levaltru.warvilore.commands.AboutMeCommand
import uwu.levaltru.warvilore.trashcan.CustomItems
import java.awt.Component

const val DeveloperMode = true

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
                namespace("recipe_bow"),
                CustomItems.NETHERITE_BOW.getAsItem(),
                RecipeChoice.MaterialChoice(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE),
                RecipeChoice.MaterialChoice(Material.BOW),
                RecipeChoice.MaterialChoice(Material.NETHERITE_INGOT),
                true
            )
        )

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
    }

    companion object {
        lateinit var instance: JavaPlugin
        var abilitiesList: MutableList<String>? = null
        var softwareList: MutableList<String>? = null

        fun log(string: String) {
            instance.logger.info(string)
        }

        fun severe(string: String) {
            instance.logger.severe(string)
        }

        fun namespace(s: String): NamespacedKey = NamespacedKey(instance, s)
    }
}
