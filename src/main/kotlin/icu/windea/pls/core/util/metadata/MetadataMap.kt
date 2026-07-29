package icu.windea.pls.core.util.metadata

import com.intellij.openapi.util.Key
import icu.windea.pls.core.util.KeyWithDefault
import icu.windea.pls.core.util.KeyWithProducer

/**
 * 元数据映射。
 *
 * 用于保存拥有不定数量的元数据，通常数量很小。
 * 元数据可以拥有不同的数据类型。
 *
 * 作为一种特殊的数据结构，通常仅在构建时可变（构建逻辑应发生在同一线程中），构建后即是只读的（因此不保证底层数据结构的线程安全）。
 */
interface MetadataMap {
    fun isEmpty(): Boolean

    operator fun <T> get(key: Key<T>): T?

    operator fun <T> get(key: KeyWithDefault<T>): T

    operator fun <T> get(key: KeyWithProducer<T>): T

    // move to MetadataMapBase
    // operator fun <T> set(key: Key<T>, value: T?)
}
