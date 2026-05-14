package icu.windea.pls.lang.codeInsight.highlighting

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import icu.windea.pls.core.ReadWriteAccess
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.psi.PsiReadWriteAccessAwareElement
import icu.windea.pls.core.resolveFirst
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.lang.ParadoxLanguage
import icu.windea.pls.lang.psi.light.CwtConfigLightElementBase
import icu.windea.pls.lang.psi.light.CwtConfigSymbolLightElement
import icu.windea.pls.lang.references.cwt.CwtConfigSymbolPsiReference

/**
 * 在查找用法中，区分规则符号的读写使用。
 */
class CwtConfigReadWriteAccessDetector : ReadWriteAccessDetector() {
    // 如果在查找用法页面中能够获取具体的读/写图标，就不会再显示 `PsiElement` 对应的图标（`element.presentation.getIcon()`）

    override fun isReadWriteAccessible(element: PsiElement): Boolean {
        return element is PsiReadWriteAccessAwareElement && element is CwtConfigLightElementBase
    }

    override fun isDeclarationWriteAccess(element: PsiElement): Boolean {
        return element is PsiReadWriteAccessAwareElement && element is CwtConfigLightElementBase
    }

    override fun getReferenceAccess(referencedElement: PsiElement, reference: PsiReference): Access {
        val resolved = reference.resolveFirst() ?: return ReadWriteAccess.ReadWrite
        if(resolved is PsiReadWriteAccessAwareElement) return resolved.readWriteAccess
        return ReadWriteAccess.ReadWrite
    }

    override fun getExpressionAccess(expression: PsiElement): Access {
        // find usages use this method finally
        if (expression.language !is ParadoxLanguage) return Access.ReadWrite
        for (reference in expression.references) {
            ProgressManager.checkCanceled()
            val resolved = reference.resolveFirst() ?: continue
            if (resolved is PsiReadWriteAccessAwareElement) return resolved.readWriteAccess
        }
        return Access.ReadWrite
    }
}
