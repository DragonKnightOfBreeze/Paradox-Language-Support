package icu.windea.pls.model.policies

import icu.windea.pls.ChronicleBundle

/**
 * 本地化的生成策略。
 */
enum class ParadoxLocalisationGenerationStrategy(val text: String) {
    EmptyText(ChronicleBundle.message("policy.localisationGeneration.0")),
    SpecificText(ChronicleBundle.message("policy.localisationGeneration.1")),
    FromLocale(ChronicleBundle.message("policy.localisationGeneration.2")),
    ;
}
