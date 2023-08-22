package icu.windea.pls.inject

import com.intellij.openapi.extensions.*
import com.intellij.openapi.util.*

/**
 * 代码注入器。
 */
abstract class CodeInjector: UserDataHolderBase() {
    open val id: String = javaClass.name
    
    abstract fun inject()
    
    override fun <T : Any?> getUserData(key: Key<T>): T? {
        return super.getUserData(key) ?: getUserData(CodeInjectorService.codeInjectorServiceKey)?.getUserData(key)
    }
    
    companion object {
        val EP_NAME = ExtensionPointName.create<CodeInjector>("icu.windea.pls.inject.codeInjector")
    }
}

