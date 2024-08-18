package uwu.levaltru.warvilore.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityRegainHealthEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemConsumeEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.persistence.PersistentDataType
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.interfaces.EvilAurable
import uwu.levaltru.warvilore.trashcan.Namespaces

abstract class AbilitiesCore(val nickname: String) {

    val random = java.util.Random()
    var player: Player? = null

    init {
        hashMap[nickname] = this
    }

    companion object {
        val hashMap = HashMap<String, AbilitiesCore>()

        fun Player.getAbilities(): AbilitiesCore? {
            return hashMap[this.name]
        }

        fun String.getAbilities(): AbilitiesCore? {
            return hashMap[this]
        }

        fun Player.isEvil(): Boolean {
            return hashMap[this.name]?.isEvil() == true
        }

        fun AbilitiesCore.isEvil(): Boolean {
            return this is EvilAurable
        }


        fun loadAbility(player: Player) {
            val data = player.persistentDataContainer

            if (data.has(Namespaces.ABILITY_SAVE_PLACE.namespace)) {
                val s = data[Namespaces.ABILITY_SAVE_PLACE.namespace, PersistentDataType.STRING]
                if (s == "remove") return
                try {
                    Class.forName(Warvilore::class.java.`package`.name + ".abilities.abilities." + s)
                        .constructors[0].newInstance(player.name)
                } catch (e: ClassNotFoundException) {
                }
            }
        }

        fun saveAbility(player: Player) {
            val abilitiesCore = hashMap[player.name]
            val data = player.persistentDataContainer

            if (abilitiesCore != null) {
                data[Namespaces.ABILITY_SAVE_PLACE.namespace, PersistentDataType.STRING] =
                    abilitiesCore::class.java.simpleName

            } else data[Namespaces.ABILITY_SAVE_PLACE.namespace, PersistentDataType.STRING] = "remove"
        }

    }

    open fun onTick(event: ServerTickEndEvent) {}
    open fun onBlockBreak(event: BlockBreakEvent) {}
    open fun onBlockPlace(event: BlockPlaceEvent) {}
    open fun onDamage(event: EntityDamageEvent) {}
    open fun onHeal(event: EntityRegainHealthEvent) {}
    open fun onAction(event: PlayerInteractEvent) {}
    open fun onEating(event: PlayerItemConsumeEvent) {}
    open fun onDeath(event: PlayerDeathEvent) {}
    open fun onBowShooting(event: EntityShootBowEvent) {}
    open fun onAttack(event: PrePlayerAttackEntityEvent) {}
    open fun onJoin(event: PlayerJoinEvent) {}
    open fun onLeave(event: PlayerQuitEvent) {}

    abstract fun getAboutMe(): List<Component>

    open fun executeCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>) =
        sender.sendMessage(text("Нет кастомного функционала =(").color(NamedTextColor.RED))
    open fun completeCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): List<String>? =
        listOf("nulla")

    fun text(text: String) = Component.text(text)

}