package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.search.searches.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.core.psi.*
import icu.windea.pls.script.psi.*
import java.util.concurrent.*
import javax.swing.*

/**
 * 值集值值（`some_flag`）被设置但未被使用的检查
 *
 * 例如，有`set_flag = xxx`但没有`has_flag = xxx`。
 */
class UnusedValueSetValueInspection : LocalInspectionTool() {
	//may be slow for ReferencesSearch
	
	companion object {
		private val statusMapKey = Key.create<MutableMap<ParadoxValueSetValueElement, Boolean>>("paradox.statusMap")
	}
	
	@JvmField var ignoreDefinitionNames = true
	
	override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean, session: LocalInspectionToolSession): PsiElementVisitor {
		session.putUserData(statusMapKey, ConcurrentHashMap())
		return object : PsiElementVisitor() {
			private fun shouldVisit(element: PsiElement): Boolean {
				return (element is ParadoxScriptStringExpressionElement && !element.isParameterAwareExpression())
			}
			
			override fun visitElement(element: PsiElement) {
				if(!shouldVisit(element)) return
				
				ProgressManager.checkCanceled()
				
				//ignore definition names if necessary
				if(ignoreDefinitionNames && element is ParadoxScriptString && element.isDefinitionName()) return
				//may only resolve to single ParadoxValueSetValueElement (set-flag expression)
				val reference = element.reference ?: return
				if(!reference.canResolveValueSetValue()) return
				
				val resolved = reference.resolve()
				if(resolved !is ParadoxValueSetValueElement) return
				if(!resolved.read) {
					//当确定同一文件中某一名称的参数已被使用时，后续不需要再进行ReferencesSearch
					val statusMap = session.getUserData(statusMapKey)!!
					val used = statusMap[resolved]
					val isUsed = if(used == null) {
						ProgressManager.checkCanceled()
						val r = ReferencesSearch.search(resolved).processQuery {
							ProgressManager.checkCanceled()
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
						registerProblem(element, resolved.name, reference.rangeInElement)
					}
				}
			}
			
			private fun registerProblem(element: PsiElement, name: String, range: TextRange) {
				val message = PlsBundle.message("inspection.script.general.unusedValueSetValue.description", name)
				holder.registerProblem(element, message, ProblemHighlightType.LIKE_UNUSED_SYMBOL, range)
			}
		}
	}
	
	override fun createOptionsPanel(): JComponent {
		return panel {
			//ignoreDefinitionNames
			row {
				checkBox(PlsBundle.message("inspection.script.general.unusedValueSetValue.option.ignoreDefinitionNames"))
					.bindSelected(::ignoreDefinitionNames)
					.applyToComponent { toolTipText = PlsBundle.message("inspection.script.general.unusedValueSetValue.option.ignoreDefinitionNames.tooltip") }
					.actionListener { _, component -> ignoreDefinitionNames = component.isSelected }
			}
		}
	}
}

