package icu.windea.pls.lang.codeInsight.highlighting

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.resolveFirst
import icu.windea.pls.lang.ParadoxLanguage
import icu.windea.pls.lang.psi.mock.ParadoxComplexEnumValueElement
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.lang.psi.mock.ParadoxLocalisationParameterElement
import icu.windea.pls.lang.psi.mock.ParadoxParameterElement
import icu.windea.pls.model.constraints.ParadoxResolveConstraint

// 如果在查找用法页面中能够获取具体的读/写图标，就不会再显示PsiElement对应的图标（element.presentation.getIcon()）

/**
 * 在查找用法中，区分复杂枚举值、动态值、参数等的读写使用。
 */
class ParadoxReadWriteAccessDetector : ReadWriteAccessDetector() {
    override fun isReadWriteAccessible(element: PsiElement): Boolean {
        return when {
            element is ParadoxParameterElement -> true
            element is ParadoxDynamicValueElement -> true
            element is ParadoxComplexEnumValueElement -> true
            else -> false
        }
    }

    override fun isDeclarationWriteAccess(element: PsiElement): Boolean {
        return when {
            element is ParadoxParameterElement -> true
            element is ParadoxDynamicValueElement -> true
            element is ParadoxComplexEnumValueElement -> true
            else -> false
        }
    }

    override fun getReferenceAccess(referencedElement: PsiElement, reference: PsiReference): Access {
        if (ParadoxResolveConstraint.Parameter.canResolve(reference)) {
            val resolved = reference.resolveFirst()?.castOrNull<ParadoxParameterElement>()
            if (resolved != null) return resolved.readWriteAccess
        }
        if (ParadoxResolveConstraint.LocalisationParameter.canResolve(reference)) {
            val resolved = reference.resolveFirst()?.castOrNull<ParadoxLocalisationParameterElement>()
            if (resolved != null) return resolved.readWriteAccess
        }
        if (ParadoxResolveConstraint.DynamicValue.canResolve(reference)) {
            val resolved = reference.resolveFirst()?.castOrNull<ParadoxDynamicValueElement>()
            if (resolved != null) return resolved.readWriteAccess
        }
        if (ParadoxResolveConstraint.ComplexEnumValue.canResolve(reference)) {
            val resolved = reference.resolveFirst()?.castOrNull<ParadoxComplexEnumValueElement>()
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
                val resolved = reference.resolveFirst()?.castOrNull<ParadoxParameterElement>()
                if (resolved != null) return resolved.readWriteAccess
            }
            if (ParadoxResolveConstraint.LocalisationParameter.canResolve(reference)) {
                val resolved = reference.resolveFirst()?.castOrNull<ParadoxLocalisationParameterElement>()
                if (resolved != null) return resolved.readWriteAccess
            }
            if (ParadoxResolveConstraint.DynamicValue.canResolve(reference)) {
                val resolved = reference.resolveFirst()?.castOrNull<ParadoxDynamicValueElement>()
                if (resolved != null) return resolved.readWriteAccess
            }
            if (ParadoxResolveConstraint.ComplexEnumValue.canResolve(reference)) {
                val resolved = reference.resolveFirst()?.castOrNull<ParadoxComplexEnumValueElement>()
                if (resolved != null) return resolved.readWriteAccess
            }
        }
        return Access.ReadWrite
    }
}
