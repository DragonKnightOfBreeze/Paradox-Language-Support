package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import icu.windea.pls.core.expression.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.checker.*
import icu.windea.pls.script.psi.*

/**
 * 不正确的表达式的检查。
 */
class IncorrectExpressionInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxScriptExpressionElement) visitExpressionElement(element)
            }
            
            //TODO 提取成扩展点并加入一些极个别情况下的检查
            
            private fun visitExpressionElement(element: ParadoxScriptExpressionElement) {
                if(!element.isExpression()) return // skip check if element is not a expression
                
                //跳过一些脚本表达式类型
                 if(element is ParadoxScriptBlock) return
                 if(element is ParadoxScriptBoolean) return
                
                //得到完全匹配的CWT规则
                val config = CwtConfigHandler.getConfigs(element, orDefault = false).firstOrNull() ?: return
                val configExpression = config.expression
                val dataType = configExpression.type
                when {
                    dataType == CwtDataType.Int -> {
                        val expression = element.expression ?: return
                        val (min, max) = configExpression.extraValue<Tuple2<Int?, Int?>>() ?: return
                        val min0 = min ?: Int.MIN_VALUE
                        val max0 = max ?: Int.MAX_VALUE
                        val value = element.intValue() ?: return
                        if(value !in min0..max0) {
                            val min1 = min?.toString() ?: "-inf"
                            val max1 = max?.toString() ?: "inf"
                            holder.registerProblem(element, PlsBundle.message("inspection.script.general.incorrectExpression.description.1", expression, min1, max1, value))
                        }
                    }
                    dataType == CwtDataType.Float -> {
                        val expression = element.expression ?: return
                        val (min, max) = configExpression.extraValue<Tuple2<Float?, Float?>>() ?: return
                        val min0 = min ?: Float.MIN_VALUE
                        val max0 = max ?: Float.MAX_VALUE
                        val value = element.floatValue() ?: return
                        if(value !in min0..max0) {
                            val min1 = min?.toString() ?: "-inf"
                            val max1 = max?.toString() ?: "inf"
                            holder.registerProblem(element, PlsBundle.message("inspection.script.general.incorrectExpression.description.1", expression, min1, max1, value))
                        }
                    }
                    dataType == CwtDataType.ColorField -> {
                        val expression = element.expression ?: return
                        if(element !is ParadoxScriptColor) return
                        val expectedColorType = configExpression.value ?: return
                        val colorType = element.colorType
                        if(colorType == expectedColorType) return
                        val message = PlsBundle.message("inspection.script.general.incorrectExpression.description.3", expression, expectedColorType, colorType)
                        holder.registerProblem(element, message)
                    }
                    dataType == CwtDataType.Scope -> {
                        if(element !is ParadoxScriptStringExpressionElement) return
                        val expectedScope = configExpression.value ?: return
                        val text = element.text
                        val textRange = TextRange.create(0, text.length)
                        val configGroup = config.info.configGroup
                        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(text, textRange, configGroup) ?: return
                        val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = true) ?: return
                        val parentScopeContext = ParadoxScopeHandler.getScopeContext(memberElement) ?: return
                        val scopeContext = ParadoxScopeHandler.getScopeContext(element, scopeFieldExpression, parentScopeContext)
                        if(ParadoxScopeHandler.matchesScope(scopeContext, expectedScope, configGroup)) return
                        val expression = element.expression ?: return
                        val message = PlsBundle.message("inspection.script.general.incorrectExpression.description.5", expression, expectedScope, scopeContext.scope.id)
                        holder.registerProblem(element, message)
                    }
                    dataType == CwtDataType.ScopeGroup -> {
                        if(element !is ParadoxScriptStringExpressionElement) return
                        val expectedScopeGroup = configExpression.value ?: return
                        val text = element.text
                        val textRange = TextRange.create(0, text.length)
                        val configGroup = config.info.configGroup
                        val scopeFieldExpression = ParadoxScopeFieldExpression.resolve(text, textRange, configGroup) ?: return
                        val memberElement = element.parentOfType<ParadoxScriptMemberElement>(withSelf = true) ?: return
                        val parentScopeContext = ParadoxScopeHandler.getScopeContext(memberElement) ?: return
                        val scopeContext = ParadoxScopeHandler.getScopeContext(element, scopeFieldExpression, parentScopeContext)
                        if(ParadoxScopeHandler.matchesScopeGroup(scopeContext, expectedScopeGroup, configGroup)) return
                        val expression = element.expression ?: return
                        val message = PlsBundle.message("inspection.script.general.incorrectExpression.description.6", expression, expectedScopeGroup, scopeContext.scope.id)
                        holder.registerProblem(element, message)
                    }
                    dataType == CwtDataType.IntValueField -> {
                        //TODO
                    }
                    dataType == CwtDataType.IntVariableField -> {
                        //TODO
                    }
                    else -> pass()
                }
                
                ParadoxIncorrectExpressionChecker.check(element, config, holder)
            }
        }
    }
}

