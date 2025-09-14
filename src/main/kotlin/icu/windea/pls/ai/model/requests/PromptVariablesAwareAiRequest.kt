package icu.windea.pls.ai.model.requests

interface PromptVariablesAwareAiRequest : AiRequest {
    fun toPromptVariables(variables: MutableMap<String, Any?> = mutableMapOf()): Map<String, Any?> = variables
}
