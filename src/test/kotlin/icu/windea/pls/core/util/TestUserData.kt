package icu.windea.pls.core.util

import com.intellij.openapi.util.UserDataHolderBase

@Suppress("unused")
class TestUserData : UserDataHolderBase() {
    object Keys : KeyRegistry() {
        val name by registerKey(this) { "" }
    }

    var name by Keys.name
}
