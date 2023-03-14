package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.config.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

/**
 * 对应的CWT规则有多个且存在冲突的表达式的检测。
 */
class ConflictingResolvedExpressionInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxScriptBlock) visitBlock(element)
            }
            
            private fun visitBlock(element: ParadoxScriptBlock) {
                ProgressManager.checkCanceled()
                //skip checking property if its property key may contain parameters
                //position: (in property) property key / (standalone) left curly brace
                val property = element.parent
                    ?.castOrNull<ParadoxScriptProperty>()
                val position = property?.propertyKey
                    ?.also { if(it.isParameterAwareExpression()) return }
                    ?: element.findChild(ParadoxScriptElementTypes.LEFT_BRACE)
                    ?: return
                val expression = property?.expression ?: element.expression
                val configs = ParadoxConfigHandler.getConfigs(element, allowDefinition = true)
                doCheck(element, position, configs, expression)
            }
            
            private fun doCheck(element: ParadoxScriptMemberElement, position: PsiElement, configs: List<CwtDataConfig<*>>, expression: String) {
                if(skipCheck(element, configs)) return
                val isKey = position is ParadoxScriptPropertyKey
                val description = when {
                    isKey -> PlsBundle.message("inspection.script.general.conflictingResolvedExpression.description.1", expression)
                    else -> PlsBundle.message("inspection.script.general.conflictingResolvedExpression.description.2", expression)
                }
                holder.registerProblem(position, description)
            }
            
            private fun skipCheck(element: ParadoxScriptMemberElement, configs: List<CwtDataConfig<*>>): Boolean {
                //子句不为空且可以精确匹配多个子句规则时，适用此检查
                if(configs.isEmpty()) return true
                if(configs.size == 1) return true
                if(element is ParadoxScriptFile && element.block?.isEmpty == true) return true
                if(element is ParadoxScriptBlock && element.isEmpty) return true
                return false
            }
        }
    }
}