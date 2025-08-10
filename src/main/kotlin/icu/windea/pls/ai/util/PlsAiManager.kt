package icu.windea.pls.ai.util

import com.fasterxml.jackson.module.kotlin.*
import dev.langchain4j.exception.*
import icu.windea.pls.ai.model.*
import icu.windea.pls.core.*
import icu.windea.pls.core.util.*
import kotlin.coroutines.cancellation.*

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
