package icu.windea.pls.ai.providers

import dev.langchain4j.model.chat.ChatModel
import dev.langchain4j.model.chat.StreamingChatModel
import icu.windea.pls.ai.model.ChatModelType

interface ChatModelProvider<S : ChatModelOptions> {
    val type: ChatModelType

    val options: S?

    fun isAvailable(): Boolean = true

    fun createChatModel(): ChatModel?

    fun createStreamingChatModel(): StreamingChatModel?
}
