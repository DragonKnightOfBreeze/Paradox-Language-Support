package icu.windea.pls.model.index

import icu.windea.pls.lang.index.ParadoxMergedIndex

/**
 * 索引信息的类型。
 *
 * @property key 构建合并索引时，使用的文件数据的键。
 *
 * @see ParadoxMergedIndex
 */
class ParadoxIndexInfoType<T : ParadoxIndexInfo>(
    val id: String,
    val key: Byte,
    val type: Class<T>,
) {
    override fun toString() = "ParadoxIndexInfoType(id=$id, key=$key)"
}
