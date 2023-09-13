package icu.windea.pls.lang.priority.impl

import icu.windea.pls.lang.priority.*

class ParadoxBasePriorityProvider : ParadoxFilePathBasedPriorityProvider() {
    override val fiosPaths = listOf(
        "common/event_chains",
        "common/scripted_variables",
        "events"
    )
    override val orderedPaths = listOf(
        "common/on_actions"
    )
}
