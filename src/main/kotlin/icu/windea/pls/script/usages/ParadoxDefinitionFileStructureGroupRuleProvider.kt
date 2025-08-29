package icu.windea.pls.script.usages

import com.intellij.openapi.project.Project
import com.intellij.usages.UsageViewSettings
import com.intellij.usages.impl.FileStructureGroupRuleProvider
import com.intellij.usages.rules.UsageGroupingRule

/**
 * 文件结构支持 - 定义的使用分组规则。
 */
class ParadoxDefinitionFileStructureGroupRuleProvider : FileStructureGroupRuleProvider {
    //com.intellij.usages.impl.rules.JavaMethodGroupRuleProvider

    override fun getUsageGroupingRule(project: Project): UsageGroupingRule {
        return getUsageGroupingRule(project, UsageViewSettings.instance)
    }

    override fun getUsageGroupingRule(project: Project, usageViewSettings: UsageViewSettings): UsageGroupingRule {
        return ParadoxDefinitionUsageGroupingRule(usageViewSettings)
    }
}
