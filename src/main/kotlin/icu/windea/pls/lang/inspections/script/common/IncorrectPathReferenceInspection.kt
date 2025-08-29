package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.config.CwtDataTypeGroups
import icu.windea.pls.config.CwtDataTypes
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxExpressionManager
import icu.windea.pls.lang.util.ParadoxFileManager
import icu.windea.pls.script.psi.ParadoxScriptString

class IncorrectPathReferenceInspection : LocalInspectionTool() {
    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptString) visitExpressionElement(element)
            }

            private fun visitExpressionElement(element: ParadoxScriptString) {
                //忽略可能包含参数的表达式
                if (element.text.isParameterized()) return
                //得到完全匹配的CWT规则
                val config = ParadoxExpressionManager.getConfigs(element, orDefault = false).firstOrNull() ?: return
                val configExpression = config.configExpression
                val dataType = configExpression.type
                if (dataType == CwtDataTypes.AbsoluteFilePath) return
                if (dataType !in CwtDataTypeGroups.PathReference) return
                val fileExtensions = ParadoxFileManager.getFileExtensionOptionValues(config)
                if (fileExtensions.isEmpty()) return
                val value = element.value
                if (fileExtensions.any { value.endsWith(it, true) }) return
                val extensionsString = fileExtensions.joinToString()
                val message = PlsBundle.message("inspection.script.incorrectPathReference.desc.1", value, extensionsString)
                holder.registerProblem(element, message)
            }
        }
    }
}
