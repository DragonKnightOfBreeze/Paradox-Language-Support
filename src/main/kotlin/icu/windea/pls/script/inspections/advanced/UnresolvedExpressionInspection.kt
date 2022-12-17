package icu.windea.pls.script.inspections.advanced

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.*
import icu.windea.pls.core.*
import icu.windea.pls.core.handler.ParadoxCwtConfigHandler.resolvePropertyConfigs
import icu.windea.pls.core.handler.ParadoxCwtConfigHandler.resolveValueConfigs
import icu.windea.pls.core.psi.*
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 定义声明中无法解析的表达式的检查。
 */
class UnresolvedExpressionInspection : LocalInspectionTool() {
	@JvmField var checkForPropertyKey = true
	@JvmField var checkForPropertyValue = true
	@JvmField var checkForValue = true
	
	override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
		if(file !is ParadoxScriptFile) return null
		val holder = ProblemsHolder(manager, file, isOnTheFly)
		file.accept(object : ParadoxScriptRecursiveElementWalkingVisitor() {
			override fun visitProperty(element: ParadoxScriptProperty) {
				ProgressManager.checkCanceled()
				run {
					val shouldCheck = checkForPropertyKey
					if(!shouldCheck) return@run
					//skip checking property if property key may contain parameters
					if(element.propertyKey.isParameterAwareExpression()) return
					val definitionElementInfo = element.definitionElementInfo
					if(definitionElementInfo == null || definitionElementInfo.isDefinition || definitionElementInfo.isParameterAware) return
					val matchType = CwtConfigMatchType.INSPECTION
					val configs = resolvePropertyConfigs(element, matchType = matchType)
					val config = configs.firstOrNull()
					if(config == null) {
						val fix = ImportGameOrModDirectoryFix(element)
						val message = PlsBundle.message("script.inspection.advanced.unresolvedExpression.description.1", element.expression)
						holder.registerProblem(element, message, fix)
						return
					}
				}
				super.visitProperty(element)
			}
			
			override fun visitValue(element: ParadoxScriptValue) {
				ProgressManager.checkCanceled()
				run {
					val shouldCheck = when {
						element is ParadoxScriptedVariableReference -> return //skip
						element.isPropertyValue() -> checkForPropertyValue
						element.isBlockValue() -> checkForValue
						else -> return //skip
					}
					if(!shouldCheck) return@run
					//skip checking value if it may contain parameters
					if(element is ParadoxScriptString && element.isParameterAwareExpression()) return
					val definitionElementInfo = element.definitionElementInfo
					if(definitionElementInfo == null || definitionElementInfo.isDefinition || definitionElementInfo.isParameterAware) return
					val matchType = CwtConfigMatchType.INSPECTION
					val configs = resolveValueConfigs(element, matchType = matchType, orDefault = false)
					val config = configs.firstOrNull()
					if(config == null) {
						val expectConfigs = resolveValueConfigs(element, orDefault = true)
						val expect = expectConfigs.mapTo(mutableSetOf()) { it.expression }.joinToString()
						val fix = ImportGameOrModDirectoryFix(element)
						val message = when {
							expect.isEmpty() -> PlsBundle.message("script.inspection.advanced.unresolvedExpression.description.1", element.expression, expect)
							else -> PlsBundle.message("script.inspection.advanced.unresolvedExpression.description.2", element.expression, expect)
						}
						holder.registerProblem(element, message, fix)
						//skip checking children
						return
					}
				}
				super.visitValue(element)
			}
		})
		return holder.resultsArray
	}
	
	override fun createOptionsPanel(): JComponent {
		return panel {
			row {
				checkBox(PlsBundle.message("script.inspection.advanced.unresolvedExpression.option.checkForPropertyKey"))
					.bindSelected(::checkForPropertyKey)
					.actionListener { _, component -> checkForPropertyKey = component.isSelected }
			}
			row {
				checkBox(PlsBundle.message("script.inspection.advanced.unresolvedExpression.option.checkForPropertyValue"))
					.bindSelected(::checkForPropertyValue)
					.actionListener { _, component -> checkForPropertyValue = component.isSelected }
			}
			row {
				checkBox(PlsBundle.message("script.inspection.advanced.unresolvedExpression.option.checkForValue"))
					.bindSelected(::checkForValue)
					.actionListener { _, component -> checkForValue = component.isSelected }
			}
		}
	}
}
