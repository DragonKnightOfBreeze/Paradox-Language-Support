@file:Suppress("UnstableApiUsage", "unused")

package icu.windea.pls.inject.injectors

import com.intellij.psi.PsiFile
import com.intellij.psi.impl.source.tree.injected.Place
import icu.windea.pls.core.collections.process
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.InjectMethod
import icu.windea.pls.inject.annotations.InjectTarget
import icu.windea.pls.inject.processors.InjectedFileProcessor

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
