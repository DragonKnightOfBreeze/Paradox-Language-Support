package icu.windea.pls.config.configContext

// region CwtConfigContext Extensions

fun CwtConfigContext.isDefinition(): Boolean {
    return definitionInfo != null && elementPathFromRoot.let { it != null && it.isEmpty() }
}

fun CwtConfigContext.isDefinitionOrMember(): Boolean {
    return elementPathFromRoot != null
}

// endregion
