package icu.windea.pls.ai.model

import dev.langchain4j.model.chat.response.*

sealed class ChatFlowCompletionStatus {
    abstract val text: String

    class Completed(val response: ChatResponse? = null) : ChatFlowCompletionStatus() {
        override val text = buildString {
            append("Completed")
            if (response != null) {
                append(" (")
                append("model name: ")
                append(response.modelName())
                append(", ")
                append("token usage: ")
                append(response.tokenUsage().totalTokenCount())
                append(", ")
                append("finish reason: ")
                append(response.finishReason())
                append(")")
            }
        }
    }

    class Error(val e: Throwable) : ChatFlowCompletionStatus() {
        override val text = "Failed (${e.message})"
    }

    object Cancelled : ChatFlowCompletionStatus() {
        override val text = "Cancelled"
    }
}
