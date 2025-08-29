package icu.windea.pls.lang.codeInsight.highlight

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.Access
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import com.intellij.usages.PsiElementUsageTarget
import com.intellij.usages.UsageTarget
import com.intellij.usages.impl.rules.UsageType
import com.intellij.usages.impl.rules.UsageTypeProviderEx
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.collections.findIsInstance
import icu.windea.pls.core.resolveFirst
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.lang.psi.mock.CwtConfigSymbolElement
import icu.windea.pls.lang.references.cwt.CwtConfigSymbolPsiReference

/**
 * 在查找使用中，区分CWT规则符号的使用类型。
 */
class CwtConfigUsageTypeProvider : UsageTypeProviderEx {
    override fun getUsageType(element: PsiElement): UsageType? {
        return getUsageType(element, UsageTarget.EMPTY_ARRAY)
    }

    override fun getUsageType(element: PsiElement, targets: Array<out UsageTarget>): UsageType? {
        if (element.language !is CwtLanguage) return null
        run {
            val targetElements = targets.mapNotNull { it.castOrNull<PsiElementUsageTarget>()?.element }
            val targetElement = targetElements.findIsInstance<CwtConfigSymbolElement>()
            if (targetElement == null) return@run
            for (reference in element.references) {
                ProgressManager.checkCanceled()
                if (reference !is CwtConfigSymbolPsiReference) continue
                val resolved = reference.resolveFirst()?.castOrNull<CwtConfigSymbolElement>()
                if (resolved == null) continue
                if (resolved != targetElement) continue
                return when (resolved.readWriteAccess) {
                    Access.Read -> CwtConfigUsageTypes.SYMBOL_REFERENCE
                    Access.Write -> CwtConfigUsageTypes.SYMBOL_DECLARATION
                    else -> null
                }
            }
        }
        return null
    }
}
