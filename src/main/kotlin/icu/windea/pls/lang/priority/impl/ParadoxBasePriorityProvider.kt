package icu.windea.pls.lang.priority.impl

class ParadoxBasePriorityProvider : ParadoxFilePathBasedPriorityProvider() {
    override val FIOS_PATHS = listOf(
        "common/event_chains",
        "common/scripted_variables",
        "events"
    )
    override val ORDERED_PATHS = listOf(
        "common/on_actions"
    )
}
