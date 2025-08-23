package icu.windea.pls.ai.model

import dev.langchain4j.internal.*
import dev.langchain4j.model.chat.response.*
import dev.langchain4j.rag.content.*
import dev.langchain4j.service.*
import dev.langchain4j.service.tool.*
import kotlinx.coroutines.channels.*
import org.slf4j.*

private val logger = LoggerFactory.getLogger(ChatFlowProducer::class.java)

class ChatFlowProducer(private val scope: ProducerScope<ChatFlowReply>) {
    fun onPartialResponse(token: String) {
        logger.trace(Markers.SENSITIVE, "Received partialResponse: {}", token)
        scope.trySend(ChatFlowReply.PartialResponse(token))
    }

    fun onPartialThinking(partialThinking: PartialThinking) {
        logger.trace(Markers.SENSITIVE, "Received partialThinking: {}", partialThinking)
        scope.trySend(ChatFlowReply.PartialThinking(partialThinking.text()))
    }

    fun onRetrieved(contents: List<Content>) {
        logger.trace(Markers.SENSITIVE, "Received retrieved: {}", contents)
        scope.trySend(ChatFlowReply.Retrieved(contents))
    }

    fun onIntermediateResponse(response: ChatResponse) {
        logger.trace(Markers.SENSITIVE, "Received intermediateResponse: {}", response)
        scope.trySend(ChatFlowReply.IntermediateResponse(response))
    }

    fun beforeToolExecution(beforeToolExecution: BeforeToolExecution) {
        logger.trace(Markers.SENSITIVE, "Received beforeToolExecution: {}", beforeToolExecution)
        scope.trySend(ChatFlowReply.BeforeToolExecution(beforeToolExecution.request()))
    }

    fun onToolExecuted(toolExecution: ToolExecution) {
        logger.trace(Markers.SENSITIVE, "Received toolExecuted: {}", toolExecution)
        scope.trySend(ChatFlowReply.ToolExecuted(toolExecution.request(), toolExecution.result()))
    }

    fun onCompleteToolCall(completeToolCall: CompleteToolCall) {
        logger.trace(Markers.SENSITIVE, "Received completeToolCall: {}", completeToolCall)
        scope.trySend(ChatFlowReply.CompleteToolCall(completeToolCall.index(), completeToolCall.toolExecutionRequest()))
    }

    fun onCompleteResponse(response: ChatResponse) {
        logger.trace(Markers.SENSITIVE, "Received completeResponse: {}", response)
        scope.trySend(ChatFlowReply.CompleteResponse(response))
        scope.close()
    }

    fun onError(error: Throwable) {
        logger.trace(Markers.SENSITIVE, "Received error: {}", error.message, error)
        scope.trySend(ChatFlowReply.Error(error))
        scope.close(error)
    }

    fun toHandler(): StreamingChatResponseHandler {
        val producer = this
        return object : StreamingChatResponseHandler {
            override fun onPartialResponse(partialResponse: String) = producer.onPartialResponse(partialResponse)
            override fun onPartialThinking(partialThinking: PartialThinking) = producer.onPartialThinking(partialThinking)
            override fun onCompleteToolCall(completeToolCall: CompleteToolCall) = producer.onCompleteToolCall(completeToolCall)
            override fun onCompleteResponse(completeResponse: ChatResponse) = producer.onCompleteResponse(completeResponse)
            override fun onError(error: Throwable) = producer.onError(error)
        }
    }

    fun applyTo(tokenStream: TokenStream) {
        val producer = this
        tokenStream
            .onPartialResponse { producer.onPartialResponse(it) }
            .onRetrieved { producer.onRetrieved(it) }
            .onIntermediateResponse { producer.onIntermediateResponse(it) }
            .beforeToolExecution { producer.beforeToolExecution(it) }
            .onToolExecuted { producer.onToolExecuted(it) }
            .onPartialThinking { producer.onPartialThinking(it) }
            .onCompleteResponse { producer.onCompleteResponse(it) }
            .onError { producer.onError(it) }
    }
}
