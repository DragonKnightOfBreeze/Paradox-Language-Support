package icu.windea.pls.script.inspections.general

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.*

class IncorrectDefinitionNameInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if(element is ParadoxScriptStringExpressionElement) visitExpressionElement(element)
            }
            
            private fun visitExpressionElement(element: ParadoxScriptStringExpressionElement) {
                ProgressManager.checkCanceled()
                //忽略可能包含参数的表达式
                if(element.isParameterizedExpression()) return
                //仅适用于作为定义名的key或value
                if(!element.isDefinitionRootKeyOrName()) return
                val definition = element.findParentDefinition() ?: return
                val definitionInfo = definition.definitionInfo ?: return
                if(definitionInfo.typeConfig.nameField != null && element is ParadoxScriptPropertyKey) return
                //开始检查（如果定义是匿名的，这里不直接跳过）
                val definitionName = definitionInfo.name
                val prefix = ParadoxDefinitionHandler.getDefinitionNamePrefixOption(definitionInfo)
                if(prefix.isEmpty()) return
                if(definitionName.startsWith(prefix)) return
                val message = PlsBundle.message("inspection.script.general.incorrectDefinitionName.description.1", definitionName, prefix)
                holder.registerProblem(element, message)
            }
        }
    }
}
