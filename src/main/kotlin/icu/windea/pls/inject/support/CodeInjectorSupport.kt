package icu.windea.pls.inject.support

import com.intellij.openapi.extensions.*
import icu.windea.pls.inject.*

abstract class CodeInjectorSupport {
    abstract fun apply(codeInjector: CodeInjector)
    
    companion object {
        val EP_NAME = ExtensionPointName.create<CodeInjectorSupport>("icu.windea.pls.codeInjectorSupport")
    }
}