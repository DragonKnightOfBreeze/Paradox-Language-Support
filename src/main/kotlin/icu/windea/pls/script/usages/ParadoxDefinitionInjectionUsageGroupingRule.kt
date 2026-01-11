package icu.windea.pls.script.usages

import com.intellij.usages.Usage
import com.intellij.usages.UsageGroup
import com.intellij.usages.UsageInfo2UsageAdapter
import com.intellij.usages.UsageTarget
import com.intellij.usages.UsageViewSettings
import com.intellij.usages.rules.PsiElementUsage
import com.intellij.usages.rules.SingleParentUsageGroupingRule
import icu.windea.pls.core.castOrNull
import icu.windea.pls.lang.definitionInjectionInfo
import icu.windea.pls.lang.psi.parentDefinitionInjection
import icu.windea.pls.lang.psi.search
import icu.windea.pls.script.ParadoxScriptLanguage
import icu.windea.pls.script.psi.ParadoxScriptFile
import icu.windea.pls.script.psi.ParadoxScriptProperty

class ParadoxDefinitionInjectionUsageGroupingRule(
    private val usageViewSettings: UsageViewSettings
) : SingleParentUsageGroupingRule() {
    // com.intellij.usages.impl.rules.MethodGroupingRule
    // org.jetbrains.kotlin.idea.findUsages.KotlinDeclarationGroupingRule

    override fun getParentGroupFor(usage: Usage, targets: Array<out UsageTarget>): UsageGroup? {
        val element = getDefinitionInjection(usage) ?: return null
        val info = element.definitionInjectionInfo ?: return null
        if (info.target.isNullOrEmpty() || info.type.isNullOrEmpty()) return null
        return ParadoxDefinitionInjectionUsageGroup(element, info.target, info.type, info.project, usageViewSettings)
    }

    private fun getDefinitionInjection(usage: Usage): ParadoxScriptProperty? {
        var element = usage.castOrNull<PsiElementUsage>()?.element ?: return null
        if (element.language !is ParadoxScriptLanguage) return null
        if (element is ParadoxScriptFile) {
            val offset = usage.castOrNull<UsageInfo2UsageAdapter>()?.usageInfo?.navigationOffset
            if (offset != null) {
                element = element.findElementAt(offset) ?: element
            }
        }
        return element.search { parentDefinitionInjection() }
    }
}
