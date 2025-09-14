package icu.windea.pls.ai.model.requests

interface AiRequest {
    val requestId: String

    val logPrefix get() = "[AI REQUEST #${requestId}]"

    fun toPromptVariables(variables: MutableMap<String, Any?> = mutableMapOf()): Map<String, Any?> = variables
}
