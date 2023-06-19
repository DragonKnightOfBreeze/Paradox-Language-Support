package icu.windea.pls.lang.priority.impl

import icu.windea.pls.core.annotations.*
import icu.windea.pls.lang.model.*

@WithGameType(ParadoxGameType.Stellaris)
class StellarisPriorityProvider : ParadoxFilePathBasedPriorityProvider() {
    override val FIOS_PATHS = listOf(
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
    override val ORDERED_PATHS = listOf(
        "common/on_actions"
    )
}
