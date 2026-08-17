package icu.windea.pls.lang.index

import icu.windea.pls.model.index.ParadoxIndexInfo

/**
 * 合并索引类型。
 *
 * @property id 用作展示的 ID。需要是唯一的。
 * @property key 构建合并索引时，不需要是唯一的。
 * @property type 对应的索引信息的类型。不需要是唯一的。
 *
 * @see ParadoxMergedIndexTypes
 * @see ParadoxMergedIndex
 */
class ParadoxMergedIndexType<T : ParadoxIndexInfo> private constructor(
    val id: String,
    val key: String,
    val type: Class<T>,
) {
    override fun toString() = "ParadoxMergedIndexType(id=$id, key=$key, type=$type)"

    class Builder<T : ParadoxIndexInfo>(
        val id: String,
        val key: String,
        val type: Class<T>,
    ) {
        fun build(): ParadoxMergedIndexType<T> = ParadoxMergedIndexType(id, key, type).also { _entries[id] = it }
    }

    companion object {
        private val _entries = mutableMapOf<String, ParadoxMergedIndexType<*>>()

        @JvmStatic
        val entries: Map<String, ParadoxMergedIndexType<*>> get() = _entries

        @JvmStatic
        fun <T : ParadoxIndexInfo> builder(id: String, key: String, type: Class<T>): Builder<T> = Builder(id, key, type)

        inline fun <reified T : ParadoxIndexInfo> builder(id: String, key: String): Builder<T> = builder(id, key, T::class.java)
    }
}
