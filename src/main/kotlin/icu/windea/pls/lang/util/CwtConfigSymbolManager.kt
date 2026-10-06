package icu.windea.pls.lang.util

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiReference
import com.intellij.psi.util.CachedValue
import com.intellij.psi.util.CachedValueProvider
import com.intellij.psi.util.PsiModificationTracker
import icu.windea.pls.ChronicleCapabilities
import icu.windea.pls.core.annotations.Optimized
import icu.windea.pls.core.collections.mapToArray
import icu.windea.pls.core.optimized
import icu.windea.pls.core.runSmartReadAction
import icu.windea.pls.core.util.KeyRegistry
import icu.windea.pls.core.util.getCachedValueOnDemand
import icu.windea.pls.core.util.getValue
import icu.windea.pls.core.util.provideDelegate
import icu.windea.pls.core.util.registerKey
import icu.windea.pls.cwt.CwtLanguage
import icu.windea.pls.cwt.psi.CwtStringExpressionElement
import icu.windea.pls.cwt.psi.isDataExpression
import icu.windea.pls.lang.references.cwt.CwtConfigSymbolPsiReference
import icu.windea.pls.lang.resolve.CwtConfigSymbolService
import icu.windea.pls.model.CwtConfigSymbolInfo

@Optimized
object CwtConfigSymbolManager {
    object Keys : KeyRegistry() {
        val cachedConfigSymbolInfos by registerKey<CachedValue<List<CwtConfigSymbolInfo>>>(this)
    }

    // NOTE 相比 Symbol API，通过实现继承自 CwtMockPsiElement 的 CwtConfigSymbolElement ，应当能更加简单地实现相关功能（且区分读写访问）

    fun getInfos(element: CwtStringExpressionElement): List<CwtConfigSymbolInfo> {
        ProgressManager.checkCanceled()
        if (!element.isDataExpression()) return emptyList()
        val infos = getInfoInternal(element)
        return infos
    }

    fun getReferences(element: CwtStringExpressionElement): Array<out PsiReference> {
        ProgressManager.checkCanceled()
        if (!element.isDataExpression()) return PsiReference.EMPTY_ARRAY
        val infos = getInfoInternal(element)
        if (infos.isEmpty()) return PsiReference.EMPTY_ARRAY
        // val references = infos.mapFast { CwtConfigSymbolPsiReference(element, TextRange.from(it.offset, it.name.length), it) }
        // return references.toArray(PsiReference.EMPTY_ARRAY)
        return infos.mapToArray(PsiReference.EMPTY_ARRAY) { CwtConfigSymbolPsiReference(element, TextRange.from(it.offset, it.name.length), it) }
    }

    private fun getInfoInternal(element: CwtStringExpressionElement): List<CwtConfigSymbolInfo> {
        return getCachedValueOnDemand(element, Keys.cachedConfigSymbolInfos, onDemand = ChronicleCapabilities.Cache.configSymbol) {
            runSmartReadAction {
                val value = CwtConfigSymbolService.resolveInfos(element).optimized()
                CachedValueProvider.Result.create(value, getInfoDependencies(element))
            }
        }
    }

    private fun getInfoDependencies(element: CwtStringExpressionElement): List<Any> {
        return listOf(element, PsiModificationTracker.getInstance(element.project).forLanguage(CwtLanguage))
    }
}
