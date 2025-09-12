package icu.windea.pls.lang.settings

import icu.windea.pls.PlsBundle
import icu.windea.pls.core.util.TextAware

interface PlsStrategies {
    /**
     * 本地化的生成策略。
     */
    enum class LocalisationGeneration(override val text: String): TextAware {
        EmptyText(PlsBundle.message("settings.strategy.localisationGeneration0")),
        SpecificText(PlsBundle.message("settings.strategy.localisationGeneration1")),
        FromLocale(PlsBundle.message("settings.strategy.localisationGeneration2")),
        ;
    }

    /**
     * 默认 DIFF 分组的策略。
     */
    enum class DiffGroup(override val text: String):TextAware {
        VsCopy(PlsBundle.message("settings.strategy.diffGroup.0")),
        First(PlsBundle.message("settings.strategy.diffGroup.1")),
        Last(PlsBundle.message("settings.strategy.diffGroup.2")),
        ;
    }

    /**
     * 层级视图的分组策略。
     */
    interface Grouping: TextAware

    /**
     * 事件树的层级视图的分组策略。
     */
    enum class EventTreeGrouping(override val text: String) : Grouping {
        None(PlsBundle.message("settings.strategy.eventTreeGrouping.0")),
        Type(PlsBundle.message("settings.strategy.eventTreeGrouping.1")),
        ;
    }

    /**
     * 科技树的层级视图的分组策略。
     */
    enum class TechTreeGrouping(override val text: String) : Grouping {
        None(PlsBundle.message("settings.strategy.techTreeGrouping.0")),
        Tier(PlsBundle.message("settings.strategy.techTreeGrouping.1")),
        Area(PlsBundle.message("settings.strategy.techTreeGrouping.2")),
        Category(PlsBundle.message("settings.strategy.techTreeGrouping.3")),
        Tier2Area(PlsBundle.message("settings.strategy.techTreeGrouping.4")),
        Tier2Category(PlsBundle.message("settings.strategy.techTreeGrouping.5")),
        Area2Tier(PlsBundle.message("settings.strategy.techTreeGrouping.6")),
        Category2Tier(PlsBundle.message("settings.strategy.techTreeGrouping.7")),
        ;
    }
}
