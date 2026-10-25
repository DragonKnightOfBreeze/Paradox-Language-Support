package icu.windea.pls.core.util.metadata

import com.intellij.util.keyFMap.KeyFMap

/**
 * 使用 [KeyFMap] 作为底层数据结构的元数据映射。
 */
open class MetadataMapBase : MetadataMapOnKeyFMap {
    // volatile is enough here since strict thread safe is not required
    @Volatile override var map: KeyFMap = KeyFMap.EMPTY_MAP
}
