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
import icu.windea.pls.lang.fixes.navigation.NavigateToRecursionsFix
import icu.windea.pls.lang.psi.ParadoxPsiFileMatcher
import icu.windea.pls.lang.psi.ParadoxPsiMatcher
import icu.windea.pls.lang.util.ParadoxRecursionManager
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * （脚本文件中的）不支持的递归的代码检查。
 *
 * 规则如下：
 * - 不支持递归的封装变量（scripted variable）引用。
 * - 不支持递归的封装触发器（scripted trigger）调用。
 * - 不支持递归的封装效果（scripted effect）调用。
 */
class UnsupportedRecursionInspection : LocalInspectionTool(), DumbAware {
    // 目前仅做检查即可，不需要显示递归的装订线图标
    // 在封装变量声明/定义声明级别进行此项检查

    override fun isAvailableForFile(file: PsiFile): Boolean {
        // 要求是可接受的脚本文件
        return ParadoxPsiFileMatcher.isScriptFile(file, injectable = true)
    }

    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxScriptVisitor() {
            override fun visitScriptedVariable(element: ParadoxScriptScriptedVariable) {
                ProgressManager.checkCanceled()
                val name = element.name
                if (name.isNullOrEmpty()) return

                val recursions = mutableSetOf<PsiElement>()
                ParadoxRecursionManager.checkScriptedVariable(element, recursions)
                if (recursions.isEmpty()) return
                val description = PlsBundle.message("inspection.script.unsupportedRecursion.desc.1")
                val location = element.scriptedVariableName
                holder.registerProblem(location, description, NavigateToRecursionsFix(name, element, recursions))
            }

            override fun visitProperty(element: ParadoxScriptProperty) {
                ProgressManager.checkCanceled()
                val definitionInfo = element.definitionInfo ?: return
                if (definitionInfo.name.isEmpty()) return // ignored for anonymous definitions
                if (definitionInfo.type != "scripted_trigger" && definitionInfo.type != "scripted_effect") return

                val recursions = mutableSetOf<PsiElement>()
                ParadoxRecursionManager.checkDefinition(element, recursions) { _, re -> ParadoxPsiMatcher.isDefinitionCall(element, re) }
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
