package icu.windea.pls.lang.codeInsight.highlight

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.usages.*
import com.intellij.usages.impl.rules.*
import icu.windea.pls.core.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.cwt.*
import icu.windea.pls.lang.psi.mock.*
import icu.windea.pls.lang.references.cwt.*

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
