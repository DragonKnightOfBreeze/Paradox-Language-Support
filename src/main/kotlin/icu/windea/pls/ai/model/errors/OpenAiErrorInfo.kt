package icu.windea.pls.ai.model.errors

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

/**
 * 参见：
 * - https://api-docs.deepseek.com/zh-cn/quick_start/error_codes
 */
@Serializable
data class OpenAiErrorInfo(
    val error: Error
) : AiErrorInfo {
    @Serializable
    data class Error(
        val code: String,
        val type: String,
        val message: String,
        val param: JsonElement? = null,
    )
}
