package icu.windea.pls.ai.model

/**
 * AI 请求可能返回的错误消息。
 */
object ErrorInfos {
    /**
     * 参见：
     * - https://api-docs.deepseek.com/zh-cn/quick_start/error_codes
     */
    data class OpenAiErrorInfo(
        val error: Error
    ) {
        data class Error(
            val code: String,
            val type: String,
            val message: String,
            val param: Any?,
        )
    }

    /**
     * 参见：
     * - https://docs.anthropic.com/en/api/messages
     */
    data class AnthropicErrorInfo(
        val type: String, // "error"
        val error: Error,
    ) {
        data class Error(
            val type: String,
            val message: String,
        )
    }
}
