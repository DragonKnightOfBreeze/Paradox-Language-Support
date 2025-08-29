package icu.windea.pls.localisation.usages

import com.intellij.psi.util.parentOfType
import com.intellij.usages.Usage
import com.intellij.usages.UsageGroup
import com.intellij.usages.UsageInfo2UsageAdapter
import com.intellij.usages.UsageTarget
import com.intellij.usages.UsageViewSettings
import com.intellij.usages.rules.PsiElementUsage
import com.intellij.usages.rules.SingleParentUsageGroupingRule
import icu.windea.pls.core.castOrNull
import icu.windea.pls.localisation.ParadoxLocalisationLanguage
import icu.windea.pls.localisation.psi.ParadoxLocalisationFile
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList

class ParadoxLocalisationLocaleGroupingRule(
    private val usageViewSettings: UsageViewSettings
) : SingleParentUsageGroupingRule() {
    //com.intellij.usages.impl.rules.MethodGroupingRule

    @Suppress("UNUSED_PARAMETER")
    private fun getLocalisationLocale(usage: Usage, targets: Array<out UsageTarget>): ParadoxLocalisationLocale? {
        var element = usage.castOrNull<PsiElementUsage>()?.element ?: return null
        if (element.language !is ParadoxLocalisationLanguage) return null
        if (element is ParadoxLocalisationFile) {
            val offset = usage.castOrNull<UsageInfo2UsageAdapter>()?.usageInfo?.navigationOffset
            if (offset != null) {
                element = element.findElementAt(offset) ?: element
            }
        }
        return element.parentOfType<ParadoxLocalisationPropertyList>()?.locale
    }

    override fun getParentGroupFor(usage: Usage, targets: Array<out UsageTarget>): UsageGroup? {
        val localisationLocale = getLocalisationLocale(usage, targets) ?: return null
        return ParadoxLocalisationLocaleGroup(localisationLocale, usageViewSettings)
    }
}
