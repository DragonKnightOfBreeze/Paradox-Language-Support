package icu.windea.pls.lang.settings

import icu.windea.pls.ChronicleBundle

interface ChronicleSettingsStrategies {
    /**
     * 本地化的生成策略。
     */
    enum class LocalisationGeneration(override val text: String) : ChronicleSettingsStrategy {
        EmptyText(ChronicleBundle.message("settings.strategy.localisationGeneration.0")),
        SpecificText(ChronicleBundle.message("settings.strategy.localisationGeneration.1")),
        FromLocale(ChronicleBundle.message("settings.strategy.localisationGeneration.2")),
        ;
    }

    /**
     * 默认差异比较分组的策略。
     */
    enum class DiffGroup(override val text: String) : ChronicleSettingsStrategy {
        Current(ChronicleBundle.message("settings.strategy.diffGroup.0")),
        Vanilla(ChronicleBundle.message("settings.strategy.diffGroup.1")),
        First(ChronicleBundle.message("settings.strategy.diffGroup.2")),
        Last(ChronicleBundle.message("settings.strategy.diffGroup.3")),
        ;
    }

    /**
     * 事件树的层级视图的分组策略。
     */
    enum class EventTreeGrouping(override val text: String) : ChronicleSettingsStrategy {
        None(ChronicleBundle.message("settings.strategy.eventTreeGrouping.0")),
        Type(ChronicleBundle.message("settings.strategy.eventTreeGrouping.1")),
        ;
    }

    /**
     * 科技树的层级视图的分组策略。
     */
    enum class TechTreeGrouping(override val text: String) : ChronicleSettingsStrategy {
        None(ChronicleBundle.message("settings.strategy.techTreeGrouping.0")),
        Tier(ChronicleBundle.message("settings.strategy.techTreeGrouping.1")),
        Area(ChronicleBundle.message("settings.strategy.techTreeGrouping.2")),
        Category(ChronicleBundle.message("settings.strategy.techTreeGrouping.3")),
        Tier2Area(ChronicleBundle.message("settings.strategy.techTreeGrouping.4")),
        Tier2Category(ChronicleBundle.message("settings.strategy.techTreeGrouping.5")),
        Area2Tier(ChronicleBundle.message("settings.strategy.techTreeGrouping.6")),
        Category2Tier(ChronicleBundle.message("settings.strategy.techTreeGrouping.7")),
        ;
    }
}
