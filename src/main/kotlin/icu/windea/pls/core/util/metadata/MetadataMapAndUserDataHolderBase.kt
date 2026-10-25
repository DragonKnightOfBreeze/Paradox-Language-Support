package icu.windea.pls.core.util.metadata

import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.util.keyFMap.KeyFMap

open class MetadataMapAndUserDataHolderBase : UserDataHolderBase(), MetadataMapOnKeyFMap {
    // volatile is enough here since strict thread safe is not required
    @Volatile override var map: KeyFMap = KeyFMap.EMPTY_MAP
}
