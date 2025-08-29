package icu.windea.pls.ai.util

import com.fasterxml.jackson.module.kotlin.readValue
import dev.langchain4j.exception.LangChain4jException
import icu.windea.pls.ai.model.ChatFlowCompletionException
import icu.windea.pls.ai.model.ChatFlowCompletionStatus
import icu.windea.pls.ai.model.OpenAiErrorInfo
import icu.windea.pls.core.isNotNullOrEmpty
import icu.windea.pls.core.runCatchingCancelable
import icu.windea.pls.core.util.ObjectMappers
import kotlin.coroutines.cancellation.CancellationException

object PlsAiManager {
    fun getChatFlowCompletionStatus(e: Throwable?): ChatFlowCompletionStatus {
        return when {
            e is ChatFlowCompletionException -> ChatFlowCompletionStatus.Completed(e.response)
            e is CancellationException -> ChatFlowCompletionStatus.Cancelled
            e != null -> ChatFlowCompletionStatus.Error(e)
            else -> ChatFlowCompletionStatus.Completed()
        }
    }

    fun getOptimizedErrorMessage(e: Throwable?): String? {
        if (e == null) return null
        val message = e.message
        when (e) {
            is LangChain4jException -> {
                if (message.isNotNullOrEmpty()) {
                    runCatchingCancelable {
                        val errorInfo = ObjectMappers.jsonMapper.readValue<OpenAiErrorInfo>(message)
                        return "[${errorInfo.error.code}] ${errorInfo.error.message}"
                    }
                }
                return e.message
            }
            else -> return e.message
        }
    }
}
