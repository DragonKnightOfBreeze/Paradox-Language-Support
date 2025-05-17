package icu.windea.pls.lang.hierarchy.type

import icu.windea.pls.*
import icu.windea.pls.model.*
import icu.windea.pls.model.constants.*

@Suppress("InvalidBundleOrProperty")
enum class ParadoxDefinitionHierarchyType(
    val text: String,
    val grouped: Boolean = false,
    val nested: Boolean = false,
    val predicate: ((ParadoxDefinitionInfo) -> Boolean) = { true }
) {
    Type(PlsBundle.message("title.hierarchy.definition")),
    TypeAndSubtypes(PlsBundle.message("title.hierarchy.definition.with.subtypes")),

    EventTreeInvoker(PlsBundle.message("title.hierarchy.eventTree.invoker"), true, true, { it.type == ParadoxDefinitionTypes.Event }),
    EventTreeInvoked(PlsBundle.message("title.hierarchy.eventTree.invoked"), true, true, { it.type == ParadoxDefinitionTypes.Event }),
    TechTreePre(PlsBundle.message("title.hierarchy.techTree.pre"), true, true, { it.type == ParadoxDefinitionTypes.Technology && it.gameType == ParadoxGameType.Stellaris }),
    TechTreePost(PlsBundle.message("title.hierarchy.techTree.post"), true, true, { it.type == ParadoxDefinitionTypes.Technology && it.gameType == ParadoxGameType.Stellaris }),
    ;
}
