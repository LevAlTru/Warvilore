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
            list.removeIf { try { it.tick() } catch (e: Exception) {
                e.printStackTrace()
                true }
            }
            LIST.addAll(list)
        }
    }

    abstract fun tick(): Boolean
}