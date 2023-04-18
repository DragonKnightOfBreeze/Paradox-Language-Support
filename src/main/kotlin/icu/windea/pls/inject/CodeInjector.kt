package icu.windea.pls.inject

import com.intellij.openapi.extensions.*

/**
 * 用于在运行时动态修改第三方代码。
 */
interface CodeInjector {
    fun inject()
    
    companion object {
        @JvmField val EP_NAME = ExtensionPointName.create<CodeInjector>("icu.windea.pls.codeInjector")
    }
}

