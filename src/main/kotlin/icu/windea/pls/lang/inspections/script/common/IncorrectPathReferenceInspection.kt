package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.config.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*

class IncorrectPathReferenceInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxScriptString) visitExpressionElement(element)
            }
            
            private fun visitExpressionElement(element: ParadoxScriptString) {
                ProgressManager.checkCanceled()
                //忽略可能包含参数的表达式
                if(element.text.isParameterized()) return
                //得到完全匹配的CWT规则
                val config = ParadoxExpressionHandler.getConfigs(element, orDefault = false).firstOrNull() ?: return
                val configExpression = config.expression
                val dataType = configExpression.type
                if(dataType == CwtDataTypes.AbsoluteFilePath) return
                if(dataType !in CwtDataTypeGroups.PathReference) return
                val fileExtensions = ParadoxFilePathHandler.getFileExtensionOptionValues(config)
                if(fileExtensions.isEmpty()) return
                val value = element.value
                if(fileExtensions.any { value.endsWith(it, true) }) return
                val extensionsString = fileExtensions.joinToString()
                val message = PlsBundle.message("inspection.script.incorrectPathReference.description.1", value, extensionsString)
                holder.registerProblem(element, message)
            }
        }
    }
}
