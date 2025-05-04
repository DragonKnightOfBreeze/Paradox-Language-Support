package icu.windea.pls.lang.codeInsight.highlight

import com.intellij.codeInsight.highlighting.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.model.constraints.*

//如果在查找使用页面中能够获取具体的读/写图标，就不会再显示PsiElement对应的图标（element.presentation.getIcon()）

/**
 * 在查找使用中，区分参数和动态值值的读/写使用
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
        //find usages use this method finally
        if (expression.language !is ParadoxBaseLanguage) return Access.ReadWrite
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
