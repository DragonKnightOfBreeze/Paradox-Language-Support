package icu.windea.pls.ai.model.errors

/**
 * 参见：
 * - https://docs.anthropic.com/en/api/messages
 */
data class AnthropicErrorInfo(
    val type: String, // "error"
    val error: Error,
) : AiErrorInfo {
    data class Error(
        val type: String,
        val message: String,
    )
}
