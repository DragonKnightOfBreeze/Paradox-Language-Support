package icu.windea.pls.core.util.metadata

import com.intellij.openapi.util.Key
import icu.windea.pls.core.util.KeyWithDefault
import icu.windea.pls.core.util.KeyWithProducer

/**
 * 元数据映射。
 *
 * 用于保存拥有不定数量的元数据。
 * 这些元数据通常数量很少，且可以拥有不同的数据类型。
 *
 * ## API 笔记
 *
 * 作为一种特殊的数据结构，元数据映射在构建后应是只读的。
 * 构建逻辑应发生在同一线程安全，因此不需要保证底层数据结构的线程安全。
 * 构建后的修改操作是未定义行为（Undefined behaviour）。
 *
 * @see MutableMetadataMap
 */
interface MetadataMap {
    /** 检查当前的元数据映射是否为空。 */
    fun isEmpty(): Boolean

    /** 获取 [key] 对应的元数据。如果不存在，则返回 `null`。 */
    operator fun <T> get(key: Key<T>): T?

    /** 获取 [key] 对应的元数据。如果不存在，则返回默认值。 */
    operator fun <T> get(key: KeyWithDefault<T>): T

    /** 获取 [key] 对应的元数据。如果不存在，则保存并返回计算得到的值（`null` 也会被保存）。 */
    operator fun <T> get(key: KeyWithProducer<T>): T
}
