package icu.windea.pls.model.policies

import icu.windea.pls.ChronicleBundle

/**
 * 差异比较使用的默认分组策略。
 */
enum class ParadoxDiffGroupingPolicy(val text: String) {
    Current(ChronicleBundle.message("policy.diffGroup.0")),
    Vanilla(ChronicleBundle.message("policy.diffGroup.1")),
    First(ChronicleBundle.message("policy.diffGroup.2")),
    Last(ChronicleBundle.message("policy.diffGroup.3")),
    ;
}
