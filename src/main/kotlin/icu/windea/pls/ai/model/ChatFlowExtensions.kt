@file:Suppress("unused")

package icu.windea.pls.ai.model

import dev.langchain4j.data.message.ChatMessage
import dev.langchain4j.data.message.UserMessage
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
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
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

fun StreamingChatModel.chatFlow(userMessage: String): Flow<ChatFlowReply> = chatFlow(ChatRequest.builder().messages(UserMessage.from(userMessage)).build())

fun StreamingChatModel.chatFlow(messages: List<ChatMessage>): Flow<ChatFlowReply> = chatFlow(ChatRequest.builder().messages(messages).build())

const val DEFAULT_BUFFER_CAPACITY: Int = 32768

fun TokenStream.asChatFlow(bufferCapacity: Int = 32768, onBufferOverflow: BufferOverflow): Flow<ChatFlowReply> = callbackFlow {
    val tokenStream = this@asChatFlow
    val producer = ChatFlowProducer(this)
    producer.applyTo(tokenStream)
    onError { throwable -> close(throwable) }
    start()
    // This will be called when the flow collection is closed or cancelled.
    awaitClose()
}.buffer(capacity = bufferCapacity, onBufferOverflow = onBufferOverflow)

fun Flow<ChatFlowReply>.onCompletionResult(action: suspend FlowCollector<ChatFlowReply>.(result: ChatFlowCompletionResult) -> Unit): Flow<ChatFlowReply> {
    val result = ChatFlowCompletionResult()
    return onEach { apply ->
        when (apply) {
            is ChatFlowReply.PartialThinking -> {
                result.thinking.append(apply.token)
            }
            is ChatFlowReply.PartialResponse -> {
                result.text.append(apply.token)
            }
            is ChatFlowReply.CompleteResponse -> {
                result.status = ChatFlowCompletionStatus.Completed
                result.response = apply.response
            }
            is ChatFlowReply.Error -> {
                result.status = ChatFlowCompletionStatus.Error
                result.error = apply.error
            }
            else -> {}
        }
    }.onCompletion { e ->
        when {
            e is CancellationException -> {
                result.status = ChatFlowCompletionStatus.Cancelled
                result.error = e
            }
            e != null -> {
                result.status = ChatFlowCompletionStatus.Error
                result.error = e
            }
            result.status == ChatFlowCompletionStatus.Processing -> {
                result.status = ChatFlowCompletionStatus.Completed
            }
        }
        action(result)
    }
}

suspend fun Flow<ChatFlowReply>.toCompletionResult(): ChatFlowCompletionResult {
    return transform { onCompletionResult { emit(it) }.collect() }.first()
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
