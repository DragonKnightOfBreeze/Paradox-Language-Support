package icu.windea.pls.lang.inspections.script.common

import com.intellij.codeInspection.LocalInspectionTool
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import com.intellij.psi.PsiFile
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.psi.ParadoxPsiMatcher
import icu.windea.pls.lang.quickfix.navigation.NavigateToRecursionsFix
import icu.windea.pls.lang.util.ParadoxRecursionManager
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * （对于脚本文件）检查是否存在不支持的递归。
 * - 对于每个scripted_variable，检测其值中是否存在递归的scripted_variable引用。
 * - 对于每个scripted_trigger，检测其值中是否存在递归的scripted_trigger调用。
 * - 对于每个scripted_effect，检测其值中是否存在递归的scripted_effect调用。
 */
class UnsupportedRecursionInspection : LocalInspectionTool(), DumbAware {
    // 目前仅做检查即可，不需要显示递归的装订线图标
    // 在封装变量声明/定义声明级别进行此项检查

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求是符合条件的脚本文件
        return ParadoxPsiFileMatcher.isScriptFile(file, smart = true, injectable = true)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxScriptVisitor() {
            override fun visitScriptedVariable(element: ParadoxScriptScriptedVariable) {
                ProgressManager.checkCanceled()
                val name = element.name
                if (name.isNullOrEmpty()) return

                val recursions = mutableSetOf<PsiElement>()
                ParadoxRecursionManager.isRecursiveScriptedVariable(element, recursions)
                if (recursions.isEmpty()) return
                val description = PlsBundle.message("inspection.script.unsupportedRecursion.desc.1")
                val location = element.scriptedVariableName
                holder.registerProblem(location, description, NavigateToRecursionsFix(name, element, recursions))
            }

            @Suppress("KotlinConstantConditions")
            override fun visitProperty(element: ParadoxScriptProperty) {
                ProgressManager.checkCanceled()
                val definitionInfo = element.definitionInfo ?: return
                if (definitionInfo.name.isEmpty()) return // ignored for anonymous definitions
                if (definitionInfo.type != "scripted_trigger" && definitionInfo.type != "scripted_effect") return

                val recursions = mutableSetOf<PsiElement>()
                ParadoxRecursionManager.isRecursiveDefinition(element, recursions) { _, re -> ParadoxPsiMatcher.isInvocationReference(element, re) }
                if (recursions.isEmpty()) return
                val description = when {
                    definitionInfo.type == "scripted_trigger" -> PlsBundle.message("inspection.script.unsupportedRecursion.desc.2")
                    definitionInfo.type == "scripted_effect" -> PlsBundle.message("inspection.script.unsupportedRecursion.desc.3")
                    else -> return
                }
                val location = element.propertyKey
                holder.registerProblem(location, description, NavigateToRecursionsFix(definitionInfo.name, element, recursions))
            }
        }
    }
}
