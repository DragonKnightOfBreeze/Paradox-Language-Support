package icu.windea.pls.lang.inspections.script.bug

import com.intellij.codeInspection.*
import com.intellij.openapi.editor.*
import com.intellij.openapi.progress.*
import com.intellij.psi.*
import icu.windea.pls.*
import icu.windea.pls.lang.*
import icu.windea.pls.lang.quickfix.*
import icu.windea.pls.lang.util.*
import icu.windea.pls.script.psi.*
import org.jetbrains.annotations.*

/**
 * （对于脚本文件）检查是否存在不支持的递归。
 * * 对于每个scripted_variable，检测其值中是否存在递归的scripted_variable引用。
 * * 对于每个scripted_trigger，检测其值中是否存在递归的scripted_trigger调用。
 * * 对于每个scripted_effect，检测其值中是否存在递归的scripted_effect调用。
 */
class UnsupportedRecursionInspection : LocalInspectionTool() {
    //目前仅做检查即可，不需要显示递归的装订线图标
    //在封装变量声明/定义声明级别进行此项检查

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        if (!shouldCheckFile(holder.file)) return PsiElementVisitor.EMPTY_VISITOR

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                ProgressManager.checkCanceled()
                when (element) {
                    is ParadoxScriptScriptedVariable -> visitScriptedVariable(element)
                    is ParadoxScriptProperty -> visitProperty(element)
                }
            }

            private fun visitScriptedVariable(element: ParadoxScriptScriptedVariable) {
                val name = element.name
                if (name.isNullOrEmpty()) return

                val recursions = mutableSetOf<PsiElement>()
                ParadoxRecursionManager.isRecursiveScriptedVariable(element, recursions)
                if (recursions.isEmpty()) return
                val message = PlsBundle.message("inspection.script.unsupportedRecursion.desc.1")
                val location = element.scriptedVariableName
                holder.registerProblem(location, message, NavigateToRecursionFix(name, element, recursions))
            }

            @Suppress("KotlinConstantConditions")
            private fun visitProperty(element: ParadoxScriptProperty) {
                val definitionInfo = element.definitionInfo ?: return
                if (definitionInfo.name.isEmpty()) return //ignored for anonymous definitions
                if (definitionInfo.type != "scripted_trigger" && definitionInfo.type != "scripted_effect") return

                val recursions = mutableSetOf<PsiElement>()
                ParadoxRecursionManager.isRecursiveDefinition(element, recursions) { _, re -> ParadoxPsiManager.isInvocationReference(element, re) }
                if (recursions.isEmpty()) return
                val message = when {
                    definitionInfo.type == "scripted_trigger" -> PlsBundle.message("inspection.script.unsupportedRecursion.desc.2.1")
                    definitionInfo.type == "scripted_effect" -> PlsBundle.message("inspection.script.unsupportedRecursion.desc.2.2")
                    else -> return
                }
                val location = element.propertyKey
                holder.registerProblem(location, message, NavigateToRecursionFix(definitionInfo.name, element, recursions))
            }
        }
    }

    private fun shouldCheckFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    private class NavigateToRecursionFix(key: String, target: PsiElement, recursions: Collection<PsiElement>) : NavigateToFix(key, target, recursions) {
        override fun getText() = PlsBundle.message("inspection.script.unsupportedRecursion.fix.1")

        override fun getPopupTitle(editor: Editor) = PlsBundle.message("inspection.script.unsupportedRecursion.fix.1.popup.title", key)

        override fun getPopupText(editor: Editor, value: PsiElement): @Nls String {
            val lineNumber = editor.document.getLineNumber(value.textOffset)
            return PlsBundle.message("inspection.fix.navigate.popup.text.2", key, lineNumber)
        }
    }
}
