package icu.windea.pls.lang.usages

import com.intellij.openapi.application.runReadAction
import com.intellij.openapi.project.Project
import com.intellij.psi.util.parentOfType
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
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList

// com.intellij.usages.impl.rules.JavaMethodGroupRuleProvider
// com.intellij.usages.impl.rules.MethodGroupingRule
// org.jetbrains.kotlin.idea.base.searching.usages.KotlinDeclarationGroupRuleProvider
// org.jetbrains.kotlin.idea.base.searching.usages.KotlinDeclarationGroupingRule

/**
 * 文件结构支持 - 本地化语言环境的用法分组规则。
 */
class ParadoxLocalisationLocaleFileStructureGroupRuleProvider : FileStructureGroupRuleProvider {
    override fun getUsageGroupingRule(project: Project): UsageGroupingRule {
        return ParadoxLocalisationLocaleGroupingRule()
    }
}

private class ParadoxLocalisationLocaleGroupingRule : SingleParentUsageGroupingRule() {
    override fun getParentGroupFor(usage: Usage, targets: Array<out UsageTarget>): UsageGroup? {
        val localisationLocale = getLocalisationLocale(usage) ?: return null
        val name = localisationLocale.name
        return ParadoxLocalisationLocaleGroup(localisationLocale, name)
    }

    private fun getLocalisationLocale(usage: Usage): ParadoxLocalisationLocale? {
        var element = usage.castOrNull<PsiElementUsage>()?.element ?: return null
        if (element.language !== ParadoxLocalisationLanguage) return null
        if (element is ParadoxLocalisationFile) {
            val offset = usage.castOrNull<UsageInfo2UsageAdapter>()?.usageInfo?.navigationOffset
            if (offset != null) {
                element = element.findElementAt(offset) ?: element
            }
        }
        return element.parentOfType<ParadoxLocalisationPropertyList>()?.locale
    }
}

// com.intellij.usages.PsiElementUsageGroupBase
private class ParadoxLocalisationLocaleGroup(
    element: ParadoxLocalisationLocale,
    private val name: String,
) : PsiElementUsageGroupBase<ParadoxLocalisationLocale>(element, ChronicleIcons.Nodes.LocalisationLocale, name.or.anonymous()) {
    override fun equals(other: Any?): Boolean {
        return this === other || other is ParadoxLocalisationLocaleGroup && name == other.name && isSame(other)
    }

    private fun isSame(other: ParadoxLocalisationLocaleGroup): Boolean {
        return runReadAction { isValid && other.isValid && element.manager.areElementsEquivalent(element, other.element) }
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}
