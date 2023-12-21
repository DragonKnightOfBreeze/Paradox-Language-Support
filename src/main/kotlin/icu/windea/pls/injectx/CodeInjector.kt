package icu.windea.pls.injectx

import com.intellij.openapi.extensions.*

/**
 * 代码注入器。
 */
interface CodeInjector {
    companion object {
        val EP_NAME = ExtensionPointName.create<CodeInjector>("icu.windea.pls.injectx.codeInjector")
    }
}