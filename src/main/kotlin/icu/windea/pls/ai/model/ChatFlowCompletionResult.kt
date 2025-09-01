package icu.windea.pls.ai.model

import dev.langchain4j.model.chat.response.ChatResponse
import icu.windea.pls.core.quote

class ChatFlowCompletionResult {
    var status = ChatFlowCompletionStatus.Processing
    val thinking = StringBuilder()
    val text = StringBuilder()
    var response: ChatResponse? = null
    var error: Throwable? = null
    
    fun statusText(): String {
        return when (status) {
            ChatFlowCompletionStatus.Processing -> "Processing"
            ChatFlowCompletionStatus.Completed -> "Completed"
            ChatFlowCompletionStatus.Error -> "Failed: ${error?.message}"
            ChatFlowCompletionStatus.Cancelled -> "Cancelled"
        }
    }

    fun metadataText(): String {
        val metadata = response?.metadata() ?: return ""
        val tokenUsageText = mapOf(
            "input" to metadata.tokenUsage().inputTokenCount(),
            "output" to metadata.tokenUsage().outputTokenCount(),
            "total" to metadata.tokenUsage().totalTokenCount(),
        ).entries.joinToString(", ", "{ ", " }") { (k, v) -> "$k = $v" }
        val metadataText = mapOf(
            "modelName" to metadata.modelName().quote(),
            "tokenUsage" to tokenUsageText,
            "finishReason" to metadata.finishReason(),
        ).entries.joinToString(" = ") { (k, v) -> "$k = $v" }
        return metadataText
    }
}
