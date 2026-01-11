package icu.windea.pls.script.usages

import com.intellij.usages.Usage
import com.intellij.usages.UsageGroup
import com.intellij.usages.UsageInfo2UsageAdapter
import com.intellij.usages.UsageTarget
import com.intellij.usages.UsageViewSettings
import com.intellij.usages.rules.PsiElementUsage
import com.intellij.usages.rules.SingleParentUsageGroupingRule
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.definitionInfo
import icu.windea.pls.lang.psi.parentDefinition
import icu.windea.pls.lang.psi.search
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.psi.ParadoxScriptFile

class ParadoxDefinitionUsageGroupingRule(
    private val usageViewSettings: UsageViewSettings
) : SingleParentUsageGroupingRule() {
    // com.intellij.usages.impl.rules.MethodGroupingRule
    // org.jetbrains.kotlin.idea.findUsages.KotlinDeclarationGroupingRule

    override fun getParentGroupFor(usage: Usage, targets: Array<out UsageTarget>): UsageGroup? {
        val element = getDefinition(usage) ?: return null
        val info = element.definitionInfo ?: return null
        return ParadoxDefinitionUsageGroup(element, info.name, info.type, info.project, usageViewSettings)
    }

    private fun getDefinition(usage: Usage): ParadoxScriptDefinitionElement? {
        var element = usage.castOrNull<PsiElementUsage>()?.element ?: return null
        if (element.language !is ParadoxScriptLanguage) return null
        if (element is ParadoxScriptFile) {
            val offset = usage.castOrNull<UsageInfo2UsageAdapter>()?.usageInfo?.navigationOffset
            if (offset != null) {
                element = element.findElementAt(offset) ?: element
            }
        }
        return element.search { parentDefinition() }
    }
}
