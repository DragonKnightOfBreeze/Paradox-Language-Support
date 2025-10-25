package icu.windea.pls.lang.inspections.overrides

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.quickfix.navigation.NavigateToOverridingDefinitionsFix
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 检测对定义的重载的代码检查。
 *
 * - 仅适用于项目中的、作为游戏或模组文件的、非内存非注入的脚本文件。
 * - 仅适用于作为脚本属性的定义。
 */
class OverrideForDefinitionInspection : OverrideRelatedInspectionBase() {
    override fun buildVisitor(holder: ProblemsHolder, isOnTheFly: Boolean): PsiElementVisitor {
        val file = holder.file
        val project = holder.project
        val fileInfo = file.fileInfo
        if (fileInfo == null) return PsiElementVisitor.EMPTY_VISITOR

        return object : PsiElementVisitor() {
            override fun visitElement(element: PsiElement) {
                if (element is ParadoxScriptProperty) visitDefinition(element)
            }

            private fun visitDefinition(element: ParadoxScriptProperty) {
                val definitionInfo = element.definitionInfo ?: return

                val name = definitionInfo.name
                val type = definitionInfo.type
                if (name.isEmpty()) return // anonymous -> skipped
                if (name.isParameterized()) return // parameterized -> ignored
                val selector = selector(project, file).definition()
                val results = ParadoxDefinitionSearch.search(name, type, selector).findAll()
                if (results.size < 2) return // no override -> skip

                val locationElement = element.propertyKey
                val message = PlsBundle.message("inspection.overrideForDefinition.desc", name)
                val fix = NavigateToOverridingDefinitionsFix(name, element, results)
                holder.registerProblem(locationElement, message, fix)
            }
        }
    }
}
