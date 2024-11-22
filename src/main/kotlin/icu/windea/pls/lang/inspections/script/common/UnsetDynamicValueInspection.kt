package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.codeInspection.*
import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.search.*
import icu.windea.pls.lang.search.scope.*
import icu.windea.pls.lang.search.selector.*
import icu.windea.pls.model.constraints.*
import icu.windea.pls.script.psi.*
import kotlin.collections.set

/**
 * 动态值值被使用但未被设置的检查。
 *
 * 例如，有`has_flag = xxx`但没有`set_flag = xxx`。
 *
 * 默认不启用。
 */
class UnsetDynamicValueInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        if (!shouldCheckFile(holder.file)) return PsiElementVisitor.EMPTY_VISITOR

        val project = holder.project
        val file = holder.file
        //compute once per file
        val searchScope = runReadAction { ParadoxSearchScope.fromFile(project, file.virtualFile) }
        //it's unnecessary to make it synced
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
                ProgressManager.checkCanceled()

                val references = element.references
                for (reference in references) {
                    ProgressManager.checkCanceled()
                    if (!reference.canResolve(ParadoxResolveConstraint.DynamicValue)) continue
                    val resolved = reference.resolveFirst()
                    if (resolved !is ParadoxDynamicValueElement) continue
                    if (resolved.readWriteAccess != Access.Read) continue
                    val cachedStatus = statusMap[resolved]
                    val status = if (cachedStatus == null) {
                        ProgressManager.checkCanceled()
                        val selector = selector(project, file).dynamicValue().withSearchScope(searchScope) //use file as context
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

    private fun shouldCheckFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }
}
