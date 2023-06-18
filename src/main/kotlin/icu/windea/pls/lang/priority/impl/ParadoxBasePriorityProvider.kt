package icu.windea.pls.lang.priority.impl

import icu.windea.pls.core.search.*
import icu.windea.pls.lang.priority.*

class ParadoxBasePriorityProvider : ParadoxPriorityProvider() {
    val FIOS_PATHS = listOf(
        "common/event_chains",
        "common/scripted_variables",
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
