package icu.windea.pls.script.inspections.advanced

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.script.expression.reference.*
import icu.windea.pls.script.psi.*
import icu.windea.pls.script.psi.impl.*
import icu.windea.pls.script.reference.*
import java.util.concurrent.*
import javax.swing.*

/**
 * 值集中的值（`some_flag`）被设置/引用但未被使用的检查
 *
 * 例如，有`set_flag = xxx`但没有`has_flag = xxx`。
 * @property forScopeFieldExpressions 是否对作用域字段表达式进行检查。
 * @property forValueFieldExpressions 是否对值字段表达式（包括SV表达式）进行检查。
 */
class UnusedValueSetValueInspection : LocalInspectionTool() {
	//may be slow for ReferencesSearch
	
	companion object {
		private val statusMapKey = Key.create<MutableMap<ParadoxValueSetValueElement, Boolean>>("paradox.statusMap")
	}
	
	@JvmField var forScopeFieldExpressions = true
	@JvmField var forValueFieldExpressions = true
	
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
			return (element is ParadoxScriptExpressionElement && !element.isParameterAwareExpression())
		}
		
		override fun visitElement(element: PsiElement) {
			if(!shouldVisit(element)) return
			ProgressManager.checkCanceled()
			
			val references = element.references
			for(reference in references) {
				ProgressManager.checkCanceled()
				if(reference !is ParadoxValueSetValueResolvable) continue
				if(reference is ParadoxScriptScopeFieldDataSourceReference && !inspection.forScopeFieldExpressions) continue
				if(reference is ParadoxScriptValueFieldDataSourceReference && !inspection.forValueFieldExpressions) continue
				val resolved = if(reference is PsiPolyVariantReference) {
					val multiResolved = reference.multiResolve(false)
					if(multiResolved.size != 1) continue
					multiResolved.single().element ?: continue
				} else {
					reference.resolve()
				}
				if(resolved !is ParadoxValueSetValueElement) continue
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
		}
		
		private fun registerProblem(resolved: ParadoxValueSetValueElement, reference: PsiReference) {
			val message = PlsBundle.message("script.inspection.advanced.unusedValueSetValue.description", resolved.name)
			holder.registerProblem(reference, message, ProblemHighlightType.LIKE_UNUSED_SYMBOL)
		}
	}
	
	
	override fun createOptionsPanel(): JComponent {
		return panel {
			row {
				checkBox(PlsBundle.message("script.inspection.advanced.unusedValueSetValue.option.forScopeFieldExpressions"))
					.bindSelected(::forScopeFieldExpressions)
					.applyToComponent { toolTipText = PlsBundle.message("script.inspection.advanced.unusedValueSetValue.option.forScopeFieldExpressions.tooltip") }
					.actionListener { _, component -> forScopeFieldExpressions = component.isSelected }
			}
			row {
				checkBox(PlsBundle.message("script.inspection.advanced.unusedValueSetValue.option.forValueFieldExpressions"))
					.bindSelected(::forValueFieldExpressions)
					.applyToComponent { toolTipText = PlsBundle.message("script.inspection.advanced.unusedValueSetValue.option.forValueFieldExpressions.tooltip") }
					.actionListener { _, component -> forValueFieldExpressions = component.isSelected }
			}
		}
	}
}