package icu.windea.pls.lang.usages

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.usages.PsiElementUsageGroupBase
import com.intellij.usages.Usage
import com.intellij.usages.UsageGroup
import com.intellij.usages.UsageInfo2UsageAdapter
import com.intellij.usages.UsageTarget
import com.intellij.usages.impl.FileStructureGroupRuleProvider
import com.intellij.usages.rules.PsiElementUsage
import com.intellij.usages.rules.SingleParentUsageGroupingRule
import com.intellij.usages.rules.UsageGroupingRule
import icu.windea.pls.ChronicleIcons
import icu.windea.pls.core.castOrNull
import icu.windea.pls.core.util.values.anonymous
import icu.windea.pls.core.util.values.or
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.ParadoxDefinitionElement
import icu.windea.pls.lang.select.selectScope
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptFile
import java.util.*

// com.intellij.usages.impl.rules.JavaMethodGroupRuleProvider
// com.intellij.usages.impl.rules.MethodGroupingRule
// org.jetbrains.kotlin.idea.base.searching.usages.KotlinDeclarationGroupRuleProvider
// org.jetbrains.kotlin.idea.base.searching.usages.KotlinDeclarationGroupingRule

/**
 * 文件结构支持 - 定义的用法分组规则。
 */
class ParadoxDefinitionFileStructureGroupRuleProvider : FileStructureGroupRuleProvider {
    override fun getUsageGroupingRule(project: Project): UsageGroupingRule {
        return ParadoxDefinitionUsageGroupingRule()
    }
}

private class ParadoxDefinitionUsageGroupingRule : SingleParentUsageGroupingRule() {
    override fun getParentGroupFor(usage: Usage, targets: Array<out UsageTarget>): UsageGroup? {
        val element = getDefinition(usage) ?: return null
        val info = element.definitionInfo ?: return null
        return ParadoxDefinitionUsageGroup(element, info.name, info.type)
    }

    private fun getDefinition(usage: Usage): ParadoxDefinitionElement? {
        var element = usage.castOrNull<PsiElementUsage>()?.element ?: return null
        if (element.language !== ParadoxScriptLanguage) return null
        if (element is ParadoxScriptFile) {
            val offset = usage.castOrNull<UsageInfo2UsageAdapter>()?.usageInfo?.navigationOffset
            if (offset != null) {
                element = element.findElementAt(offset) ?: element
            }
        }
        return selectScope { element.parentDefinition() }
    }
}

private class ParadoxDefinitionUsageGroup(
    element: ParadoxDefinitionElement,
    private val name: String,
    private val type: String,
) : PsiElementUsageGroupBase<ParadoxDefinitionElement>(element, ChronicleIcons.Nodes.Definition, name.or.anonymous()) {
    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxDefinitionUsageGroup && name == other.name && type == other.type && isSame(other)
    }

    private fun isSame(other: ParadoxDefinitionUsageGroup): Boolean {
        return runReadAction { isValid && other.isValid && element.manager.areElementsEquivalent(element, other.element) }
    }

    override fun hashCode(): Int {
        return Objects.hash(name, type)
    }
}
