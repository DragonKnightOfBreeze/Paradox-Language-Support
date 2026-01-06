package icu.windea.pls.config.configContext

// region CwtConfigContext Extensions

fun CwtConfigContext.isRootForDefinition(): Boolean {
    return (definitionInfo != null || definitionInjectionInfo != null)
        && memberPathFromRoot.let { it != null && it.isEmpty() }
}

fun CwtConfigContext.inRoot(): Boolean {
    return memberPathFromRoot != null
}

// endregion
