package uwu.levaltru.warvilore.software

import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.Levelled
import org.bukkit.entity.Player
import uwu.levaltru.warvilore.SoftwareBase
import uwu.levaltru.warvilore.Warvilore

class GimmeLight(string: String) : SoftwareBase(string) {

    private var prevLoc: Location? = null

    override fun tick(player: Player): Boolean {
        if (prevLoc != null) {
            val block = prevLoc!!.block
            if (block.type == Material.LIGHT) block.type = Material.AIR
        }

        val level = if (arguments["level"] != null) arguments["level"]?.toIntOrNull() else 15
        if (level == null || level > 15 || level < 1) {
            player.sendMessage(
                text("Invalid level of ${arguments["level"]} (should be between 1 and 15)").color(
                    NamedTextColor.RED
                )
            )
            return true
        }
        val eyeLocation = player.eyeLocation
        val type = eyeLocation.block.type
        if (type == Material.LIGHT) {
            player.sendMessage(text("ERROR: LIGHT COLLISION").color(NamedTextColor.RED))
            return true
        }
        if (type.isEmpty && !player.isDead) {
            val blockData = Material.LIGHT.createBlockData { (it as Levelled).level = level }
            player.world.setType(eyeLocation, Material.LIGHT)
            player.world.setBlockData(eyeLocation, blockData)
        }
        prevLoc = eyeLocation.clone()
        return false
    }

    override fun onShutDown(player: Player) {
        val block = player.eyeLocation.block
        if (block.type == Material.LIGHT) block.type = Material.AIR
        val block2 = prevLoc?.block
        if (block2?.type == Material.LIGHT) block.type = Material.AIR
    }

    override fun onDeath(player: Player) = onShutDown(player)

    override fun possibleArguments(): List<String> =  listOf(
        "level:1", "level:2", "level:3", "level:4", "level:5",
        "level:6", "level:7", "level:8", "level:9", "level:10",
        "level:11", "level:12", "level:13", "level:14", "level:15",
    )

    override fun description(): List<TextComponent> = listOf(
        text("A cozy light for cave traveling.").color(NamedTextColor.LIGHT_PURPLE),
        text(""),
        text("level - light level.").color(NamedTextColor.YELLOW),
    )

}