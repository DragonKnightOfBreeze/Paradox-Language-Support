package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.ep.checker.*
import icu.windea.pls.lang.util.*
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
            
            private fun visitExpressionElement(element: ParadoxScriptExpressionElement) {
                if(!element.isExpression()) return // skip check if element is not a expression
                
                //跳过一些脚本表达式类型
                 if(element is ParadoxScriptBlock) return
                 if(element is ParadoxScriptBoolean) return
                
                //得到完全匹配的CWT规则
                val config = ParadoxExpressionManager.getConfigs(element, orDefault = false).firstOrNull() ?: return
                
                //开始检查
                ParadoxIncorrectExpressionChecker.check(element, config, holder)
            }
        }
    }
}

