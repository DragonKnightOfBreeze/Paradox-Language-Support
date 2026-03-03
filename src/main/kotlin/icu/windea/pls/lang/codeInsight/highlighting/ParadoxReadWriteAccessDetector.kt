package icu.windea.pls.lang.codeInsight.highlighting

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.resolveFirst
import icu.windea.pls.lang.ParadoxLanguage
import icu.windea.pls.lang.psi.light.ParadoxComplexEnumValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxLocalisationParameterLightElement
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.model.constraints.ParadoxResolveConstraint

/**
 * 在查找用法中，区分复杂枚举值、动态值、参数等的读写使用。
 */
class ParadoxReadWriteAccessDetector : ReadWriteAccessDetector() {
    // 如果在查找用法页面中能够获取具体的读/写图标，就不会再显示 `PsiElement` 对应的图标（`element.presentation.getIcon()`）

    override fun isReadWriteAccessible(element: PsiElement): Boolean {
        return when {
            element is ParadoxParameterLightElement -> true
            element is ParadoxDynamicValueLightElement -> true
            element is ParadoxComplexEnumValueLightElement -> true
            else -> false
        }
    }

    override fun isDeclarationWriteAccess(element: PsiElement): Boolean {
        return when {
            element is ParadoxParameterLightElement -> true
            element is ParadoxDynamicValueLightElement -> true
            element is ParadoxComplexEnumValueLightElement -> true
            else -> false
        }
    }

    override fun getReferenceAccess(referencedElement: PsiElement, reference: PsiReference): Access {
        if (ParadoxResolveConstraint.Parameter.canResolve(reference)) {
            val resolved = reference.resolveFirst()?.castOrNull<ParadoxParameterLightElement>()
            if (resolved != null) return resolved.readWriteAccess
        }
        if (ParadoxResolveConstraint.LocalisationParameter.canResolve(reference)) {
            val resolved = reference.resolveFirst()?.castOrNull<ParadoxLocalisationParameterLightElement>()
            if (resolved != null) return resolved.readWriteAccess
        }
        if (ParadoxResolveConstraint.DynamicValue.canResolve(reference)) {
            val resolved = reference.resolveFirst()?.castOrNull<ParadoxDynamicValueLightElement>()
            if (resolved != null) return resolved.readWriteAccess
        }
        if (ParadoxResolveConstraint.ComplexEnumValue.canResolve(reference)) {
            val resolved = reference.resolveFirst()?.castOrNull<ParadoxComplexEnumValueLightElement>()
            if (resolved != null) return resolved.readWriteAccess
        }
        return Access.ReadWrite
    }

    override fun getExpressionAccess(expression: PsiElement): Access {
        // find usages use this method finally
        if (expression.language !is ParadoxLanguage) return Access.ReadWrite
        for (reference in expression.references) {
            ProgressManager.checkCanceled()
            if (ParadoxResolveConstraint.Parameter.canResolve(reference)) {
                val resolved = reference.resolveFirst()?.castOrNull<ParadoxParameterLightElement>()
                if (resolved != null) return resolved.readWriteAccess
            }
            if (ParadoxResolveConstraint.LocalisationParameter.canResolve(reference)) {
                val resolved = reference.resolveFirst()?.castOrNull<ParadoxLocalisationParameterLightElement>()
                if (resolved != null) return resolved.readWriteAccess
            }
            if (ParadoxResolveConstraint.DynamicValue.canResolve(reference)) {
                val resolved = reference.resolveFirst()?.castOrNull<ParadoxDynamicValueLightElement>()
                if (resolved != null) return resolved.readWriteAccess
            }
            if (ParadoxResolveConstraint.ComplexEnumValue.canResolve(reference)) {
                val resolved = reference.resolveFirst()?.castOrNull<ParadoxComplexEnumValueLightElement>()
                if (resolved != null) return resolved.readWriteAccess
            }
        }
        return Access.ReadWrite
    }
}
