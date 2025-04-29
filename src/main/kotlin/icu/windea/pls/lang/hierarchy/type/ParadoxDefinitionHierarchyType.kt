package icu.windea.pls.lang.hierarchy.type

import icu.windea.pls.*
import icu.windea.pls.model.*

@Suppress("InvalidBundleOrProperty")
enum class ParadoxDefinitionHierarchyType(
    val text: String,
    val predicate: ((ParadoxDefinitionInfo) -> Boolean)? = null
) {
    Type(PlsBundle.message("title.hierarchy.definition")),
    TypeAndSubtypes(PlsBundle.message("title.hierarchy.definition.with.subtypes")),

    EventTreeInvoker(PlsBundle.message("title.hierarchy.eventTree.invoker"), { it.type == "event" }),
    EventTreeInvoked(PlsBundle.message("title.hierarchy.eventTree.invoked"), { it.type == "event" }),
    TechTreePre(PlsBundle.message("title.hierarchy.techTree.pre"), { it.type == "technology" && it.gameType == ParadoxGameType.Stellaris }),
    TechTreePost(PlsBundle.message("title.hierarchy.techTree.post"), { it.type == "technology" && it.gameType == ParadoxGameType.Stellaris }),
    ;
}
