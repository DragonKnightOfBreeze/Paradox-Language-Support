package icu.windea.pls.lang.settings

import icu.windea.pls.*

interface ParadoxStrategies {
    /**
     * 生成本地化的策略。
     */
    enum class LocalisationGeneration(val text:String) {
        EmptyText(PlsBundle.message("settings.strategy.localisationGeneration0")),
        SpecificText(PlsBundle.message("settings.strategy.localisationGeneration1")),
        FromLocale(PlsBundle.message("settings.strategy.localisationGeneration2")),
        ;

        override fun toString() = text
    }

    /**
     * 默认DIFF分组的策略。
     */
    enum class DiffGroup(val text: String) {
        VsCopy(PlsBundle.message("settings.strategy.diffGroup.0")),
        First(PlsBundle.message("settings.strategy.diffGroup.1")),
        Last(PlsBundle.message("settings.strategy.diffGroup.2")),
        ;

        override fun toString() = text
    }

    /**
     * 事件树的层级视图的分组策略。
     */
    enum class EventTreeGrouping(val text: String) {
        None(PlsBundle.message("settings.strategy.eventTreeGrouping.0")),
        Attribute(PlsBundle.message("settings.strategy.eventTreeGrouping.1")),
        Type(PlsBundle.message("settings.strategy.eventTreeGrouping.2")),
        ;

        override fun toString() = text
    }

    /**
     * 科技树的层级视图的分组策略。
     */
    enum class TechTreeGrouping(val text: String) {
        None(PlsBundle.message("settings.strategy.techTreeGrouping.0")),
        Attribute(PlsBundle.message("settings.strategy.techTreeGrouping.1")),
        Tier(PlsBundle.message("settings.strategy.techTreeGrouping.2")),
        Area(PlsBundle.message("settings.strategy.techTreeGrouping.3")),
        Category(PlsBundle.message("settings.strategy.techTreeGrouping.4")),
        Area2Category(PlsBundle.message("settings.strategy.techTreeGrouping.5")),
        ;

        override fun toString() = text
    }
}
