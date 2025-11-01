package icu.windea.pls.model.scope

data class ParadoxScopeContextInferenceInfo(
    val scopeContextMap: Map<String, String>,
    val hasConflict: Boolean
)
