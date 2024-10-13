package uwu.levaltru.warvilore

import java.util.*

abstract class Tickable {

    var age = 0

    init {
        LIST.add(this)
    }

    companion object {
        private val LIST = LinkedList<Tickable>()
        fun Tick() {
            val list = LinkedList<Tickable>()
            list.addAll(LIST)
            LIST.clear()
            list.removeIf {
                try {
                    if (it.tick()) {
                        it.collapse()
                        return@removeIf true
                    } else false
                } catch (e: Exception) {
                    e.printStackTrace()
                    true
                }
            }
            LIST.addAll(list)
        }

        fun clearWithEffects() {
            for (tickable in LIST) tickable.collapse()
            LIST.clear()
        }
    }

    abstract fun tick(): Boolean
    open fun collapse() {}
}