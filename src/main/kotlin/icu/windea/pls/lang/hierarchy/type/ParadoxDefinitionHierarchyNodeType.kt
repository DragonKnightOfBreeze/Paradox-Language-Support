package icu.windea.pls.lang.hierarchy.type

enum class ParadoxDefinitionHierarchyNodeType(
    val grouped: Boolean = false
) {
    Type,
    Subtype,
    NoSubtype,
    Definition,
    EventType(true),
    TechTier(true),
    TechArea(true),
    TechCategory(true),
    ;
}
