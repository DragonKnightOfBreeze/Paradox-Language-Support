package icu.windea.pls.core.util.metadata

import com.intellij.openapi.util.Key

/**
 * 可变的元数据映射。
 *
 * 提供一组额外的方法，以修改底层数据结构。
 *
 * ## API 笔记
 *
 * 作为一种特殊的数据结构，元数据映射在构建后应是只读的。
 * 构建逻辑应发生在同一线程安全，因此不需要保证底层数据结构的线程安全。
 * 构建后的修改操作是未定义行为（Undefined behaviour）。
 *
 * @see MetadataMap
 */
interface MutableMetadataMap: MetadataMap {
    /** 修改 [key] 对应的元数据。如果传入的 [value] 为 `null`，则清空对应的数元据。 */
    operator fun <T> set(key: Key<T>, value: T?)

    /** 清空所有元数据。 */
    fun clearMetadata()

    /** 将当前元数据映射中的所有元数据拷贝到 [other]。[other] 中的底层数据结构会被直接替换。 */
    fun copyMetadataTo(other: MetadataMapOnKeyFMap)

    /** 将当前元数据映射中的所有元数据合并到 [other]。[other] 中的底层元数据会被按需替换，已存在时也会被覆盖。 */
    fun mergeMetadataTo(other: MetadataMapOnKeyFMap)
}
