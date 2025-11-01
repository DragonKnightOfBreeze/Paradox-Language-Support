package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.processQueryAsync
import icu.windea.pls.core.resolveFirst
import icu.windea.pls.core.runReadActionSmartly
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.psi.mock.ParadoxDynamicValueElement
import icu.windea.pls.lang.search.ParadoxDynamicValueSearch
import icu.windea.pls.lang.search.scope.ParadoxSearchScope
import icu.windea.pls.lang.search.selector.dynamicValue
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.lang.search.selector.withSearchScope
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.model.constraints.ParadoxResolveConstraint
import icu.windea.pls.script.psi.ParadoxScriptStringExpressionElement

/**
 * 动态值值被使用但未被设置的代码检查。
 *
 * 例如，有`has_flag = xxx`但没有`set_flag = xxx`。
 *
 * 默认不启用。
 */
class UnsetDynamicValueInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val project = holder.project
        val file = holder.file
        // compute once per file
        val searchScope = runReadActionSmartly { ParadoxSearchScope.fromFile(project, file.virtualFile) }
        // it's unnecessary to make it synced
        val statusMap = mutableMapOf<PsiElement, Boolean>()

        return object : PsiElementVisitor() {
            private fun shouldVisit(element: PsiElement): Boolean {
                return when {
                    element is ParadoxScriptStringExpressionElement -> !element.text.isParameterized()
                    else -> false
                }
            }

            override fun visitElement(element: PsiElement) {
                if (!shouldVisit(element)) return

                val references = element.references
                for (reference in references) {
                    ProgressManager.checkCanceled()
                    if (!ParadoxResolveConstraint.DynamicValue.canResolve(reference)) continue
                    val resolved = reference.resolveFirst()
                    if (resolved !is ParadoxDynamicValueElement) continue
                    if (resolved.readWriteAccess != Access.Read) continue
                    val cachedStatus = statusMap[resolved]
                    val status = if (cachedStatus == null) {
                        ProgressManager.checkCanceled()
                        val selector = selector(project, file).dynamicValue().withSearchScope(searchScope) // use file as context
                        val r = ParadoxDynamicValueSearch.search(resolved.name, resolved.dynamicValueTypes, selector).processQueryAsync p@{
                            ProgressManager.checkCanceled()
                            if (it.readWriteAccess == Access.Write) {
                                statusMap[resolved] = true
                                false
                            } else {
                                true
                            }
                        }

                        if (r) {
                            statusMap[resolved] = false
                            false
                        } else {
                            true
                        }
                    } else {
                        cachedStatus
                    }
                    if (!status) {
                        registerProblem(element, resolved.name, resolved.dynamicValueTypes.joinToString(), reference.rangeInElement)
                    }
                }
            }

            private fun registerProblem(element: PsiElement, name: String, dynamicValueType: String, range: TextRange) {
                val message = PlsBundle.message("inspection.script.unsetDynamicValue.desc", name, dynamicValueType)
                holder.registerProblem(element, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, range)
            }
        }
    }
}
