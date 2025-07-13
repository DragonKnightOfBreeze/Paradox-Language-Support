package icu.windea.pls.inject.processors

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiFile

/**
 * 用于在创建或者重新解析注入的PSI文件时，进行额外的处理。
 *
 * @see icu.windea.pls.inject.injectors.InjectionRegistrarImplCodeInjector
 */
interface InjectedFileProcessor {
    fun process(file: PsiFile): Boolean

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<InjectedFileProcessor>("icu.windea.pls.inject.injectedFileProcessor")

        fun process(file: PsiFile): Boolean {
            return EP_NAME.extensionList.any { it.process(file) }
        }
    }
}
