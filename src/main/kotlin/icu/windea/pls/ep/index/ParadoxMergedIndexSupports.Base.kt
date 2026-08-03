package icu.windea.pls.ep.index

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReference
import icu.windea.pls.base.context.ChronicleThreadContext
import icu.windea.pls.core.collections.asMutable
import icu.windea.pls.core.withState
import icu.windea.pls.csv.psi.ParadoxCsvExpressionElement
import icu.windea.pls.lang.index.ChronicleIndexStatisticService
import icu.windea.pls.lang.index.ParadoxMergedIndexContext
import icu.windea.pls.lang.index.ParadoxMergedIndexCsvContext
import icu.windea.pls.lang.index.ParadoxMergedIndexLocalisationContext
import icu.windea.pls.lang.index.ParadoxMergedIndexScriptContext
import icu.windea.pls.lang.psi.ParadoxExpressionElement
import icu.windea.pls.localisation.psi.ParadoxLocalisationExpressionElement
import icu.windea.pls.model.constraints.ParadoxReferenceConstraint
import icu.windea.pls.model.index.ParadoxIndexInfo
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

abstract class ParadoxMergedIndexSupportBase<T : ParadoxIndexInfo> : ParadoxMergedIndexSupport<T> {
    protected fun checkAvailable(context: ParadoxMergedIndexScriptContext): Boolean {
        if (context.definitionCandidateInfo == null) return true
        if (context.definitionCandidateAvailableTypesUnchanged) return true
        if (context.definitionCandidateAvailableTypes.contains(type)) return true
        return false
    }

    protected fun <T : ParadoxIndexInfo> addToFileData(info: T, context: ParadoxMergedIndexContext) {
        ChronicleIndexStatisticService.recordMerged(info.gameType, type)
        context.fileData.getOrPut(type.key) { mutableListOf() }.asMutable() += info
    }
}

abstract class ParadoxMergedIndexSupportFromExpressionReferencesBase<T : ParadoxIndexInfo> : ParadoxMergedIndexSupportBase<T>() {
    abstract val constraint: ParadoxReferenceConstraint

    override fun buildDataForExpression(element: ParadoxScriptStringExpressionElement, context: ParadoxMergedIndexScriptContext) {
        if (!checkAvailable(context)) return
        buildDataFromExpressionReferences(element, context)
    }

    override fun buildDataForExpression(element: ParadoxLocalisationExpressionElement, context: ParadoxMergedIndexLocalisationContext) {
        buildDataFromExpressionReferences(element, context)
    }

    override fun buildDataForExpression(element: ParadoxCsvExpressionElement, context: ParadoxMergedIndexCsvContext) {
        buildDataFromExpressionReferences(element, context)
    }

    protected fun buildDataFromExpressionReferences(element: ParadoxExpressionElement, context: ParadoxMergedIndexContext) {
        // write access
        if (!constraint.canResolveReference(element)) return
        // use cached expression references from context to optimize performance
        val references = context.expressionReferences
        if (references.isEmpty()) return
        for (reference in references) {
            buildDataFromReference(reference, context)
        }
    }

    protected fun buildDataFromReference(reference: PsiReference, context: ParadoxMergedIndexContext) {
        if (!constraint.canResolve(reference)) return
        val resolved = withState(ChronicleThreadContext.resolveForMergedIndex) { reference.resolve() }
        if (resolved == null) return
        buildDataFromResolved(resolved, context)
    }

    protected open fun buildDataFromResolved(resolved: PsiElement, context: ParadoxMergedIndexContext) {

    }
}
