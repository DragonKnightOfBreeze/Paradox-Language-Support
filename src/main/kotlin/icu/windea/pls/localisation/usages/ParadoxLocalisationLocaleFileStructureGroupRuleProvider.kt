package icu.windea.pls.localisation.usages

import com.intellij.openapi.project.Project
import com.intellij.usages.UsageViewSettings
import com.intellij.usages.impl.FileStructureGroupRuleProvider
import com.intellij.usages.rules.UsageGroupingRule

/**
 * 文件结构支持 - 本地化语言区域的使用分组规则。
 */
class ParadoxLocalisationLocaleFileStructureGroupRuleProvider : FileStructureGroupRuleProvider {
    //com.intellij.usages.impl.rules.JavaMethodGroupRuleProvider
    //org.jetbrains.kotlin.idea.findUsages.KotlinDeclarationGroupingRule

    override fun getUsageGroupingRule(project: Project): UsageGroupingRule {
        return getUsageGroupingRule(project, UsageViewSettings.instance)
    }

    override fun getUsageGroupingRule(project: Project, usageViewSettings: UsageViewSettings): UsageGroupingRule {
        return ParadoxLocalisationLocaleGroupingRule(usageViewSettings)
    }
}

