package icu.windea.pls.ai.model.errors

import kotlinx.serialization.Serializable

/**
 * 参见：
 * - https://docs.anthropic.com/en/api/messages
 */
@Serializable
data class AnthropicErrorInfo(
    val type: String, // "error"
    val error: Error,
) : AiErrorInfo {
    @Serializable
    data class Error(
        val type: String,
        val message: String,
    )
}
