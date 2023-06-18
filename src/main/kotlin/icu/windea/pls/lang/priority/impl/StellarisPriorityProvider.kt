package icu.windea.pls.lang.priority.impl

import icu.windea.pls.core.annotations.*
import icu.windea.pls.core.search.*
import icu.windea.pls.lang.model.*
import icu.windea.pls.lang.priority.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisPriorityProvider : ParadoxPriorityProvider() {
    val FIOS_PATHS = listOf(
        "common/component_sets",
        "common/component_templates",
        "common/event_chains",
        "common/global_ship_designs",
        "common/relics",
        "common/scripted_variables",
        "common/section_templates",
        "common/ship_behaviors",
        "common/solar_system_initializers",
        "common/special_projects",
        "common/star_classes",
        "common/start_screen_messages",
        "common/static_modifiers",
        "common/strategic_resources",
        "events"
    )
    val ORDERED_PATHS = listOf(
        "common/on_actions"
    )
    
    override fun getPriority(target: Any): ParadoxPriority? {
        val filePath = getFilePath(target) ?: return null
        return when {
            filePath in FIOS_PATHS -> ParadoxPriority.FIOS
            filePath in ORDERED_PATHS -> ParadoxPriority.ORDERED
            else -> null
        }
    }
    
    override fun getPriority(searchParameters: ParadoxSearchParameters<*>): ParadoxPriority? {
        val filePath = getFilePath(searchParameters) ?: return null
        return when {
            filePath in FIOS_PATHS -> ParadoxPriority.FIOS
            filePath in ORDERED_PATHS -> ParadoxPriority.ORDERED
            else -> null
        }
    }
}
