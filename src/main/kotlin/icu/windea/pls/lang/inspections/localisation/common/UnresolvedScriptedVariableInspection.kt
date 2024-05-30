package icu.windea.pls.lang.inspections.localisation.common

import com.intellij.codeInsight.intention.*
import com.intellij.codeInspection.*
import com.intellij.openapi.command.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.openapi.project.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.psi.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.lang.refactoring.actions.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.localisation.psi.*
import icu.windea.pls.script.psi.*

/**
 * 无法解析的封装变量引用的检查。
 *
 * 提供快速修复：
 * * 声明全局封装变量（在`common/scripted_variables`目录下的某一文件中）
 * * 导入游戏目录或模组目录
 */
class UnresolvedScriptedVariableInspection : LocalInspectionTool() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                if(element is ParadoxLocalisationScriptedVariableReference) visitScriptedVariableReference(element)
            }
            
            private fun visitScriptedVariableReference(element: ParadoxLocalisationScriptedVariableReference) {
                val name = element.name ?: return
                if(name.isParameterized()) return //skip if name is parameterized
                val reference = element.reference ?: return
                if(reference.resolve() != null) return
                val quickFixes = listOf(
                    IntroduceLocalVariableFix(name, element),
                    IntroduceGlobalVariableFix(name, element)
                )
                val message = PlsBundle.message("inspection.localisation.unresolvedScriptedVariable.description", name)
                holder.registerProblem(element, message, ProblemHighlightType.LIKE_UNKNOWN_SYMBOL, *quickFixes.toTypedArray())
            }
        }
    }
    
    private class IntroduceGlobalVariableFix(
        private val variableName: String,
        element: ParadoxScriptedVariableReference,
    ) : LocalQuickFixAndIntentionActionOnPsiElement(element), PriorityAction {
        override fun getPriority() = PriorityAction.Priority.HIGH
        
        override fun getText() = PlsBundle.message("inspection.localisation.unresolvedScriptedVariable.fix.2", variableName)
        
        override fun getFamilyName() = text
        
        override fun invoke(project: Project, file: PsiFile, editor: Editor?, startElement: PsiElement, endElement: PsiElement) {
            //打开对话框
            val virtualFile = file.virtualFile ?: return
            val scriptedVariablesDirectory = ParadoxFileHandler.getScriptedVariablesDirectory(virtualFile) ?: return //不期望的结果
            val dialog = IntroduceGlobalScriptedVariableDialog(project, scriptedVariablesDirectory, variableName, "0")
            if(!dialog.showAndGet()) return //取消
            
            //声明对应名字的封装变量，默认值给0并选中
            val variableNameToUse = dialog.variableName
            val variableValue = dialog.variableValue
            val targetFile = dialog.file.toPsiFile(project) ?: return //不期望的结果
            if(targetFile !is ParadoxScriptFile) return
            val command = Runnable {
                ParadoxPsiManager.introduceGlobalScriptedVariable(variableNameToUse, variableValue, targetFile, project)
                
                val targetDocument = PsiDocumentManager.getInstance(project).getDocument(targetFile)
                if(targetDocument != null) PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(targetDocument) //提交文档更改
                
                //不移动光标
            }
            WriteCommandAction.runWriteCommandAction(project, PlsBundle.message("localisation.command.introduceGlobalScriptedVariable.name"), null, command, targetFile)
        }
        
        override fun startInWriteAction() = false
        
        override fun availableInBatchMode() = false
    }
}

