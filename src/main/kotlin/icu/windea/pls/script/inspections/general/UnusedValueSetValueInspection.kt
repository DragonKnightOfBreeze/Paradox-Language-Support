package icu.windea.pls.script.inspections.general

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.codeInspection.*
import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.*
import icu.windea.pls.core.search.scope.*
import icu.windea.pls.core.search.selector.chained.*
import icu.windea.pls.localisation.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

/**
 * 值集值值（`some_flag`）被设置但未被使用的检查。
 *
 * 例如，有`set_flag = xxx`但没有`has_flag = xxx`。
 * 
 * 默认不启用。
 */
@SlowApi
@OptimizedApi
class UnusedValueSetValueInspection : LocalInspectionTool() {
    //may be slow for ParadoxValueSetValueSearch
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        val project = holder.project
        val file = holder.file
        val searchScope = runReadAction { ParadoxSearchScope.fromFile(project, file.virtualFile, file.fileInfo) }
            .withFileTypes(ParadoxScriptFileType, ParadoxLocalisationFileType) //compute once per file
        val statusMap = mutableMapOf<PsiElement, Boolean>() //it's unnecessary to make it synced
        
        return object : PsiElementVisitor() {
            private fun shouldVisit(element: PsiElement): Boolean {
                return (element is ParadoxScriptStringExpressionElement && !element.isParameterized())
            }
            
            override fun visitElement(element: PsiElement) {
                if(!shouldVisit(element)) return
                ProgressManager.checkCanceled()
                
                val references = element.references
                for(reference in references) {
                    ProgressManager.checkCanceled()
                    if(!reference.canResolveValueSetValue()) continue
                    val resolved = reference.resolveFirst()
                    ProgressManager.checkCanceled()
                    if(resolved !is ParadoxValueSetValueElement) continue
                    if(resolved.readWriteAccess == Access.Write) {
                        val used = statusMap[resolved]
                        val isUsed = if(used == null) {
                            ProgressManager.checkCanceled()
                            val selector = valueSetValueSelector(project, file).withSearchScope(searchScope) //use file as context
                            val r = ParadoxValueSetValueSearch.search(resolved.name, resolved.valueSetNames, selector).processQueryAsync p@{
                                ProgressManager.checkCanceled()
                                if(it.readWriteAccess == Access.Read) {
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
                            registerProblem(element, resolved.name, resolved.valueSetNames.joinToString(), reference.rangeInElement)
                        }
                    }
                }
            }
            
            private fun registerProblem(element: PsiElement, name: String, valueSetName: String, range: TextRange) {
                val message = PlsBundle.message("inspection.script.general.unusedValueSetValue.description", name, valueSetName)
                holder.registerProblem(element, message, ProblemHighlightType.LIKE_UNUSED_SYMBOL, range)
            }
        }
    }
}

