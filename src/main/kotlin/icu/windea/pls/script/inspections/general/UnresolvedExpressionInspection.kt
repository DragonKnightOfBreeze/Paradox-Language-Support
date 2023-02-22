package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import com.intellij.ui.dsl.builder.*
import icu.windea.pls.*
import icu.windea.pls.config.cwt.config.*
import icu.windea.pls.core.collections.*
import icu.windea.pls.core.quickfix.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*
import javax.swing.*

/**
 * 定义声明中无法解析的表达式的检查。
 */
class UnresolvedExpressionInspection : LocalInspectionTool() {
    @JvmField var showExpectInfo = true
    @JvmField var checkPropertyKey = true
    @JvmField var checkPropertyValue = true
    @JvmField var checkValue = true
    
    override fun checkFile(file: PsiFile, manager: InspectionManager, isOnTheFly: Boolean): Array<ProblemDescriptor>? {
        if(file !is ParadoxScriptFile) return null
        val holder = ProblemsHolder(manager, file, isOnTheFly)
        file.accept(object : PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                val result = when(element) {
                    is ParadoxScriptProperty -> visitProperty(element)
                    is ParadoxScriptValue -> visitValue(element)
                    else -> true
                }
                if(result && element.isExpressionOrMemberContext()) super.visitElement(element)
            }
            
            private fun visitProperty(element: ParadoxScriptProperty): Boolean {
                ProgressManager.checkCanceled()
                val shouldCheck = checkPropertyKey
                if(!shouldCheck) return true
                //skip checking property if property key may contain parameters
                if(element.propertyKey.isParameterAwareExpression()) return false
                val definitionMemberInfo = element.definitionMemberInfo
                if(definitionMemberInfo == null || definitionMemberInfo.isDefinition) return true
                val matchType = CwtConfigMatchType.INSPECTION
                val configs = ParadoxCwtConfigHandler.getPropertyConfigs(element, matchType = matchType)
                val config = configs.firstOrNull()
                if(config == null) {
                    //这里使用合并后的子规则，即使parentProperty可以精确匹配
                    val expect = if(showExpectInfo) {
                        val allConfigs = element.findParentProperty()?.definitionMemberInfo?.getChildConfigs()
                        val allExpressions = if(allConfigs.isNullOrEmpty()) emptySet() else {
                            buildSet {
                                for(c in allConfigs) {
                                    if(c is CwtPropertyConfig) add(c.expression)
                                }
                            }
                        }
                        allExpressions.takeIfNotEmpty()?.joinToString()
                    } else null
                    val message = when {
                        expect == null -> PlsBundle.message("inspection.script.general.unresolvedExpression.description.1.1", element.expression)
                        expect.isNotEmpty() -> PlsBundle.message("inspection.script.general.unresolvedExpression.description.1.2", element.expression, expect)
                        else -> PlsBundle.message("inspection.script.general.unresolvedExpression.description.1.3", element.expression)
                    }
                    holder.registerProblem(element, message)
                    return false
                }
                return true
            }
            
            private fun visitValue(element: ParadoxScriptValue): Boolean {
                ProgressManager.checkCanceled()
                val shouldCheck = when {
                    //also check if element is a scripted_variable
                    //element is ParadoxScriptedVariableReference -> return false
                    element.isPropertyValue() -> checkPropertyValue
                    element.isBlockValue() -> checkValue
                    else -> return false //skip
                }
                if(!shouldCheck) return true
                //skip checking value if it may contain parameters
                if(element is ParadoxScriptString && element.isParameterAwareExpression()) return false
                val definitionMemberInfo = element.definitionMemberInfo
                if(definitionMemberInfo == null || definitionMemberInfo.isDefinition) return true
                val matchType = CwtConfigMatchType.INSPECTION
                val configs = ParadoxCwtConfigHandler.getValueConfigs(element, matchType = matchType, orDefault = false)
                val config = configs.firstOrNull()
                if(config == null) {
                    val expect = if(showExpectInfo) {
                        val allConfigs = ParadoxCwtConfigHandler.getValueConfigs(element, orDefault = true)
                        val allExpressions = if(allConfigs.isEmpty()) emptySet() else {
                            buildSet {
                                for(c in allConfigs) {
                                    add(c.expression)
                                }
                            }
                        }
                        allExpressions.takeIfNotEmpty()?.joinToString()
                    } else null
                    val message = when {
                        expect == null -> PlsBundle.message("inspection.script.general.unresolvedExpression.description.2.1", element.expression)
                        expect.isNotEmpty() -> PlsBundle.message("inspection.script.general.unresolvedExpression.description.2.2", element.expression, expect)
                        else -> PlsBundle.message("inspection.script.general.unresolvedExpression.description.2.3", element.expression)
                    }
                    holder.registerProblem(element, message)
                    //skip checking children
                    return false
                }
                return true
            }
        })
        return holder.resultsArray
    }
    
    override fun createOptionsPanel(): JComponent {
        return panel {
            row {
                checkBox(PlsBundle.message("inspection.script.general.unresolvedExpression.option.showExpectInfo"))
                    .bindSelected(::showExpectInfo)
                    .actionListener { _, component -> showExpectInfo = component.isSelected }
            }
            row {
                checkBox(PlsBundle.message("inspection.script.general.unresolvedExpression.option.checkPropertyKey"))
                    .bindSelected(::checkPropertyKey)
                    .actionListener { _, component -> checkPropertyKey = component.isSelected }
            }
            row {
                checkBox(PlsBundle.message("inspection.script.general.unresolvedExpression.option.checkPropertyValue"))
                    .bindSelected(::checkPropertyValue)
                    .actionListener { _, component -> checkPropertyValue = component.isSelected }
            }
            row {
                checkBox(PlsBundle.message("inspection.script.general.unresolvedExpression.option.checkValue"))
                    .bindSelected(::checkValue)
                    .actionListener { _, component -> checkValue = component.isSelected }
            }
        }
    }
}

