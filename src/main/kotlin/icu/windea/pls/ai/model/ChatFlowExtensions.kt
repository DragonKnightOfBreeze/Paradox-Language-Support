@file:Suppress("unused")

package icu.windea.pls.ai.model

import dev.langchain4j.model.chat.*
import dev.langchain4j.model.chat.request.*
import dev.langchain4j.model.chat.response.*
import dev.langchain4j.service.*
import icu.windea.pls.core.coroutines.*
import kotlinx.coroutines.channels.*
import kotlinx.coroutines.flow.*

fun StreamingChatModel.chatFlow(chatRequest: ChatRequest): Flow<ChatFlowReply> = callbackFlow {
    val model = this@chatFlow
    val producer = ChatFlowProducer(this)
    model.chat(chatRequest, producer.toHandler())
}

const val DEFAULT_BUFFER_CAPACITY: Int = 32768

fun TokenStream.asChatFlow(bufferCapacity: Int = DEFAULT_BUFFER_CAPACITY, onBufferOverflow: BufferOverflow): Flow<ChatFlowReply> = callbackFlow {
    val tokenStream = this@asChatFlow
    val producer = ChatFlowProducer(this)
    producer.applyTo(tokenStream)
    onError { throwable -> close(throwable) }
    start()
    awaitClose()
}.buffer(capacity = bufferCapacity, onBufferOverflow = onBufferOverflow)

fun Flow<ChatFlowReply>.toTokenFlow(): Flow<String> {
    var response: ChatResponse? = null
    return transform { apply ->
        when (apply) {
            is ChatFlowReply.PartialResponse -> emit(apply.token)
            is ChatFlowReply.CompleteResponse -> response = apply.response
            is ChatFlowReply.Error -> throw apply.error
            else -> {}
        }
    }.onCompletion {
        if (it == null && response != null) throw ChatFlowCompletionException(response)
    }
}

fun Flow<ChatFlowReply>.toLineFlow(): Flow<String> {
    return toTokenFlow().toLineFlow()
}

fun <T> Flow<T>.onChatCompletion(action: suspend FlowCollector<T>.(cause: Throwable?) -> Unit): Flow<T> {
    return onCompletion(action).catch { e -> if (e !is ChatFlowCompletionException) throw e }
}
