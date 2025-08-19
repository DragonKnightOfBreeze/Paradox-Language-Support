package icu.windea.pls.script.usages

import com.intellij.usages.*
import com.intellij.usages.rules.*
import icu.windea.pls.core.*
import icu.windea.pls.lang.*
import icu.windea.pls.script.psi.ParadoxScriptDefinitionElement
import icu.windea.pls.script.*
import icu.windea.pls.script.psi.*

class ParadoxDefinitionUsageGroupingRule(
    private val usageViewSettings: UsageViewSettings
) : SingleParentUsageGroupingRule() {
    //com.intellij.usages.impl.rules.MethodGroupingRule
    //org.jetbrains.kotlin.idea.findUsages.KotlinDeclarationGroupingRule

    @Suppress("UNUSED_PARAMETER")
    private fun getDefinition(usage: Usage, targets: Array<out UsageTarget>): ParadoxScriptDefinitionElement? {
        var element = usage.castOrNull<PsiElementUsage>()?.element ?: return null
        if (element.language !is ParadoxScriptLanguage) return null
        if (element is ParadoxScriptFile) {
            val offset = usage.castOrNull<UsageInfo2UsageAdapter>()?.usageInfo?.navigationOffset
            if (offset != null) {
                element = element.findElementAt(offset) ?: element
            }
        }
        return element.findParentDefinition()
    }

    override fun getParentGroupFor(usage: Usage, targets: Array<out UsageTarget>): UsageGroup? {
        val definition = getDefinition(usage, targets) ?: return null
        val definitionInfo = definition.definitionInfo ?: return null
        return ParadoxDefinitionUsageGroup(definition, definitionInfo, usageViewSettings)
    }
}
