@file:Suppress("UnstableApiUsage", "unused")

package icu.windea.pls.inject.injectors

import com.intellij.psi.*
import com.intellij.psi.impl.source.tree.injected.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*
import icu.windea.pls.inject.processors.*

/**
 * @see com.intellij.psi.impl.source.tree.injected.InjectionRegistrarImpl
 * @see com.intellij.psi.impl.source.tree.injected.InjectionRegistrarImpl.cacheEverything
 * @see icu.windea.pls.inject.processors.InjectedFileProcessor
 */
@InjectTarget("com.intellij.psi.impl.source.tree.injected.InjectionRegistrarImpl")
class InjectionRegistrarImplCodeInjector : CodeInjectorBase() {
    //用于在创建或者重新解析注入的PSI文件时，进行额外的处理

    @InjectMethod(pointer = InjectMethod.Pointer.AFTER, static = true)
    fun cacheEverything(place: Place, documentWindow: Any, viewProvider: Any, psiFile: PsiFile, returnValue: Boolean): Boolean {
        runCatchingCancelable {
            InjectedFileProcessor.EP_NAME.extensionList.process { it.process(psiFile) }
        }
        return returnValue
    }
}
