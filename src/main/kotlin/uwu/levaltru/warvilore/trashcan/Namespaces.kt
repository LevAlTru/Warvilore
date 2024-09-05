package uwu.levaltru.warvilore.trashcan

import org.bukkit.NamespacedKey
import uwu.levaltru.warvilore.Warvilore

enum class Namespaces(val namespace: NamespacedKey) {
    ABILITY_SAVE_PLACE(Warvilore.namespace("ability")),
    SOFTWARE_SAVE_PLACE(Warvilore.namespace("activeSoftware")),
    SOFTWARE_SAVE_ARGS_PLACE(Warvilore.namespace("activeSoftwareArgs")),


    INVISIBILITY_COUNTER(Warvilore.namespace("invisibilityCounter")),
    EVIL_AURA_SICKNESS(Warvilore.namespace("evilAuraSickness")),
    MANA(Warvilore.namespace("mana")),
    TREE_COOLDOWN(Warvilore.namespace("treeCooldown")),
    BEFORE_NEXT_STROKE(Warvilore.namespace("timeBeforeNextStroke")),
    BEFORE_NEXT_PRAY(Warvilore.namespace("timeBeforeNextPray")),
    CAN_BLOOD_SLICE(Warvilore.namespace("canBloodSlice")),
    COLDNESS(Warvilore.namespace("coldness")),
    CHARGE(Warvilore.namespace("charge")),

    HALO_SPEED(Warvilore.namespace("haloSpeed")),
    HALO_COLOR(Warvilore.namespace("haloColor")),
    HALO_TYPE(Warvilore.namespace("haloType")),
    HALO_SHOULD_WIGGLE(Warvilore.namespace("haloShouldWiggle")),


    CUSTOM_ITEM(Warvilore.namespace("customItem")),
    SOULBOUND(Warvilore.namespace("souldbound")),
    TIMES_BEFORE_BREAK(Warvilore.namespace("timesBeforeBreak")),


    WHO_HAVE_HIT(Warvilore.namespace("whoHaveHit")),


    SHOULD_DESPAWN(Warvilore.namespace("shouldDespawn")),
}