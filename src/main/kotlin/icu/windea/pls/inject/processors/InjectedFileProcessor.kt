package icu.windea.pls.inject.processors

import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.psi.PsiFile

/**
 * 用于在创建或者重新解析注入的 PSI 文件时，进行额外的处理。
 *
 * @see icu.windea.pls.inject.injectors.InjectionRegistrarImplCodeInjector
 */
interface InjectedFileProcessor {
    /** 处理注入的 PSI 文件，返回是否需要继续处理。 */
    fun process(file: PsiFile): Boolean

    companion object INSTANCE {
        val EP_NAME = ExtensionPointName<InjectedFileProcessor>("icu.windea.pls.inject.injectedFileProcessor")
    }
}
