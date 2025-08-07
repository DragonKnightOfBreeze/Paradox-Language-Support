package icu.windea.pls.inject

import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*

/**
 * 代码注入器。
 */
interface CodeInjector : UserDataHolder {
    val id: String

    fun inject()

    companion object {
        val EP_NAME = ExtensionPointName<CodeInjector>("icu.windea.pls.inject.codeInjector")
    }
}

