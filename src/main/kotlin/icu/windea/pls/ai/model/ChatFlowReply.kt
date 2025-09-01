package icu.windea.pls.ai.model

import dev.langchain4j.agent.tool.ToolExecutionRequest
import dev.langchain4j.model.chat.response.ChatResponse
import dev.langchain4j.rag.content.Content

sealed interface ChatFlowReply {
    data class PartialResponse(
        val token: String
    ) : ChatFlowReply

    data class PartialThinking(
        val token: String
    ) : ChatFlowReply
    
    data class Retrieved(
        val contents: List<Content>
    ) : ChatFlowReply

    data class IntermediateResponse(
        val response: ChatResponse
    ) : ChatFlowReply

    data class BeforeToolExecution(
        val request: ToolExecutionRequest
    ) : ChatFlowReply

    data class ToolExecuted(
        val request: ToolExecutionRequest,
        val result: String,
    ) : ChatFlowReply

    data class CompleteToolCall(
        val index: Int,
        val toolExecutionRequest: ToolExecutionRequest,
    ) : ChatFlowReply

    data class CompleteResponse(
        val response: ChatResponse
    ) : ChatFlowReply

    data class Error(
        val error: Throwable
    ) : ChatFlowReply
}
