package icu.windea.pls.ai

import dev.langchain4j.kotlin.model.chat.*
import dev.langchain4j.model.chat.response.*
import icu.windea.pls.ai.model.*
import icu.windea.pls.core.coroutines.*
import kotlinx.coroutines.flow.*

fun Flow<StreamingChatModelReply>.toLineFlow(): Flow<String> {
    var response: ChatResponse? = null
    return toLineFlow { apply ->
        when (apply) {
            is StreamingChatModelReply.PartialResponse -> apply.partialResponse
            is StreamingChatModelReply.CompleteResponse -> "".also { response = apply.response }
            is StreamingChatModelReply.Error -> throw apply.cause
        }
    }.onCompletion {
        if (it == null && response != null) throw ChatFlowCompletionException(response)
    }
}

fun <T> Flow<T>.catchCompletion(): Flow<T> {
    return catch { e ->
        if (e !is ChatFlowCompletionException) throw e
    }
}
