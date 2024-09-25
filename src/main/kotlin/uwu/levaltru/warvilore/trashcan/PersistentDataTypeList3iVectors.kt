package uwu.levaltru.warvilore.trashcan

import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType
import org.joml.Vector3i

class PersistentDataTypeList3iVectors : PersistentDataType<List<Int>, List<Vector3i>> {

    override fun getPrimitiveType(): Class<List<Int>> {
        return listOf<Int>().javaClass
    }

    override fun getComplexType(): Class<List<Vector3i>> = listOf<Vector3i>().javaClass

    override fun fromPrimitive(primitive: List<Int>, context: PersistentDataAdapterContext): List<Vector3i> {
        val list = mutableListOf<Vector3i>()
        for (x in 0..<primitive.size / 3) list += Vector3i(primitive[x + 0], primitive[x + 1], primitive[x + 2])
        return list
    }

    override fun toPrimitive(complex: List<Vector3i>, context: PersistentDataAdapterContext): List<Int> {
        val list = mutableListOf<Int>()
        for (vector3i in complex) {
            list += vector3i.x
            list += vector3i.y
            list += vector3i.z
        }
        return list
    }
}
