package icu.windea.pls.inject.support

import com.intellij.openapi.extensions.*
import icu.windea.pls.inject.*

/**
 * 用于提供额外的代码注入策略的支持。
 */
abstract class CodeInjectorSupport {
    abstract fun apply(codeInjector: CodeInjector)
    
    companion object {
        val EP_NAME = ExtensionPointName.create<CodeInjectorSupport>("icu.windea.pls.inject.codeInjectorSupport")
    }
}