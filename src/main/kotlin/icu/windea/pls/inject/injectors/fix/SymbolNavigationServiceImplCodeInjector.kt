@file:Suppress("UnstableApiUsage", "UNCHECKED_CAST", "unused")

package icu.windea.pls.inject.injectors.fix

import com.intellij.model.Symbol
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.navigation.NavigationTarget
import com.intellij.psi.PsiElement
import icu.windea.pls.core.psi.light.LightElementBase
import icu.windea.pls.inject.CodeInjectorBase
import icu.windea.pls.inject.annotations.InjectMethod
import icu.windea.pls.inject.annotations.InjectReturnValue
import icu.windea.pls.inject.annotations.InjectionTarget
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.isAccessible

/**
 * @see icu.windea.pls.core.psi.light.LightElementBase
 */
@InjectionTarget("com.intellij.codeInsight.navigation.impl.SymbolNavigationServiceImpl")
class SymbolNavigationServiceImplCodeInjector : CodeInjectorBase() {
    // 用于修复从 IDEA 2023.2 开始，按住 Ctrl 并点击复杂枚举值、动态值、参数等目标后，无法直接查找用法的问题

    // see: com.intellij.codeInsight.navigation.impl.SymbolNavigationServiceImpl
    // see: com.intellij.codeInsight.navigation.impl.SymbolNavigationServiceImpl.getNavigationTargets

    @InjectMethod(pointer = InjectMethod.Pointer.AFTER)
    fun getNavigationTargets(project: Project, symbol: Symbol, @InjectReturnValue returnValue: Collection<NavigationTarget>): Collection<NavigationTarget> {
        if (returnValue.isEmpty()) return returnValue
        return returnValue.filter { getElement(it) !is LightElementBase }
    }

    private fun getElement(navigationTarget: NavigationTarget): PsiElement? {
        val kClass = navigationTarget::class
        if (kClass.qualifiedName != "com.intellij.codeInsight.navigation.impl.PsiElementNavigationTarget") return null
        val prop = kClass.memberProperties.find { it.name == "myElement" } as? KProperty1<NavigationTarget, PsiElement> ?: return null
        prop.isAccessible = true
        return prop.get(navigationTarget)
    }
}
