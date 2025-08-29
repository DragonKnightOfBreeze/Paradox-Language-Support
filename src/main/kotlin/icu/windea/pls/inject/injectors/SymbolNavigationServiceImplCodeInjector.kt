@file:Suppress("UnstableApiUsage", "UNCHECKED_CAST", "unused")

package icu.windea.pls.inject.injectors

import com.intellij.model.Symbol
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.psi.PsiElement
import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.InjectMethod
import icu.windea.pls.inject.annotations.InjectTarget
import icu.windea.pls.lang.psi.mock.MockPsiElement
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * @see com.intellij.codeInsight.navigation.impl.SymbolNavigationServiceImpl
 * @see com.intellij.codeInsight.navigation.impl.SymbolNavigationServiceImpl.getNavigationTargets
 * @see icu.windea.pls.lang.psi.mock.MockPsiElement
 */
@InjectTarget("com.intellij.codeInsight.navigation.impl.SymbolNavigationServiceImpl")
class SymbolNavigationServiceImplCodeInjector : CodeInjectorBase() {
    //用于修复从IDEA 2023.2开始，按住Ctrl并点击参数（以及其他类似目标）后，无法查找其使用的问题

    @InjectMethod(pointer = InjectMethod.Pointer.AFTER)
    fun getNavigationTargets(project: Project, symbol: Symbol, returnValue: Collection<NavigationTarget>): Collection<NavigationTarget> {
        if (returnValue.isEmpty()) return returnValue
        return returnValue.filter { getElement(it) !is MockPsiElement }
    }

    private fun getElement(navigationTarget: NavigationTarget): PsiElement? {
        val kClass = navigationTarget::class
        if (kClass.qualifiedName != "com.intellij.codeInsight.navigation.impl.PsiElementNavigationTarget") return null
        val prop = kClass.memberProperties.find { it.name == "myElement" } as? KProperty1<NavigationTarget, PsiElement> ?: return null
        prop.isAccessible = true
        return prop.get(navigationTarget)
    }
}

