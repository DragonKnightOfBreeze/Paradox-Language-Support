package icu.windea.pls.config.configContext

// region CwtConfigContext Extensions

fun CwtConfigContext.isRootForDefinition(): Boolean {
    return memberPathFromRoot.let { it != null && it.isEmpty() }
        && (definitionInfo != null || definitionInjectionInfo != null)
}

fun CwtConfigContext.inRoot(): Boolean {
    return memberPathFromRoot != null
}

// endregion
