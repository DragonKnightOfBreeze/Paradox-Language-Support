package icu.windea.pls.inject

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.util.UserDataHolder
import icu.windea.pls.inject.annotations.InjectionTarget

/**
 * 代码注入器。
 *
 * @see CodeInjectorBase
 * @see CodeInjectorSupport
 * @see InjectionTarget
 */
interface CodeInjector : UserDataHolder {
    val id: String

    fun inject()

    companion object {
        val EP_NAME = ExtensionPointName<CodeInjector>("icu.windea.pls.inject.codeInjector")
    }
}
