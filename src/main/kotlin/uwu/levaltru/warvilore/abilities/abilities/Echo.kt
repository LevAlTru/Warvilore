package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import uwu.levaltru.warvilore.DeveloperMode
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.tickables.effect.ReturnTommy
import uwu.levaltru.warvilore.tickables.untraditional.RemainsOfTheDeads

class Echo(nickname: String) : AbilitiesCore(nickname) {

    var frames = 0
    var tommy: String? = null
    var loce: Location? = null

    override fun onTick(event: ServerTickEndEvent) {
        val player = player ?: return

        if (player.gameMode != GameMode.SPECTATOR && player.ticksLived % 5 == 0) {
            val box = player.boundingBox
            val x = random.nextDouble(box.minX, box.maxX)
            val y = random.nextDouble(box.minY, box.maxY)
            val z = random.nextDouble(box.minZ, box.maxZ)

            player.world.spawnParticle(Particle.TRIAL_SPAWNER_DETECTION, x, y, z, 1, .0, .0, .0, .02, null, true)
        }

        if (tommy == null) {
            frames = 0
            return
        }

        if (frames == 0) player.world.playSound(player.location, "levaltru:the_sun_station", 16f, 1f)

        player.sendActionBar(Component.text(frames / 20 - 155).color(NamedTextColor.LIGHT_PURPLE))

        if (frames == 155 * 20) {
            ReturnTommy(loce ?: player.location.add(0.0, 5.0, 0.0), player.name, tommy!!)
            tommy = null
        }

        frames++
    }


    override fun completeCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): List<String>? {
        return when (args.size) {
            1 -> listOf("create", "remove", "start")
            2 -> if (args[0].lowercase() == "create") listOf("size") else if (args[0].lowercase() == "start") null else listOf()
            3 -> if (args[0].lowercase() == "create") listOf("intensity") else listOf()
            else -> listOf()
        }
    }

    override fun executeCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>) {
        try {
            if (args[0].lowercase() == "create") {
                RemainsOfTheDeads(
                    player!!.location.blockX,
                    player!!.location.blockZ,
                    player!!.world,
                    args[1].toInt(),
                    args[2].toFloat()
                )
            } else if (args[0].lowercase() == "remove") {
                val minBy =
                    RemainsOfTheDeads.LIST.filter { it.world.uid == player!!.world.uid }
                        .minByOrNull {
                            val x = it.x - player!!.x
                            val z = it.z - player!!.z
                            x * x + z * z
                        }
                if (minBy == null) {
                    player!!.sendMessage(text("couldn't remove player's remainings").color(NamedTextColor.RED))
                    return
                }
                minBy.markedForRemoval = true
            } else if (args[0].lowercase() == "start") {
                if (args.size <= 1) {
                    tommy = null
                    return
                }
                tommy = Bukkit.getPlayer(args[1])?.name
                loce = player!!.location.add(0.0, 5.0, 0.0)
            }
        } catch (e: Exception) {
            player!!.sendMessage(Component.text(e.toString()).color(NamedTextColor.RED))
            Warvilore.severe(e.toString())
        }
    }

    override fun getAboutMe(): List<Component> = listOf(text("echo").color(NamedTextColor.DARK_PURPLE))
}