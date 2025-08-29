package icu.windea.pls.lang.codeInsight.highlight

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.resolveFirst
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.lang.psi.mock.CwtConfigSymbolElement
import icu.windea.pls.lang.references.cwt.CwtConfigSymbolPsiReference

//如果在查找使用页面中能够获取具体的读/写图标，就不会再显示PsiElement对应的图标（element.presentation.getIcon()）

/**
 * 在查找使用中，区分CWT规则符号的读写使用。
 */
class CwtConfigReadWriteAccessDetector : ReadWriteAccessDetector() {
    override fun isReadWriteAccessible(element: PsiElement): Boolean {
        return element is CwtConfigSymbolElement
    }

    override fun isDeclarationWriteAccess(element: PsiElement): Boolean {
        return element is CwtConfigSymbolElement
    }

    override fun getReferenceAccess(referencedElement: PsiElement, reference: PsiReference): Access {
        if (reference !is CwtConfigSymbolPsiReference) return Access.ReadWrite
        val resolved = reference.resolveFirst()?.castOrNull<CwtConfigSymbolElement>()
        if (resolved == null) return Access.ReadWrite
        return resolved.readWriteAccess
    }

    override fun getExpressionAccess(expression: PsiElement): Access {
        //find usages use this method finally
        if (expression.language !is CwtLanguage) return Access.ReadWrite
        val results = mutableSetOf<Access>()
        for (reference in expression.references) {
            ProgressManager.checkCanceled()
            if (reference !is CwtConfigSymbolPsiReference) continue
            val resolved = reference.resolveFirst()?.castOrNull<CwtConfigSymbolElement>()
            if (resolved == null) continue
            results += resolved.readWriteAccess
        }
        return results.singleOrNull() ?: Access.ReadWrite
    }
}
