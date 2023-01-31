package icu.windea.pls.script.inspections.advanced

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.script.psi.*
import java.util.concurrent.*

/**
 * 值集值值（`some_flag`）被使用但未被设置的检查。
 *
 * 例如，有`has_flag = xxx`但没有`set_flag = xxx`。
 */
class UnsetValueSetValueInspection : LocalInspectionTool() {
	//may be slow for ReferencesSearch
	
	companion object {
		private val statusMapKey = Key.create<MutableMap<ParadoxValueSetValueElement, Boolean>>("paradox.statusMap")
	}
	
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
		session.putUserData(statusMapKey, ConcurrentHashMap())
		return Visitor(this, holder, session)
	}
	
	private class Visitor(
		private val inspection: UnsetValueSetValueInspection,
		private val holder: ProblemsHolder,
		private val session: LocalInspectionToolSession
	) : ParadoxScriptVisitor() {
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
				if(resolved !is ParadoxValueSetValueElement) continue
				if(resolved.read) {
					//当确定同一文件中某一名称的参数已被使用时，后续不需要再进行ReferencesSearch
					val statusMap = session.getUserData(statusMapKey)!!
					val used = statusMap[resolved]
					val isUsed = if(used == null) {
						val r = ReferencesSearch.search(resolved).processQuery {
							val res = it.resolve()
							if(res is ParadoxValueSetValueElement && !res.read) {
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
			val message = PlsBundle.message("inspection.script.advanced.unsetValueSetValue.description", name)
			holder.registerProblem(element, message, ProblemHighlightType.GENERIC_ERROR_OR_WARNING, range,
				ImportGameOrModDirectoryFix(element)
			)
		}
	}
}
