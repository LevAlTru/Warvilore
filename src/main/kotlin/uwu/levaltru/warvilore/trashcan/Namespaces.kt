package uwu.levaltru.warvilore.trashcan

import uwu.levaltru.warvilore.Warvilore

enum class Namespaces {
    ABILITY_SAVE_PLACE,
    SOFTWARE_SAVE_PLACE,
    SOFTWARE_SAVE_ARGS_PLACE,

    DISABLE_ABILITIES,


    INVISIBILITY_COUNTER,
    EVIL_AURA_SICKNESS,
    MANA,
    TREE_COOLDOWN,
    COOLDOWN,
    ALLOWED_NICKNAMES,
    DRANK_SOULS,
    BEFORE_NEXT_STROKE,
    BEFORE_NEXT_PRAY,
    BLOOD_SLICE_COOLDOWN,
    COLDNESS,
    CHARGE,
    CHARGES,
    REFILL,
    PARTICLES_MODE,
    ARE_PARTICLES_YELLOW,

    ARE_ABILITIES_ENABLED,


    HALO_SPEED,
    HALO_COLOR,
    HALO_TYPE,
    HALO_SHOULD_WIGGLE,


    CUSTOM_ITEM,
    SOULBOUND,
    SOUL_IN_THE_BOTTLE,
    TIMES_BEFORE_BREAK,


    WHO_HAVE_HIT,


    SHOULD_DESPAWN,


    WORLD_ZONE_BLUR,
    WORLD_ZONE_BLUR_SPEED,
    WORLD_ZONE_APPROACHING_BLUR,
    WORLD_ZONE_DISTANCE,
    WORLD_ZONE_APPROACHING_DISTANCE,
    WORLD_ZONE_SPEED,
    WORLD_ZONE_TYPE,
    WORLD_ZONE_HOW_CLOSE_PLAYER_SHOULD_BE,

    WORLD_ARE_PORTAL_ALLOWED,


    TICK_TIME_OF_DEATH,
    LAST_HIT_WITH_DEATH,

    LIVES_REMAIN,


    PUBLIC_DEAD_PEOPLE,
    PUBLIC_HIDDEN_PEOPLE,


    ;

    val namespace = Warvilore.namespace(this.toString())
}