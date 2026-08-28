package icu.windea.pls.lang.inspections

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElement
import icu.windea.pls.core.processAsync
import icu.windea.pls.core.resolveFirst
import icu.windea.pls.core.util.ReadWriteAccess
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.light.ParadoxDynamicValueLightElement
import icu.windea.pls.lang.psi.light.ParadoxParameterLightElement
import icu.windea.pls.lang.search.ParadoxDynamicValueSearch
import icu.windea.pls.lang.search.ParadoxParameterSearch
import icu.windea.pls.lang.search.util.withSearchScope
import icu.windea.pls.model.constraints.ParadoxReferenceConstraint
import icu.windea.pls.script.psi.ParadoxScriptConditionParameter
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * @see ReadWriteAccess
 */
object ParadoxAccessInspectionService {
    // region UnusedParameterInspection

    fun checkForUnusedParameter(element: PsiElement, context: ParadoxAccessInspectionContext) {
        val holder = context.holder
        if (element !is ParadoxScriptStringExpressionElement && element !is ParadoxScriptConditionParameter) return
        val references = element.references
        for (reference in references) {
            ProgressManager.checkCanceled()
            if (!ParadoxReferenceConstraint.Parameter.canResolve(reference)) continue
            val resolved = reference.resolve()
            if (resolved !is ParadoxParameterLightElement) continue
            if (resolved.contextName.isParameterized()) continue // skip if context name is parameterized
            if (resolved.readWriteAccess != ReadWriteAccess.Write) continue
            val cachedStatus = context.statusMap[resolved]
            val status = if (cachedStatus == null) {
                ProgressManager.checkCanceled()
                val selector = ParadoxParameterSearch.selector(holder.project, holder.file).withSearchScope(context.searchScope) // use file as context
                val r = ParadoxParameterSearch.search(resolved.name, resolved.contextKey, selector).processAsync p@{
                    ProgressManager.checkCanceled()
                    if (it.readWriteAccess == ReadWriteAccess.Read) {
                        context.statusMap[resolved] = true
                        false
                    } else {
                        true
                    }
                }

                if (r) {
                    context.statusMap[resolved] = false
                    false
                } else {
                    true
                }
            } else {
                cachedStatus
            }
            if (!status) {
                val description = ChronicleInspectionBundle.message("script.unusedParameter.desc", resolved.name)
                holder.registerProblem(element, description, ProblemHighlightType.LIKE_UNUSED_SYMBOL, reference.rangeInElement)
            }
        }
    }

    // endregion

    // endregion UnusedDynamicValueInspection

    fun checkForUnusedDynamicValue(element: ParadoxScriptStringExpressionElement, context: ParadoxAccessInspectionContext) {
        val holder = context.holder
        val references = element.references
        for (reference in references) {
            ProgressManager.checkCanceled()
            if (!ParadoxReferenceConstraint.DynamicValue.canResolve(reference)) continue
            val resolved = reference.resolveFirst()
            if (resolved !is ParadoxDynamicValueLightElement) continue
            if (resolved.readWriteAccess != Access.Write) continue
            val cachedStatus = context.statusMap[resolved]
            val status = if (cachedStatus == null) {
                ProgressManager.checkCanceled()
                val selector = ParadoxDynamicValueSearch.selector(holder.project, holder.file).withSearchScope(context.searchScope) // use file as context
                val r = ParadoxDynamicValueSearch.search(resolved.name, resolved.types, selector).processAsync p@{
                    ProgressManager.checkCanceled()
                    if (it.readWriteAccess == Access.Read) {
                        context.statusMap[resolved] = true
                        false
                    } else {
                        true
                    }
                }

                if (r) {
                    context.statusMap[resolved] = false
                    false
                } else {
                    true
                }
            } else {
                cachedStatus
            }
            if (!status) {
                val description = ChronicleInspectionBundle.message("script.unusedDynamicValue.desc", resolved.name, resolved.types.joinToString())
                holder.registerProblem(element, description, ProblemHighlightType.LIKE_UNUSED_SYMBOL, reference.rangeInElement)
            }
        }
    }

    // endregion

    // region UnsetDynamicValueInspection

    fun checkForUnsetDynamicValue(element: ParadoxScriptStringExpressionElement, context: ParadoxAccessInspectionContext) {
        val holder = context.holder
        val references = element.references
        for (reference in references) {
            ProgressManager.checkCanceled()
            if (!ParadoxReferenceConstraint.DynamicValue.canResolve(reference)) continue
            val resolved = reference.resolveFirst()
            if (resolved !is ParadoxDynamicValueLightElement) continue
            if (resolved.readWriteAccess != Access.Read) continue
            val cachedStatus = context.statusMap[resolved]
            val status = if (cachedStatus == null) {
                ProgressManager.checkCanceled()
                val selector = ParadoxDynamicValueSearch.selector(holder.project, holder.file).withSearchScope(context.searchScope) // use file as context
                val r = ParadoxDynamicValueSearch.search(resolved.name, resolved.types, selector).processAsync p@{
                    ProgressManager.checkCanceled()
                    if (it.readWriteAccess == Access.Write) {
                        context.statusMap[resolved] = true
                        false
                    } else {
                        true
                    }
                }

                if (r) {
                    context.statusMap[resolved] = false
                    false
                } else {
                    true
                }
            } else {
                cachedStatus
            }
            if (!status) {
                val description = ChronicleInspectionBundle.message("script.unsetDynamicValue.desc", resolved.name, resolved.types.joinToString())
                holder.registerProblem(element, description, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, reference.rangeInElement)
            }
        }
    }

    // endregion
}
