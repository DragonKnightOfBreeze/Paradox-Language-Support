package icu.windea.pls.ai

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
