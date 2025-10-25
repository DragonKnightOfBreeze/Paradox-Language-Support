package icu.windea.pls.lang.inspections.script.bug

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxPsiMatcher
import icu.windea.pls.lang.quickfix.navigation.NavigateToRecursionsFix
import icu.windea.pls.lang.selectRootFile
import icu.windea.pls.lang.util.ParadoxRecursionManager
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable

/**
 * （对于脚本文件）检查是否存在不支持的递归。
 * - 对于每个scripted_variable，检测其值中是否存在递归的scripted_variable引用。
 * - 对于每个scripted_trigger，检测其值中是否存在递归的scripted_trigger调用。
 * - 对于每个scripted_effect，检测其值中是否存在递归的scripted_effect调用。
 */
class UnsupportedRecursionInspection : LocalInspectionTool() {
    // 目前仅做检查即可，不需要显示递归的装订线图标
    // 在封装变量声明/定义声明级别进行此项检查

    override fun isAvailableForFile(file: PsiFile): Boolean {
        if (selectRootFile(file) == null) return false
        return true
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
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
                holder.registerProblem(location, message, NavigateToRecursionsFix(name, element, recursions))
            }

            @Suppress("KotlinConstantConditions")
            private fun visitProperty(element: ParadoxScriptProperty) {
                val definitionInfo = element.definitionInfo ?: return
                if (definitionInfo.name.isEmpty()) return // ignored for anonymous definitions
                if (definitionInfo.type != "scripted_trigger" && definitionInfo.type != "scripted_effect") return

                val recursions = mutableSetOf<PsiElement>()
                ParadoxRecursionManager.isRecursiveDefinition(element, recursions) { _, re -> ParadoxPsiMatcher.isInvocationReference(element, re) }
                if (recursions.isEmpty()) return
                val message = when {
                    definitionInfo.type == "scripted_trigger" -> PlsBundle.message("inspection.script.unsupportedRecursion.desc.2.1")
                    definitionInfo.type == "scripted_effect" -> PlsBundle.message("inspection.script.unsupportedRecursion.desc.2.2")
                    else -> return
                }
                val location = element.propertyKey
                holder.registerProblem(location, message, NavigateToRecursionsFix(definitionInfo.name, element, recursions))
            }
        }
    }
}
