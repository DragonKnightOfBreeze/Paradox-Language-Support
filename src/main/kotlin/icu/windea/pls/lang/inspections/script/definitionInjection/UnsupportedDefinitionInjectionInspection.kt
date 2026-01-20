package icu.windea.pls.lang.inspections.script.definitionInjection

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.openapi.progress.ProgressManager
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.PlsBundle
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.lang.util.ParadoxDefinitionInjectionManager
import icu.windea.pls.script.psi.ParadoxScriptProperty
import icu.windea.pls.script.psi.ParadoxScriptVisitor

/**
 * 检查是否在不支持的地方使用了定义注入。
 *
 * - 必须存在可以匹配的定义类型（在脚本文件的顶层声明定义，使用类型键作为定义的名字，且不存在类型键前缀）。
 * - TODO 2.1.1+ 基于 Wiki 和官方开发日志，进行更加严格的检查。
 */
class UnsupportedDefinitionInjectionInspection : DefinitionInjectionInspectionBase()/*, DumbAware*/ {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        return object : ParadoxScriptVisitor() {
            override fun visitProperty(element: ParadoxScriptProperty) {
                ProgressManager.checkCanceled()
                val definitionInjectionInfo = ParadoxDefinitionInjectionManager.getInfo(element) ?: return
                if (definitionInjectionInfo.target.isNullOrEmpty()) return // considered "incorrect"
                if (definitionInjectionInfo.type.isNotNullOrEmpty()) return
                if (definitionInjectionInfo.typeConfig != null) return
                holder.registerProblem(element.propertyKey, PlsBundle.message("inspection.script.unsupportedDefinitionInjectionUsage.desc.1"))
            }
        }
    }
}
