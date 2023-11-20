package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.util.*
import com.intellij.psi.*
import com.intellij.psi.util.*
import icu.windea.pls.*
import icu.windea.pls.config.expression.*
import icu.windea.pls.core.*
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
            
            private fun visitExpressionElement(element: ParadoxScriptExpressionElement) {
                if(!element.isExpression()) return // skip check if element is not a expression
                
                //跳过一些脚本表达式类型
                 if(element is ParadoxScriptBlock) return
                 if(element is ParadoxScriptBoolean) return
                
                //得到完全匹配的CWT规则
                val config = CwtConfigHandler.getConfigs(element, orDefault = false).firstOrNull() ?: return
                
                //开始检查
                ParadoxIncorrectExpressionChecker.check(element, config, holder)
            }
        }
    }
}

