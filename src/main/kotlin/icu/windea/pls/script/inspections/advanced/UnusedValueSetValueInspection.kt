package icu.windea.pls.script.inspections.advanced

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import icu.windea.pls.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.reference.*
import java.util.concurrent.*

/**
 * 值集中的值（`some_flag`）被设置但未被使用的检查
 *
 * 例如，有`set_flag = xxx`但没有`has_flag = xxx`。
 */
class UnusedValueSetValueInspection : LocalInspectionTool() {
	//may be slow for ReferencesSearch
	
	companion object {
		private val statusMapKey = Key.create<MutableMap<ParadoxValueSetValueElement, Boolean>>("paradox.statusMap")
	}
	
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
		session.putUserData(statusMapKey, ConcurrentHashMap())
		return Visitor(this, holder, session)
	}
	
	private class Visitor(
		private val inspection: UnusedValueSetValueInspection,
		private val holder: ProblemsHolder,
		private val session: LocalInspectionToolSession
	) : ParadoxScriptVisitor() {
		private fun shouldVisit(element: PsiElement): Boolean {
			//ignore with parameters situation
			return (element is ParadoxExpressionAwareElement && !element.isParameterAwareExpression())
		}
		
		override fun visitElement(element: PsiElement) {
			if(!shouldVisit(element)) return
			ProgressManager.checkCanceled()
			
			//may only resolve to single ParadoxValueSetValueElement (set-flag expression)
			val reference = element.reference
			if(reference !is ParadoxValueSetValueResolvable) return
			val resolved = reference.resolve()
			if(resolved !is ParadoxValueSetValueElement) return
			if(!resolved.read) {
				//当确定同一文件中某一名称的参数已被使用时，后续不需要再进行ReferencesSearch
				val statusMap = session.getUserData(statusMapKey)!!
				val used = statusMap[resolved]
				val isUsed = if(used == null) {
					val r = ReferencesSearch.search(resolved).processResult {
						val res = it.resolve()
						if(res is ParadoxValueSetValueElement && res.read) {
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
					registerProblem(resolved, reference)
				}
			}
		}
		
		private fun registerProblem(resolved: ParadoxValueSetValueElement, reference: PsiReference) {
			val message = PlsBundle.message("script.inspection.advanced.unusedValueSetValue.description", resolved.name)
			holder.registerProblem(reference, message, ProblemHighlightType.LIKE_UNUSED_SYMBOL)
		}
	}
}

