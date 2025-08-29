@file:Suppress("unused")

package icu.windea.pls.ai.model

import dev.langchain4j.model.chat.StreamingChatModel
import dev.langchain4j.model.chat.request.ChatRequest
import dev.langchain4j.service.TokenStream
import icu.windea.pls.core.coroutines.toLineFlow
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transform
import kotlin.coroutines.cancellation.CancellationException

fun StreamingChatModel.chatFlow(chatRequest: ChatRequest): Flow<ChatFlowReply> = callbackFlow {
    val model = this@chatFlow
    val producer = ChatFlowProducer(this)
    model.chat(chatRequest, producer.toHandler())
    // This will be called when the flow collection is closed or cancelled.
    awaitClose()
}

const val DEFAULT_BUFFER_CAPACITY: Int = 32768

fun TokenStream.asChatFlow(bufferCapacity: Int = DEFAULT_BUFFER_CAPACITY, onBufferOverflow: BufferOverflow): Flow<ChatFlowReply> = callbackFlow {
    val tokenStream = this@asChatFlow
    val producer = ChatFlowProducer(this)
    producer.applyTo(tokenStream)
    onError { throwable -> close(throwable) }
    start()
    // This will be called when the flow collection is closed or cancelled.
    awaitClose()
}.buffer(capacity = bufferCapacity, onBufferOverflow = onBufferOverflow)

fun  Flow<ChatFlowReply>.onCompletionStatus(action: suspend FlowCollector<ChatFlowReply>.(status: ChatFlowCompletionStatus) -> Unit): Flow<ChatFlowReply> {
    var status: ChatFlowCompletionStatus? = null
    return onEach { apply ->
        when (apply) {
            is ChatFlowReply.CompleteResponse -> status = ChatFlowCompletionStatus.Completed(apply.response)
            is ChatFlowReply.Error -> status = ChatFlowCompletionStatus.Error(apply.error)
            else -> {}
        }
    }.onCompletion { e ->
        if (e is CancellationException) status = ChatFlowCompletionStatus.Cancelled
        if (e != null) status = ChatFlowCompletionStatus.Error(e)
        if (status == null) status = ChatFlowCompletionStatus.Completed()
        action(status)
    }
}

fun Flow<ChatFlowReply>.toTokenFlow(): Flow<String> {
    return transform { apply ->
        when (apply) {
            // emit an partial token
            is ChatFlowReply.PartialResponse -> emit(apply.token)
            // emit an empty string here to ensure the flow is completed as expected
            is ChatFlowReply.CompleteResponse -> emit("")
            // throw the error
            is ChatFlowReply.Error -> throw apply.error
            else -> {}
        }
    }
}

fun Flow<ChatFlowReply>.toLineFlow(): Flow<String> {
    return toTokenFlow().toLineFlow()
}
