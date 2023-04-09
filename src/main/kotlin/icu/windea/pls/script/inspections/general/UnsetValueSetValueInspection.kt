package icu.windea.pls.script.inspections.general

import com.intellij.codeInsight.highlighting.ReadWriteAccessDetector.*
import com.intellij.codeInspection.*
import com.intellij.openapi.application.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.search.scope.*
import icu.windea.pls.localisation.*
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*
import java.util.concurrent.*

/**
 * 值集值值（`some_flag`）被使用但未被设置的检查。
 *
 * 例如，有`has_flag = xxx`但没有`set_flag = xxx`。
 */
class UnsetValueSetValueInspection : LocalInspectionTool(){
    companion object {
        private val statusMapKey = Key.create<MutableMap<ParadoxValueSetValueElement, Boolean>>("paradox.statusMap")
    }
    
    //may be slow for ReferencesSearch
    
    override fun runForWholeFile(): Boolean {
        return true
    }
    
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
        session.putUserData(statusMapKey, ConcurrentHashMap())
        return object : PsiElementVisitor() {
            private fun shouldVisit(element: PsiElement): Boolean {
                //ignore with parameters situation
                return (element is ParadoxScriptStringExpressionElement && !element.isParameterAwareExpression())
            }
            
            override fun visitElement(element: PsiElement) {
                if(!shouldVisit(element)) return
                ProgressManager.checkCanceled()
                
                val references = element.references
                for(reference in references) {
                    ProgressManager.checkCanceled()
                    if(!reference.canResolveValueSetValue()) continue
                    val resolved = reference.resolveSingle()
                    ProgressManager.checkCanceled()
                    if(resolved !is ParadoxValueSetValueElement) continue
                    if(resolved.readWriteAccess == Access.Read) {
                        //当确定同一文件中某一名称的参数已被使用时，后续不需要再进行ReferencesSearch
                        val statusMap = session.getUserData(statusMapKey)!!
                        val used = statusMap[resolved]
                        val isUsed = if(used == null) {
                            ProgressManager.checkCanceled()
                            //optimize search scope
                            val searchScope = runReadAction { ParadoxSearchScope.fromElement(element) }
                                ?.withFileTypes(ParadoxScriptFileType, ParadoxLocalisationFileType)
                                ?: return
                            val r = ReferencesSearch.search(resolved, searchScope).processQuery {
                                ProgressManager.checkCanceled()
                                val res = it.resolveFast()
                                ProgressManager.checkCanceled()
                                if(res is ParadoxValueSetValueElement && res.readWriteAccess == Access.Write) {
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
                val message = PlsBundle.message("inspection.script.general.unsetValueSetValue.description", name)
                holder.registerProblem(element, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, range)
            }
        }
    }
}
