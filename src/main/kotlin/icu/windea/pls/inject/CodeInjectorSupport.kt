package icu.windea.pls.inject

import com.intellij.openapi.extensions.ExtensionPointName

/**
 * 用于支持额外的代码注入器策略。
 * @see CodeInjector
 * @see CodeInjectorBase
 */
interface CodeInjectorSupport {
    fun apply(codeInjector: CodeInjector)

    companion object {
        val EP_NAME = ExtensionPointName<CodeInjectorSupport>("icu.windea.pls.inject.codeInjectorSupport")
    }
}
