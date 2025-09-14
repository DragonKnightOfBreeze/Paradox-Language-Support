package icu.windea.pls.ai.model

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
