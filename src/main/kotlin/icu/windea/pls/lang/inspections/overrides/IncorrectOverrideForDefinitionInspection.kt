package icu.windea.pls.lang.inspections.overrides

import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import icu.windea.pls.PlsBundle
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.fileInfo
import icu.windea.pls.lang.isParameterized
import icu.windea.pls.lang.overrides.ParadoxOverrideService
import icu.windea.pls.lang.overrides.ParadoxOverrideStrategy
import icu.windea.pls.lang.quickfix.navigation.NavigateToOverridingDefinitionsFix
import icu.windea.pls.lang.search.ParadoxDefinitionSearch
import icu.windea.pls.lang.search.selector.definition
import icu.windea.pls.lang.search.selector.selector
import icu.windea.pls.model.ParadoxRootInfo
import icu.windea.pls.script.psi.ParadoxScriptProperty

/**
 * 不正确的对定义的重载的代码检查。
 *
 * 说明：
 * - 如果当前上下文中存在同名同类型的定义，那么就说存在对此定义的重载。
 * - 仅适用于项目中的、作为游戏或模组文件的、非内存非注入的脚本文件。
 * - 仅适用于非参数化的、非匿名的、作为脚本属性的定义。
 * - 基于其使用的覆盖方式进行检查。
 *
 * 参见：[优先级规则](https://windea.icu/Paradox-Language-Support/ref-config-format.html#config-priority)
 *
 * @see ParadoxOverrideStrategy
 * @see ParadoxOverrideService
 */
class IncorrectOverrideForDefinitionInspection : OverrideRelatedInspectionBase() {
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
                val priority = ParadoxOverrideService.getOverrideStrategy(element) ?: return
                if (priority == ParadoxOverrideStrategy.ORDERED) return // skip for ORDERED

                val definitionInfo = element.definitionInfo ?: return
                val name = definitionInfo.name
                val type = definitionInfo.type
                if (name.isEmpty()) return // anonymous -> skipped
                if (name.isParameterized()) return // parameterized -> ignored
                val selector = selector(project, file).definition()
                val results = ParadoxDefinitionSearch.search(name, type, selector).findAll()
                if (results.size < 2) return // no override -> skip
                val firstResult = results.first()
                val firstRootInfo = firstResult.fileInfo?.rootInfo
                if (firstRootInfo !is ParadoxRootInfo.MetadataBased) return
                val rootInfo = fileInfo.rootInfo
                if (rootInfo !is ParadoxRootInfo.MetadataBased) return
                if (firstRootInfo.rootFile == rootInfo.rootFile) return

                // different root file -> incorrect override
                val locationElement = element.propertyKey
                val message = PlsBundle.message("inspection.incorrectOverrideForDefinition.desc", name, priority)
                val fix = NavigateToOverridingDefinitionsFix(name, element, results)
                holder.registerProblem(locationElement, message, fix)
            }
        }
    }
}
