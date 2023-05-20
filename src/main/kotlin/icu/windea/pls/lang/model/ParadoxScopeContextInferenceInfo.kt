package icu.windea.pls.lang.model

data class ParadoxScopeContextInferenceInfo(
    val scopeContextMap: Map<String, String?>,
    val hasConflict: Boolean
)