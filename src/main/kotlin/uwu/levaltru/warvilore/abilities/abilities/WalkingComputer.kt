package uwu.levaltru.warvilore.abilities.abilities

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import net.kyori.adventure.text.Component
import org.bukkit.damage.DamageType
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.persistence.PersistentDataType
import uwu.levaltru.warvilore.SoftwareBase
import uwu.levaltru.warvilore.Warvilore
import uwu.levaltru.warvilore.abilities.AbilitiesCore
import uwu.levaltru.warvilore.trashcan.Namespaces

private const val MAX_CHARGE = 10000

class WalkingComputer(string: String) : AbilitiesCore(string) {

    var charge: Int = 0
        get() {
            field = player?.persistentDataContainer?.get(
                Namespaces.CHARGE.namespace,
                PersistentDataType.INTEGER
            ) ?: MAX_CHARGE
            return field
        }
        set(value) {
            player?.persistentDataContainer?.set(
                Namespaces.CHARGE.namespace,
                PersistentDataType.INTEGER, value
            )
            field = value
        }
    private var activeSoftware: SoftwareBase? = null

    override fun onTick(event: ServerTickEndEvent) {
        if (activeSoftware?.tick(player!!) == true) changeSoftware(null)
    }

    override fun onDamage(event: EntityDamageEvent) {
        if (event.damageSource.damageType != DamageType.LIGHTNING_BOLT) return
        charge = MAX_CHARGE
        event.damage = 0.01
    }

    override fun onJoin(event: PlayerJoinEvent) {
        loadSoftware()
    }

    fun changeSoftware(software: SoftwareBase?) {
        activeSoftware = software
        saveSoftware(software)
    }

    private fun loadSoftware() {
        val data = player!!.persistentDataContainer

        if (data.has(Namespaces.SOFTWARE_SAVE_PLACE.namespace)) {
            val s = data[Namespaces.SOFTWARE_SAVE_PLACE.namespace, PersistentDataType.STRING]
            if (s == null || s == "null") return
            try {
                changeSoftware(
                    Class.forName(Warvilore::class.java.`package`.name + ".software." + s)
                        .constructors[0].newInstance(data[Namespaces.SOFTWARE_SAVE_ARGS_PLACE.namespace, PersistentDataType.STRING])
                            as? SoftwareBase ?: return
                )
            } catch (e: ClassNotFoundException) {
            }
        }
    }

    private fun saveSoftware(software: SoftwareBase?) {
        val data = player!!.persistentDataContainer
        if (software != null) {
            data[Namespaces.SOFTWARE_SAVE_PLACE.namespace, PersistentDataType.STRING] = software::class.java.simpleName
            data[Namespaces.SOFTWARE_SAVE_ARGS_PLACE.namespace, PersistentDataType.STRING] =
                software.arguments.map { "${it.key}:${it.value}" }.joinToString(separator = " ")
        } else {
            data[Namespaces.SOFTWARE_SAVE_PLACE.namespace, PersistentDataType.STRING] = "null"
            data[Namespaces.SOFTWARE_SAVE_ARGS_PLACE.namespace, PersistentDataType.STRING] = "null"
        }
    }

    override fun getAboutMe(): List<Component> {
        TODO("Not yet implemented")
    }
}