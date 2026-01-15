package icu.windea.pls.core.util

import com.intellij.openapi.util.UserDataHolder
import com.intellij.openapi.util.UserDataHolderBase

@Suppress("unused")
class TestUserData : UserDataHolderBase() {
    object Keys : KeyRegistry() {
        val name by registerKey<String, UserDataHolder>(this) { "" }
    }

    var name by Keys.name
}
