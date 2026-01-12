package icu.windea.pls.lang.psi.select

import com.intellij.openapi.util.UserDataHolderBase
import icu.windea.pls.core.util.KeyRegistry

class ParadoxPsiSelectScope : UserDataHolderBase() {
    object Keys : KeyRegistry()

    operator fun plus(other: ParadoxPsiSelectScope) {
        Keys.copy(other, this, ifPresent = false)
    }
}
