package icu.windea.pls.script.inspections.general

import com.intellij.codeInsight.highlighting.*
import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.codeInspection.*
import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.search.*
import com.intellij.psi.search.searches.*
import com.intellij.psi.util.*
import com.intellij.ui.dsl.builder.*
import com.intellij.util.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.core.search.scope.*
import icu.windea.pls.localisation.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.impl.*
import icu.windea.pls.script.references.*
import java.util.concurrent.*
import javax.swing.*

/**
 * 参数（`$PARAM$`）被设置/引用但未被使用的检查。
 *
 * 例如：有`some_effect = {PARAM = some_value}`但没有`some_effect = { some_prop = $PARAM$ }`，后者是定义的声明。
 * @property forParameterConditionExpressions 是否对参数条件表达式进行检查。（`[[PARAM] ... ]`）
 * @property forInvocationExpressions 是否对调用表达式进行检查。（`some_effect = {PARAM = some_value}`）
 * @property forScriptValueExpressions 是否对SV表达式进行检查。（`some_prop = value:some_sv|PARAM|value|`）
 */
@SlowApi
class UnusedParameterInspection : LocalInspectionTool() {
    @JvmField var forParameterConditionExpressions = true
    @JvmField var forInvocationExpressions = true
    @JvmField var forScriptValueExpressions = true
    
    //may be very slow for ReferencesSearch
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        return object : PsiElementVisitor() {
            var searchScope: GlobalSearchScope? = null //compute once per file
            val statusMap = mutableMapOf<PsiElement, Boolean>() //it's unnecessary to make it synced
            
            private fun shouldVisit(element: PsiElement): Boolean {
                //ignore with parameters situation
                //propertyKey -> can only be invocation expression
                //string -> can only be sv expression
                //parameterConditionParameter -> must be parameter condition expression
                return (element is ParadoxScriptPropertyKey && forInvocationExpressions)
                    || (element is ParadoxScriptString && forScriptValueExpressions)
                    || (element is ParadoxScriptParameterConditionParameter && forParameterConditionExpressions)
            }
            
            override fun visitFile(file: PsiFile) {
                val virtualFile = file.virtualFile
                searchScope = runReadAction { ParadoxSearchScope.fromFile(holder.project, virtualFile, virtualFile.fileInfo) }
                    .withFileTypes(ParadoxScriptFileType, ParadoxLocalisationFileType)
            }
            
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(!shouldVisit(element)) return
                
                val references = element.references
                for(reference in references) {
                    ProgressManager.checkCanceled()
                    if(!reference.canResolveParameter()) continue
                    val resolved = reference.resolveFirst()
                    ProgressManager.checkCanceled()
                    if(resolved !is ParadoxParameterElement) continue
                    if(resolved.contextName.isParameterized()) continue //skip if context name is parameterized
                    if(resolved.readWriteAccess == Access.Write) {
                        //当确定已被使用时，后续不需要再进行ReferencesSearch
                        val used = statusMap[resolved]
                        val isUsed = if(used == null) {
                            ProgressManager.checkCanceled()
                            val searchScope = searchScope ?: GlobalSearchScope.allScope(holder.project)
                            val r = ReferencesSearch.search(resolved, searchScope).processQueryAsync p@{
                                ProgressManager.checkCanceled()
                                val res = it.resolve()
                                ProgressManager.checkCanceled()
                                if(res is ParadoxParameterElement && res.readWriteAccess == Access.Read) {
                                    statusMap[resolved] = true
                                    false
                                } else {
                                    true
                                }
                            }
                            if(r) {
                                statusMap[resolved] = false
                                false
                            } else {
                                true
                            }
                        } else {
                            used
                        }
                        if(!isUsed) {
                            registerProblem(element, resolved.name, reference.rangeInElement)
                        }
                    }
                }
            }
            
            private fun registerProblem(element: PsiElement, name: String, range: TextRange) {
                val message = PlsBundle.message("inspection.script.general.unusedParameter.description", name)
                holder.registerProblem(element, message, ProblemHighlightType.LIKE_UNUSED_SYMBOL, range)
            }
        }
    }
    
    override fun createOptionsPanel(): JComponent {
        return panel {
            row {
                checkBox(PlsBundle.message("inspection.script.general.unusedParameter.option.forParameterConditionExpressions"))
                    .bindSelected(::forParameterConditionExpressions)
                    .applyToComponent { toolTipText = PlsBundle.message("inspection.script.general.unusedParameter.option.forParameterConditionExpressions.tooltip") }
                    .actionListener { _, component -> forParameterConditionExpressions = component.isSelected }
            }
            row {
                checkBox(PlsBundle.message("inspection.script.general.unusedParameter.option.forInvocationExpressions"))
                    .bindSelected(::forInvocationExpressions)
                    .applyToComponent { toolTipText = PlsBundle.message("inspection.script.general.unusedParameter.option.forInvocationExpressions.tooltip") }
                    .actionListener { _, component -> forInvocationExpressions = component.isSelected }
            }
            row {
                checkBox(PlsBundle.message("inspection.script.general.unusedParameter.option.forScriptValueExpressions"))
                    .bindSelected(::forScriptValueExpressions)
                    .applyToComponent { toolTipText = PlsBundle.message("inspection.script.general.unusedParameter.option.forScriptValueExpressions.tooltip") }
                    .actionListener { _, component -> forScriptValueExpressions = component.isSelected }
            }
        }
    }
}

