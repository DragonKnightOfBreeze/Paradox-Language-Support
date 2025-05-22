@file:Suppress("UnstableApiUsage", "UNUSED_PARAMETER", "UNCHECKED_CAST")

package icu.windea.pls.inject.injectors

import com.intellij.model.*
import com.intellij.openapi.project.*
import com.intellij.platform.backend.navigation.*
import com.intellij.psi.*
import icu.windea.pls.inject.*
import icu.windea.pls.inject.annotations.*
import icu.windea.pls.lang.psi.*
import kotlin.reflect.*
import kotlin.reflect.full.*
import kotlin.reflect.jvm.*

/**
 * @see com.intellij.codeInsight.navigation.impl.SymbolNavigationServiceImpl
 * @see com.intellij.codeInsight.navigation.impl.SymbolNavigationServiceImpl.getNavigationTargets
 * @see icu.windea.pls.lang.psi.ParadoxFakePsiElement
 */
@InjectTarget("com.intellij.codeInsight.navigation.impl.SymbolNavigationServiceImpl")
class SymbolNavigationServiceImplCodeInjector : CodeInjectorBase() {
    //用于修复从IDEA 2023.2开始，按住Ctrl并点击参数（以及其他类似目标）后，无法查找其使用的问题

    @InjectMethod(pointer = InjectMethod.Pointer.AFTER)
    fun getNavigationTargets(project: Project, symbol: Symbol, returnValue: Collection<NavigationTarget>): Collection<NavigationTarget> {
        if (returnValue.isEmpty()) return returnValue
        return returnValue.filter { getElement(it) !is ParadoxFakePsiElement }
    }

    private fun getElement(navigationTarget: NavigationTarget): PsiElement? {
        val kClass = navigationTarget::class
        if (kClass.qualifiedName != "com.intellij.codeInsight.navigation.impl.PsiElementNavigationTarget") return null
        val prop = kClass.memberProperties.find { it.name == "myElement" } as? KProperty1<NavigationTarget, PsiElement> ?: return null
        prop.isAccessible = true
        return prop.get(navigationTarget)
    }
}
