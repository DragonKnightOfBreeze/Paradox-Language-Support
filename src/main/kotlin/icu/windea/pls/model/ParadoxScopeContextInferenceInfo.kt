package icu.windea.pls.model

data class ParadoxScopeContextInferenceInfo(
    val scopeContextMap: Map<String, String?>,
    val hasConflict: Boolean
)