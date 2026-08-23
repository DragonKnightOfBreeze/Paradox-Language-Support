package icu.windea.pls.model.policies

import icu.windea.pls.ChronicleBundle

/**
 * 特殊层级视图使用的分组策略。
 */
interface ParadoxHierarchyGroupingPolicy {
    val text: String

    /**
     * 事件树的层级视图使用的分组策略。
     */
    enum class EventTree(override val text: String) : ParadoxHierarchyGroupingPolicy {
        None(ChronicleBundle.message("policy.eventTreeGrouping.0")),
        Type(ChronicleBundle.message("policy.eventTreeGrouping.1")),
        ;
    }

    /**
     * 科技树的层级视图使用的分组策略。
     */
    enum class TechTree(override val text: String) : ParadoxHierarchyGroupingPolicy {
        None(ChronicleBundle.message("policy.techTreeGrouping.0")),
        Tier(ChronicleBundle.message("policy.techTreeGrouping.1")),
        Area(ChronicleBundle.message("policy.techTreeGrouping.2")),
        Category(ChronicleBundle.message("policy.techTreeGrouping.3")),
        Tier2Area(ChronicleBundle.message("policy.techTreeGrouping.4")),
        Tier2Category(ChronicleBundle.message("policy.techTreeGrouping.5")),
        Area2Tier(ChronicleBundle.message("policy.techTreeGrouping.6")),
        Category2Tier(ChronicleBundle.message("policy.techTreeGrouping.7")),
        ;
    }
}
