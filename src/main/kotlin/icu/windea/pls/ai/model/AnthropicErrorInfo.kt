package icu.windea.pls.ai.model

/**
 * 参见：
 * - https://docs.anthropic.com/en/api/messages
 */
data class AnthropicErrorInfo(
    val error: Error,
    val type: String, // "error"
) {
    data class Error(
        val message: String,
        val type: String,
    )
}
