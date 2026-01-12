package icu.windea.pls.config.select

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.util.KeyRegistry

class CwtConfigSelectScope : UserDataHolderBase() {
    object Keys : KeyRegistry()

    operator fun plus(other: CwtConfigSelectScope) {
        Keys.copy(other, this, ifPresent = false)
    }
}
