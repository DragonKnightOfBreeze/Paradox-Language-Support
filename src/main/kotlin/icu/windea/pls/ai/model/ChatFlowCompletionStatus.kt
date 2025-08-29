package icu.windea.pls.ai.model

import dev.langchain4j.model.chat.response.ChatResponse
import kotlin.coroutines.cancellation.CancellationException

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

    companion object {
        @JvmStatic
        fun from(e: Throwable?): ChatFlowCompletionStatus {
            return when {
                e is ChatFlowCompletionException -> Completed(e.response)
                e is CancellationException -> Cancelled
                e != null -> Error(e)
                else -> Completed()
            }
        }
    }
}
