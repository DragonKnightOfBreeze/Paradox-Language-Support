package icu.windea.pls.lang.codeInsight.navigation

import com.intellij.codeInsight.navigation.GotoTargetHandler
import com.intellij.psi.PsiElement
import icu.windea.pls.core.collections.toArray

// com.intellij.codeInsight.navigation.GotoTargetHandler.GotoTargetHandler
// com.intellij.testIntegration.GotoTestOrCodeHandler

/**
 * 导航处理器的基类。为实现类提供一些实用的封装和抽象。
 */
abstract class GotoHandlerBase : GotoTargetHandler() {
    override fun shouldSortTargets(): Boolean {
        return false // by default false
    }

    protected fun getGotoData(sourceElement: PsiElement, targets: MutableList<PsiElement>, removeSource: Boolean = true): GotoData? {
        if (targets.isEmpty()) return null // unavailable
        if (removeSource) targets.removeIf { it == sourceElement } // remove current target from targets if needed
        return GotoData(sourceElement, targets.distinct().toArray(PsiElement.EMPTY_ARRAY), emptyList())
    }
}
